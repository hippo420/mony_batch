package app.monybatch.mony.domian.earning.repository;

import app.monybatch.mony.domian.earning.dto.ReportQuarter;
import app.monybatch.mony.domian.earning.entity.EarningsRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EarningsReleaseRepository extends JpaRepository<EarningsRelease, Long> {

    @Query("SELECT e FROM EarningsRelease e " +
            "WHERE e.isuSrtCd = :isuSrtCd " +
            "AND e.reportYear = :reportYear " +
            "AND e.reportQuarter = :reportQuarter")
    Optional<EarningsRelease> findByIsuSrtCdAndReportYearAndReportQuarter(
            @Param("isuSrtCd") String isuSrtCd,
            @Param("reportYear") String reportYear,
            @Param("reportQuarter") ReportQuarter reportQuarter);
}
