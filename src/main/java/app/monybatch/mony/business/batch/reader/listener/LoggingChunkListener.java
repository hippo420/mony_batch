package app.monybatch.mony.business.batch.reader.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

public class LoggingChunkListener extends ChunkListenerSupport {
    private static final Logger logger = LoggerFactory.getLogger(LoggingChunkListener.class);
    private int chunkCount = 0;

    @Override
    public void beforeChunk(ChunkContext context) {
        // 청크가 시작될 때 호출됩니다.
    }

    @Override
    public void afterChunk(ChunkContext context) {
        // 청크가 끝날 때마다 카운터를 증가시키고 로그를 남깁니다.
        chunkCount++;
        logger.info("Chunk {} has been processed.", chunkCount);
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        logger.error("Error occurred in chunk {}", chunkCount);
    }
}
