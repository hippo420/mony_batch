package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartMergerRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartMerger;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MergerProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.MERGER; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartMergerRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartMergerRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartMerger toEntity(DartMergerRawDto r, DartRssQueueDto q) {
        DartMerger e = new DartMerger();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setMergerMethod(r.getMgMth());
        e.setMergerRatio(r.getMgRt());
        e.setMergerPurpose(truncate(r.getMgPp(), 500));
        e.setPartnerCorpName(r.getMgptncmpCmpnm());
        e.setPartnerTotalAsset(r.getRbsnfdtlTast());
        e.setPartnerNetIncome(r.getRbsnfdtlNic());
        e.setBackdoorListingYn(r.getBdlstAtn());
        e.setMergerDt(r.getMgscMgdt());
        e.setShareholderMeetingDt(r.getMgscGmtsckPrd());
        e.setNewShareListingDt(r.getMgscNstklstprd());
        return e;
    }
}
