package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock,Long> {
    Stock findByIsuCd(String isuCd);
}
