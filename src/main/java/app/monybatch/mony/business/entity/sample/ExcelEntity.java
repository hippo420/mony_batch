package app.monybatch.mony.business.entity.sample;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name="EXCEL_ENTITY")
public class ExcelEntity {
    @Id
    private String idxNm;

    private BigDecimal closePrice;

    private BigDecimal comparison;

    private BigDecimal fRate;

    private BigDecimal openPrice;

    private BigDecimal upperPrice;

    private BigDecimal lowerPrice;

    private BigDecimal volume;

    private BigDecimal tranPrice;

    private BigDecimal mktCapital;

}
