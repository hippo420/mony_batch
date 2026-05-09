package app.monybatch.mony.batch.stock.step;

import app.monybatch.mony.batch.stock.tasklet.StockAlertTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class StockAlertStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager batchTransactionManager;
    private final StockAlertTasklet stockAlertTasklet;

    @Bean
    public Step alertStep() {
        return new StepBuilder("alertStep", jobRepository)
                .tasklet(stockAlertTasklet, batchTransactionManager)
                .build();
    }
}
