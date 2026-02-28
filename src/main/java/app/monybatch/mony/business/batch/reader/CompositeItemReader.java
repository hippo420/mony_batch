package app.monybatch.mony.business.batch.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CompositeItemReader<T> implements ItemReader<T> {

    private final Queue<ItemReader<T>> readers;

    public CompositeItemReader(List<ItemReader<T>> readers) {
        this.readers = new LinkedList<>(readers);
    }

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        T item = null;
        while (!readers.isEmpty()) {
            item = readers.peek().read();
            if (item != null) {
                return item;
            }
            readers.poll(); // 현재 Reader가 null을 반환하면(데이터 소진), 큐에서 제거하고 다음 Reader로 넘어감
        }
        return null; // 모든 Reader가 소진됨
    }
}
