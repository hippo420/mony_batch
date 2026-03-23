package app.monybatch.mony.batch.support.reader;

import app.monybatch.mony.common.constant.DataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class OpenAPIItemReader<T> extends OpenAPIBaseReader<T> implements ItemReader<T> {

    private Iterator<T> iterator;

    public OpenAPIItemReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, DataType datatype) {
        super(clazz, params, key, path, datatype, null);
    }

    public OpenAPIItemReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, DataType datatype, ConcurrentHashMap<String, String> headers) {
        super(clazz, params, key, path, datatype, headers);
    }

    public OpenAPIItemReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, DataType datatype, ConcurrentHashMap<String, String> headers, String isuSrtCd) {
        super(clazz, params, key, path, datatype, headers,isuSrtCd);
    }

    @Override
    public T read() throws Exception {
        if (iterator == null) {
            // KIS API는 초당 호출 제한이 있으므로 API 호출 전에 지연시간을 줍니다.

            List<T> data = fetch(); // 부모의 fetch 사용
            //log.info("fetch건수 : {}",data.size());
            iterator = data.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
