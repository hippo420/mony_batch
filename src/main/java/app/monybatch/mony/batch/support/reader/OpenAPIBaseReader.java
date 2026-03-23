package app.monybatch.mony.batch.support.reader;

import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static app.monybatch.mony.common.constant.StockConstant.*;

@Slf4j
public abstract class OpenAPIBaseReader<T> {

    protected final Class<T> clazz;
    protected final String key;
    protected final MultiValueMap<String, String> params;
    protected final String path;
    protected final DataType datatype;
    protected final ConcurrentHashMap<String, String> headers;
    protected String isuSrtCd;

    protected OpenAPIBaseReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, DataType datatype, ConcurrentHashMap<String, String> headers, String isuSrtCd) {
        this.clazz = clazz;
        this.params = params;
        this.key = key;
        this.path = path;
        this.datatype = datatype;
        this.headers = headers;
        this.isuSrtCd = isuSrtCd;
    }

    protected OpenAPIBaseReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, DataType datatype, ConcurrentHashMap<String, String> headers) {
        this.clazz = clazz;
        this.params = params;
        this.key = key;
        this.path = path;
        this.datatype = datatype;
        this.headers = headers;
    }

    // 공통 로직: API 호출 후 List<T> 반환
    protected List<T> fetch() throws Exception {
        String URL = null;
        String keyCode = null;

        if ("KRX".equals(this.key)) {
            keyCode = "OutBlock_1";
            URL = KRX_STOCK_URL;
        }
        else if ("DART".equals(this.key)) {
            keyCode = "list";
            URL = DART_API_URL;
        } else if ("NEWS".equals(this.key)) {
            keyCode = "items";
            URL = NAVER_NEWS_API_URL;
        } else if ("KIS".equals(this.key)) {
            keyCode = "output2";
            URL = KIS_API_URL;
        } else{
            throw new InvalidParameterException("적절한 Key값을 넣어야 합니다.");
        }

        JSONObject data;
        if (!"NEWS".equals(this.key)) {
            if ("KIS".equals(key)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Sleep interrupted", e);
                }
            }
            data = OpenAPIUtil.requestApiFromFile(URL, path, params, headers, datatype);

        } else {
            data = OpenAPIUtil.requestApi(URL, path, params, headers, datatype);
        }
        //log.info("data =[{}]",data);
        if (data != null) {
            List<T> result = JsonUtil.convert(new org.json.JSONObject(data.toJSONString()), keyCode, clazz);
            // ✅ KIS API인 경우 리플렉션을 사용하여 종목 코드 주입
            if ("KIS".equals(this.key) && !result.isEmpty()) {
                //result.forEach(item -> log.info("Item: {}", item));

                for (T item : result) {
                    try {
                        // 1. 해당 클래스에 "isuSrtCd" 필드가 있는지 확인
                        Field field = item.getClass().getDeclaredField("isuSrtCd");

                        // 2. private 필드인 경우 접근 허용
                        field.setAccessible(true);

                        // 3. 값 주입
                        field.set(item, isuSrtCd);

                    } catch (NoSuchFieldException e) {
                        // 필드가 없는 경우 로그만 남기고 다음으로 진행 (유연한 처리)
                        log.warn("{} 클래스에 isuSrtCd 필드가 존재하지 않습니다.", clazz.getSimpleName());
                        break;
                    } catch (IllegalAccessException e) {
                        log.error("리플렉션 데이터 주입 중 오류 발생", e);
                    }
                }
            }
            //log.info("✅ [{}] Fetch 성공: {} 건", this.key, result.size());
            return result;
        } else {
            log.info("⚠️ [{}] Fetch 결과 없음", this.key);
            return Collections.emptyList();
        }
    }
}
