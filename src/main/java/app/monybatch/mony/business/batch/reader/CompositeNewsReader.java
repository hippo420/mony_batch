package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.business.entity.news.News;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.List;

public class CompositeNewsReader implements ItemReader<News> {

    private final Iterator<ItemReader<News>> readerIterator;
    private ItemReader<News> currentReader;

    public CompositeNewsReader(List<ItemReader<News>> readers) {
        this.readerIterator = readers.iterator();
        if (readerIterator.hasNext()) {
            this.currentReader = readerIterator.next();
        }
    }

    @Override
    public News read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentReader == null) {
            return null;
        }

        News news = currentReader.read();
        if (news != null) {
            return news;
        } else {
            // 현재 Reader가 끝났으면 다음 Reader로 넘어감
            if (readerIterator.hasNext()) {
                currentReader = readerIterator.next();
                return read(); // 재귀 호출로 다음 Reader의 첫 번째 아이템 읽기
            } else {
                return null; // 모든 Reader 완료
            }
        }
    }
}
