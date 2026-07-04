package app.monybatch.mony.system.controller;

import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.schedule.DynamicBatchScheduler;
import app.monybatch.mony.common.constant.JobDomain;
import app.monybatch.mony.domian.schedule.BatchScheduleConfig;
import app.monybatch.mony.domian.schedule.BatchScheduleConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 배치 Job의 실행 여부/스케줄(cron) 설정 API (vue-batch-admin 스케줄 화면용).
 */
@Slf4j
@RestController
@RequestMapping("/api/batch/schedules")
@RequiredArgsConstructor
public class BatchScheduleController {

    private final BatchScheduleConfigRepository scheduleRepository;
    private final DynamicBatchScheduler dynamicBatchScheduler;
    private final ApplicationContext context;

    /** 등록된 전체 Job에 대한 스케줄 설정 조회 — 설정이 없는 Job도 "미설정" 상태로 노출 */
    @GetMapping
    public List<Map<String, Object>> list() {
        Map<String, BatchScheduleConfig> configByJob = new LinkedHashMap<>();
        scheduleRepository.findAll().forEach(c -> configByJob.put(c.getJobName(), c));

        List<String> jobNames = new ArrayList<>();
        context.getBeansOfType(DescriptiveJob.class).values()
                .forEach(dj -> jobNames.add(dj.getJob().getName()));
        context.getBeansOfType(Job.class).values().forEach(job -> {
            if (!jobNames.contains(job.getName())) {
                jobNames.add(job.getName());
            }
        });

        return jobNames.stream().sorted().map(jobName -> {
            BatchScheduleConfig config = configByJob.get(jobName);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("jobName", jobName);
            item.put("domain", JobDomain.resolve(jobName));
            item.put("enabled", config != null && config.isEnabled());
            item.put("cronExpression", config != null ? config.getCronExpression() : null);
            item.put("nextFireTime", (config != null && config.isEnabled())
                    ? nextFireTime(config.getCronExpression()) : null);
            return item;
        }).toList();
    }

    /** 주어진 cron의 다음 실행 시각 미리보기 — 저장 전 폼에서 확인용 */
    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> preview(@RequestParam String cronExpression) {
        LocalDateTime next = nextFireTime(cronExpression);
        if (next == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", "INVALID_CRON", "message", "cron 표현식이 올바르지 않습니다"));
        }
        return ResponseEntity.ok(Map.of("nextFireTime", next));
    }

    /** 실행 여부/cron 저장 — 즉시 재스케줄 */
    @PutMapping("/{jobName}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String jobName,
                                                       @RequestBody ScheduleUpdateRequest req) {
        if (req.enabled() && !StringUtils.hasText(req.cronExpression())) {
            return error(HttpStatus.BAD_REQUEST, "CRON_REQUIRED", "실행을 켜려면 cron 표현식이 필요합니다");
        }
        if (StringUtils.hasText(req.cronExpression()) && nextFireTime(req.cronExpression()) == null) {
            return error(HttpStatus.BAD_REQUEST, "INVALID_CRON", "cron 표현식이 올바르지 않습니다: " + req.cronExpression());
        }

        BatchScheduleConfig config = scheduleRepository.findById(jobName).orElseGet(() -> {
            BatchScheduleConfig c = new BatchScheduleConfig();
            c.setJobName(jobName);
            return c;
        });
        config.setEnabled(req.enabled());
        config.setCronExpression(req.cronExpression());
        config.setUpdatedAt(LocalDateTime.now());
        scheduleRepository.save(config);
        dynamicBatchScheduler.reschedule(config);

        log.info("배치 조작 감사 - action=SCHEDULE_UPDATE, job={}, enabled={}, cron={}",
                jobName, req.enabled(), req.cronExpression());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobName", jobName);
        result.put("enabled", config.isEnabled());
        result.put("cronExpression", config.getCronExpression());
        result.put("nextFireTime", config.isEnabled() ? nextFireTime(config.getCronExpression()) : null);
        return ResponseEntity.ok(result);
    }

    private LocalDateTime nextFireTime(String cron) {
        if (!StringUtils.hasText(cron)) {
            return null;
        }
        try {
            return CronExpression.parse(cron).next(LocalDateTime.now());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }

    public record ScheduleUpdateRequest(boolean enabled, String cronExpression) {
    }
}
