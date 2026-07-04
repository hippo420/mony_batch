package app.monybatch.mony.batch.dart.dto;

import app.monybatch.mony.domian.dart.entity.DisclosureType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DartRssQueueDto {
    private String rceptNo;
    private String corpCode;
    private String stockCode;
    private String corpNm;
    private DisclosureType type;

    /** corpNm 없이 생성하는 하위호환 생성자 (실적 외 공시는 corpNm 불필요) */
    public DartRssQueueDto(String rceptNo, String corpCode, String stockCode, DisclosureType type) {
        this(rceptNo, corpCode, stockCode, null, type);
    }
}
