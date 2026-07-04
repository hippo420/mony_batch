package app.monybatch.mony.batch.dart.processor;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.classifier.DisclosureTypeClassifier;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class DartRssPublishProcessor implements ItemProcessor<DartRssDto, DartRssQueueDto> {

    private final CorpCodeRegistry registry;
    private final DisclosureTypeClassifier classifier;

    @Override
    public DartRssQueueDto process(DartRssDto item) {
        // 1. corpCode/stockCode 확인 — Reader에서 이미 세팅됐으면 재조회 생략
        String corpCode  = item.getCorpCode();
        String stockCode = item.getStockCode();
        if (corpCode == null || corpCode.isBlank()) {
            CorpMappingDto corp = registry.getCorpInfo(item.getCorpNm());
            if (corp == null) {
                log.warn("unmapped_corp_nm={}", item.getCorpNm());
                return null;
            }
            corpCode  = corp.getCorpCode();
            stockCode = corp.getStockCode();
        }

        // 2. 공시유형 분류 — 대상 아닌 공시는 스킵
        DisclosureType type = classifier.classify(item.getReportNm());
        if (type == DisclosureType.UNKNOWN) {
            log.info("unclassified_report_nm={}", item.getReportNm());
            return null;
        }

        return new DartRssQueueDto(item.getRceptNo(), corpCode, stockCode, item.getCorpNm(), type);
    }
}
