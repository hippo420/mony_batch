package app.monybatch.mony.system.service;

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
                        .addLong("forced.id", System.currentTimeMillis())
                        .toJobParameters();

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .toJobParameters();

            }

            jobLauncher.run(jobRegistry.getJob("stockItemJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
            JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public void fetchPrice(String basDd, boolean forced) {

        String today = DateUtil.getDateYmd();
        //if(today.equals(basDd))

        JobParameters jobParameters = null;
        try {
            if(forced)
            {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .addLong("forced_id", System.currentTimeMillis())
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd == null ? DateUtil.getDateYmd() : basDd)
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }

            jobLauncher.run(jobRegistry.getJob("stockPriceJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
               JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fetchMappring(boolean forced) {
        JobParameters jobParameters = null;
        try {
            if(forced)
            {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", DateUtil.getDateYmd())
                        .addLong("forced_id", System.currentTimeMillis())
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", DateUtil.getDateYmd())
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }

            jobLauncher.run(jobRegistry.getJob("dartJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
               JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fetchDartInfo(String basDd, boolean forced) {
        JobParameters jobParameters = null;
        try {
            if(forced)
            {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", DateUtil.getDateYmd())
                        .addLong("forced_id", System.currentTimeMillis())
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", DateUtil.getDateYmd())
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }

            jobLauncher.run(jobRegistry.getJob("fetchDartInfoJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
               JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void fetchDartAcct(String corp_code, String bsns_year, boolean forced) {
        String[] rpt = {"11014","11013","11012","11011"};

        for(int i=0;i<4;i++)
        {
            JobParameters jobParameters = null;
            try {
                if(forced)
                {
                    jobParameters = new JobParametersBuilder()
                            .addString("corp_code", corp_code)
                            .addString("bsns_year", bsns_year)
                            .addString("reprt_code", rpt[i])
                            .addLong("forced_id", System.currentTimeMillis())
                            .toJobParameters();
                    log.info("Job Parameters: {}", jobParameters);

                }else {
                    jobParameters = new JobParametersBuilder()
                            .addString("corp_code", corp_code)
                            .addString("bsns_year", bsns_year)
                            .addString("reprt_code", rpt[i])
                            .toJobParameters();
                    log.info("Job Parameters: {}", jobParameters);

                }

                jobLauncher.run(jobRegistry.getJob("fetchDartAcctJob"),jobParameters);
            }
            catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                   JobRestartException | JobParametersInvalidException e) {
                log.error("배치처리오류 {}",e.getMessage());
                throw new RuntimeException(e);
            }
        }

    }

    public void fetchInvestorTrade(String basDd, boolean forced) {


        JobParameters jobParameters = null;
        try {
            if(forced)
            {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd)
                        .addLong("forced_id", System.currentTimeMillis())
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }else {
                jobParameters = new JobParametersBuilder()
                        .addString("basDd", basDd)
                        .toJobParameters();
                log.info("Job Parameters: {}", jobParameters);

            }

            jobLauncher.run(jobRegistry.getJob("investorTradeInfoJob"),jobParameters);
        }
        catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
               JobRestartException | JobParametersInvalidException e) {
            log.error("배치처리오류 {}",e.getMessage());
            throw new RuntimeException(e);
        }
    }


}
