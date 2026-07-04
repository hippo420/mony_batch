package app.monybatch.mony.domian.dart.dto.raw;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor
public class DartPaidCapitalIncreaseRawDto {
    @JsonProperty("rcept_no")   private String rceptNo;
    @JsonProperty("corp_cls")   private String corpCls;
    @JsonProperty("corp_code")  private String corpCode;
    @JsonProperty("corp_name")  private String corpName;

    @JsonProperty("nstk_ostk_cnt")      private Long nstkOstkCnt;       // 신주(보통주식)
    @JsonProperty("nstk_estk_cnt")      private Long nstkEstkCnt;       // 신주(기타주식)
    @JsonProperty("bfic_tisstk_ostk")   private Long bficTiisstkOstk;   // 증자전 발행주식총수(보통)
    @JsonProperty("bfic_tisstk_estk")   private Long bficTiisstkEstk;   // 증자전 발행주식총수(기타)
    @JsonProperty("fdpp_fclt")          private BigDecimal fdppFclt;     // 시설자금
    @JsonProperty("fdpp_bsninh")        private BigDecimal fdppBsninh;   // 영업양수자금
    @JsonProperty("fdpp_op")            private BigDecimal fdppOp;       // 운영자금
    @JsonProperty("fdpp_dtrp")          private BigDecimal fdppDtrp;     // 채무상환자금
    @JsonProperty("fdpp_ocsa")          private BigDecimal fdppOcsa;     // 타법인증권취득자금
    @JsonProperty("fdpp_etc")           private BigDecimal fdppEtc;      // 기타
    @JsonProperty("ic_mthn")            private String icMthn;           // 증자방식
    @JsonProperty("ssl_at")             private String sslAt;            // 공매도 해당여부
    @JsonProperty("ssl_bgd")            private String sslBgd;           // 공매도 시작일
    @JsonProperty("ssl_edd")            private String sslEdd;           // 공매도 종료일
}
