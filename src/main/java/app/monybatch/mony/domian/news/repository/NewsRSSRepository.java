package app.monybatch.mony.domian.news.repository;

import app.monybatch.mony.domian.news.entity.NewsRss;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRSSRepository extends JpaRepository<NewsRss,Long> {
    @Cacheable
    List<NewsRss> findByUseYn(String useYn);
}
