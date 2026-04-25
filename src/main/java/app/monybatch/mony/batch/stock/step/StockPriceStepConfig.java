package app.monybatch.mony.batch.stock.step;

import app.monybatch.mony.domian.stock.entity.StockTrade;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class StockPriceStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager batchTransactionManager;

    private final ItemReader<String> stockPriceDateReader;
    private final ItemProcessor<String, List<StockTrade>> stockPriceProcessor;
    private final ItemWriter<List<StockTrade>> stockPriceWriter;

    @Bean
    public Step priceStep() {
        return new StepBuilder("priceStep", jobRepository)
                .<String, List<StockTrade>>chunk(1, batchTransactionManager)
                .reader(stockPriceDateReader)
                .processor(stockPriceProcessor)
                .writer(stockPriceWriter)
                .build();
    }
}
