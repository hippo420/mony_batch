package app.monybatch.mony.domian.dart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "dart_account",
        uniqueConstraints =
                {
                        @UniqueConstraint(
                                name = "uk_dart_account",
                                columnNames = {"id","rcept_no", "bsns_year","corp_code","ord"} // 순서가 중요!
                        )
                },
        indexes = {

                /**
                 * 회사 + 연도 기준 조회 최적화
                 */
                @Index(name = "idx_dart_stock_year",columnList = "bsns_year,corp_code,stock_code"),

                /**
                 * 공시 접수번호 조회용
                 */
                @Index(name = "idx_dart_account_rcept_no",columnList = "rcept_no"),

                /**
                 * 특정 계정 검색용 (예: 자본총계)
                 */
                @Index( name = "idx_dart_account_nm",columnList = "account_nm"),

                /**
                 * 핵심 재무 조회 패턴 복합 인덱스
                 */
                @Index(name = "idx_dart_core_query",columnList = "corp_code, stock_code, bsns_year, reprt_code, fs_div, sj_div")
        }
)
public class DartAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * 접수번호
     */
    @Column(name = "rcept_no", length = 14, nullable = false)
    @JsonProperty("rcept_no")
    private String rceptNo;

    /**
     * 사업연도(RANGE 파티셔닝)
     */
    @Column(name = "bsns_year", nullable = false)
    @JsonProperty("bsns_year")
    private String bsnsYear;

    /**
     * 고유코드
     */
    @Column(name = "corp_code", nullable = false)
    @JsonProperty("corp_code")
    private String corpCode;

    /**
     * 계정 정렬 순서
     */
    @Column(name = "ord")
    @JsonProperty("ord")
    private Integer ord;

    /**
     * 종목코드 (6자리)
     */
    @Column(name = "stock_code", length = 6, nullable = false)
    @JsonProperty("stock_code")
    private String stockCode;

    /**
     * 보고서 코드(11011: 사업보고서, 11012: 반기보고서, 11013: 1분기보고서, 11014: 3분기보고서)
     */
    @Column(name = "reprt_code", length = 5, nullable = false)
    @JsonProperty("reprt_code")
    private String reprtCode;

    /**
     * 계정명
     */
    @Column(name = "account_nm", length = 100)
    @JsonProperty("account_nm")
    private String accountNm;

    /**
     * 개별/연결 구분(OFS: 개별재무제표, CFS: 연결재무제표)
     */
    @Column(name = "fs_div", length = 3)
    @JsonProperty("fs_div")
    private String fsDiv;

    /**
     * 개별/연결 명칭 (출력용)
     */
    @Column(name = "fs_nm", length = 30)
    @JsonProperty("fs_nm")
    private String fsNm;

    /**
     * 재무제표 구분(BS: 재무상태표, IS: 손익계산서)
     */
    @Column(name = "sj_div", length = 5)
    @JsonProperty("sj_div")
    private String sjDiv;

    /**
     * 재무제표 명칭 (출력용)
     */
    @Column(name = "sj_nm", length = 30)
    @JsonProperty("sj_nm")
    private String sjNm;

    /**
     * 당기명
     */
    @Column(name = "thstrm_nm", length = 50)
    @JsonProperty("thstrm_nm")
    private String thstrmNm;

    /**
     * 당기 기준일
     */
    @Column(name = "thstrm_dt", length = 30)
    @JsonProperty("thstrm_dt")
    private String thstrmDt;

    /**
     * 당기 금액
     */
    @Column(name = "thstrm_amount", precision = 20, scale = 0)
    @JsonProperty("thstrm_amount")
    private BigDecimal thstrmAmount;

    public void setThstrmAmount(Object obj)
    {
        if(obj==null)
            this.frmtrmAmount = BigDecimal.ZERO;

        if (obj instanceof String amt)
        {
            amt =  amt.replaceAll(",","");
            this.thstrmAmount = new BigDecimal(amt);
        }
        else
        {
            this.thstrmAmount = (BigDecimal) obj;
        }

    }

    /**
     * 당기 누적 금액
     */
    @Column(name = "thstrm_add_amount", precision = 20, scale = 0)
    @JsonProperty("thstrm_add_amount")
    private BigDecimal thstrmAddAmount;
    public void setThstrmAddAmount(Object obj)
    {
        if(obj==null)
            this.frmtrmAmount = BigDecimal.ZERO;

        if (obj instanceof String amt)
        {
            amt =  amt.replaceAll(",","");
            this.thstrmAddAmount = new BigDecimal(amt);
        }
        else
        {
            this.thstrmAddAmount = (BigDecimal) obj;
        }

    }
    /**
     * 전기명
     */
    @Column(name = "frmtrm_nm", length = 50)
    @JsonProperty("frmtrm_nm")
    private String frmtrmNm;

    /**
     * 전기 기준일
     */
    @Column(name = "frmtrm_dt", length = 50)
    @JsonProperty("frmtrm_dt")
    private String frmtrmDt;

    /**
     * 전기 금액
     */
    @Column(name = "frmtrm_amount", precision = 20, scale = 0)
    @JsonProperty("frmtrm_amount")
    private BigDecimal frmtrmAmount;
    public void setFrmtrmAmount(Object obj)
    {
        if(obj==null)
            this.frmtrmAmount = BigDecimal.ZERO;

        if (obj instanceof String amt)
        {
            amt =  amt.replaceAll(",","");
            this.frmtrmAmount = new BigDecimal(amt);
        }
        else
        {
            this.frmtrmAmount = (BigDecimal) obj;
        }

    }
    /**
     * 전기 누적 금액
     */
    @Column(name = "frmtrm_add_amount", precision = 20, scale = 0)
    @JsonProperty("frmtrm_add_amount")
    private BigDecimal frmtrmAddAmount;
    public void setFrmtrmAddAmount(Object obj)
    {
        if(obj==null)
            this.frmtrmAmount = BigDecimal.ZERO;

        if (obj instanceof String amt)
        {
            amt =  amt.replaceAll(",","");
            this.frmtrmAddAmount = new BigDecimal(amt);
        }
        else
        {
            this.frmtrmAddAmount = (BigDecimal) obj;
        }
    }

    /**
     * 전전기명
     */
    @Column(name = "bfefrmtrm_nm", length = 50)
    @JsonProperty("bfefrmtrm_nm")
    private String bfefrmtrmNm;

    /**
     * 전전기 기준일
     */
    @Column(name = "bfefrmtrm_dt", length = 50)
    @JsonProperty("bfefrmtrm_dt")
    private String bfefrmtrmDt;

    /**
     * 전전기 금액
     */
    @Column(name = "bfefrmtrm_amount", precision = 20, scale = 0)
    @JsonProperty("bfefrmtrm_amount")
    private BigDecimal bfefrmtrmAmount;
    public void setBfefrmtrmAmount(Object obj)
    {
        if(obj==null)
            this.frmtrmAmount = BigDecimal.ZERO;

        if (obj instanceof String amt)
        {
            amt =  amt.replaceAll(",","");
            this.bfefrmtrmAmount = new BigDecimal(amt);
        }
        else
        {
            this.bfefrmtrmAmount = (BigDecimal) obj;
        }
    }



    /**
     * 통화 단위
     */
    @Column(name = "currency", length = 10)
    @JsonProperty("currency")
    private String currency;
}
