package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.StockTemp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTempRepository extends JpaRepository<StockTemp,String> {


}
