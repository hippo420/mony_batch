package app.monybatch.mony.batch.news.service;

import app.monybatch.mony.common.core.utils.DateUtil;
import app.monybatch.mony.domian.news.entity.NewsCluster;
import app.monybatch.mony.domian.news.repository.NewsClusterRepository;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticle;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsClusterService {

    private static final double SIMILARITY_THRESHOLD = 0.85;
    private static final int TOP_K = 5;
    private static final int EXPOSE_WEIGHT = 3;
    private static final int RETENTION_DAYS = 30;

    private final VectorStore vectorStore;
    private final NewsClusterRepository clusterRepository;
    private final NewsArticleRepository newsArticleRepository;

    @Transactional("batchTransactionManager")
    public ClusterAssignResult assignCluster(NewsArticle article) {
        String queryText = buildSearchText(article);

        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(queryText)
                        .withTopK(TOP_K)
                        .withSimilarityThreshold(SIMILARITY_THRESHOLD)
        );

        if (similarDocs == null || similarDocs.isEmpty()) {
            return createNewCluster(article);
        }

        String clusterId = chooseClusterId(similarDocs);
        NewsCluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new IllegalStateException("cluster not found: " + clusterId));

        LocalDateTime articlePublishedAt = resolvePublishedDate(article);
        LocalDateTime articleExpireAt = articlePublishedAt.plusDays(RETENTION_DAYS);

        int nextClusterSize = cluster.getClusterSize() + 1;
        int nextWeight = cluster.getWeight() + 1;

        cluster.setClusterSize(nextClusterSize);
        cluster.setWeight(nextWeight);
        cluster.setTrendScore(calculateTrend(nextClusterSize, nextWeight));
        cluster.setUpdatedAt(LocalDateTime.now());

        if (cluster.getLastPublishedAt() == null || articlePublishedAt.isAfter(cluster.getLastPublishedAt())) {
            cluster.setLastPublishedAt(articlePublishedAt);
            cluster.setMainNewsId(article.getId());
        }

        if (cluster.getExpireAt() == null || articleExpireAt.isAfter(cluster.getExpireAt())) {
            cluster.setExpireAt(articleExpireAt);
        }

        clusterRepository.save(cluster);

        if (nextWeight >= EXPOSE_WEIGHT) {
            updateRepresentativeArticle(clusterId, article.getId());
        }

        List<String> relatedLinks = newsArticleRepository.findByClusterId(clusterId).stream()
                .map(NewsArticle::getLink)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));

        if (article.getLink() != null && !article.getLink().isBlank() && !relatedLinks.contains(article.getLink())) {
            relatedLinks.add(article.getLink());
        }

        return new ClusterAssignResult(
                clusterId,
                nextWeight,
                nextWeight >= EXPOSE_WEIGHT,
                relatedLinks
        );
    }

    private ClusterAssignResult createNewCluster(NewsArticle article) {
        String clusterId = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishedAt = resolvePublishedDate(article);
        LocalDateTime expireAt = publishedAt.plusDays(RETENTION_DAYS);

        NewsCluster cluster = new NewsCluster();
        cluster.setClusterId(clusterId);
        cluster.setMainNewsId(article.getId());
        cluster.setClusterSize(1);
        cluster.setWeight(1);
        cluster.setTrendScore(calculateTrend(1, 1));
        cluster.setCreatedAt(now);
        cluster.setUpdatedAt(now);
        cluster.setLastPublishedAt(publishedAt);
        cluster.setExpireAt(expireAt);

        clusterRepository.save(cluster);

        List<String> relatedLinks = article.getLink() == null || article.getLink().isBlank()
                ? new ArrayList<>()
                : new ArrayList<>(List.of(article.getLink()));

        return new ClusterAssignResult(
                clusterId,
                1,
                false,
                relatedLinks
        );
    }

    private String chooseClusterId(List<Document> similarDocs) {
        Map<String, Long> clusterCountMap = similarDocs.stream()
                .map(doc -> doc.getMetadata().get("clusterId"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        return clusterCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No clusterId found in vector metadata"));
    }

    private String buildSearchText(NewsArticle article) {
        String title = defaultString(article.getTitle());
        String content = defaultString(article.getContent());
        return title + "\n" + content;
    }

    private LocalDateTime resolvePublishedDate(NewsArticle article) {
        return DateUtil.parseToLocalDateTime(article.getPublishedDate());
    }

    private double calculateTrend(int clusterSize, int weight) {
        return Math.log(clusterSize + 1) + (weight * 0.3);
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }

    private void updateRepresentativeArticle(String clusterId, String currentArticleId) {
        // 1. 해당 클러스터의 모든 기사 representative를 'N'으로 초기화
        log.info("updateRepresentativeArticle - clusterId {}",clusterId);
        // 1. 해당 클러스터 아이디를 가진 모든 기사 조회
        List<NewsArticle> articles = newsArticleRepository.findByClusterId(clusterId);

        // 2. 대표 상태 수정
        articles.forEach(article -> article.setRepresentative("Y"));

        // 3. 일괄 저장 (Elasticsearch는 ID가 같으면 덮어쓰기(Upsert)로 동작함)
        newsArticleRepository.saveAll(articles);
    }

    public record ClusterAssignResult(
            String clusterId,
            int weight,
            boolean exposed,
            List<String> relatedLinks
    ) {
    }
}
