package app.monybatch.mony.common.config;


import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorConfig {
    @Bean
    public VectorStore vectorDBStore(EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        // 생성자를 통해 설정값 주입
        // 기본값: "vector_store", "embedding", "content", "metadata" 순서
        return new PgVectorStore(jdbcTemplate, embeddingModel,768);
    }
}
