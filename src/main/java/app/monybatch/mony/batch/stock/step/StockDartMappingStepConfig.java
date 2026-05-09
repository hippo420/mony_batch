package app.monybatch.mony.batch.stock.step;

import app.monybatch.mony.batch.stock.processor.DartMappingProcessor;
import app.monybatch.mony.domian.stock.entity.Stock;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@AllArgsConstructor
public class StockDartMappingStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager batchTransactionManager;
    private final DartMappingProcessor dartMappingProcessor;

    @Qualifier("batchEntityManager")
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Step dartMappingStep() {
        return new StepBuilder("dartMappingStep", jobRepository)
                .<Stock, Stock>chunk(100, batchTransactionManager)
                .reader(dartMappingReader())
                .processor(dartMappingProcessor)
                .writer(dartMappingWriter())
                .build();
    }

    @Bean
    @StepScope
    public JpaCursorItemReader<Stock> dartMappingReader() {
        return new JpaCursorItemReaderBuilder<Stock>()
                .name("dartMappingReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Stock s")
                .build();
    }

    @Bean
    @StepScope
    public JpaItemWriter<Stock> dartMappingWriter() {
        JpaItemWriter<Stock> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}