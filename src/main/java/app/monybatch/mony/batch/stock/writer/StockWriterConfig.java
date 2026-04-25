package app.monybatch.mony.batch.stock.writer;

import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.entity.StockTemp;
import app.monybatch.mony.domian.stock.entity.StockTrade;
import app.monybatch.mony.domian.stock.repository.StockRepository;
import app.monybatch.mony.domian.stock.repository.StockTempRepository;
import app.monybatch.mony.domian.stock.repository.StockTradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class StockWriterConfig {

    private final StockRepository stockRepository;
    private final StockTempRepository stockTempRepository;
    private final StockTradeRepository stockTradeRepository;

    @Bean
    @StepScope
    public ItemWriter<Stock> stockWriter() {
        return chunk -> {
            stockRepository.saveAll(chunk.getItems());
        };
    }

    @Bean
    @StepScope
    public ItemWriter<StockTemp> stockTempWriter() {
        return chunk -> {
            stockTempRepository.saveAll(chunk.getItems());
        };
    }

    @Bean
    @StepScope
    public ItemWriter<List<StockTrade>> stockPriceWriter() {
        return chunk -> {
            for (List<StockTrade> trades : chunk.getItems()) {
                stockTradeRepository.saveAll(trades);
            }
        };
    }
}
