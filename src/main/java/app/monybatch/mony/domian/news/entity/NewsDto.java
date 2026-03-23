package app.monybatch.mony.domian.news.entity;

import app.monybatch.mony.domian.news.dto.FilterType;
import lombok.Data;

@Data
public class NewsDto {
    private String title;
    private String originallink;
    private String link;
    private String company;
    private String category;
    private String description;
    private String pubDate;

    //통계
    private FilterType filterType;
}
