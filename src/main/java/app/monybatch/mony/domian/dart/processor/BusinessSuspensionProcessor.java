package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartBusinessSuspensionRawDto;
import app.monybatch.mony.domian.dart.entity.DartBusinessSuspension;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BusinessSuspensionProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.BUSINESS_SUSPENSION; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartBusinessSuspensionRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartBusinessSuspensionRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartBusinessSuspension toEntity(DartBusinessSuspensionRawDto r, DartRssQueueDto q) {
        DartBusinessSuspension e = new DartBusinessSuspension();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setSuspensionAmount(r.getBsnspAmt());
        e.setRevenueRatio(r.getSlVs());
        e.setSuspensionReason(truncate(r.getBsnspRs(), 500));
        e.setSuspensionDt(r.getBsnspd());
        return e;
    }
}
