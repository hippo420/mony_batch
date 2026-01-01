package app.monybatch.mony.business.batch.writer;

import app.monybatch.mony.business.entity.news.NewsArticle;
import app.monybatch.mony.business.repository.es.NewsArticleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
@Slf4j
@AllArgsConstructor
public class NewsElasticsearchItemWriter implements ItemWriter<List<NewsArticle>> {

    private final NewsArticleRepository newsArticleRepository;




    @Override
    public void write(Chunk<? extends List<NewsArticle>> chunk) throws Exception {
        for (List<NewsArticle> articles : chunk) {
            log.info("Writing {} articles", articles.size());
            for (NewsArticle article : articles) {
                log.info("Writing {} article", article);
            }
            newsArticleRepository.saveAll(articles);

        }
        System.out.println(String.format("💾 Elasticsearch에 %d개의 기사 Chunk 저장 완료.", chunk.getItems().getFirst().size()));
    }
}
