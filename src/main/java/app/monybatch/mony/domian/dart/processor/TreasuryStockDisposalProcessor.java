package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartTreasuryStockDisposalRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartTreasuryStockDisposal;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TreasuryStockDisposalProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.TREASURY_DISPOSAL; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartTreasuryStockDisposalRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartTreasuryStockDisposalRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartTreasuryStockDisposal toEntity(DartTreasuryStockDisposalRawDto r, DartRssQueueDto q) {
        DartTreasuryStockDisposal e = new DartTreasuryStockDisposal();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());

        e.setTotalDisposalShareCnt(safeAddLong(r.getDpplnStkOstk(), r.getDpplnStkEstk()));
        e.setTotalDisposalAmount(safeAdd(r.getDpplnPrcOstk(), r.getDpplnPrcEstk()));
        e.setDisposalPurpose(r.getDpPp());
        e.setDisposalStartDt(r.getDpprpdBgd());
        e.setDisposalEndDt(r.getDpprpdEdd());
        e.setDecisionDt(r.getDpDd());

        // 처분방법: 금액 최대 항목 라벨
        Map<String, Long> methodMap = new LinkedHashMap<>();
        methodMap.put("시장매도",    orZero(r.getDpMMkt()));
        methodMap.put("시간외대량매매", orZero(r.getDpMOvtm()));
        methodMap.put("장외처분",    orZero(r.getDpMOtc()));
        methodMap.put("기타",        orZero(r.getDpMEtc()));
        methodMap.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .filter(en -> en.getValue() > 0)
                .ifPresent(en -> e.setDisposalMethod(en.getKey()));

        return e;
    }

    private long orZero(Long v) { return v == null ? 0L : v; }
}
