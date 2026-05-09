package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.StockTrade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface StockTradeRepository extends JpaRepository<StockTrade,Long> {

    @Query("SELECT MAX(m.basDd) FROM StockTrade m")
    String findMaxDate();

    // 당일 등락률 급등락 종목 (|flucRt| >= threshold)
    @Query("SELECT t FROM StockTrade t WHERE t.basDd = :basDd AND ABS(t.flucRt) >= :threshold ORDER BY ABS(t.flucRt) DESC")
    List<StockTrade> findSpikedStocks(@Param("basDd") String basDd, @Param("threshold") BigDecimal threshold);

    // 당일 거래량 상위 N종목
    @Query("SELECT t FROM StockTrade t WHERE t.basDd = :basDd ORDER BY t.accTrdvol DESC")
    List<StockTrade> findTopVolumeStocks(@Param("basDd") String basDd, Pageable pageable);
}
