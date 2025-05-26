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
}
