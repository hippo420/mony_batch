package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.domian.stock.entity.Stock;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StockProcessor implements ItemProcessor<Stock, Stock> {

    @Override
    public Stock process(Stock item) {
        return item;
    }
}