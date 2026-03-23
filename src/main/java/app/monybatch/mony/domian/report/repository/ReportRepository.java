package app.monybatch.mony.domian.report.repository;

import app.monybatch.mony.domian.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
