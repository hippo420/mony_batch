package app.monybatch.mony.batch.stock.partitioner;

import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class StockPartitioner implements Partitioner {

    private final StockRepository stockRepository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        List<Stock> stocks = stockRepository.findKospiKosdaqStocks();
        int total = stocks.size();
        int partitionSize = (int) Math.ceil((double) total / gridSize);

        Map<String, ExecutionContext> result = new HashMap<>();
        for (int i = 0; i < gridSize; i++) {
            int start = i * partitionSize;
            if (start >= total) break;
            int end = Math.min(start + partitionSize, total);

            ExecutionContext ctx = new ExecutionContext();
            ctx.putInt("startIndex", start);
            ctx.putInt("endIndex", end);
            result.put("partition" + i, ctx);
            log.info("파티션[{}] 종목 인덱스 [{}, {}) → {}건", i, start, end, end - start);
        }
        return result;
    }
}
