package app.monybatch.mony.business.entity;

import java.io.Serializable;
import java.util.Objects;

public class StockPriceId implements Serializable {
    private String basDd;
    private String isuCd;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockPriceId)) return false;
        StockPriceId that = (StockPriceId) o;
        return Objects.equals(basDd, that.basDd) &&
                Objects.equals(isuCd, that.isuCd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(basDd, isuCd);
    }
}
