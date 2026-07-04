package app.monybatch.mony.domian.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Job별 스케줄 설정 (실행 여부 + cron 표현식).
 * 모니터링 화면에서 편집하면 DynamicBatchScheduler가 즉시 재등록한다.
 */
@Entity
@Table(name = "batch_schedule_config")
@Getter
@Setter
@NoArgsConstructor
public class BatchScheduleConfig {

    @Id
    @Column(name = "job_name", length = 100)
    private String jobName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "cron_expression", length = 100)
    private String cronExpression;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
