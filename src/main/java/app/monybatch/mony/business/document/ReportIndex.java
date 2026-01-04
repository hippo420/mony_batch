package app.monybatch.mony.business.document;

import app.monybatch.mony.business.entity.report.Keyword;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Keyword)
    private String company;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    // --- 추가된 핵심 필드 ---

    /**
     * LLM에서 추출한 핵심 키워드 리스트
     * 집계(Aggregation)를 위해 FieldType.Keyword를 사용합니다.
     */
    @Field(type = FieldType.Object)
    private List<Keyword> keywords;

    /**
     * 리포트 날짜 (날짜 형식을 명시하여 정렬 및 기간 조회를 용이하게 함)
     */
    @Field(type = FieldType.Date)
    private String reportDate;

    private String minioUrl;

    /**
     * 추가하면 좋은 필드: 종목코드 (종목 검색 연동용)
     */
    @Field(type = FieldType.Keyword)
    private String itemCode;

    @Field(type = FieldType.Keyword)
    private String itemName;
}
