package app.monybatch.mony.domian.trade.repository;

import app.monybatch.mony.domian.trade.entity.Trade;
import app.monybatch.mony.domian.trade.entity.TradeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, TradeId> {

    @Query("SELECT MAX(t.stckBsopDate) FROM Trade t WHERE t.isuSrtCd = :isuSrtCd")
    String findMaxDateByIsuSrtCd(@Param("isuSrtCd") String isuSrtCd);

    @Query("SELECT t.stckBsopDate, s.industryCode.code, t.acmlVol, t.acmlTrPbmn, " +
           "t.frgnNtbyQty, t.frgnNtbyTrPbmn, t.orgnNtbyQty, t.orgnNtbyTrPbmn, " +
           "t.prsnNtbyQty, t.prsnNtbyTrPbmn, t.fundNtbyTrPbmn, t.peFundNtbyTrPbmn " +
           "FROM Trade t, Stock s " +
           "WHERE t.isuSrtCd = s.ISU_SRT_CD " +
           "AND t.stckBsopDate = :basDd " +
           "AND s.industryCode IS NOT NULL")
    List<Object[]> findTradeWithIndustryByDate(@Param("basDd") String basDd);


}
