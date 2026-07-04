package app.monybatch.mony.common.config;

import org.slf4j.MDC;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * 모니터링 API(launch/restart) 전용 비동기 JobLauncher.
 * HTTP 요청 스레드에서 배치를 동기 실행하면 프론트 타임아웃(10초)에 걸리므로
 * 실행 접수 즉시 executionId를 반환하고 실제 실행은 별도 스레드풀에서 수행한다.
 * 스케줄러/서비스가 쓰는 기본 jobLauncher(동기)는 그대로 유지 — 주입 지점의
 * 파라미터명이 bean 이름과 일치하므로 타입 모호성은 이름으로 해소된다.
 */
@Configuration
public class AsyncBatchLaunchConfig {

    @Bean
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("batch-launch-");
        // 제출 시점의 MDC(JOB_NAME/DOMAIN)를 실행 스레드로 복사 —
        // 비동기 실행에서도 도메인별 로그 파일 분리가 유지되도록 한다
        executor.setTaskDecorator(task -> {
            Map<String, String> mdc = MDC.getCopyOfContextMap();
            return () -> {
                if (mdc != null) {
                    MDC.setContextMap(mdc);
                }
                try {
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        });
        executor.initialize();

        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(executor);
        launcher.afterPropertiesSet();
        return launcher;
    }
}
