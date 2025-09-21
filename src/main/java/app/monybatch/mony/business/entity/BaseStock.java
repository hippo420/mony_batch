package app.monybatch.mony.business.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseStock extends CommonEntitiy {
    @Id
    @JsonProperty("ISU_CD")
    @Column(name ="ISU_CD")
    private String isuCd;

    @JsonProperty("ISU_SRT_CD")
    @Column(name ="ISU_SRT_CD")
    private String ISU_SRT_CD;

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
