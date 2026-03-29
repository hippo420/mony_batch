-- 1. pgvector 확장 활성화 (이미 활성화되어 있다면 무시)
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. 기존 테이블이 존재하면 삭제 (초기화)
DROP TABLE IF EXISTS vector_store;

-- 3. 768 차원 벡터를 포함한 테이블 생성
CREATE TABLE vector_store (
                              id BIGSERIAL PRIMARY KEY,
                              content TEXT NOT NULL,                -- buildVectorText(article)의 결과
                              news_id VARCHAR(255),                 -- metadata.put("newsId", ...)
                              title TEXT,                           -- metadata.put("title", ...)
                              link TEXT,                            -- metadata.put("link", ...)
                              cluster_id VARCHAR(200),              -- metadata.put("clusterId", ...)
                              published_date TIMESTAMP,             -- metadata.put("publishedDate", ...)
                              expire_at TIMESTAMP,                  -- metadata.put("expireAt", ...)
                              embedding VECTOR(768)                 -- 768 차원 임베딩 벡터
);

-- 4. 검색 성능 최적화를 위한 HNSW 인덱스 생성
-- 768 차원 데이터에 최적화된 설정입니다.
CREATE INDEX ON vector_store
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);