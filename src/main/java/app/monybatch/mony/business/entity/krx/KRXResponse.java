package app.monybatch.mony.business.entity.krx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class KRXResponse<T> {

    @JsonProperty("OutBlock_1")
    private List<T> OutBlock_1;

}
