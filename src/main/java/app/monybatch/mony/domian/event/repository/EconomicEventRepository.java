package app.monybatch.mony.domian.event.repository;

import app.monybatch.mony.domian.event.entity.EconomicEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EconomicEventRepository extends JpaRepository<EconomicEvent,Long> {
}
