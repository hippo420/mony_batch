package app.monybatch.mony.domian.dart.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter @NoArgsConstructor
public abstract class DartDisclosureBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "rcept_no", length = 14, nullable = false)
    @JsonProperty("rcept_no")
    private String rceptNo; // 접수번호

    @Column(name = "corp_cls", length = 1)
    @JsonProperty("corp_cls")
    private String corpCls; // 법인구분 (Y:유가 K:코스닥 N:코넥스 E:기타)

    @Column(name = "corp_code", length = 8, nullable = false)
    @JsonProperty("corp_code")
    private String corpCode; // 고유번호

    @Column(name = "corp_name", length = 100)
    @JsonProperty("corp_name")
    private String corpName; // 회사명

    @Column(name = "stock_code", length = 6)
    private String stockCode; // 종목코드 (RSS 캐시에서)

    @Column(name = "rcept_dt", length = 8)
    private String rceptDt; // 접수일 (rceptNo 앞 8자리)

    @Column(name = "disclosure_type", length = 30)
    private String disclosureType; // DisclosureType enum명
}
