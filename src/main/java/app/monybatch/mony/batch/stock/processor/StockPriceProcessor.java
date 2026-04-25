package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.domian.stock.entity.StockTrade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StockPriceProcessor implements ItemProcessor<String, List<StockTrade>> {

    @Override
    public List<StockTrade> process(String basDd) throws Exception {
        List<StockTrade> datas = new ArrayList<>();
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("basDd", basDd);

        OpenAPIItemReader<StockTrade> reader = new OpenAPIItemReader<>(
                StockTrade.class, params, "KRX", StockConstant.KRX_STOCK_TRX_URL, DataType.DATA_JSON);

        StockTrade item;
        while ((item = reader.read()) != null) {
            if (StringUtils.hasText(item.getIsuCd())) {
                datas.add(item);
            }
        }

        log.info("기준일 [{}], 종가 {}건 수신 완료", basDd, datas.size());
        return datas;
    }
}
