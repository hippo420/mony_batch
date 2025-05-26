package app.monybatch.mony.system.core.aop;

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
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("컨트톨러 호출 - 클래스: {}, 메소드 : {}", className, methodName);

        // 실제 저장 실행
        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        log.info("컨트톨러 호출 완료 - 실행시간 : {}ms", (endTime-startTime));

        return result;
    }
}
