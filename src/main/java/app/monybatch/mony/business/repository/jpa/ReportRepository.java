package app.monybatch.mony.business.repository.jpa;

import app.monybatch.mony.business.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
