package app.monybatch.mony.domian.stock.entity;

import app.monybatch.mony.common.entity.CommonEntitiy;
import app.monybatch.mony.domian.theme.ThemeCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@MappedSuperclass
public class BaseStock extends CommonEntitiy {
    @Id
    @JsonProperty("ISU_CD")
    @Column(name ="ISU_CD")
    private String isuCd;

    /* DART - start */
    @JsonProperty("corp_code")
    @Column(name ="corp_code")
    private String CORP_CODE;

    /* DART - end   */

    @JsonProperty("ISU_SRT_CD")
    @Column(name ="ISU_SRT_CD")
    private String ISU_SRT_CD;

    @JsonProperty("ceo_nm")
    @Column(name ="ceo_nm")
    private String CEO_NM;

    @JsonProperty("bizr_no")
    @Column(name ="bizr_no")
    private String BIZR_NO;

    @JsonProperty("adres")
    @Column(name ="adres")
    private String ADRES;

    @JsonProperty("hm_url")
    @Column(name ="hm_url")
    private String HM_URL;

    @JsonProperty("ir_url")
    @Column(name ="ir_url")
    private String IR_URL;

    @JsonProperty("phn_no")
    @Column(name ="phn_no")
    private String PHN_NO;

    @JsonProperty("fax_no")
    @Column(name ="fax_no")
    private String FAX_NO;

    @JsonProperty("induty_code")
    @Column(name ="induty_code")
    private String INDUTY_CODE;

    @JsonProperty("est_dt")
    @Column(name ="est_dt")
    private String EST_DT;

    @JsonProperty("acc_mt")
    @Column(name ="acc_mt")
    private String ACC_MT;


    @JsonProperty("ISU_NM")
    @Column(name ="ISU_NM")
    private String ISU_NM;

    @JsonProperty("ISU_ABBRV")
    @Column(name ="ISU_ABBRV")
    private String ISU_ABBRV;

    @JsonProperty("ISU_ENG_NM")
    @Column(name ="ISU_ENG_NM")
    private String ISU_ENG_NM;

    @JsonProperty("LIST_DD")
    @Column(name ="LIST_DD")
    private String LIST_DD;

    @JsonProperty("MKT_TP_NM")
    @Column(name ="MKT_TP_NM")
    private String MKT_TP_NM;

    @JsonProperty("SECUGRP_NM")
    @Column(name ="SECUGRP_NM")
    private String SECUGRP_NM;

    @JsonProperty("SECT_TP_NM")
    @Column(name ="SECT_TP_NM")
    private String SECT_TP_NM;

    @JsonProperty("KIND_STKCERT_TP_NM")
    @Column(name ="KIND_STKCERT_TP_NM")
    private String KIND_STKCERT_TP_NM;

    @JsonProperty("PARVAL")
    @Column(name ="PARVAL")
    private String PARVAL;

    @JsonProperty("LIST_SHRS")
    @Column(name ="LIST_SHRS")
    private Long LIST_SHRS;

    /* 업종 코드 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "industry_code")
    private IndustryCode industryCode;

    /* 테마 코드 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "themeCode")
    private ThemeCode themeCode;



    @Override
    public String toString() {
        return "Stock{" +
                "ISU_CD='" + isuCd + '\'' +
                ", ISU_SRT_CD='" + ISU_SRT_CD + '\'' +
                ", ISU_NM='" + ISU_NM + '\'' +
                ", ISU_ABBRV='" + ISU_ABBRV + '\'' +
                ", ISU_ENG_NM='" + ISU_ENG_NM + '\'' +
                ", LIST_DD='" + LIST_DD + '\'' +
                ", MKT_TP_NM='" + MKT_TP_NM + '\'' +
                ", SECUGRP_NM='" + SECUGRP_NM + '\'' +
                ", SECT_TP_NM='" + SECT_TP_NM + '\'' +
                ", KIND_STKCERT_TP_NM='" + KIND_STKCERT_TP_NM + '\'' +
                ", PARVAL=" + PARVAL +
                ", LIST_SHRS=" + LIST_SHRS +
                '}';
    }
}
