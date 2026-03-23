package app.monybatch.mony.batch.stock.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockBatchScheduler {

//    private final JobLauncher jobLauncher;
//    private final Job stockItemJob;
//
//    @Scheduled(cron = "0 0 16 * * *")
//    public void run() throws Exception {
//
//        JobParameters params = new JobParametersBuilder()
//                .addString("basDd", LocalDate.now().toString())
//                .toJobParameters();
//
//        jobLauncher.run(stockItemJob, params);
//    }
}
