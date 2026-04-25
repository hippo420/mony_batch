package app.monybatch.mony.batch.stock.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockItemJobConfig {

    private final JobRepository jobRepository;

    private final Step prebatchStep;
    private final Step batchStep;
    private final Step afterBatchStep;
    private final Step dartMappingStep;

    @Bean
    public Job stockItemJob() {

        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{"basDd"});
        validator.setOptionalKeys(new String[]{"param1","param2"});

        return new JobBuilder("stockItemJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(prebatchStep)
                .next(batchStep)
                .next(afterBatchStep)
                .next(dartMappingStep)
                .build();
    }
}
