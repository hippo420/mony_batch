package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartCapitalReductionRawDto;
import app.monybatch.mony.domian.dart.entity.DartCapitalReduction;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CapitalReductionProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.CAPITAL_REDUCTION; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartCapitalReductionRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartCapitalReductionRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartCapitalReduction toEntity(DartCapitalReductionRawDto r, DartRssQueueDto q) {
        DartCapitalReduction e = new DartCapitalReduction();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setReductionRatioOstk(r.getCrRtOstk());
        e.setReductionRatioEstk(r.getCrRtEstk());
        e.setCapitalBefore(r.getBfcrCpt());
        e.setCapitalAfter(r.getAtcrCpt());
        e.setReductionMethod(r.getCrMth());
        e.setReductionReason(truncate(r.getCrRs(), 500));
        e.setStdDt(r.getCrStd());
        e.setShareholderMeetingDt(r.getCrscGmtsckPrd());
        return e;
    }
}
