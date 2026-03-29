package app.monybatch.mony.domian.news.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news_cluster")
@Getter
@Setter
public class NewsCluster {

    @Id
    private String clusterId;
    private String mainNewsId;
    private int clusterSize;
    private int weight;
    private double trendScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime expireAt;

}
