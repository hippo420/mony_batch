package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.Stock;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock,String> {
    Stock findByIsuCd(String isuCd);

    @Query("SELECT m.ISU_SRT_CD FROM Stock m")
    List<String> findDistinctIsuSrtCd();

    @Query("SELECT m.ISU_SRT_CD as stockCode, m.CORP_CODE as corpCode, m.ISU_ABBRV as corpNm FROM Stock m")
    List<Tuple> loadCacheStock();
}
