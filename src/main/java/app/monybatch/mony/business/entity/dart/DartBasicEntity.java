package app.monybatch.mony.business.entity.dart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name="INFO_DART",
        uniqueConstraints =
                {
                        @UniqueConstraint(
                                name = "uk_dart_info",
                                columnNames = {"id", "rcept_dt","corp_code","rcept_no"} // 순서가 중요!
                        )
                },
        indexes = {
                @Index(name = "idx_basymd", columnList = "rcept_dt"),
                @Index(name = "idx_corp_code", columnList = "corp_code"),
                @Index(name = "idx_rcept_no", columnList = "rcept_no")
        })
@Entity
@Getter @Setter
public class DartBasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // 공시일자
    @JsonProperty("rcept_dt")
    @Column(length = 8)
    private String RCEPT_DT;

    // 회사 고유 코드
    @JsonProperty("corp_code")
    @Column(updatable = false)
    private String CORP_CODE;

    // 접수번호
    @JsonProperty("rcept_no")
    @Column(nullable = false, length = 14)
    private String RCEPT_NO;

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

}
