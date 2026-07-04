package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartTreasuryStockAcquisitionRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartTreasuryStockAcquisition;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Component
public class TreasuryStockAcquisitionProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.TREASURY_ACQUISITION; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartTreasuryStockAcquisitionRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartTreasuryStockAcquisitionRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartTreasuryStockAcquisition toEntity(DartTreasuryStockAcquisitionRawDto r, DartRssQueueDto q) {
        DartTreasuryStockAcquisition e = new DartTreasuryStockAcquisition();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());

        e.setTotalAcqShareCnt(safeAddLong(r.getAqplnStkOstk(), r.getAqplnStkEstk()));
        e.setTotalAcqAmount(safeAdd(r.getAqplnPrcOstk(), r.getAqplnPrcEstk()));
        e.setAcquirePurpose(r.getAqPp());
        e.setAcquireMethod(r.getAqMth());
        e.setAcquireStartDt(r.getAqexpdBgd());
        e.setAcquireEndDt(r.getAqexpdEdd());
        e.setDecisionDt(r.getAqDd());

        // 취득 전 자기주식 보유비율 = 배당가능 비율 + 기타취득 비율
        BigDecimal div  = r.getAqWtnDivOstkRt() != null ? r.getAqWtnDivOstkRt() : BigDecimal.ZERO;
        BigDecimal etc  = r.getEaqOstkRt()      != null ? r.getEaqOstkRt()      : BigDecimal.ZERO;
        BigDecimal existing = div.add(etc);
        e.setExistingTreasuryRatio(existing.compareTo(BigDecimal.ZERO) > 0
                ? existing.setScale(2, RoundingMode.HALF_UP) : null);

        return e;
    }
}
