package app.monybatch.mony.batch.support.schedule;

import app.monybatch.mony.domian.schedule.BatchScheduleConfig;
import app.monybatch.mony.domian.schedule.BatchScheduleConfigRepository;
import app.monybatch.mony.system.service.BatchLaunchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * DB(batch_schedule_config)에 저장된 실행 여부/cron을 기준으로 Job을 동적으로 스케줄링.
 * 모니터링 화면에서 설정을 바꾸면 reschedule()이 즉시 재등록해 재배포 없이 반영된다.
 *
 * 기존에 클래스별로 흩어져 있던 @Scheduled(dartRssJob/stockAlertJob/newsCollectionJob)는
 * 이 스케줄러로 일원화했다 — 최초 기동 시 동일한 기본 주기로 시딩되어 동작은 그대로 유지된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicBatchScheduler {

    private static final Map<String, String> DEFAULT_CRONS = Map.of(
            "dartRssJob", "0 * * * * *",         // 기존 fixedDelay 60s
            "stockAlertJob", "0 */10 * * * *",   // 기존 스케줄러와 동일
            "newsCollectionJob", "0 */10 * * * *"
    );

    private final TaskScheduler taskScheduler;
    private final BatchScheduleConfigRepository scheduleRepository;
    private final BatchLaunchService batchLaunchService;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        DEFAULT_CRONS.forEach((jobName, cron) -> {
            if (scheduleRepository.findById(jobName).isEmpty()) {
                BatchScheduleConfig config = new BatchScheduleConfig();
                config.setJobName(jobName);
                config.setEnabled(true);
                config.setCronExpression(cron);
                config.setUpdatedAt(LocalDateTime.now());
                scheduleRepository.save(config);
                log.info("스케줄 기본값 시딩 - job={}, cron={}", jobName, cron);
            }
        });
        scheduleRepository.findAll().forEach(this::apply);
    }

    /** 설정 변경 후 즉시 재등록 (기존 스케줄은 취소 후 재생성). */
    public void reschedule(BatchScheduleConfig config) {
        apply(config);
    }

    private void apply(BatchScheduleConfig config) {
        cancel(config.getJobName());
        if (!config.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(config.getCronExpression())) {
            log.warn("스케줄 활성화됐지만 cron 표현식이 없어 등록하지 않음 - job={}", config.getJobName());
            return;
        }
        try {
            CronTrigger trigger = new CronTrigger(config.getCronExpression());
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> batchLaunchService.launchScheduled(config.getJobName()), trigger);
            scheduledTasks.put(config.getJobName(), future);
            log.info("스케줄 등록 - job={}, cron={}", config.getJobName(), config.getCronExpression());
        } catch (IllegalArgumentException e) {
            log.error("잘못된 cron 표현식이라 등록하지 않음 - job={}, cron={}", config.getJobName(), config.getCronExpression());
        }
    }

    private void cancel(String jobName) {
        ScheduledFuture<?> future = scheduledTasks.remove(jobName);
        if (future != null) {
            future.cancel(false);
        }
    }
}
