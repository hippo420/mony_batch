package app.monybatch.mony.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ControllerAspect {
    @Around("execution(* app.monybatch.mony..controller.*Controller.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            log.error("컨트롤러 호출 실패 - 클래스: {}, 메소드: {}, 실행시간: {}ms, 원인: {}",
                    className, methodName, (endTime - startTime), e.getMessage(), e);
            throw e;
        }
    }
}
