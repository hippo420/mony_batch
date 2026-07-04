package app.monybatch.mony.common.config;

import app.monybatch.mony.batch.support.listener.BatchJobLoggingListener;
import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 모든 Job 빈(DescriptiveJob 래핑 포함)에 BatchJobLoggingListener를 자동 등록.
 * 각 Job 설정 파일에 .listener(...)를 일일이 추가하지 않아도 실행 요약 로그가 남는다.
 */
@Component
public class JobLoggingListenerRegistrar implements BeanPostProcessor {

    private final BatchJobLoggingListener listener = new BatchJobLoggingListener();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DescriptiveJob descriptiveJob
                && descriptiveJob.getJob() instanceof AbstractJob job) {
            job.registerJobExecutionListener(listener);
        } else if (bean instanceof AbstractJob job) {
            job.registerJobExecutionListener(listener);
        }
        return bean;
    }
}
