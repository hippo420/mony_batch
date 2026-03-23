package app.monybatch.mony.infra.elasticsearch.news;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface NewsArticleRepository extends ElasticsearchRepository<NewsArticle, Long> {
}
