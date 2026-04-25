package app.monybatch.mony.domian.news.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_parse_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleParseRule {

    @Id
    private String company;

    @Column(name = "title_selector", length = 255)
    private String titleSelector;

    @Column(name = "content_selector", length = 255)
    private String contentSelector;

    @Column(name = "remove_selector", length = 500)
    private String removeSelector;

    @Column(name = "thumbnail_selector", length = 500)
    private String thumbnail_selector;

    @Column(name = "author_selector", length = 255)
    private String authorSelector;

    @Column(name = "date_selector", length = 255)
    private String dateSelector;

    @Column(name = "active")
    private Boolean active = true;
}
