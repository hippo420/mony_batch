select * from BATCH_JOB_EXECUTION  ;
select * from BATCH_JOB_EXECUTION_CONTEXT ;
select * from BATCH_JOB_EXECUTION_PARAMS ;
select * from BATCH_JOB_INSTANCE ;
select * from BATCH_STEP_EXECUTION   ;
select * from BATCH_STEP_EXECUTION_CONTEXT  ;

-- Step별 기본 통계
SELECT
    STEP_NAME,
    COUNT(*) AS execution_count,                    -- Step 실행 횟수
    SUM(READ_COUNT) AS total_read,                 -- 총 읽은 항목 수
    SUM(WRITE_COUNT) AS total_written,             -- 총 처리한 항목 수
    SUM(FILTER_COUNT) AS total_filtered,           -- 필터링된 항목 수
    SUM(READ_SKIP_COUNT + WRITE_SKIP_COUNT + PROCESS_SKIP_COUNT) AS total_skipped,  -- 총 스킵 수
    SUM(ROLLBACK_COUNT) AS total_rollbacks        -- 총 롤백 횟수
FROM BATCH_STEP_EXECUTION
GROUP BY STEP_NAME
ORDER BY STEP_NAME;

-- Step별 평균 실행시간
SELECT
    STEP_NAME,
    AVG(TIMESTAMPDIFF(SECOND, START_TIME, END_TIME)) AS avg_duration_sec, -- 평균 실행시간(초)
    MIN(TIMESTAMPDIFF(SECOND, START_TIME, END_TIME)) AS min_duration_sec, -- 최소 실행시간
    MAX(TIMESTAMPDIFF(SECOND, START_TIME, END_TIME)) AS max_duration_sec  -- 최대 실행시간
FROM BATCH_STEP_EXECUTION
WHERE END_TIME IS NOT NULL
GROUP BY STEP_NAME
ORDER BY STEP_NAME;


-- Step별 성공/실패 비율
SELECT
    STEP_NAME,
    SUM(CASE WHEN STATUS='COMPLETED' THEN 1 ELSE 0 END) AS success_count,
    SUM(CASE WHEN STATUS='FAILED' THEN 1 ELSE 0 END) AS fail_count,
    COUNT(*) AS total_runs,
    ROUND(SUM(CASE WHEN STATUS='COMPLETED' THEN 1 ELSE 0 END)/COUNT(*)*100, 2) AS success_rate
FROM BATCH_STEP_EXECUTION
GROUP BY STEP_NAME
ORDER BY STEP_NAME;