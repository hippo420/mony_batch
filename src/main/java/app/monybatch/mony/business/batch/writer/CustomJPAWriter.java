package app.monybatch.mony.business.batch.writer;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
@AllArgsConstructor
public class CustomJPAWriter<T> implements ItemWriter<T> {
    private final  Long CHUNK_SIZE = 100L;
    private final EntityManager entityManager;



    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        long chunkSize = chunk.getItems().size();

        for (int i = 0; i < chunkSize; i++) {
            entityManager.merge(chunk.getItems().get(i));
            if(i == chunkSize - 1){
                entityManager.flush();
                entityManager.clear();
                break;
            }
            if (i % this.CHUNK_SIZE == 0) { // 배치 사이즈마다 flush
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}
