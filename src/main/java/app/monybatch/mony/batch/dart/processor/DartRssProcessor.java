package app.monybatch.mony.batch.dart.processor;

import app.monybatch.mony.batch.dart.cache.CorpCodeRegistry;
import app.monybatch.mony.batch.dart.classifier.DisclosureTypeClassifier;
import app.monybatch.mony.batch.dart.dto.CorpMappingDto;
import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.common.core.utils.OpenAPIUtil;
import app.monybatch.mony.domian.dart.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class DartRssProcessor implements ItemProcessor<DartRssDto, DartDisclosureBase> {

    private final CorpCodeRegistry registry;
    private final DisclosureTypeClassifier classifier;

    private static final Map<DisclosureType, Class<? extends DartDisclosureBase>> TYPE_CLASS_MAP = new EnumMap<>(DisclosureType.class);

    static {
        TYPE_CLASS_MAP.put(DisclosureType.TREASURY_ACQUISITION,       DartTreasuryStockAcquisition.class);
        TYPE_CLASS_MAP.put(DisclosureType.TREASURY_DISPOSAL,          DartTreasuryStockDisposal.class);
        TYPE_CLASS_MAP.put(DisclosureType.PAID_CAPITAL_INCREASE,      DartPaidCapitalIncrease.class);
        TYPE_CLASS_MAP.put(DisclosureType.FREE_CAPITAL_INCREASE,      DartFreeCapitalIncrease.class);
        TYPE_CLASS_MAP.put(DisclosureType.CAPITAL_REDUCTION,          DartCapitalReduction.class);
        TYPE_CLASS_MAP.put(DisclosureType.TANGIBLE_ASSET_ACQUISITION, DartTangibleAssetAcquisition.class);
        TYPE_CLASS_MAP.put(DisclosureType.STOCK_ACQUISITION,          DartStockAcquisition.class);
        TYPE_CLASS_MAP.put(DisclosureType.STOCK_TRANSFER,             DartStockTransfer.class);
        TYPE_CLASS_MAP.put(DisclosureType.MERGER,                     DartMerger.class);
        TYPE_CLASS_MAP.put(DisclosureType.SPIN_OFF,                   DartSpinOff.class);
        TYPE_CLASS_MAP.put(DisclosureType.DEFAULT,                    DartDefault.class);
        TYPE_CLASS_MAP.put(DisclosureType.BUSINESS_SUSPENSION,        DartBusinessSuspension.class);
        TYPE_CLASS_MAP.put(DisclosureType.REHABILITATION,             DartRehabilitation.class);
        TYPE_CLASS_MAP.put(DisclosureType.DISSOLUTION,                DartDissolution.class);
        TYPE_CLASS_MAP.put(DisclosureType.LAWSUIT,                    DartLawsuit.class);
    }

    @Override
    public DartDisclosureBase process(DartRssDto item) {
        // 1. corpCode 확인 — Reader에서 이미 세팅됐으면 재조회 생략
        String corpCode = item.getCorpCode();
        if (corpCode == null || corpCode.isBlank()) {
            CorpMappingDto corp = registry.getCorpInfo(item.getCorpNm());
            if (corp == null) {
                log.warn("unmapped_corp_nm={}", item.getCorpNm());
                return null;
            }
            corpCode = corp.getCorpCode();
        }

        // 2. 공시유형 분류
        DisclosureType type = classifier.classify(item.getReportNm());
        if (type == DisclosureType.UNKNOWN) {
            log.info("unclassified_report_nm={}", item.getReportNm());
            return null;
        }

        // 3. DART API 호출 (bgn_de/end_de = rceptNo 앞 8자리)
        String bgnDe = item.getRceptNo().substring(0, 8);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("corp_code", corpCode);
        params.add("bgn_de", bgnDe);
        params.add("end_de", bgnDe);

        JSONObject response;
        try {
            response = OpenAPIUtil.requestApi(StockConstant.DART_API_URL, type.getApiPath(), params, Map.of(), DataType.DATA_JSON);
        } catch (Exception e) {
            log.error("DART API 호출 실패: rceptNo={}, error={}", item.getRceptNo(), e.getMessage());
            return null;
        }

        if (response == null || !"000".equals(response.get("status"))) {
            log.warn("DART API 정상응답 없음: rceptNo={}, status={}", item.getRceptNo(),
                    response != null ? response.get("status") : "null");
            return null;
        }

        // 4. list에서 rceptNo 일치 항목만 채택 (당일 다른 공시 혼입 방지)
        Class<? extends DartDisclosureBase> clazz = TYPE_CLASS_MAP.get(type);
        List<? extends DartDisclosureBase> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", clazz);

        return list.stream()
                .filter(e -> item.getRceptNo().equals(e.getRceptNo()))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("rceptNo 일치항목 없음: rceptNo={}", item.getRceptNo());
                    return null;
                });
    }
}
