package app.monybatch.mony.batch.stock.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockPriceJobConfig {

    private final JobRepository jobRepository;
    private final Step priceStep;

    @Bean
    public Job stockPriceJob() {
        return new JobBuilder("stockPriceJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(priceStep)
                .build();
    }
}
