package app.monybatch.mony.business.batch.writer;

import app.monybatch.mony.business.entity.news.NewsArticle;
import app.monybatch.mony.business.repository.es.NewsArticleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@AllArgsConstructor
public class NewsElasticsearchItemWriter implements ItemWriter<NewsArticle> {

    private final NewsArticleRepository newsArticleRepository;

    @Override
    public void write(Chunk<? extends NewsArticle> chunk) throws Exception {
        log.info("Writing {} articles", chunk.size());
        for (NewsArticle article : chunk) {
            log.info("Writing article: {}", article.getTitle());
        }
        newsArticleRepository.saveAll(chunk.getItems());
        
        log.info("💾 Elasticsearch에 {}개의 기사 Chunk 저장 완료.", chunk.size());
    }
}
