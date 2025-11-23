package app.monybatch.mony.business.batch.reader;

import app.monybatch.mony.business.entity.news.NewsArticle;
import org.springframework.batch.item.ItemReader;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NewsApiItemReader implements ItemReader<NewsArticle> {

    private final List<NewsArticle> articles;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public NewsApiItemReader(MultiValueMap<String,String> params) {
        // ⭐ [TODO] 여기에 실제 네이버/카카오 API 호출 및 파싱 로직 구현
        // 여기서는 테스트용 가상 데이터 생성
        this.articles = List.of(

                // ... (더 많은 기사 데이터) ...
        );
    }

    @Override
    public NewsArticle read() {
        if (currentIndex.get() < articles.size()) {
            // 리스트의 다음 항목을 읽고 인덱스 증가
            return articles.get(currentIndex.getAndIncrement());
        }
        // 모든 항목을 다 읽으면 null 반환 (Reader 종료)
        return null;
    }
}
