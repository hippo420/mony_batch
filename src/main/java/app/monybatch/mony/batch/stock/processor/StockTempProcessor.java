package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.domian.stock.entity.StockTemp;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class StockTempProcessor implements ItemProcessor<StockTemp, StockTemp> {

    @Override
    public StockTemp process(StockTemp item) {
        return item;
    }
}
