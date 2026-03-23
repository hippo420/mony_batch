package app.monybatch.mony.domian.news.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter @Getter
@Table(name="news_rss")
public class NewsRss {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false,length = 500)
    private String company;

    @Column(nullable = false,length = 50)
    private String category;

    @Column(nullable = false,length = 1000)
    private String link;

    @Column(nullable = false,length = 1)
    private String useYn;

}
