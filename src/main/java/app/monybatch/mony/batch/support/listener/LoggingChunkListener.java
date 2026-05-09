package app.monybatch.mony.batch.support.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

@Slf4j
public class LoggingChunkListener extends ChunkListenerSupport {

    private int chunkCount = 0;

    @Override
    public void afterChunk(ChunkContext context) {
        chunkCount++;
        int readCount = Math.toIntExact(context.getStepContext().getStepExecution().getReadCount());
        int writeCount = Math.toIntExact(context.getStepContext().getStepExecution().getWriteCount());
        log.info("Chunk #{} 완료 - totalRead={}, totalWrite={}", chunkCount, readCount, writeCount);
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        Throwable error = (Throwable) context.getAttribute("batch.exception");
        log.error("Chunk #{} 오류 발생 - step={}", chunkCount,
            context.getStepContext().getStepName(), error);
    }
}
