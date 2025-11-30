package app.monybatch.mony.system.test;

import app.monybatch.mony.business.batch.reader.OpenAPIListReader;
import app.monybatch.mony.business.entity.Stock;
import app.monybatch.mony.system.core.constant.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/test")
public class SyTestCtl {

    @RequestMapping("dart/baedang")
    public void baedang() throws Exception {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        //params.add(BASDD,basDd);
        List<Stock> resList = (List<Stock>) new OpenAPIListReader<>(Stock.class, params,"DART","", DataType.DATA_ZIP).read();
    }

    @RequestMapping("yahoo/ticker")
    public void getTickerData()  throws Exception {
        yahoofinance.Stock stock = YahooFinance.get("AAPL");
        System.out.println("Symbol: " + stock.getSymbol());
        System.out.println("Price: " + stock.getQuote().getPrice());
        System.out.println("Change: " + stock.getQuote().getChangeInPercent());
        System.out.println("P/E: " + stock.getStats().getPe());
        System.out.println("Dividend Yield: " + stock.getDividend().getAnnualYieldPercent());
    }

    @RequestMapping("yahoo/finance")
    public void getYear()  throws Exception {
        try {

            yahoofinance.Stock stock = YahooFinance.get("MSFT"); // 마이크로소프트 종목

            // 1. 시작 날짜와 종료 날짜 설정 (1년 전부터 현재까지)
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(Calendar.YEAR, -1); // 현재 날짜에서 1년을 뺌

            // 2. 시계열 데이터 조회 (1년치, 일별(DAILY) 데이터)
            List<HistoricalQuote> history = stock.getHistory(from, to, Interval.DAILY);

            // 3. 데이터 출력
            log.info("--- 💻 MSFT 1년치 일봉 데이터 ---");
            log.info("총 데이터 개수: {}개", history.size());

            // 최근 5개 데이터만 출력
            int count = 0;
            for (int i = history.size() - 1; i >= 0 && count < 5; i--) {
                HistoricalQuote quote = history.get(i);
                log.info(
                        "날짜: {}, 시가: {}, 종가: {}, 거래량: {}",
                        quote.getDate(),
                        quote.getOpen(),
                        quote.getClose(),
                        quote.getVolume()
                );
                count++;
            }

        } catch (IOException e) {
            log.error("데이터 조회 중 오류 발생: {}", e.getMessage());
        }
    }
}
