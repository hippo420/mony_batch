package app.monybatch.mony.domian.event.entity;

import app.monybatch.mony.domian.event.dto.EventCategory;
import app.monybatch.mony.domian.event.dto.MarketImpact;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "economic_events")
@Getter
@NoArgsConstructor // 접근 제한자 제거 (기본 public)
@AllArgsConstructor
@Builder
public class EconomicEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String eventDate;

    private String eventTime;

    private String country;

    @Column(nullable = false)
    private String event;

    private String actual;

    private String previous;

    private String consensus;

    private String forecast;

    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @Enumerated(EnumType.STRING)
    private MarketImpact impact;

    public void updateActual(String actual) {
        this.actual = actual;
    }
    public void updateMetadata(String category, String impact) {
        this.category = EventCategory.findByKeyword(category);
        this.impact = MarketImpact.findInText(impact);
    }
}
