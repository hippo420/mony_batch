package app.monybatch.mony.batch.support.listener;

import app.monybatch.mony.common.constant.JobDomain;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;

/**
 * 모든 Job에 자동 등록되는 실행 요약 리스너 (JobLoggingListenerRegistrar가 등록).
 * 비동기 실행에서도 실제 배치 스레드 안에서 동작하므로 MDC(도메인 로그 라우팅)와
 * 시작/종료/Step별 처리량 요약이 정확히 남는다.
 */
@Slf4j
public class BatchJobLoggingListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        MDC.put("JOB_NAME", jobName);
        MDC.put("DOMAIN", JobDomain.resolve(jobName));
        MDC.put("JOB_INSTANCE_ID", String.valueOf(jobExecution.getJobId()));
        log.info("Job 시작 - job={}, executionId={}, params={}",
                jobName, jobExecution.getId(), jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            long durationMs = (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null)
                    ? Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis()
                    : -1;

            jobExecution.getStepExecutions().forEach(se ->
                    log.info("Step 요약 - step={}, status={}, readCount={}, writeCount={}, skipCount={}, rollbackCount={}",
                            se.getStepName(), se.getStatus(), se.getReadCount(),
                            se.getWriteCount(), se.getSkipCount(), se.getRollbackCount()));

            log.info("Job 종료 - job={}, executionId={}, status={}, duration={}ms",
                    jobExecution.getJobInstance().getJobName(), jobExecution.getId(),
                    jobExecution.getStatus(), durationMs);
        } finally {
            MDC.remove("JOB_NAME");
            MDC.remove("JOB_INSTANCE_ID");
            MDC.remove("DOMAIN");
        }
    }
}
