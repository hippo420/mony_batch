package app.monybatch.mony.batch.stock.step;

import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.entity.StockTemp;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class StockItemStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager batchTransactionManager;

    private final OpenAPIItemReader<StockTemp> stockTempApiReader;
    private final OpenAPIItemReader<Stock> stockApiReader;

    private final ItemProcessor<StockTemp, StockTemp> stockTempProcessor;
    private final ItemProcessor<Stock, Stock> stockProcessor;

    private final ItemWriter<StockTemp> stockTempWriter;
    private final ItemWriter<Stock> stockWriter;

    @Bean
    public Step prebatchStep() {
        return new StepBuilder("prebatchStep", jobRepository)
                .<StockTemp, StockTemp>chunk(100, batchTransactionManager)
                .reader(stockTempApiReader)
                .processor(stockTempProcessor)
                .writer(stockTempWriter)
                .build();
    }

    @Bean
    public Step batchStep() {
        return new StepBuilder("batchStep", jobRepository)
                .<Stock, Stock>chunk(100, batchTransactionManager)
                .reader(stockApiReader)
                .processor(stockProcessor)
                .writer(stockWriter)
                .build();
    }

    @Bean
    public Step afterBatchStep() {
        return new StepBuilder("afterBatchStep", jobRepository)
                .<Stock, Stock>chunk(100, batchTransactionManager)
                .reader(stockApiReader)
                .processor(stockProcessor)
                .writer(stockWriter)
                .build();
    }
}
