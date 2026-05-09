package app.monybatch.mony.batch.support.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class LoggingItemReadListener<T> implements ItemReadListener<T> {

    private final AtomicLong count = new AtomicLong(0);

    @Override
    public void afterRead(T item) {
        long current = count.incrementAndGet();
        if (current % 1000 == 0) {
            log.info("아이템 읽기 진행 - count={}", current);
        }
    }

    @Override
    public void onReadError(Exception ex) {
        log.error("아이템 읽기 오류 - count={}", count.get(), ex);
    }
}
