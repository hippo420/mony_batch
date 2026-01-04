package app.monybatch.mony.business.entity.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Keyword {
    @Field(type = FieldType.Keyword)
    private String keyword;
    @Field(type = FieldType.Double)
    private Double score;
}
