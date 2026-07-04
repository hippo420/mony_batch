package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartFreeCapitalIncreaseRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartFreeCapitalIncrease;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class FreeCapitalIncreaseProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.FREE_CAPITAL_INCREASE; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartFreeCapitalIncreaseRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartFreeCapitalIncreaseRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartFreeCapitalIncrease toEntity(DartFreeCapitalIncreaseRawDto r, DartRssQueueDto q) {
        DartFreeCapitalIncrease e = new DartFreeCapitalIncrease();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setNewShareOstk(r.getNstkOstkCnt());
        e.setNewShareEstk(r.getNstkEstkCnt());
        e.setAllotRatioOstk(r.getNstkAscntPsOstk());
        e.setAllotRatioEstk(r.getNstkAscntPsEstk());
        e.setAllotStdDt(r.getNstkAsstd());
        e.setListingDt(r.getNstkLstprd());
        e.setBoardDecisionDt(r.getBddd());
        return e;
    }
}
