package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock,String> {
    Stock findByIsuCd(String isuCd);

    @Query("SELECT m.ISU_SRT_CD FROM Stock m")
    List<String> findDistinctIsuSrtCd();

}
