package app.monybatch.mony.business.batch.reader;

import org.springframework.batch.item.ItemReader;

import java.util.ArrayList;
import java.util.List;

public class GroupingItemReader<T> implements ItemReader<List<T>> {

    private final ItemReader<T> delegate; // 기존 단건 Reader
    private final int pageSize;           // 묶을 개수 (예: 10)

    public GroupingItemReader(ItemReader<T> delegate, int pageSize) {
        this.delegate = delegate;
        this.pageSize = pageSize;
    }

    @Override
    public List<T> read() throws Exception {
        List<T> items = new ArrayList<>();

        for (int i = 0; i < pageSize; i++) {
            T item = delegate.read();

            if (item == null) {
                // 더 이상 읽을 데이터가 없는데, 버퍼에 담긴 게 있다면 반환하고 끝냄
                if (!items.isEmpty()) {
                    return items;
                }
                return null; // 진짜 끝
            }

            items.add(item);
        }

        return items; // pageSize만큼 꽉 채워서 반환
    }
}
