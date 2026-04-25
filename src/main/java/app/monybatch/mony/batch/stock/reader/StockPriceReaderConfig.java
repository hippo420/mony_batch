package app.monybatch.mony.batch.stock.reader;

import app.monybatch.mony.domian.stock.repository.StockTradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class StockPriceReaderConfig {

    @Autowired
    private StockTradeRepository repository;

    @Bean
    @StepScope
    public ItemReader<String> stockPriceDateReader() {
        String maxDate = repository.findMaxDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        LocalDate startDate = (maxDate == null || maxDate.isEmpty())
                ? LocalDate.now().minusDays(7)
                : LocalDate.parse(maxDate, formatter).plusDays(1);

        LocalDate endDate = LocalDate.now();
        List<String> dates = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dates.add(startDate.format(formatter));
            startDate = startDate.plusDays(1);
        }

        log.info("종가 적재 대상 일자: {} ~ {}, 총 {}일",
                dates.isEmpty() ? "없음" : dates.get(0),
                dates.isEmpty() ? "없음" : dates.get(dates.size() - 1),
                dates.size());

        return new ListItemReader<>(dates);
    }
}
