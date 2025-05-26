package app.monybatch.mony.business.batch;


import app.monybatch.mony.business.entity.sample.CodeEntity;
import app.monybatch.mony.business.batch.reader.ZeroOffsetJpaPagingItemReader;
import app.monybatch.mony.business.batch.reader.ZeroOffsetJpaPagingItemReaderBuilder;
import app.monybatch.mony.business.batch.reader.listener.LoggingChunkListener;

import app.monybatch.mony.business.repository.jpa.ItemRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Configuration
@AllArgsConstructor
public class ReaderTestBatch {
    private JobRepository jobRepository;
    private final ItemRepository tobeRepository;
    private PlatformTransactionManager transactionManager;
    private EntityManagerFactory entityManagerFactory;
    private final int CHUNK_SIZE=10000;

    @Bean
    public Job pagingBatchJob() throws InstantiationException, IllegalAccessException {
        return new JobBuilder("pagingBatchJob",jobRepository)
                .start(pagingBatchStep())
                .build();
    }

    //실제 배치처리
    @Bean
    public Step pagingBatchStep() throws InstantiationException, IllegalAccessException {
        return new StepBuilder("pagingBatchStep",jobRepository)
                .<CodeEntity, CodeEntity> chunk(CHUNK_SIZE,transactionManager)
                .reader(zeroPagingReader())
                .listener(new LoggingChunkListener())
                .writer(item->{

                })
                .build();
    }

    //데이터 읽기
    @Bean
    public JpaPagingItemReader<CodeEntity> pagingReader(){
        return new JpaPagingItemReaderBuilder<CodeEntity>()
                .queryString("SELECT p FROM CODE_ENTITY p")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .name("pagingReader")
                .build();
    }

    @Bean
    public ZeroOffsetJpaPagingItemReader<CodeEntity> zeroPagingReader() throws InstantiationException, IllegalAccessException {
        return new ZeroOffsetJpaPagingItemReaderBuilder<CodeEntity>()
                .object(CodeEntity.class.newInstance())
                .queryString("SELECT p FROM CODE_ENTITY p")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .name("pagingReader")
                .build();
    }

    //배치처리
    @Bean
    public ItemProcessor<CodeEntity, CodeEntity> pagingProcessor(){
        return new ItemProcessor<CodeEntity, CodeEntity>() {
            @Override
            public CodeEntity process(CodeEntity codeEntity) throws Exception {
                CodeEntity tobeEntity = new CodeEntity();
                return tobeEntity;
            }
        };
    }

    //쓰기

    public RepositoryItemWriter<CodeEntity> pagingWriter(){
        return new RepositoryItemWriter<CodeEntity>();
//        return new RepositoryItemWriterBuilder<ItemEntity>()
//                .repository(tobeRepository)
//                .methodName("save")
//                .build();
    }


    @Bean
    public StepExecutionListener stepExecutionListener(){
        return new StepExecutionListener(){
            private LocalDateTime startime;

            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Step start!!");
                startime=LocalDateTime.now();
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("Step end!!");
                LocalDateTime endtime = LocalDateTime.now();
                long time = ChronoUnit.NANOS.between(startime,endtime);
                double seconds=time/1_000_000_000.0;
                log.info("[{}] 실행시간: {}초",stepExecution.getStepName(),seconds);
                return null;
            }

        };
    }
}
