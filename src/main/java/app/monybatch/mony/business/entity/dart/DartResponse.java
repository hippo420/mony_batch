package app.monybatch.mony.business.entity.dart;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class DartResponse {
    @JsonProperty("status")
    private String STATUS;           // 응답 상태 코드 (000: 정상)

    @JsonProperty("message")
    private String MESSAGE;          // 응답 메시지 (예: 정상)

    @JsonProperty("list")
    private List<DartFinData> DATA_LIST;  // 재무제표 항목 리스트
}
