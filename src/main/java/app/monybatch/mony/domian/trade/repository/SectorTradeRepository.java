package app.monybatch.mony.domian.trade.repository;

import app.monybatch.mony.domian.trade.entity.SectorTrade;
import app.monybatch.mony.domian.trade.entity.SectorTradeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorTradeRepository extends JpaRepository<SectorTrade, SectorTradeId> {


}
