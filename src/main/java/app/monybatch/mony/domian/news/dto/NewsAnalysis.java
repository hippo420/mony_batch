package app.monybatch.mony.domian.news.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsAnalysis {
    private String keyword;
    private String sentiment;
    private String reason;
}
