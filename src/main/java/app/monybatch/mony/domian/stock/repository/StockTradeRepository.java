package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.StockTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StockTradeRepository extends JpaRepository<StockTrade,Long> {

    @Query("SELECT MAX(m.basDd) FROM StockTrade m")
    String findMaxDate();
}
