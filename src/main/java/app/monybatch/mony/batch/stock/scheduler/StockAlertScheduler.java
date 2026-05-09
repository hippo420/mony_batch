package app.monybatch.mony.batch.stock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertScheduler {

    private final JobLauncher jobLauncher;
    @Qualifier("stockAlertJob")
    private final Job stockAlertJob;

    // 매 분 실행
    @Scheduled(cron = "0 */10 * * * *")
    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        log.info("StockAlertJob 실행: {}", LocalDateTime.now());
        jobLauncher.run(stockAlertJob, params);
    }
}
