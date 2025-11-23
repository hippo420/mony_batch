package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.system.core.constant.DataType;
import app.monybatch.mony.system.utils.JsonUtil;
import app.monybatch.mony.system.utils.OpenAPIUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;

import static app.monybatch.mony.business.constant.StockConstant.*;

@Slf4j
public abstract class OpenAPIBaseReader<T> {

    protected final Class<T> clazz;
    protected final String key;
    protected final MultiValueMap<String, String> params;
    protected final String path;
    protected final DataType datatype;

    protected OpenAPIBaseReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, DataType datatype) {
        this.clazz = clazz;
        this.params = params;
        this.key = key;
        this.path = path;
        this.datatype = datatype;
    }

    // 공통 로직: API 호출 후 List<T> 반환
    protected List<T> fetch() throws Exception {
        String URL = null;
        String keyCode = null;

        if ("KRX".equals(this.key)) {
            keyCode = "OutBlock_1";
            URL = KRX_STOCK_URL;
        } else if ("DART".equals(this.key)) {
            keyCode = "list";
            URL = DART_API_URL;
        } else if ("NEWS".equals(this.key)) {
            keyCode = "items";
            URL = NAVER_NEWS_API_URL;
        }

        JSONObject data;
        if (!"NEWS".equals(this.key)) {
            data = OpenAPIUtil.requestApiFromFile(URL, path, params, datatype);
        } else {
            data = OpenAPIUtil.requestApi(URL, path, params, datatype);
        }

        if (data != null) {
            List<T> result = JsonUtil.convert(new org.json.JSONObject(data.toJSONString()), keyCode, clazz);
            log.info("✅ [{}] Fetch 성공: {} 건", this.key, result.size());
            return result;
        } else {
            log.info("⚠️ [{}] Fetch 결과 없음", this.key);
            return Collections.emptyList();
        }
    }
}
