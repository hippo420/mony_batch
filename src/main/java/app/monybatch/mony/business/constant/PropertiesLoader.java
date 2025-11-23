package app.monybatch.mony.business.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesLoader {
    @Value("${apikey.openapi.krx.key}")
    public void setKrxApiKey(String value) {
        StockConstant.KRX_API_KEY = value;
    }

    @Value("${apikey.openapi.data.key}")
    public void setDataApiKey(String value) {
        StockConstant.DOMESTIC_STOCK_KEY = value;
    }

    @Value("${apikey.openapi.dart.key}")
    public void setDartApiKey(String value) {
        StockConstant.DART_KEY = value;
    }

    @Value("${apikey.openapi.news.key}")
    public void setNewApiKey(String value) {
        StockConstant.NAVER_NEWS_API_KEY = value;
    }

    @Value("${apikey.openapi.news.id}")
    public void setNewApiId(String value) {
        StockConstant.NAVER_NEWS_API_ID = value;
    }

    @Value("${apikey.gemini.api.key}")
    public void setGeminiAPiKey(String value) {
        StockConstant.GEMINI_API_KEY = value;
    }
}
