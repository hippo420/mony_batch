package app.monybatch.mony.business.service;

import app.monybatch.mony.system.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class StockBatchService {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public void fetchItem(String basDd, boolean forced){
        JobParameters jobParameters = null;
        try {
            if(forced)
            {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .toJobParameters();

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .addLong("forced.id", System.currentTimeMillis())
                        .toJobParameters();

            }

            jobLauncher.run(jobRegistry.getJob("itemJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
            JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
