package app.monybatch.mony.system.service;

import app.monybatch.mony.common.constant.DartConst;
import app.monybatch.mony.common.core.utils.DateUtil;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class DartBatchService {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final DartRssSyncService service;
    public void fetchItem(String basDd, boolean forced){
        JobParameters jobParameters = null;
        try {
            if(forced)
            {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .addLong("forced.id", System.currentTimeMillis())
                        .toJobParameters();

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .toJobParameters();

            }

            jobLauncher.run(jobRegistry.getJob("fetchDartInfoJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
            JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Scheduled(cron = "0 * * * * *")
    public void collectDartRss(){
        service.syncRssData(DartConst.DART_RSS);
    }

}
