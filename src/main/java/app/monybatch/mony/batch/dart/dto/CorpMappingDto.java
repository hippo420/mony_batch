package app.monybatch.mony.batch.dart.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class CorpMappingDto {
    private String corpCode;  // 고유번호
    private String corpNm;    // 회사명
    private String corpNm2;    // 회사명2
    private String stockCode; // 종목코드

    public CorpMappingDto() {

    }
}
