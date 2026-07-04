package app.monybatch.mony.batch.event.job;

import app.monybatch.mony.batch.event.processor.EconomicEventProcessor;
import app.monybatch.mony.batch.event.reader.HtmlItemReader;
import app.monybatch.mony.batch.event.writer.EconomicEventItemWriter;
import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.parameter.JobParamSpec;
import app.monybatch.mony.domian.event.dto.EconomicEventDto;
import app.monybatch.mony.domian.event.entity.EconomicEvent;
import app.monybatch.mony.domian.event.repository.EconomicEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EventJob {

    // --- 주입되는 컴포넌트 ---
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final EconomicEventRepository eventRepository;
    private final PlatformTransactionManager batchTransactionManager;

    // 실행 주기는 모니터링 화면의 스케줄 설정(batch_schedule_config)에서 관리 — DynamicBatchScheduler 참고

    // --- Job 정의 ---
    @Bean
    public DescriptiveJob economicEventCollectionJob() throws DuplicateJobException {
        log.info("economicEventCollectionJob start");
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        // required: basDd (기준일)
        validator.setRequiredKeys(new String[]{"basDd"});
        validator.setOptionalKeys(new String[]{"time"}); // 'time' 파라미터 추가

        Job job = new JobBuilder("economicEventCollectionJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(batchEventStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "금융 캘린더 주간 조회 배치", List.of(
                new JobParamSpec("basDd", "기준일자 (빈값이면 오늘)", "yyyyMMdd", "", false)
        ));
    }

    // --- Step 정의 ---
    @Bean
    public Step batchEventStep() {
        return new StepBuilder("batchEventStep", jobRepository)
                .<EconomicEventDto, EconomicEvent>chunk(10, batchTransactionManager) // Chunk size 10
                .reader(economicEventReader()) // RSS Reader 연결
                .processor(economicEventProcessor()) // Composite Processor 연결
                .writer(economicEventWriter()) // Writer 연결
                .transactionManager(batchTransactionManager)
                .build();
    }

    // --- ItemReader (RSS Feed 호출) ---
    @Bean
    @StepScope
    public HtmlItemReader economicEventReader() {
        // 수집할 대상 URL 리스트 구성
        log.info("economicEventReader");
        List<String> targetUrls = List.of(
                "https://ko.tradingeconomics.com/united-states/calendar"
        );

        return new HtmlItemReader(targetUrls);
    }

    @Bean
    @StepScope
    public ItemProcessor<EconomicEventDto, EconomicEvent> economicEventProcessor() {

        return new EconomicEventProcessor();
    }


    // --- ItemWriter (Elasticsearch 저장) ---
    @Bean
    @StepScope
    public EconomicEventItemWriter economicEventWriter() {
        return new EconomicEventItemWriter();
    }
}
