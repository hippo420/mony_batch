package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartPaidCapitalIncreaseRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartPaidCapitalIncrease;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PaidCapitalIncreaseProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.PAID_CAPITAL_INCREASE; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartPaidCapitalIncreaseRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartPaidCapitalIncreaseRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> {
                    log.warn("rceptNo 일치항목 없음: rceptNo={}", queueItem.getRceptNo());
                    return List.of();
                });
    }

    private DartPaidCapitalIncrease toEntity(DartPaidCapitalIncreaseRawDto r, DartRssQueueDto q) {
        DartPaidCapitalIncrease e = new DartPaidCapitalIncrease();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());

        e.setNewShareOstk(r.getNstkOstkCnt());
        e.setNewShareEstk(r.getNstkEstkCnt());
        e.setIncreaseMethod(r.getIcMthn());
        e.setShortSellApplied(r.getSslAt());
        e.setShortSellStartDt(r.getSslBgd());
        e.setShortSellEndDt(r.getSslEdd());

        // 희석률 = 신주 / (기존+신주) × 100
        if (r.getNstkOstkCnt() != null && r.getBficTiisstkOstk() != null && r.getBficTiisstkOstk() > 0) {
            BigDecimal newShare = BigDecimal.valueOf(r.getNstkOstkCnt());
            BigDecimal total    = BigDecimal.valueOf(r.getBficTiisstkOstk() + r.getNstkOstkCnt());
            e.setDilutionRatio(newShare.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP));
        }

        // 총 자금조달금액
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal v : List.of(
                orZero(r.getFdppFclt()), orZero(r.getFdppBsninh()), orZero(r.getFdppOp()),
                orZero(r.getFdppDtrp()), orZero(r.getFdppOcsa()), orZero(r.getFdppEtc()))) {
            total = total.add(v);
        }
        e.setTotalFundAmount(total.compareTo(BigDecimal.ZERO) > 0 ? total : null);

        // 주요 자금조달목적 (금액 최대 항목)
        Map<String, BigDecimal> purposeMap = new LinkedHashMap<>();
        purposeMap.put("시설자금",   orZero(r.getFdppFclt()));
        purposeMap.put("영업양수자금", orZero(r.getFdppBsninh()));
        purposeMap.put("운영자금",   orZero(r.getFdppOp()));
        purposeMap.put("채무상환자금", orZero(r.getFdppDtrp()));
        purposeMap.put("타법인증권취득", orZero(r.getFdppOcsa()));
        purposeMap.put("기타",       orZero(r.getFdppEtc()));
        purposeMap.entrySet().stream()
                .max(Comparator.comparingDouble(en -> en.getValue().doubleValue()))
                .filter(en -> en.getValue().compareTo(BigDecimal.ZERO) > 0)
                .ifPresent(en -> e.setMainFundPurpose(en.getKey()));

        return e;
    }

    private BigDecimal orZero(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
