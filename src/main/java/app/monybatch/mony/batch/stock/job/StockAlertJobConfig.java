package app.monybatch.mony.batch.stock.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockAlertJobConfig {

    private final JobRepository jobRepository;

    @Bean
    public Job stockAlertJob(@Qualifier("alertStep") Step alertStep) {
        return new JobBuilder("stockAlertJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(alertStep)
                .build();
    }
}
