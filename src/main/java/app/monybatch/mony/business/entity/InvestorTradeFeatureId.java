package app.monybatch.mony.business.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class InvestorTradeFeatureId implements Serializable {

    /** BAS_DD : 기준일자 */
    private String basDd;

    /** ISU_CD : 종목코드 */
    private String isuCd;
}
