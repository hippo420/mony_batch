package app.monybatch.mony.domian.earning.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EarningsReleaseDto {

    private String isuSrtCd; // 종목코드 (예: 005930)

    private Integer reportYear; //연도

    private ReportQuarter reportQuarter; //분기

    private BigDecimal revenue; // 매출액

    private BigDecimal operatingProfit; // 영업이익

    private BigDecimal netIncome; // 당기순이익

}
