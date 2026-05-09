package app.monybatch.mony.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

/**
 * 모든 Spring Batch Job/Step 실행에 자동으로 MDC 컨텍스트를 주입.
 * 각 Job 설정 파일을 수정하지 않아도 Kibana에서 JOB_NAME/STEP_NAME으로 필터링 가능.
 */
@Aspect
@Component
@Slf4j
public class BatchMdcAspect {

    // JobLauncher.run() 가로채기 → JOB_NAME MDC 주입
    @Around("execution(* org.springframework.batch.core.launch.JobLauncher.run(..))")
    public Object injectJobMdc(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof Job job) {
            MDC.put("JOB_NAME", job.getName());
        }
        try {
            Object result = pjp.proceed();
            if (result instanceof JobExecution je) {
                MDC.put("JOB_INSTANCE_ID", String.valueOf(je.getJobId()));
                log.info("Job 실행 완료 - jobId={}, status={}", je.getJobId(), je.getStatus());
            }
            return result;
        } finally {
            MDC.remove("JOB_NAME");
            MDC.remove("JOB_INSTANCE_ID");
        }
    }

    // Step 실행 전후로 STEP_NAME MDC 주입 (SimpleStepHandler.handleStep 가로채기)
    @Around("execution(* org.springframework.batch.core.job.SimpleStepHandler.handleStep(..))")
    public Object injectStepMdc(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof StepExecution se) {
            MDC.put("STEP_NAME", se.getStepName());
        }
        try {
            return pjp.proceed();
        } finally {
            MDC.remove("STEP_NAME");
        }
    }
}
