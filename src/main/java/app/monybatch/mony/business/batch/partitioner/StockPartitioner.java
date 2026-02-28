package app.monybatch.mony.business.batch.partitioner;

import app.monybatch.mony.business.entity.Stock;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockPartitioner implements Partitioner {

    private final List<Stock> stocks;

    public StockPartitioner(List<Stock> stocks) {
        this.stocks = stocks;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();
        int size = stocks.size();
        int partitionSize = (int) Math.ceil((double) size / gridSize);

        for (int i = 0; i < gridSize; i++) {
            ExecutionContext context = new ExecutionContext();
            int start = i * partitionSize;
            int end = Math.min(start + partitionSize, size);

            if (start >= size) {
                break;
            }

            context.putInt("startIndex", start);
            context.putInt("endIndex", end);
            
            result.put("partition" + i, context);
        }
        return result;
    }
}
