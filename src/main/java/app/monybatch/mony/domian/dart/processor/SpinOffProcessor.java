package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartSpinOffRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartSpinOff;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SpinOffProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.SPIN_OFF; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartSpinOffRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartSpinOffRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartSpinOff toEntity(DartSpinOffRawDto r, DartRssQueueDto q) {
        DartSpinOff e = new DartSpinOff();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setSpinOffMethod(r.getDvMth());
        e.setSpinOffRatio(r.getDvRt());
        e.setSurvivingCorpName(r.getAtdvExcmpCmpnm());
        e.setSurvivingListingYn(r.getAtdvExcmpLstmnAtn());
        e.setNewCorpName(r.getDvfcmpCmpnm());
        e.setNewCorpRelistingYn(r.getDvfcmpRlstAtn());
        e.setSpinOffDt(r.getDvdt());
        e.setShareholderMeetingDt(r.getGmtsckPrd());
        return e;
    }
}
