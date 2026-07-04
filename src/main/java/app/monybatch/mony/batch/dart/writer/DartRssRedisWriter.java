package app.monybatch.mony.batch.dart.writer;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DartRssRedisWriter implements ItemWriter<DartRssQueueDto> {

    public static final String QUEUE_KEY = "dart:disclosure:queue";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void write(Chunk<? extends DartRssQueueDto> chunk) throws Exception {
        for (DartRssQueueDto item : chunk) {
            String json = objectMapper.writeValueAsString(item);
            redisTemplate.opsForList().rightPush(QUEUE_KEY, json);
            log.debug("큐 적재: rceptNo={}, type={}", item.getRceptNo(), item.getType());
        }
        log.info("RSS 배치 큐 적재 완료: {}건", chunk.size());
    }
}
