package app.monybatch.mony.domian.news.repository;

import app.monybatch.mony.domian.news.entity.NewsCluster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsClusterRepository extends JpaRepository<NewsCluster, String> {

}
