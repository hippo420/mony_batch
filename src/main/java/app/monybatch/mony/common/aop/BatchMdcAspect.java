package app.monybatch.mony.common.aop;

import app.monybatch.mony.common.constant.JobDomain;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

/**
 * JobLauncher.run() 제출 시점에 MDC(JOB_NAME/DOMAIN)를 주입.
 * - 동기 실행: run() 전체 구간의 로그가 도메인 파일로 라우팅됨
 * - 비동기 실행(asyncJobLauncher): 제출 시점 MDC를 TaskDecorator가 실행 스레드로 복사
 * Job 시작/종료/Step 요약 로그는 BatchJobLoggingListener가 실행 스레드에서 담당한다.
 */
@Aspect
@Component
@Slf4j
public class BatchMdcAspect {

    @Around("execution(* org.springframework.batch.core.launch.JobLauncher.run(..))")
    public Object injectJobMdc(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        String jobName = null;
        if (args.length > 0 && args[0] instanceof Job job) {
            jobName = job.getName();
            MDC.put("JOB_NAME", jobName);
            MDC.put("DOMAIN", JobDomain.resolve(jobName));
        }
        try {
            Object result = pjp.proceed();
            if (result instanceof JobExecution je) {
                log.info("Job 실행 접수 - job={}, executionId={}, status={}", jobName, je.getId(), je.getStatus());
            }
            return result;
        } finally {
            MDC.remove("JOB_NAME");
            MDC.remove("DOMAIN");
        }
    }
}
