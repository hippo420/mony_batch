package app.monybatch.mony.domian.dart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name="INFO_DART",
        uniqueConstraints =
                {
                        @UniqueConstraint(
                                name = "uk_dart_info",
                                columnNames = {"rcept_no", "rcept_dt","corp_code"} // 순서가 중요!
                        )
                },
        indexes = {
                @Index(name = "idx_dart_rcept_dt", columnList = "rcept_dt"),
                @Index(name = "idx_dart_corp_code", columnList = "corp_code"),
                @Index(name = "idx_dart_info_rcept_no", columnList = "rcept_no")
        })
@Entity
@Getter @Setter
public class DartBasicEntity {


    // 접수번호
    @Id
    //@GeneratedValue(strategy = GenerationType.U)
    @JsonProperty("rcept_no")
    @Column(nullable = false, length = 14)
    private String RCEPT_NO;


    // 공시일자
    @JsonProperty("rcept_dt")
    @Column(length = 8)
    private String RCEPT_DT;

    // 회사 고유 코드
    @JsonProperty("corp_code")
    @Column(updatable = false)
    private String CORP_CODE;



    @JsonProperty("corp_name")
    @Column(updatable = false)
    private String CORP_NAME;

    //회사구분
    @JsonProperty("corp_cls")
    @Column(updatable = false)
    private String CORP_CLS;

    // 종목코드
    @Column(updatable = false)
    @JsonProperty("stock_code")
    private String STOCK_CODE;

    // 공시명
    @JsonProperty("report_nm")
    @Column(nullable = false, length = 1000)
    private String REPORT_NM;

    // 화사명
    @JsonProperty("flr_nm")
    @Column(nullable = false)
    private String FIR_NM;

    //
    @JsonProperty("rm")
    @Column
    private String RM;

    @JsonProperty("proc_yn")
    @Column
    private String PROC_YN;

}
