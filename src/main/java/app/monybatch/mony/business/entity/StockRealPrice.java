package app.monybatch.mony.business.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Setter @Getter
public class StockRealPrice {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private String stockCode;
    private String stockName;
    private BigDecimal currentPrice; // 910
    private String signedPrice; // 10 (부호 포함)
    private BigDecimal askPrice;    // 27 (매도 호가)
    private BigDecimal bidPrice;    // 28 (매수 호가)
    private long tradeAmount;   // 911 (누적 거래량)
    private long unitTradeAmount; // 915 (순간 체결량)
    private LocalTime tradeTime; // 908
    public static StockRealPrice fromRawData(String item, List<Map<String, Object>> values) {
        StockRealPrice data = new StockRealPrice();
        data.stockCode = item;

        for (Map<String, Object> entry : values) {
            String key = entry.keySet().iterator().next();
            Object value = entry.get(key);

            switch (key) {
                case "302": data.stockName = (String) value; break;
                case "10": data.signedPrice = (String) value; break;
                case "27": data.askPrice = new BigDecimal((String) value); break;
                case "28": data.bidPrice = new BigDecimal((String) value); break;
                case "908": data.tradeTime = LocalTime.parse((String) value, TIME_FORMATTER); break;
                case "910": data.currentPrice = new BigDecimal(String.valueOf(value)); break;
                case "911": data.tradeAmount = Long.parseLong(String.valueOf(value)); break;
                case "915": data.unitTradeAmount = Long.parseLong(String.valueOf(value)); break;
                default:
                    // 알 수 없는 필드는 무시합니다.
            }
        }
        return data;
    }


    // DTO의 모든 필드를 포함하는 toString() 메서드 (로그용)
    @Override
    public String toString() {
        return "StockData{" +
                "code='" + stockCode + '\'' +
                ", name='" + stockName + '\'' +
                ", price=" + currentPrice +
                ", time=" + tradeTime +
                '}';
    }
}
