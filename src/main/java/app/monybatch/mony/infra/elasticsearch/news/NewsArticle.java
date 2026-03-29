package app.monybatch.mony.infra.elasticsearch.news;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Document(indexName = "news_articles")
@Getter
@Setter

public class NewsArticle {
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String fullContent;

    @Field(type = FieldType.Text)
    private String opinion;

    @Field(type = FieldType.Text)
    private String reason;

    @Field(type = FieldType.Text)
    private String keywords;

    @Field(type = FieldType.Text)
    private String type;

    @Field(type = FieldType.Text)
    private String rationale;

    @Field(type = FieldType.Date)
    private String publishedDate;

    @Field(type = FieldType.Date)
    private String expireAt;

    @Field(type = FieldType.Text)
    private String category;

    @Field(type = FieldType.Keyword)
    private String representative; // Y/N

    @Field(type = FieldType.Integer)
    private Integer weight = 0; // 가중치 (기본값 0)

    @Field(type = FieldType.Text)
    private String company;

    @Field(type = FieldType.Text)
    private String link;

    @Field(type = FieldType.Text)
    private String clusterId;

    private Boolean exposed;
    private List<String> relatedLinks;

    @Override
    public String toString() {
        return "NewsArticle{" +
                "publishedDate='" + publishedDate + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", fullContent='" + fullContent + '\'' +
                ", opinion='" + opinion + '\'' +
                ", keywords='" + keywords + '\'' +
                ", rationale='" + rationale + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
    // 생성자, Getter, Setter (생략)



}
