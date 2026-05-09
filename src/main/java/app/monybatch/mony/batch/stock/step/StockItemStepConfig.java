package app.monybatch.mony.batch.stock.step;

import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.entity.StockTemp;
import app.monybatch.mony.domian.stock.repository.StockTempRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@AllArgsConstructor
public class StockItemStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager batchTransactionManager;

    private final StockTempRepository stockTempRepository;

    private final OpenAPIItemReader<Stock> stockApiReader;
    private final OpenAPIItemReader<Stock> stockApiKosdaqReader;
    private final OpenAPIItemReader<Stock> stockApiKonexReader;

    private final ItemProcessor<Stock, StockTemp> stockTempProcessor;
    private final ItemProcessor<Stock, Stock> stockProcessor;

    private final ItemWriter<StockTemp> stockTempWriter;
    private final ItemWriter<Stock> stockWriter;

    @Qualifier("batchEntityManager")
    private final EntityManagerFactory entityManagerFactory;

    // info_stock_temp 전체 삭제
    @Bean
    public Step truncateStockTempStep() {
        return new StepBuilder("truncateStockTempStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    stockTempRepository.deleteAllInBatch();
                    return RepeatStatus.FINISHED;
                }, batchTransactionManager)
                .build();
    }

    // info_stock 전체 → info_stock_temp 스냅샷 (DART 필드 포함)
    @Bean
    @StepScope
    public JpaCursorItemReader<Stock> stockDbReader() {
        return new JpaCursorItemReaderBuilder<Stock>()
                .name("stockDbReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Stock s")
                .build();
    }

    @Bean
    public Step prebatchStep() {
        return new StepBuilder("prebatchStep", jobRepository)
                .<Stock, StockTemp>chunk(100, batchTransactionManager)
                .reader(stockDbReader())
                .processor(stockTempProcessor)
                .writer(stockTempWriter)
                .build();
    }

    // KOSPI API → 대사(DART 필드 보존) → info_stock
    @Bean
    public Step kospiBatchStep() {
        return new StepBuilder("kospiBatchStep", jobRepository)
                .<Stock, Stock>chunk(100, batchTransactionManager)
                .reader(stockApiReader)
                .processor(stockProcessor)
                .writer(stockWriter)
                .build();
    }

    // KOSDAQ API → 대사(DART 필드 보존) → info_stock
    @Bean
    public Step kosdaqBatchStep() {
        return new StepBuilder("kosdaqBatchStep", jobRepository)
                .<Stock, Stock>chunk(100, batchTransactionManager)
                .reader(stockApiKosdaqReader)
                .processor(stockProcessor)
                .writer(stockWriter)
                .build();
    }

    // KONEX API → 대사(DART 필드 보존) → info_stock
    @Bean
    public Step konexBatchStep() {
        return new StepBuilder("konexBatchStep", jobRepository)
                .<Stock, Stock>chunk(100, batchTransactionManager)
                .reader(stockApiKonexReader)
                .processor(stockProcessor)
                .writer(stockWriter)
                .build();
    }
}
