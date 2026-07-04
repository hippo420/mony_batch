package app.monybatch.mony.domian.stock.repository;

import app.monybatch.mony.domian.stock.entity.Stock;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock,String> {
    Stock findByIsuCd(String isuCd);

    // 모든 종목을 상폐(Y)로 선마킹. 이후 API step에서 살아있는 종목만 N/관리종목 Y로 덮어쓴다.
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Stock m SET m.DELIST_YN = 'Y'")
    int markAllAsDelisted();

    @Query("SELECT m FROM Stock m WHERE m.MKT_TP_NM IN ('KOSPI', 'KOSDAQ') and m.DELIST_YN = 'N'")
    List<Stock> findKospiKosdaqStocks();

    @Query("SELECT m.ISU_SRT_CD FROM Stock m WHERE m.CORP_CODE is null and m.DELIST_YN = 'N'")
    List<String> findDistinctIsuSrtCd();

    @Query("SELECT m.ISU_SRT_CD as stockCode, m.CORP_CODE as corpCode, m.ISU_ABBRV as corpNm, m.ISU_ABBRV2 as corpNm2 FROM Stock m WHERE m.DELIST_YN = 'N'")
    List<Tuple> loadCacheStock();
}
