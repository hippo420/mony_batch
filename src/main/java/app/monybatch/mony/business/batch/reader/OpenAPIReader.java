package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.system.core.constant.DataType;
import app.monybatch.mony.system.utils.JsonUtil;
import app.monybatch.mony.system.utils.OpenAPIUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;

import static app.monybatch.mony.business.constant.StockConstant.KRX_STOCK_URL;

@Slf4j
@Component
@StepScope
public class OpenAPIReader<T> implements ItemReader<T> {

    private final Class<T> clazz; // 제너릭 타입 클래스
    private final String key;
    private final MultiValueMap<String,String> params;

    private Iterator<T> iterator;

    public OpenAPIReader(Class<T> clazz, MultiValueMap<String,String> params ,String key) {
        this.clazz = clazz;
        this.params = params;
        this.key = key;
    }

    @Override
    public T read() throws Exception {

        String keyCode = null;
        if(this.key.equals("KRX"))
        {
            keyCode = "OutBlock_1";
        }
        else if(this.key.equals("DART"))
        {
            keyCode = "OutBlock_2";
        }
        if (iterator == null) {
            JSONObject data = OpenAPIUtil.requestApiFromFile(KRX_STOCK_URL, "", params, DataType.DATA_JSON);
            List<T> apiDataList  = JsonUtil.convert(new org.json.JSONObject(data.toJSONString()),keyCode, clazz);
            log.info("주식종목 Fetch: {} 건", apiDataList.size());
            iterator = apiDataList.iterator();
        }

        // 다음 항목이 있으면 반환, 없으면 null => chunk처리시 List로는 처리X, 단건으로 처리
        return iterator != null && iterator.hasNext() ? iterator.next() : null;

    }
}
