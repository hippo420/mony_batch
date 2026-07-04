package app.monybatch.mony.domian.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchScheduleConfigRepository extends JpaRepository<BatchScheduleConfig, String> {
}
