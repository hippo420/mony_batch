package app.monybatch.mony.batch.stock.reader;

import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.entity.StockTemp;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static app.monybatch.mony.common.constant.ColumnConst.BASDD;

@Configuration
public class StockReaderConfig {

    private final String PATH = "/svc/apis/sto/stk_isu_base_info";
    private final String PRICE_PATH = "/svc/apis/sto/stk_isu_base_info";


    @Bean
    @StepScope
    public OpenAPIItemReader<StockTemp> stockTempApiReader(
            @Value("#{jobParameters['basDd']}") String basDd) {

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add(BASDD, basDd);

        return new OpenAPIItemReader<>(
                StockTemp.class,
                params,
                "KRX",
                PATH,
                DataType.DATA_JSON
        );
    }

    @Bean
    @StepScope
    public OpenAPIItemReader<Stock> stockApiReader(
            @Value("#{jobParameters['basDd']}") String basDd) {

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add(BASDD, basDd);

        return new OpenAPIItemReader<>(
                Stock.class,
                params,
                "KRX",
                PATH,
                DataType.DATA_JSON
        );
    }
}
