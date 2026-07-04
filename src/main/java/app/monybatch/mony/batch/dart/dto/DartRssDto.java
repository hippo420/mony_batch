package app.monybatch.mony.batch.dart.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;


@Setter @Getter
public class DartRssDto extends CorpMappingDto{
    private String rceptNo;   // 공시번호 (중복 체크의 키)
    private String reportNm;  // 공시 제목
    private String link;      // 상세 링크
    private String firNm;     // 카테고리
    private Date pubDate;     // 발행일

    public DartRssDto(String corpCode, String corpNm, String corpNm2, String stockCode) {
        super(corpCode, corpNm, corpNm2, stockCode);
    }
    public DartRssDto() {
        super();
    }

    @Override
    public String toString() {
        return "DartRssDto{" +
                "rceptNo='" + rceptNo + '\'' +
                ", reportNm='" + reportNm + '\'' +
                ", link='" + link + '\'' +
                ", firNm='" + firNm + '\'' +
                ", pubDate=" + pubDate +
                '}';
    }
}
