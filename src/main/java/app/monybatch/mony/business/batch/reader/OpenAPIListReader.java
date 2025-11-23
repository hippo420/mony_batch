package app.monybatch.mony.business.batch.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.util.MultiValueMap;

import java.util.List;

public class OpenAPIListReader<T> extends OpenAPIBaseReader<T> implements ItemReader<List<T>> {

    private boolean isRead = false; // 한 번 읽었는지 체크하는 플래그

    public OpenAPIListReader(Class<T> clazz, MultiValueMap<String, String> params, String key, String path, app.monybatch.mony.system.core.constant.DataType datatype) {
        super(clazz, params, key, path, datatype);
    }

    @Override
    public List<T> read() throws Exception {
        if (!isRead) {
            List<T> data = fetch(); // 부모의 fetch 사용
            isRead = true; // 읽음 처리

            if (data.isEmpty()) {
                return null; // 데이터 없으면 바로 종료
            }
            return data; // 리스트 통째로 반환 (Job의 ChunkSize는 1이어야 함)
        }
        return null; // 두 번째 호출 시 null 반환하여 Step 종료
    }
}
