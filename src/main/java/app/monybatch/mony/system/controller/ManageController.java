package app.monybatch.mony.system.controller;

import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.parameter.JobParamSpec;
import app.monybatch.mony.common.constant.JobDomain;
import app.monybatch.mony.system.service.BatchLaunchService;
import app.monybatch.mony.system.service.BatchLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 배치 모니터링/조작 API (vue-batch-admin 화면용).
 * 응답 필드는 camelCase로 고정한다 — DB 종류에 따라 컬럼 키 대소문자가 바뀌지 않도록
 * 모든 SQL에서 쌍따옴표 alias를 사용할 것.
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class ManageController {

    private static final String RUNNING_STATUSES = "('STARTING','STARTED','STOPPING')";

    private static final String EXECUTION_SELECT = """
            SELECT je.JOB_EXECUTION_ID  AS "executionId"
                 , ji.JOB_NAME          AS "jobName"
                 , ji.JOB_INSTANCE_ID   AS "jobInstanceId"
                 , je.STATUS            AS "status"
                 , je.EXIT_CODE         AS "exitCode"
                 , je.START_TIME        AS "startTime"
                 , je.END_TIME          AS "endTime"
                 , CAST(EXTRACT(EPOCH FROM (je.END_TIME - je.START_TIME)) * 1000 AS BIGINT) AS "durationMs"
              FROM BATCH_JOB_EXECUTION je
              JOIN BATCH_JOB_INSTANCE ji ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
            """;

    private final JobRegistry jobRegistry;
    private final JobExplorer jobExplorer;
    private final JdbcTemplate jdbcTemplate;
    private final JobOperator jobOperator;
    private final ApplicationContext context;
    private final BatchLogService batchLogService;
    private final BatchLaunchService batchLaunchService;
    // 재시작 시 이전 실행의 원본 JobParameters(타입 보존)를 그대로 재사용하기 위해 직접 사용
    // 파라미터명이 bean 이름과 일치해야 비동기 런처가 주입됨 (기본 jobLauncher는 동기)
    private final JobLauncher asyncJobLauncher;

    /** 1. Job 목록 + 최근 실행 요약 */
    @GetMapping("/jobs")
    public List<Map<String, Object>> listJobs() {
        String sql = EXECUTION_SELECT + """
                 WHERE je.JOB_EXECUTION_ID IN (
                       SELECT MAX(je2.JOB_EXECUTION_ID)
                         FROM BATCH_JOB_EXECUTION je2
                         JOIN BATCH_JOB_INSTANCE ji2 ON ji2.JOB_INSTANCE_ID = je2.JOB_INSTANCE_ID
                        GROUP BY ji2.JOB_NAME)
                """;
        Map<String, Map<String, Object>> lastByJob = jdbcTemplate.queryForList(sql).stream()
                .collect(Collectors.toMap(row -> (String) row.get("jobName"), row -> row));

        // 등록된 Job 빈 기준으로 목록 구성 — 한 번도 실행되지 않은 Job도 노출
        Map<String, String> descriptions = new HashMap<>();
        List<String> jobNames = new ArrayList<>();
        context.getBeansOfType(DescriptiveJob.class).values().forEach(dj -> {
            jobNames.add(dj.getJob().getName());
            descriptions.put(dj.getJob().getName(), dj.getDescription());
        });
        context.getBeansOfType(Job.class).values().forEach(job -> {
            if (!jobNames.contains(job.getName())) {
                jobNames.add(job.getName());
            }
        });

        return jobNames.stream()
                .map(jobName -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", jobName);
                    item.put("description", descriptions.getOrDefault(jobName, "설명 없음"));
                    item.put("domain", JobDomain.resolve(jobName));
                    item.put("lastExecution", lastByJob.get(jobName));
                    return item;
                })
                .sorted(Comparator.comparing(m -> m.get("domain") + "/" + m.get("name")))
                .toList();
    }

    /** 2. 실행 이력 검색 (jobName/상태/기간 필터 + 페이징) */
    @GetMapping("/executions")
    public Map<String, Object> searchExecutions(@RequestParam(required = false) String jobName,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String from,
                                                @RequestParam(required = false) String to,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (StringUtils.hasText(jobName)) {
            where.append(" AND ji.JOB_NAME = ?");
            args.add(jobName);
        }
        if (StringUtils.hasText(status)) {
            if ("RUNNING".equalsIgnoreCase(status)) {
                where.append(" AND je.STATUS IN ").append(RUNNING_STATUSES);
            } else {
                where.append(" AND je.STATUS = ?");
                args.add(status.toUpperCase());
            }
        }
        if (StringUtils.hasText(from)) {
            where.append(" AND je.START_TIME >= ?");
            args.add(Timestamp.valueOf(LocalDate.parse(from).atStartOfDay()));
        }
        if (StringUtils.hasText(to)) {
            where.append(" AND je.START_TIME < ?");
            args.add(Timestamp.valueOf(LocalDate.parse(to).plusDays(1).atStartOfDay()));
        }

        String countSql = """
                SELECT COUNT(*)
                  FROM BATCH_JOB_EXECUTION je
                  JOIN BATCH_JOB_INSTANCE ji ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
                """ + where;
        long total = Optional.ofNullable(
                jdbcTemplate.queryForObject(countSql, Long.class, args.toArray())).orElse(0L);

        int pageSize = Math.min(Math.max(size, 1), 100);
        int pageNo = Math.max(page, 0);
        List<Object> listArgs = new ArrayList<>(args);
        listArgs.add(pageSize);
        listArgs.add(pageNo * pageSize);
        List<Map<String, Object>> content = jdbcTemplate.queryForList(
                EXECUTION_SELECT + where + " ORDER BY je.JOB_EXECUTION_ID DESC LIMIT ? OFFSET ?",
                listArgs.toArray());
        content.forEach(row -> row.put("domain", JobDomain.resolve((String) row.get("jobName"))));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("totalElements", total);
        result.put("totalPages", (total + pageSize - 1) / pageSize);
        result.put("page", pageNo);
        result.put("size", pageSize);
        return result;
    }

    /** 3. 실행중 목록 (대시보드 폴링용) */
    @GetMapping("/executions/running")
    public List<Map<String, Object>> listRunningExecutions() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                EXECUTION_SELECT + " WHERE je.STATUS IN " + RUNNING_STATUSES
                        + " ORDER BY je.START_TIME DESC");
        rows.forEach(row -> row.put("domain", JobDomain.resolve((String) row.get("jobName"))));
        return rows;
    }

    /** 4. 실행 상세 (실행 + 파라미터 + Step) */
    @GetMapping("/executions/{jobExecutionId}")
    public ResponseEntity<Map<String, Object>> getExecution(@PathVariable Long jobExecutionId) {
        List<Map<String, Object>> execs = jdbcTemplate.queryForList("""
                SELECT je.JOB_EXECUTION_ID  AS "executionId"
                     , ji.JOB_NAME          AS "jobName"
                     , ji.JOB_INSTANCE_ID   AS "jobInstanceId"
                     , je.STATUS            AS "status"
                     , je.EXIT_CODE         AS "exitCode"
                     , je.EXIT_MESSAGE      AS "exitMessage"
                     , je.START_TIME        AS "startTime"
                     , je.END_TIME          AS "endTime"
                     , CAST(EXTRACT(EPOCH FROM (je.END_TIME - je.START_TIME)) * 1000 AS BIGINT) AS "durationMs"
                  FROM BATCH_JOB_EXECUTION je
                  JOIN BATCH_JOB_INSTANCE ji ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
                 WHERE je.JOB_EXECUTION_ID = ?
                """, jobExecutionId);
        if (execs.isEmpty()) {
            return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "실행 이력이 없습니다 (executionId=" + jobExecutionId + ")");
        }
        Map<String, Object> execution = execs.get(0);
        execution.put("domain", JobDomain.resolve((String) execution.get("jobName")));

        List<Map<String, Object>> params = jdbcTemplate.queryForList("""
                SELECT PARAMETER_NAME  AS "name"
                     , PARAMETER_TYPE  AS "type"
                     , PARAMETER_VALUE AS "value"
                     , IDENTIFYING     AS "identifying"
                  FROM BATCH_JOB_EXECUTION_PARAMS
                 WHERE JOB_EXECUTION_ID = ?
                """, jobExecutionId);

        List<Map<String, Object>> steps = jdbcTemplate.queryForList("""
                SELECT STEP_EXECUTION_ID   AS "stepExecutionId"
                     , STEP_NAME           AS "stepName"
                     , STATUS              AS "status"
                     , READ_COUNT          AS "readCount"
                     , WRITE_COUNT         AS "writeCount"
                     , COMMIT_COUNT        AS "commitCount"
                     , ROLLBACK_COUNT      AS "rollbackCount"
                     , FILTER_COUNT        AS "filterCount"
                     , READ_SKIP_COUNT     AS "readSkipCount"
                     , WRITE_SKIP_COUNT    AS "writeSkipCount"
                     , PROCESS_SKIP_COUNT  AS "processSkipCount"
                     , EXIT_CODE           AS "exitCode"
                     , EXIT_MESSAGE        AS "exitMessage"
                     , START_TIME          AS "startTime"
                     , END_TIME            AS "endTime"
                     , LAST_UPDATED        AS "lastUpdated"
                     , CAST(EXTRACT(EPOCH FROM (END_TIME - START_TIME)) * 1000 AS BIGINT) AS "durationMs"
                  FROM BATCH_STEP_EXECUTION
                 WHERE JOB_EXECUTION_ID = ?
                 ORDER BY STEP_EXECUTION_ID
                """, jobExecutionId);

        return ResponseEntity.ok(Map.of("execution", execution, "params", params, "steps", steps));
    }

    /** 5. KPI 요약 (최근 N시간) */
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam(defaultValue = "24") int hours) {
        Timestamp cutoff = Timestamp.valueOf(LocalDateTime.now().minusHours(Math.max(hours, 1)));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT ji.JOB_NAME AS "jobName"
                     , je.STATUS   AS "status"
                     , COUNT(*)    AS "cnt"
                  FROM BATCH_JOB_EXECUTION je
                  JOIN BATCH_JOB_INSTANCE ji ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
                 WHERE je.CREATE_TIME >= ?
                 GROUP BY ji.JOB_NAME, je.STATUS
                """, cutoff);

        long total = 0, completed = 0, failed = 0, running = 0;
        Map<String, Map<String, Long>> byDomain = new TreeMap<>();
        for (Map<String, Object> row : rows) {
            long cnt = ((Number) row.get("cnt")).longValue();
            String st = (String) row.get("status");
            String domain = JobDomain.resolve((String) row.get("jobName"));
            Map<String, Long> d = byDomain.computeIfAbsent(domain,
                    k -> new LinkedHashMap<>(Map.of("total", 0L, "completed", 0L, "failed", 0L, "running", 0L)));

            total += cnt;
            d.merge("total", cnt, Long::sum);
            switch (st) {
                case "COMPLETED" -> { completed += cnt; d.merge("completed", cnt, Long::sum); }
                case "FAILED" -> { failed += cnt; d.merge("failed", cnt, Long::sum); }
                case "STARTING", "STARTED", "STOPPING" -> { running += cnt; d.merge("running", cnt, Long::sum); }
                default -> { }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hours", hours);
        result.put("total", total);
        result.put("completed", completed);
        result.put("failed", failed);
        result.put("running", running);
        result.put("byDomain", byDomain);
        return result;
    }

    /** 6. Job 실행 (비동기 접수, 중복 실행 가드) */
    @PostMapping("/jobs/{jobName}/launch")
    public ResponseEntity<Map<String, Object>> launchJob(@PathVariable String jobName,
                                                         @RequestBody(required = false) Map<String, Object> params) throws Exception {
        Set<JobExecution> runningSet = jobExplorer.findRunningJobExecutions(jobName);
        if (!runningSet.isEmpty()) {
            return error(HttpStatus.CONFLICT, "ALREADY_RUNNING",
                    jobName + " 이(가) 이미 실행 중입니다 (executionId=" + runningSet.iterator().next().getId() + ")");
        }

        JobExecution execution = batchLaunchService.launch(jobName, params);
        log.info("배치 조작 감사 - action=LAUNCH, job={}, params={}, executionId={}", jobName, params, execution.getId());
        return ResponseEntity.accepted().body(Map.of(
                "executionId", execution.getId(),
                "status", String.valueOf(execution.getStatus())));
    }

    /** 7. 실행 중지 — 실행 중이 아니면 JobExecutionNotRunningException, BatchApiExceptionHandler가 409로 변환 */
    @PostMapping("/executions/{jobExecutionId}/stop")
    public ResponseEntity<Map<String, Object>> stopExecution(@PathVariable Long jobExecutionId) throws Exception {
        jobOperator.stop(jobExecutionId);
        log.info("배치 조작 감사 - action=STOP, executionId={}", jobExecutionId);
        return ResponseEntity.ok(Map.of("message", "중지 요청됨 (executionId=" + jobExecutionId + ")"));
    }

    /** 8. 실행 재시작 (FAILED/STOPPED 대상, 비동기 접수) */
    @PostMapping("/executions/{jobExecutionId}/restart")
    public ResponseEntity<Map<String, Object>> restartExecution(@PathVariable Long jobExecutionId) throws Exception {
        JobExecution old = jobExplorer.getJobExecution(jobExecutionId);
        if (old == null) {
            return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "실행 이력이 없습니다 (executionId=" + jobExecutionId + ")");
        }
        BatchStatus st = old.getStatus();
        if (st != BatchStatus.FAILED && st != BatchStatus.STOPPED) {
            return error(HttpStatus.CONFLICT, "NOT_RESTARTABLE",
                    "FAILED/STOPPED 상태만 재시작할 수 있습니다 (현재 " + st + ")");
        }
        String jobName = old.getJobInstance().getJobName();
        if (!jobExplorer.findRunningJobExecutions(jobName).isEmpty()) {
            return error(HttpStatus.CONFLICT, "ALREADY_RUNNING", jobName + " 이(가) 이미 실행 중입니다");
        }

        // 동일 파라미터로 run() → 실패한 JobInstance의 새 Execution 생성 (= 재시작 의미)
        Job job = jobRegistry.getJob(jobName);
        JobExecution newExecution = asyncJobLauncher.run(job, old.getJobParameters());
        log.info("배치 조작 감사 - action=RESTART, job={}, oldExecutionId={}, newExecutionId={}",
                jobName, jobExecutionId, newExecution.getId());
        return ResponseEntity.accepted().body(Map.of(
                "executionId", newExecution.getId(),
                "restartOf", jobExecutionId));
    }

    /** 9. 실행 로그 조회 — 도메인 로그 파일 tail + Kibana 딥링크 */
    @GetMapping("/executions/{jobExecutionId}/logs")
    public ResponseEntity<Map<String, Object>> fetchLogs(@PathVariable Long jobExecutionId,
                                                         @RequestParam(defaultValue = "300") int lines) {
        JobExecution execution = jobExplorer.getJobExecution(jobExecutionId);
        if (execution == null) {
            return error(HttpStatus.NOT_FOUND, "NOT_FOUND", "실행 이력이 없습니다 (executionId=" + jobExecutionId + ")");
        }
        return ResponseEntity.ok(batchLogService.getExecutionLogs(
                execution.getJobInstance().getJobName(),
                execution.getJobId(),
                execution.getStartTime(),
                execution.getEndTime(),
                lines));
    }

    /** 10. Job 파라미터 명세 조회 — 실행 모달의 라벨/형식 힌트/기본값 */
    @GetMapping("/jobs/{jobName}/param-specs")
    public List<JobParamSpec> getParamSpecs(@PathVariable String jobName) {
        return batchLaunchService.getParamSpecs(jobName);
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
