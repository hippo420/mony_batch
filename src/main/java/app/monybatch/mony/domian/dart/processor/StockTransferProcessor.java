package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartStockTransferRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartStockTransfer;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StockTransferProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.STOCK_TRANSFER; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartStockTransferRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartStockTransferRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartStockTransfer toEntity(DartStockTransferRawDto r, DartRssQueueDto q) {
        DartStockTransfer e = new DartStockTransfer();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setPartnerCorpName(r.getIscmpCmpnm());
        e.setTransferShareCnt(r.getTrfdtlStkcnt());
        e.setTransferAmount(r.getTrfdtlTrfprc());
        e.setTotalAsset(r.getTrfdtlTast());
        e.setTotalAssetRatio(r.getTrfdtlTastVs());
        e.setPostTrfShareCnt(r.getAttrfOwstkcnt());
        e.setPostTrfEquityRatio(r.getAttrfEqrt());
        e.setPurpose(r.getTrfPp());
        e.setPlannedDt(r.getTrfPrd());
        return e;
    }
}
