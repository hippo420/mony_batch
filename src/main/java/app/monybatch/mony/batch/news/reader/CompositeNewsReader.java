package app.monybatch.mony.batch.news.reader;

import app.monybatch.mony.domian.news.entity.NewsDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.Iterator;
import java.util.List;

public class CompositeNewsReader implements ItemReader<NewsDto> {

    private final Iterator<ItemReader<NewsDto>> readerIterator;
    private ItemReader<NewsDto> currentReader;

    public CompositeNewsReader(List<ItemReader<NewsDto>> readers) {
        this.readerIterator = readers.iterator();
        if (readerIterator.hasNext()) {
            this.currentReader = readerIterator.next();
        }
    }

    @Override
    public NewsDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (currentReader == null) {
            return null;
        }

        NewsDto news = currentReader.read();
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
