package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartRehabilitationRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartRehabilitation;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RehabilitationProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.REHABILITATION; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartRehabilitationRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartRehabilitationRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartRehabilitation toEntity(DartRehabilitationRawDto r, DartRssQueueDto q) {
        DartRehabilitation e = new DartRehabilitation();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setApplicant(r.getApcnt());
        e.setCourt(r.getCpct());
        e.setRequestReason(truncate(r.getRqRs(), 500));
        e.setRequestDt(r.getRqd());
        return e;
    }
}
