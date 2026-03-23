package app.monybatch.mony.batch.stock.writer;

import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.entity.StockTemp;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StockWriterConfig {

    @Qualifier("batchEntityManager")
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    @StepScope
    public JpaItemWriter<Stock> stockWriter() {
        JpaItemWriter<Stock> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    @Bean
    @StepScope
    public JpaItemWriter<StockTemp> stockTempWriter() {
        JpaItemWriter<StockTemp> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
