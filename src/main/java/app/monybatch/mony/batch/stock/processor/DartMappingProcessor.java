package app.monybatch.mony.batch.stock.processor;

import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.dart.DartCorpInfo;
import app.monybatch.mony.domian.dart.DartMapping;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.repository.StockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class DartMappingProcessor implements ItemProcessor<Stock, Stock> {


    @Autowired
    private StockRepository repository;

    private static final String CORP_CODE_PATH = "/api/corpCode.xml";
    private static final String COMPANY_PATH = "/api/company.json";
    private static final int THREAD_COUNT = 5;
    // 분당 800회 → 75ms 간격
    private static final long RATE_INTERVAL_MS = 60_000L / 800;

    private Map<String, String> corpCodeMap;
    private Map<String, DartCorpInfo> corpInfoMap;
    private Map<String, String> stockCodeMap;
    private volatile boolean initialized = false;

    // 5개 쓰레드가 공유하는 슬롯 타임스탬프 (lock-free rate limiter)
    private final AtomicLong nextSlotTime = new AtomicLong(0L);

    private synchronized void init() {
        if (initialized) return;
        stockCodeMap = loadStockCodeMap();
        corpCodeMap = loadCorpCodeMap();
        corpInfoMap = loadCorpInfoMap();
        initialized = true;
    }

    private Map<String, String> loadStockCodeMap() {
        Map<String, String> map = new HashMap<>();
        List<String> codes = repository.findDistinctIsuSrtCd();
        log.info("DB 조회 건수 :{}",codes.size());
        codes.forEach(code -> map.put(code, code));
        return map;
    }

    private Map<String, String> loadCorpCodeMap() {
        Map<String, String> map = new HashMap<>();
        try {
            OpenAPIItemReader<DartMapping> reader = new OpenAPIItemReader<>(
                    DartMapping.class, new LinkedMultiValueMap<>(), "DART", CORP_CODE_PATH, DataType.DATA_ZIP);
            DartMapping item;
            while ((item = reader.read()) != null) {
                if (StringUtils.hasText(item.getStock_code())) {
                    if(stockCodeMap.containsKey(item.getStock_code()))
                        map.put(item.getStock_code(), item.getCorp_code());
                }
            }
        } catch (Exception e) {
            log.error("DART corpCode 수신 오류", e);
        }
        return map;
    }

    private Map<String, DartCorpInfo> loadCorpInfoMap() {
        Map<String, DartCorpInfo> map = new ConcurrentHashMap<>();
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Map.Entry<String, String>> entries = new ArrayList<>(corpCodeMap.entrySet());
        int total = entries.size();
        int partitionSize = (int) Math.ceil((double) total / THREAD_COUNT);

        log.info("DART company 로딩 시작: 총 {}건 / {}개 쓰레드 / 분당 {}회 제한",
                total, THREAD_COUNT, 60_000L / RATE_INTERVAL_MS);

        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT)) {
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < total; i += partitionSize) {
                List<Map.Entry<String, String>> partition =
                        new ArrayList<>(entries.subList(i, Math.min(i + partitionSize, total)));
                futures.add(executor.submit(() -> fetchPartition(partition, map, mapper)));
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("DART company 로딩 중단", e);
                } catch (ExecutionException e) {
                    log.error("DART company 파티션 처리 오류", e.getCause());
                }
            }
        }

        log.info("DART company 로딩 완료: {}건 수집", map.size());
        return map;
    }

    /**
     * 5개 쓰레드가 공유하는 rate limiter.
     * 각 쓰레드가 슬롯을 원자적으로 예약하고, 해당 시각까지 대기.
     * - 유휴 상태 후 재시작 시 now 기준으로 리셋되어 불필요한 대기 없음.
     */
    private void acquireSlot() throws InterruptedException {
        long slotTime = nextSlotTime.accumulateAndGet(
                System.currentTimeMillis(),
                (prev, now) -> Math.max(prev, now) + RATE_INTERVAL_MS);
        long wait = slotTime - System.currentTimeMillis();
        if (wait > 0) {
            Thread.sleep(wait);
        }
    }

    private void fetchPartition(List<Map.Entry<String, String>> partition,
                                Map<String, DartCorpInfo> map,
                                ObjectMapper mapper) {
        for (Map.Entry<String, String> entry : partition) {
            String stockCode = entry.getKey();
            String corpCode = entry.getValue();
            try {
                acquireSlot();
                LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("corp_code", corpCode);
                JSONObject data = OpenAPIUtil.requestApi(
                        StockConstant.DART_API_URL, COMPANY_PATH, params,
                        new HashMap<>(), DataType.DATA_JSON);
                if (data != null && "000".equals(data.get("status"))) {
                    DartCorpInfo info = mapper.readValue(data.toJSONString(), DartCorpInfo.class);
                    map.put(stockCode, info);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("DART company 수신 중단 [{}]", corpCode, e);
            } catch (Exception e) {
                log.error("DART company 수신 오류 [{}]", corpCode, e);
            }
        }
    }

    @Override
    public Stock process(Stock item) {
        if (!initialized) init();

        String srtCd = item.getISU_SRT_CD();
        if (!StringUtils.hasText(srtCd)) return item;

        String corpCode = corpCodeMap.get(srtCd);
        if (corpCode == null) return item;

        item.setCORP_CODE(corpCode);
        item.setIndustryCode(null);

        DartCorpInfo info = corpInfoMap.get(srtCd);
        if (info != null) {
            item.setBIZR_NO(info.getBizrNo());
            item.setADRES(info.getAdres());
            item.setCEO_NM(info.getCeoNm());
            item.setEST_DT(info.getEstDt());
            item.setFAX_NO(info.getFaxNo());
            item.setACC_MT(info.getAccMt());
            item.setHM_URL(info.getHmUrl());
            item.setIR_URL(info.getIrUrl());
            //item.setIndustCode(info.getIndutyCode());
        }

        return item;
    }
}
