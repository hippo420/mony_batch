package app.monybatch.mony.batch.dart.job;

import app.monybatch.mony.batch.dart.dto.DartRssDto;
import app.monybatch.mony.batch.dart.dto.DartRssQueueDto;
import app.monybatch.mony.batch.dart.processor.DartRssPublishProcessor;
import app.monybatch.mony.batch.dart.reader.DartRssItemReader;
import app.monybatch.mony.batch.dart.writer.DartRssRedisWriter;
import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.common.constant.DartConst;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@AllArgsConstructor
public class DartRssJobConfig {

    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final DartRssPublishProcessor dartRssPublishProcessor;
    private final DartRssRedisWriter dartRssRedisWriter;
    private final PlatformTransactionManager batchTransactionManager;

    @Bean
    public DescriptiveJob dartRssJob() throws DuplicateJobException {
        Job job = new JobBuilder("dartRssJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(dartRssStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "DART RSS 공시 감지 → Redis 적재");
    }

    @Bean
    public Step dartRssStep() {
        return new StepBuilder("dartRssStep", jobRepository)
                .<DartRssDto, DartRssQueueDto>chunk(10, batchTransactionManager)
                .reader(dartRssItemReader())
                .processor(dartRssPublishProcessor)
                .writer(dartRssRedisWriter)
                .build();
    }

    @Bean
    @StepScope
    public DartRssItemReader dartRssItemReader() {
        return new DartRssItemReader(DartConst.DART_RSS);
    }
}
