package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.common.core.utils.JsonUtil;
import app.monybatch.mony.domian.dart.dto.raw.DartDividendRawDto;
import app.monybatch.mony.domian.dart.entity.DartDividend;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class DividendProcessor extends BaseDisclosureProcessor implements DisclosureProcessor {

    private static final String DPS_SE = "주당 현금배당금(원)";

    @Override
    public DisclosureType getSupportedType() { return DisclosureType.BAEDANG; }

    @Override
    public List<DartDisclosureBase> process(JSONObject response, DartRssQueueDto queueItem) {
        List<DartDividendRawDto> rawList = JsonUtil.convert(
                new org.json.JSONObject(response.toJSONString()), "list", DartDividendRawDto.class);

        return rawList.stream()
                .filter(raw -> DPS_SE.equals(raw.getSe()))
                .map(raw -> toEntity(raw, queueItem))
                .filter(Objects::nonNull)
                .map(e -> (DartDisclosureBase) e)
                .toList();
    }

    private DartDividend toEntity(DartDividendRawDto raw, DartRssQueueDto queueItem) {
        BigDecimal dpsThis  = parseAmount(raw.getThstrm(), raw.getRceptNo());
        BigDecimal dpsPrev  = parseAmount(raw.getFrmtrm(), raw.getRceptNo());
        BigDecimal dpsPrev2 = parseAmount(raw.getLwfr(),   raw.getRceptNo());

        DartDividend entity = new DartDividend();
        setBaseFields(entity, raw.getRceptNo(), raw.getCorpCode(), raw.getCorpName(),
                raw.getCorpCls(), queueItem, getSupportedType());

        entity.setStockKind(raw.getStockKnd());
        entity.setDpsThisYear(dpsThis);
        entity.setDpsPrevYear(dpsPrev);
        entity.setDpsPrev2Year(dpsPrev2);
        entity.setSettlementDt(sanitizeDate(raw.getStlmDt()));
        entity.setYoyChangeAmt(calcChangeAmt(dpsThis, dpsPrev));
        entity.setYoyChangeRatio(calcChangeRatio(dpsThis, dpsPrev));

        return entity;
    }

    private BigDecimal calcChangeAmt(BigDecimal thisYear, BigDecimal prevYear) {
        if (thisYear == null || prevYear == null) return null;
        return thisYear.subtract(prevYear);
    }

    private BigDecimal calcChangeRatio(BigDecimal thisYear, BigDecimal prevYear) {
        if (thisYear == null || prevYear == null) return null;
        if (prevYear.compareTo(BigDecimal.ZERO) == 0) return null;
        return thisYear.subtract(prevYear)
                .divide(prevYear, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** "2025.12.31", "20251231 " 등 → 숫자만 추출 후 최대 8자리 */
    private String sanitizeDate(String date) {
        if (date == null || date.isBlank()) return null;
        String digits = date.replaceAll("[^0-9]", "");
        return digits.length() >= 8 ? digits.substring(0, 8) : digits;
    }

    private BigDecimal parseAmount(String value, String rceptNo) {
        if (value == null || value.isBlank() || "-".equals(value.trim())) return null;
        String digits = value.replaceAll("[^0-9.]", "");
        if (digits.isBlank()) return null;
        try {
            return new BigDecimal(digits).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            log.warn("배당금 파싱 실패: rceptNo={}, value={}", rceptNo, value);
            return null;
        }
    }
}
