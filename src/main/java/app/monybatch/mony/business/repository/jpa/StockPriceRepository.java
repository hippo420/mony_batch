package app.monybatch.mony.business.repository.jpa;

import app.monybatch.mony.business.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
    @Query("select max(m.basDd) from StockPrice m group by m.basDd")
    String findLastDay();
}
