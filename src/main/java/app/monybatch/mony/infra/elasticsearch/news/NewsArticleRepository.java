package app.monybatch.mony.infra.elasticsearch.news;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface NewsArticleRepository extends ElasticsearchRepository<NewsArticle, Long> {
    boolean existsByLink(String link);

    List<NewsArticle> findByClusterId(String clusterId);


}
