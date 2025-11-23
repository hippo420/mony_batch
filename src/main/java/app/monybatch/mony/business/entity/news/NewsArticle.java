package app.monybatch.mony.business.entity.news;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "news_articles")
@Getter
@Setter

public class NewsArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Field(type = FieldType.Text)
    private String title;
    @Field(type = FieldType.Text)
    private String content;
    @Field(type = FieldType.Text)
    private String opinion;
    @Field(type = FieldType.Text)
    private String keywords;
    @Field(type = FieldType.Text)
    private String rationale;
    @Field(type = FieldType.Date)
    private String publishedDate;

    @Override
    public String toString() {
        return "NewsArticle{" +
                "publishedDate='" + publishedDate + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", opinion='" + opinion + '\'' +
                ", keywords='" + keywords + '\'' +
                ", rationale='" + rationale + '\'' +
                '}';
    }
    // 생성자, Getter, Setter (생략)



}
