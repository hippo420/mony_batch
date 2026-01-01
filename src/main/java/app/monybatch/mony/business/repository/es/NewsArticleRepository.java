package app.monybatch.mony.business.repository.es;

import app.monybatch.mony.business.entity.news.NewsArticle;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsArticleRepository extends ElasticsearchRepository<NewsArticle, Long> {
}
