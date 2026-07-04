package app.monybatch.mony.system.service;

import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.parameter.JobParamSpec;
import app.monybatch.mony.common.core.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Job 실행 공용 로직 — ManageController(수동 실행/모니터링 화면)와
 * DynamicBatchScheduler(자동 스케줄 실행)가 함께 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchLaunchService {

    // 기준일 성격의 파라미터는 값이 비어있으면 오늘 날짜로 자동 채운다 (기존 스케줄러들의 관행과 동일)
    private static final Set<String> TODAY_DEFAULT_KEYS = Set.of("basDd", "fromYmd", "toYmd");

    // DescriptiveJob으로 래핑되지 않은 원시 Job 빈의 파라미터 명세
    // (stockAlertJob은 Job 타입으로 직접 주입되는 곳이 있어 래핑하지 않음)
    private static final Map<String, List<JobParamSpec>> RAW_JOB_PARAM_SPECS = Map.of(
            "stockItemJob", List.of(new JobParamSpec("basDd", "기준일자", "yyyyMMdd", "", true))
    );

    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;
    private final ApplicationContext context;
    // 파라미터명이 bean 이름과 일치해야 비동기 런처가 주입됨 (기본 jobLauncher는 동기)
    private final JobLauncher asyncJobLauncher;

    public List<JobParamSpec> getParamSpecs(String jobName) {
        return context.getBeansOfType(DescriptiveJob.class).values().stream()
                .filter(dj -> dj.getJob().getName().equals(jobName))
                .findFirst()
                .map(DescriptiveJob::getParamSpecs)
                .filter(specs -> !specs.isEmpty())
                .orElseGet(() -> RAW_JOB_PARAM_SPECS.getOrDefault(jobName, List.of()));
    }

    /** 사용자가 지정한 파라미터로 실행 (모니터링 화면의 수동 실행/재시작). */
    public JobExecution launch(String jobName, Map<String, Object> params) throws Exception {
        Job job = jobRegistry.getJob(jobName);
        JobParametersBuilder builder = new JobParametersBuilder(jobExplorer);
        if (params != null) {
            params.forEach((k, v) -> builder.addString(k, String.valueOf(v)));
        }
        // incrementer(run.id) 적용 — 파라미터 없는 Job도 반복 실행 가능
        JobParameters jobParameters = builder.getNextJobParameters(job).toJobParameters();
        return asyncJobLauncher.run(job, jobParameters);
    }

    /** 스케줄러 전용: 파라미터 명세의 기본값(+ 기준일 자동 채움)으로 실행. 이미 실행 중이면 건너뛴다. */
    public void launchScheduled(String jobName) {
        if (!jobExplorer.findRunningJobExecutions(jobName).isEmpty()) {
            log.info("스케줄 실행 스킵 - job={} 이(가) 이미 실행 중입니다", jobName);
            return;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        String today = DateUtil.getDateYmd();
        for (JobParamSpec spec : getParamSpecs(jobName)) {
            String value = spec.defaultValue();
            if (!StringUtils.hasText(value) && TODAY_DEFAULT_KEYS.contains(spec.key())) {
                value = today;
            }
            params.put(spec.key(), value);
        }

        try {
            JobExecution execution = launch(jobName, params);
            log.info("배치 조작 감사 - action=SCHEDULED_LAUNCH, job={}, params={}, executionId={}",
                    jobName, params, execution.getId());
        } catch (Exception e) {
            log.error("스케줄 실행 실패 - job={}, params={}", jobName, params, e);
        }
    }
}
