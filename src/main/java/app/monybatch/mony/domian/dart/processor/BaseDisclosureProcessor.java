package app.monybatch.mony.domian.dart.processor;

import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.domian.dart.entity.DartDisclosureBase;
import app.monybatch.mony.domian.dart.entity.DisclosureType;

import java.math.BigDecimal;

public abstract class BaseDisclosureProcessor {

    protected void setBaseFields(DartDisclosureBase entity, String rceptNo, String corpCode,
                                 String corpName, String corpCls, DartRssQueueDto queueItem,
                                 DisclosureType type) {
        entity.setRceptNo(rceptNo);
        entity.setRceptDt(rceptNo.substring(0, 8));
        entity.setCorpCode(corpCode);
        entity.setCorpName(corpName);
        entity.setCorpCls(corpCls);
        entity.setStockCode(queueItem.getStockCode());
        entity.setDisclosureType(type.name());
    }

    protected String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    protected BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a.add(b);
    }

    protected Long safeAddLong(Long a, Long b) {
        if (a == null && b == null) return null;
        if (a == null) return b;
        if (b == null) return a;
        return a + b;
    }
}
