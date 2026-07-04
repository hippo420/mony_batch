package app.monybatch.mony.domian.trade.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode // JPA 복합키 필수: equals와 hashCode 자동 생성
public class SectorTradeId implements Serializable {

    private String stckBsopDate; // StockData 엔티티의 필드명과 일치해야 함
    private String industryCode;     // StockData 엔티티의 필드명과 일치해야 함

    // 생성자 예시
    public SectorTradeId(String stckBsopDate, String industryCode) {
        this.stckBsopDate = stckBsopDate;
        this.industryCode = industryCode;
    }
}