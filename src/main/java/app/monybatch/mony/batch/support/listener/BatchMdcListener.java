package app.monybatch.mony.batch.support.listener;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Kibana 필터링을 위해 MDC에 배치 컨텍스트를 주입하는 리스너.
 * Job/Step 빌더에 .listener(batchMdcListener) 로 등록해서 사용.
 */
@Slf4j
@Component
public class BatchMdcListener implements JobExecutionListener, StepExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put("JOB_NAME", jobExecution.getJobInstance().getJobName());
        MDC.put("JOB_INSTANCE_ID", String.valueOf(jobExecution.getJobId()));
        log.info("Job 시작 - jobId={}", jobExecution.getJobId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job 종료 - status={}, duration={}ms",
            jobExecution.getStatus(),
            jobExecution.getEndTime() != null && jobExecution.getStartTime() != null
                ? java.time.Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis()
                : -1
        );
        MDC.remove("JOB_NAME");
        MDC.remove("JOB_INSTANCE_ID");
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        MDC.put("STEP_NAME", stepExecution.getStepName());
        log.info("Step 시작 - step={}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step 종료 - step={}, readCount={}, writeCount={}, skipCount={}, status={}",
            stepExecution.getStepName(),
            stepExecution.getReadCount(),
            stepExecution.getWriteCount(),
            stepExecution.getSkipCount(),
            stepExecution.getStatus()
        );
        MDC.remove("STEP_NAME");
        return stepExecution.getExitStatus();
    }
}
