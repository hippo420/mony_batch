package app.monybatch.mony.business.repository.jpa;

import app.monybatch.mony.business.entity.news.NewsRss;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRSSRepository extends JpaRepository<NewsRss,Long> {
    @Cacheable
    List<NewsRss> findByUseYn(String useYn);
}
