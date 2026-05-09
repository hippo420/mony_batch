package app.monybatch.mony.batch.stock.tasklet;

import app.monybatch.mony.domian.stock.entity.StockTrade;
import app.monybatch.mony.domian.stock.repository.StockTradeRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertTasklet implements Tasklet {

    private static final BigDecimal FLUC_THRESHOLD = new BigDecimal("5.0");
    private static final int TOP_VOLUME_COUNT = 10;
    private static final int NEWS_TOP_K = 5;
    private static final double NEWS_SIMILARITY_THRESHOLD = 0.6;
    private static final long REDIS_TTL_MINUTES = 60L;
    private static final String REDIS_KEY_PREFIX = "stock:alert:";

    private final StockTradeRepository stockTradeRepository;
    private final VectorStore vectorDBStore;
    private final OllamaModelClient ollamaModelClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        String basDd = stockTradeRepository.findMaxDate();
        if (basDd == null) {
            log.warn("StockAlert - 기준일자 데이터 없음");
            return RepeatStatus.FINISHED;
        }

        List<StockTrade> targets = detectTargetStocks(basDd);
        if (targets.isEmpty()) {
            log.info("StockAlert - 탐지된 이상 종목 없음 (basDd={})", basDd);
            return RepeatStatus.FINISHED;
        }

        log.info("StockAlert - 탐지된 이상 종목 수: {} (basDd={})", targets.size(), basDd);

        for (StockTrade stock : targets) {
            try {
                cacheStockAlertReport(basDd, stock);
            } catch (Exception e) {
                log.error("StockAlert - 종목 처리 실패 ({}/{}): {}", stock.getIsuNm(), stock.getIsuSrtCd(), e.getMessage());
            }
        }

        return RepeatStatus.FINISHED;
    }

    // 등락률 급등락 + 거래량 상위 종목 합산 (중복 제거)
    private List<StockTrade> detectTargetStocks(String basDd) {
        List<StockTrade> flucSpiked = stockTradeRepository.findSpikedStocks(basDd, FLUC_THRESHOLD);
        List<StockTrade> topVolume = stockTradeRepository.findTopVolumeStocks(
                basDd, PageRequest.of(0, TOP_VOLUME_COUNT));

        Set<Long> seen = new HashSet<>();
        List<StockTrade> merged = new ArrayList<>();
        Stream.concat(flucSpiked.stream(), topVolume.stream())
                .filter(s -> seen.add(s.getId()))
                .forEach(merged::add);
        return merged;
    }

    private void cacheStockAlertReport(String basDd, StockTrade stock) {
        String redisKey = REDIS_KEY_PREFIX + basDd + ":" + stock.getIsuSrtCd();

        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
            log.debug("StockAlert - 이미 캐시됨: {}", redisKey);
            return;
        }

        List<String> relatedNews = fetchRelatedNews(stock.getIsuNm());
        String report = generateReport(stock, relatedNews);

        stringRedisTemplate.opsForValue().set(redisKey, report, REDIS_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("StockAlert - 리포트 캐시 완료: {} [{}]", stock.getIsuNm(), redisKey);
    }

    private List<String> fetchRelatedNews(String stockName) {
        String query = stockName + " 주가 거래량 이슈";
        List<Document> docs = vectorDBStore.similaritySearch(
                SearchRequest.query(query)
                        .withTopK(NEWS_TOP_K)
                        .withSimilarityThreshold(NEWS_SIMILARITY_THRESHOLD)
        );
        return docs == null ? List.of() : docs.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }

    private String generateReport(StockTrade stock, List<String> newsList) {
        StringBuilder newsContext = new StringBuilder();
        for (int i = 0; i < newsList.size(); i++) {
            newsContext.append(String.format("뉴스%d: %s\n", i + 1, newsList.get(i)));
        }

        String prompt = String.format("""
                역할: 당신은 주식 시장 분석 전문가입니다.

                아래 종목의 급등락/거래량 급변 현상을 분석하고 투자 참고 리포트를 JSON으로 작성하세요.

                [종목 정보]
                - 종목명: %s (%s)
                - 시장: %s
                - 기준일자: %s
                - 종가: %d원
                - 등락률: %s%%
                - 거래량: %d주

                [관련 뉴스]
                %s

                출력 형식(JSON만 출력, 다른 텍스트 금지):
                {
                  "stockName": "종목명",
                  "stockCode": "종목코드",
                  "basDd": "기준일자",
                  "closingPrice": 종가(숫자),
                  "fluctuationRate": 등락률(숫자),
                  "volume": 거래량(숫자),
                  "summary": "2~3줄 요약",
                  "sentiment": "POSITIVE or NEGATIVE or NEUTRAL",
                  "reason": "급등락/거래량 변동 원인",
                  "risk": "투자 주의사항",
                  "generatedAt": "%s"
                }

                규칙:
                1. 반드시 JSON만 출력 (```json 코드블록 없이)
                2. 근거 없는 추측 금지
                3. 관련 뉴스가 없으면 기술적 분석만으로 작성
                """,
                stock.getIsuNm(), stock.getIsuSrtCd(),
                stock.getMktNm(),
                stock.getBasDd(),
                stock.getTddClsprc(),
                stock.getFlucRt(),
                stock.getAccTrdvol(),
                newsContext.isEmpty() ? "관련 뉴스 없음" : newsContext,
                LocalDateTime.now()
        );

        return ollamaModelClient.generate(prompt);
    }
}
