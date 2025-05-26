package app.monybatch.mony.business.repository.jpa;

import app.monybatch.mony.business.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock,Long> {
    Stock findByIsuCd(String isuCd);
}
