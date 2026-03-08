package app.monybatch.mony.business.entity.news;

import lombok.Data;

@Data
public class News {
    private String title;
    private String originallink;
    private String link;
    private String company;
    private String category;
    private String description;
    private String pubDate;
}
