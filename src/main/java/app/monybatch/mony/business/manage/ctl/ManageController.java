package app.monybatch.mony.business.manage.ctl;

import app.monybatch.mony.business.batch.job.DescriptiveJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class ManageController {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;
    private final JdbcTemplate jdbcTemplate;
    private final JobOperator jobOperator;
    private final ApplicationContext context;

    /** 1. Job 목록 조회 */
    @GetMapping("/jobs")
    public List<Map<String, String>> listJobs() {

        // 1️⃣ DB에서 JOB_NAME 조회
        List<String> jobNamesFromDb = jdbcTemplate.queryForList(
                "SELECT DISTINCT JOB_NAME FROM BATCH_JOB_INSTANCE ORDER BY JOB_NAME",
                String.class
        );
        log.info("jobNamesFromDb: {}", jobNamesFromDb);

        // 2️⃣ 스프링 Bean에서 DescriptiveJob 조회
        Map<String, DescriptiveJob> jobBeans = context.getBeansOfType(DescriptiveJob.class);

        jobNamesFromDb.forEach(jobName -> log.info(jobName));

        // 3️⃣ DB JOB_NAME과 Bean을 매칭해 Map 반환
        return jobNamesFromDb.stream()
                .map(jobName -> {
                    DescriptiveJob bean = jobBeans.get(jobName); // 이름으로 Bean 찾기
                    String description = (bean != null) ? bean.getDescription() : "설명 없음";
                    return Map.of(
                            "name", jobName,
                            "description", description
                    );
                })
                .toList();
    }

    /** 2. Job 실행 이력 조회 (JobInstance 기준) */
    @GetMapping("/jobs/{jobInstanceId}/executions")
    public List<Map<String, Object>> listExecutions(@PathVariable Long jobInstanceId) {
        String sql = """
                SELECT JOB_EXECUTION_ID
                     , STATUS
                     , START_TIME
                     , END_TIME
                     , TO_CHAR(END_TIME - START_TIME, 'HH24:MI:SS.MS') AS execution_time
                     , EXIT_CODE
                     , EXIT_MESSAGE
                 FROM BATCH_JOB_EXECUTION
                WHERE JOB_INSTANCE_ID = ? 
                ORDER BY JOB_EXECUTION_ID DESC
                """;
        return jdbcTemplate.queryForList(sql, jobInstanceId);
    }

    /** 3. Step 실행 이력 조회 */
    @GetMapping("/executions/{jobExecutionId}/steps")
    public List<Map<String, Object>> listStepExecutions(@PathVariable Long jobExecutionId) {
        String sql = """
                    SELECT STEP_EXECUTION_ID
                         , STEP_NAME
                         , STATUS
                         , READ_COUNT
                         , WRITE_COUNT
                         , COMMIT_COUNT
                         , ROLLBACK_COUNT
                         , EXIT_CODE
                         , EXIT_MESSAGE
                         , LAST_UPDATED
                         , FILTER_COUNT
                         , READ_SKIP_COUNT
                         , WRITE_SKIP_COUNT
                         , PROCESS_SKIP_COUNT
                         , START_TIME
                         , END_TIME
                         , CREATE_TIME
                      FROM BATCH_STEP_EXECUTION 
                     WHERE JOB_EXECUTION_ID = ? 
                     ORDER BY STEP_EXECUTION_ID
                    """;
        return jdbcTemplate.queryForList(sql, jobExecutionId);
    }

    /** 4. 실행 로그 조회 (BATCH_JOB_EXECUTION의 EXIT_MESSAGE 기준) */
    @GetMapping("/executions/{jobExecutionId}/logs")
    public Map<String, Object> fetchLogs(@PathVariable Long jobExecutionId) {
        String sql = """
                     SELECT EXIT_MESSAGE 
                       FROM BATCH_JOB_EXECUTION 
                      WHERE JOB_EXECUTION_ID = ?
                     """;
        Map<String, Object> log = jdbcTemplate.queryForMap(sql, jobExecutionId);
        return log;
    }

    /** 5. 실행 중지 */
    @PostMapping("/executions/{jobExecutionId}/stop")
    public String stopExecution(@PathVariable Long jobExecutionId) throws Exception {
        boolean stopped = jobOperator.stop(jobExecutionId);
        if (stopped) {
            return "JobExecution " + jobExecutionId + " stopping...";
        } else {
            return "JobExecution " + jobExecutionId + " could not be stopped or already completed";
        }
    }

    /** 6. 실행 재시작 */
    @PostMapping("/executions/{jobExecutionId}/restart")
    public String restartExecution(@PathVariable Long jobExecutionId) throws Exception {
        // jobOperator.restart()는 이미 완료된 JobExecution을 기반으로 새 Execution 생성
        Long newExecutionId = jobOperator.restart(jobExecutionId);
        return "JobExecution " + jobExecutionId + " restarted as new execution " + newExecutionId;
    }

    /** 7. Job 즉시 실행 */
    @PostMapping("/jobs/{jobName}/launch")
    public String launchJob(@PathVariable String jobName,
                            @RequestBody(required = false) Map<String, Object> params) throws Exception {
        Job job = jobRegistry.getJob(jobName);
        JobParametersBuilder builder = new JobParametersBuilder();
        if (params != null) {
            params.forEach((k, v) -> builder.addString(k, String.valueOf(v)));
        }
        //builder.addLong("time", System.currentTimeMillis());
        jobLauncher.run(job, builder.toJobParameters());
        return "Job " + jobName + " launched";
    }

    /** Job 필수 파라미터 조회 */
    @GetMapping("/jobs/{jobName}/required-params")
    public List<String> getRequiredJobParameters(@PathVariable String jobName) throws Exception {
        log.info("getRequiredJobParameters: {}", jobName);
        Job job = jobRegistry.getJob(jobName);
        JobParametersValidator validator = job.getJobParametersValidator();

        if (validator instanceof DefaultJobParametersValidator dv) {
            // 필수 키는 private 필드이므로 reflection으로 가져옴
            Field requiredKeysField = DefaultJobParametersValidator.class.getDeclaredField("requiredKeys");
            requiredKeysField.setAccessible(true);
            Set<String> requiredKeys = (Set<String>) requiredKeysField.get(dv);
            log.info("requiredKeys: {}", requiredKeys.stream().toArray());
            return new ArrayList<>(requiredKeys);
        }


        return Collections.emptyList(); // Validator가 없으면 필수 파라미터 없음
    }
}
