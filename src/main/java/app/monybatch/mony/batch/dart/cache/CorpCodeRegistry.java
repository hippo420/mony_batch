package app.monybatch.mony.batch.dart.cache;

import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.domian.stock.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
public class CorpCodeRegistry {

    // 종목명을 키로 사용하여 DTO를 매핑
    private final Map<String, CorpMappingDto> localCorpCache = new ConcurrentHashMap<>();
    private final StockRepository repository;

    private CorpCodeRegistry(StockRepository repository){
        this.repository = repository;
    }

    // 서버 기동 시 또는 필요 시 데이터를 메모리에 로드
    @PostConstruct
    public void loadCorpCodes() {
        List<Tuple> results = repository.loadCacheStock();
        for (Tuple tuple : results) {
            String stockCode = tuple.get("stockCode", String.class);
            String corpNm = tuple.get("corpNm", String.class);
            String corpNm2 = tuple.get("corpNm2", String.class);
            String corpCode = tuple.get("corpCode", String.class);

            CorpMappingDto dto = new CorpMappingDto(corpCode, corpNm, corpNm2, stockCode);
            localCorpCache.put(corpNm, dto);
            if (corpNm2 != null && !corpNm2.isBlank()) {
                localCorpCache.put(corpNm2, dto);
            }
        }
        log.info("localCorpCache 로드 완료 : {}건",localCorpCache.size());
    }

    public CorpMappingDto getCorpInfo(String corpNm) {
        //log.info("{} [} [}",localCorpCache.get(corpNm).getCorpCode(),localCorpCache.get(corpNm).getCorpNm(),localCorpCache.get(corpNm).getStockCode());
        return localCorpCache.get(corpNm);
    }
}
