package app.monybatch.mony.batch.news.writer;

import app.monybatch.mony.batch.news.service.NewsClusterService;
import app.monybatch.mony.common.core.utils.DateUtil;
import app.monybatch.mony.domian.news.dto.NewsAnalysis;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticle;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticleRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class NewsItemWriter implements ItemWriter<NewsArticle> {

    private static final int RETENTION_DAYS = 30;

    @Autowired
    private NewsArticleRepository newsRepository;
    @Autowired
    private OllamaModelClient agent;
    @Autowired
    private VectorStore vectorStore;
    @Autowired
    private NewsClusterService newsClusterService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void write(Chunk<? extends NewsArticle> chunk) throws Exception {
        log.info("Writing {} articles", chunk.size());

        List<NewsArticle> candidates = chunk.getItems().stream()
                .filter(Objects::nonNull)
                .filter(this::isNotAlreadyStored)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            log.info("저장 대상 기사 없음");
            return;
        }

        List<NewsArticle> articlesToSave = new ArrayList<>();

        for (NewsArticle article : candidates) {
            NewsClusterService.ClusterAssignResult clusterResult = newsClusterService.assignCluster(article);

            article.setClusterId(clusterResult.clusterId());
            article.setWeight(clusterResult.weight());
            article.setExposed(clusterResult.exposed());
            article.setRelatedLinks(clusterResult.relatedLinks());

            LocalDateTime publishedAt = resolvePublishedDate(article);
            LocalDateTime expireAt = publishedAt.plusDays(RETENTION_DAYS);
            article.setExpireAt(DateUtil.toStringDateTime(expireAt));
            if("investing".equals(article.getCompany()))
            {
                article.setRepresentative("Y");
            }
            article.setRepresentative("N");
            articlesToSave.add(article);
        }

        analyzeArticles(articlesToSave);
        addVectorDocuments(articlesToSave);
        newsRepository.saveAll(articlesToSave);

        for (NewsArticle article : articlesToSave) {
            log.info("저장완료 title={}, clusterId={}, weight={}, exposed={}, keyword={}, opinion={}",
                    article.getTitle(),
                    article.getClusterId(),
                    article.getWeight(),
                    article.getExposed(),
                    article.getKeywords(),
                    article.getOpinion());
        }
    }

    private boolean isNotAlreadyStored(NewsArticle article) {
        if (article.getLink() == null || article.getLink().isBlank()) {
            return true;
        }

        boolean exists = newsRepository.existsByLink(article.getLink());
        if (exists) {
            log.debug("이미 저장된 링크 SKIP: {}", article.getLink());
            return false;
        }

        return true;
    }

    private void analyzeArticles(List<NewsArticle> articles) throws Exception {
        if (articles.isEmpty()) {
            return;
        }

        List<String> prompts = articles.stream()
                .map(this::buildAnalysisText)
                .toList();

        String data = agent.extractKeyWord(prompts, 1);

        List<NewsAnalysis> result = objectMapper.readValue(
                data,
                new TypeReference<List<NewsAnalysis>>() {}
        );

        int matchSize = Math.min(articles.size(), result.size());

        for (int i = 0; i < matchSize; i++) {
            NewsArticle article = articles.get(i);
            NewsAnalysis analysis = result.get(i);

            article.setType(analysis.getType());
            article.setKeywords(analysis.getKeyword());
            article.setOpinion(analysis.getSentiment());
            article.setReason(analysis.getReason());
        }

        if (result.size() != articles.size()) {
            log.warn("LLM 결과 개수 불일치. articleCount={}, resultCount={}", articles.size(), result.size());
        }
    }

    private void addVectorDocuments(List<NewsArticle> articles) {
        if (articles.isEmpty()) {
            return;
        }

        List<Document> documents = articles.stream()
                .map(article -> {
                    String text = buildVectorText(article);

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("newsId", article.getId());
                    metadata.put("title", article.getTitle());
                    metadata.put("link", article.getLink());
                    metadata.put("clusterId", article.getClusterId());
                    metadata.put("publishedDate", defaultString(article.getPublishedDate()));
                    metadata.put("expireAt", defaultString(article.getExpireAt()));

                    return new Document(text, metadata);
                })
                .toList();

        vectorStore.add(documents);
    }

    private String buildAnalysisText(NewsArticle article) {
        String title = defaultString(article.getTitle());
        String content = defaultString(article.getContent());

        if (content.isBlank()) {
            return title;
        }
        return title + " : " + content;
    }

    private String buildVectorText(NewsArticle article) {
        return defaultString(article.getTitle()) + "\n" + defaultString(article.getContent());
    }

    private LocalDateTime resolvePublishedDate(NewsArticle article) {
        return DateUtil.parseToLocalDateTime(article.getPublishedDate());
    }

    private String defaultString(String value) {
        return value == null ? "" : value.trim();
    }
}
