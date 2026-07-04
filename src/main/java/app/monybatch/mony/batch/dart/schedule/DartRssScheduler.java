package app.monybatch.mony.batch.dart.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DartRssScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(fixedDelay = 60_000)
    public void runDartRssJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(jobRegistry.getJob("dartRssJob"), params);
            log.info("dartRssJob 실행 완료");
        } catch (JobInstanceAlreadyCompleteException e) {
            log.debug("dartRssJob 이미 완료됨");
        } catch (Exception e) {
            log.error("dartRssJob 실행 오류: {}", e.getMessage());
        }
    }
}
