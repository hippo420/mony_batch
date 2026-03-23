package app.monybatch.mony.domian.stock.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 유가증권 종목기본정보
 */
@Entity
@Table(name = "STOCK_TRADE", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_bas_dd_isu_cd", // 제약 조건 이름 (선택)
                columnNames = {"id","bas_dd", "isu_cd"} // DB 컬럼명을 적어야 함
        )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTrade {
    //기준일자
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성 번호를 쓰거나
    private Long id;


    @Column(name = "bas_dd", length = 8)
    @JsonProperty("BAS_DD")
    private String basDd;

    //종목코드
    @Column(name = "isu_cd", length = 20)
    @JsonProperty("ISU_CD")
    private String isuCd;

    //종목코드
    @Column(name = "isu_srt_cd", length = 6)
    @JsonProperty("ISU_SRT_CD")
    private String isuSrtCd;

    //종목명
    @Column(name = "isu_nm")
    @JsonProperty("ISU_NM")
    private String isuNm;

    //시장구분
    @Column(name = "mkt_nm")
    @JsonProperty("MKT_NM")
    private String mktNm;

    //소속부
    @Column(name = "sect_tp_nm")
    @JsonProperty("SECT_TP_NM")
    private String sectTpNm;

    //종가
    @Column(name = "tdd_clsprc")
    @JsonProperty("TDD_CLSPRC")
    private Long tddClsprc;

    //대비
    @Column(name = "cmpprevdd_prc")
    @JsonProperty("CMPPREVDD_PRC")
    private Long cmpprevddPrc;

    //등락률
    @Column(name = "fluc_rt", precision = 38, scale = 2)
    @JsonProperty("FLUC_RT")
    private BigDecimal flucRt;

    //시가
    @Column(name = "tdd_opnprc", precision = 38, scale = 2)
    @JsonProperty("TDD_OPNPRC")
    private BigDecimal tddOpnprc;

    //고가
    @Column(name = "tdd_hgprc", precision = 38, scale = 2)
    @JsonProperty("TDD_HGPRC")
    private BigDecimal tddHgprc;

    //저가
    @Column(name = "tdd_lwprc", precision = 38, scale = 2)
    @JsonProperty("TDD_LWPRC")
    private BigDecimal tddLwprc;

    //거래량
    @Column(name = "acc_trdval", precision = 38, scale = 2)
    @JsonProperty("ACC_TRDVAL")
    private BigDecimal accTrdval;

    //거래대금
    @Column(name = "acc_trdvol")
    @JsonProperty("ACC_TRDVOL")
    private Long accTrdvol;

    //시가총액
    @Column(name = "mktcap")
    @JsonProperty("MKTCAP")
    private Long mktcap;

    //상장주식수
    @Column(name = "list_shrs")
    @JsonProperty("LIST_SHRS")
    private Long listShrs;

}
