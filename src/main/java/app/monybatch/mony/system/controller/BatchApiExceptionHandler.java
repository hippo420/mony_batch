package app.monybatch.mony.system.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 배치 모니터링 API(vue-batch-admin) 전용 에러 응답 통일.
 * 모든 응답을 { code, message } 형식으로 고정해 프론트가 code 기준으로 분기할 수 있게 한다.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = {ManageController.class, BatchScheduleController.class})
public class BatchApiExceptionHandler {

    @ExceptionHandler(NoSuchJobException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchJob(NoSuchJobException e) {
        return body(HttpStatus.NOT_FOUND, "JOB_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(NoSuchJobExecutionException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchExecution(NoSuchJobExecutionException e) {
        return body(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler({JobExecutionAlreadyRunningException.class, JobInstanceAlreadyCompleteException.class})
    public ResponseEntity<Map<String, String>> handleAlreadyRunning(Exception e) {
        return body(HttpStatus.CONFLICT, "ALREADY_RUNNING", e.getMessage());
    }

    @ExceptionHandler(JobExecutionNotRunningException.class)
    public ResponseEntity<Map<String, String>> handleNotRunning(JobExecutionNotRunningException e) {
        return body(HttpStatus.CONFLICT, "NOT_RUNNING", e.getMessage());
    }

    @ExceptionHandler(JobRestartException.class)
    public ResponseEntity<Map<String, String>> handleRestart(JobRestartException e) {
        return body(HttpStatus.CONFLICT, "NOT_RESTARTABLE", e.getMessage());
    }

    @ExceptionHandler(JobParametersInvalidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParams(JobParametersInvalidException e) {
        return body(HttpStatus.BAD_REQUEST, "INVALID_PARAMS", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        log.error("배치 관리 API 처리 중 예외", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "요청 처리 중 오류가 발생했습니다");
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
