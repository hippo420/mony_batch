package app.monybatch.mony.business.entity.dart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor // Lombok을 사용한다면 이것을 명시적으로 추가
@JsonIgnoreProperties(ignoreUnknown = true) // XML에 없는 필드는 무시
public class DartMapping {
    // <corp_code> 매핑
    @JsonProperty("corp_code")
    @JacksonXmlProperty(localName = "corp_code")
    private String corp_code;

    // <corp_name> 매핑
    @JsonProperty("corp_name")
    @JacksonXmlProperty(localName = "corp_name")
    private String corp_name;

    // <corp_eng_name> 매핑
    @JsonProperty("corp_eng_name")
    @JacksonXmlProperty(localName = "corp_eng_name")
    private String corp_eng_name;

    // <stock_code> 매핑
    @JsonProperty("stock_code")
    @JacksonXmlProperty(localName = "stock_code")
    private String stock_code;

    // <modify_date> 매핑
    @JsonProperty("modify_date")
    @JacksonXmlProperty(localName = "modify_date")
    private String modify_date;

    public DartMapping(String corp_code, String corp_name, String corp_eng_name, String stock_code, String modify_date) {
        this.corp_code = corp_code;
        this.corp_name = corp_name;
        this.corp_eng_name = corp_eng_name;
        this.stock_code = stock_code;
        this.modify_date = modify_date;
    }

    @Override
    public String toString() {
        return "DartMapping{" +
                "corpCode='" + corp_code.trim() + '\'' + // 공백 제거
                ", stock_code='" + stock_code + '\'' +
                ", corpName='" + corp_name + '\'' +
                ", modifyDate='" + modify_date + '\'' +
                '}';
    }

}
