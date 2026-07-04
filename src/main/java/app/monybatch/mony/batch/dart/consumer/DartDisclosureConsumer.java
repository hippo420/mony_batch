package app.monybatch.mony.batch.dart.consumer;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.batch.dart.handler.PerformanceDisclosureHandler;
import app.monybatch.mony.batch.dart.writer.DartRssRedisWriter;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.common.core.utils.DartReportCodeResolver;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import app.monybatch.mony.domian.dart.processor.DisclosureProcessor;
import app.monybatch.mony.domian.dart.processor.DisclosureProcessorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DartDisclosureConsumer {

    private static final String RETRY_KEY = "dart:disclosure:retry";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final EntityManagerFactory entityManagerFactory;
    private final DisclosureProcessorFactory processorFactory;
    private final PerformanceDisclosureHandler performanceHandler;

    @Autowired
    public DartDisclosureConsumer(StringRedisTemplate redisTemplate,
                                  ObjectMapper objectMapper,
                                  @Qualifier("batchEntityManager") EntityManagerFactory entityManagerFactory,
                                  DisclosureProcessorFactory processorFactory,
                                  PerformanceDisclosureHandler performanceHandler) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.entityManagerFactory = entityManagerFactory;
        this.processorFactory = processorFactory;
        this.performanceHandler = performanceHandler;
    }

    @Scheduled(fixedDelay = 5_000)
    public void consume() {
        String json;
        while ((json = redisTemplate.opsForList().leftPop(DartRssRedisWriter.QUEUE_KEY)) != null) {
            try {
                DartRssQueueDto item = objectMapper.readValue(json, DartRssQueueDto.class);

                // 영업(잠정)실적 공시: 원본 문서(document.xml) → 파싱 → LLM 요약 별도 파이프라인
                if (item.getType() == DisclosureType.BUSINESS_PERFORMANCE) {
                    performanceHandler.handle(item);
                    continue;
                }

                List<DartDisclosureBase> entities = fetchAndProcess(item);
                if (!entities.isEmpty()) {
                    saveAll(entities);
                    log.info("공시 적재 완료: rceptNo={}, type={}, {}건",
                            item.getRceptNo(), item.getType(), entities.size());
                }
            } catch (Exception e) {
                log.error("공시 처리 실패: {}, error={}", json, e.getMessage());
                redisTemplate.opsForList().rightPush(RETRY_KEY, json);
            }
        }
    }

    private List<DartDisclosureBase> fetchAndProcess(DartRssQueueDto item) {
        DisclosureProcessor processor = processorFactory.getProcessor(item.getType());
        if (processor == null) {
            log.warn("Processor 미등록 type={}", item.getType());
            return List.of();
        }

        JSONObject response = callDartApi(item);
        if (response == null) return List.of();

        return processor.process(response, item);
    }

    private JSONObject callDartApi(DartRssQueueDto item) {
        String rceptDt = item.getRceptNo().substring(0, 8);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("corp_code", item.getCorpCode());

        if (item.getType().isUseReportCode()) {
            // 정기 보고서 기반 API: bsns_year + reprt_code 자동 추론
            params.add("bsns_year",  DartReportCodeResolver.resolveBsnsYear(rceptDt));
            params.add("reprt_code", DartReportCodeResolver.resolveReprtCode(rceptDt));
        } else {
            // 이벤트 공시 기반 API: bgn_de + end_de (당일 범위)
            params.add("bgn_de", rceptDt);
            params.add("end_de", rceptDt);
        }

        try {
            JSONObject response = OpenAPIUtil.requestApi(
                    StockConstant.DART_API_URL, item.getType().getApiPath(), params, Map.of(), DataType.DATA_JSON);
            if (response == null || !"000".equals(response.get("status"))) {
                log.warn("DART API 정상응답 없음: rceptNo={}, status={}", item.getRceptNo(),
                        response != null ? response.get("status") : "null");
                return null;
            }
            return response;
        } catch (Exception e) {
            log.error("DART API 호출 실패: rceptNo={}, error={}", item.getRceptNo(), e.getMessage());
            return null;
        }
    }

    private void saveAll(List<DartDisclosureBase> entities) {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            entities.forEach(em::merge);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
