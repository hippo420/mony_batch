package app.monybatch.mony.domian.event.entity;

import app.monybatch.mony.domian.event.dto.EventCategory;
import app.monybatch.mony.domian.event.dto.MarketImpact;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "economic_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class EconomicEvent {

    @Id
    private String id;

    @Column(nullable = false)
    private String eventDate; // date는 DB 예약어인 경우가 많아 변경 권장

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

    // 비즈니스 로직에 필요한 업데이트 메서드 (필요 시)
    public void updateActual(String actual) {
        this.actual = actual;
    }
    public void updateMetadata(String category) {

        this.category = EventCategory.findByKeyword(category);
    }
}
