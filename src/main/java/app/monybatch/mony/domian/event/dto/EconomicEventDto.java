package app.monybatch.mony.domian.event.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EconomicEventDto {
    private String id;
    private String date;
    private String time;
    private String country;
    private String event;
    private String actual;
    private String previous;
    private String consensus;
    private String forecast;
    private EventCategory category;
    private MarketImpact impact;
}