package app.monybatch.mony.batch.support.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * KIS OpenAPI 유량 제한: 1초당 최대 10건
 * 토큰 버킷 방식 — 유휴 상태에서 재시작해도 버킷이 쌓이지 않음
 */
@Slf4j
@Component
public class KisApiRateLimiter {

    private static final long INTERVAL_MS = 250L; // 250ms = 4/sec
    private final AtomicLong nextScheduledMs = new AtomicLong(System.currentTimeMillis());

    /**
     * 슬롯을 예약하고 해당 슬롯 시각까지 대기한다.
     * 여러 스레드가 동시에 호출해도 각 스레드가 독립적인 슬롯을 받으므로 thread-safe.
     * 유휴 후 재호출 시 과거 슬롯이 쌓이지 않도록 Math.max(t, now) 로 현재 시각에 리셋.
     */
    public void acquire() throws InterruptedException {
        long now = System.currentTimeMillis();
        // 슬롯 예약: 이전 슬롯과 현재 시각 중 큰 값 기준으로 INTERVAL_MS 후가 다음 슬롯
        long slotStart = nextScheduledMs.updateAndGet(t -> Math.max(t, now) + INTERVAL_MS) - INTERVAL_MS;
        long waitMs = slotStart - now;
        if (waitMs > 0) {
            Thread.sleep(waitMs);
        }
    }
}
