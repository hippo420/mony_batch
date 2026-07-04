package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartTangibleAssetAcquisitionRawDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DartTangibleAssetAcquisition;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TangibleAssetAcquisitionProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.TANGIBLE_ASSET_ACQUISITION; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartTangibleAssetAcquisitionRawDto> list = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartTangibleAssetAcquisitionRawDto.class);

        return list.stream()
                .filter(r -> queueItem.getRceptNo().equals(r.getRceptNo()))
                .findFirst()
                .map(r -> (DartDisclosureBase) toEntity(r, queueItem))
                .map(List::of)
                .orElseGet(() -> { log.warn("rceptNo 일치항목 없음: {}", queueItem.getRceptNo()); return List.of(); });
    }

    private DartTangibleAssetAcquisition toEntity(DartTangibleAssetAcquisitionRawDto r, DartRssQueueDto q) {
        DartTangibleAssetAcquisition e = new DartTangibleAssetAcquisition();
        setBaseFields(e, r.getRceptNo(), r.getCorpCode(), r.getCorpName(), r.getCorpCls(), q, getSupportedType());
        e.setAssetType(r.getAstSen());
        e.setAssetName(r.getAstNm());
        e.setAcquisitionAmount(r.getInhdtlInhprc());
        e.setTotalAsset(r.getInhdtlTast());
        e.setTotalAssetRatio(r.getInhdtlTastVs());
        e.setPurpose(r.getInhPp());
        e.setImpact(truncate(r.getInhAf(), 500));
        e.setPlannedContractDt(r.getInhPrdCtrCnsd());
        e.setPlannedAcqDt(r.getInhPrdInhStd());
        return e;
    }
}
