package app.monybatch.mony.business.batch.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.util.MultiValueMap;

import java.util.Iterator;
import java.util.List;

public class OpenAPIItemReader<T> extends OpenAPIBaseReader<T> implements ItemReader<T> {

    private Iterator<T> iterator;

    public OpenAPIItemReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, app.monybatch.mony.system.core.constant.DataType datatype) {
        super(clazz, params, key, path, datatype);
    }

    @Override
    public T read() throws Exception {
        if (iterator == null) {
            List<T> data = fetch(); // 부모의 fetch 사용
            iterator = data.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
