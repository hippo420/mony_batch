package app.monybatch.mony.business.document;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "reports")
@Getter
@Builder
public class ReportIndex {
    @Id
    private String id;
    private String title;
    private String author;
    private String company;
    private String category;

    @Field(type = FieldType.Text, analyzer = "nori") // 한글 분석기 설정
    private String content; // PDF에서 추출한 비정형 텍스트 데이터

    private String minioUrl;
    private String reportDate;
}
