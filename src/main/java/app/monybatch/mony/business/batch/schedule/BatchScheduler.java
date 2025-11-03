package app.monybatch.mony.business.batch.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final StockPriceRepository stockPriceRepository;


    @Scheduled(cron = "0 50 19 * * *") // 매일 새벽 2시 실행
    public void runStockItemJob() {
        log.info("BatchScheduler - runStockItemJob");
        String lastDay = stockPriceRepository.findLastDay();

        List<String> dayList = DateUtil.getDateListToToday(lastDay);

        for(String date : dayList) {
            try {
                log.info("BatchScheduler - runStockItemJob Param: basDd :[{}]", date);
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("basDd", date) // JobParameter 예시
                        .toJobParameters();
                jobLauncher.run(jobRegistry.getJob("itemJob"), jobParameters);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Scheduled(cron = "0 0 20 * * *") // 매일 새벽 2시 실행
    public void runStockPriceJob() {
        log.info("BatchScheduler - runStockPriceJob");
        String lastDay = stockPriceRepository.findLastDay();

        List<String> dayList = DateUtil.getDateListToToday(lastDay);

        for(String date : dayList) {
            try {
                log.info("BatchScheduler - runStockPriceJob Param: basDd :[{}]", date);
                JobParameters jobParameters = new JobParametersBuilder()
                        .addString("basDd", date) // JobParameter 예시
                        .toJobParameters();
                jobLauncher.run(jobRegistry.getJob("priceJob"), jobParameters);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
