package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.OpenAPIItemReader;
import app.monybatch.mony.business.entity.dart.DartAccountEntity;
import app.monybatch.mony.business.repository.jpa.StockRepository;
import app.monybatch.mony.system.core.constant.DataType;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Configuration
@AllArgsConstructor
public class DartAcctJob {
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final StockRepository stockRepository;
    private final String PATH = "/api/fnlttSinglAcnt.json";

    @Qualifier("batchEntityManager")
    private EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager batchTransactionManager;

    @Bean
    public DescriptiveJob fetchDartAcctJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{"corp_code","bsns_year","reprt_code"});
        validator.setOptionalKeys(new String[] { "forced_id"});

        Job job = new JobBuilder("fetchDartAcctJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(readDartAcctStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "DART 재무정보(단일회사) 배치 처리");
    }


    //실제 배치처리
    @Bean
    public Step readDartAcctStep() {

        return new StepBuilder("readDartAcctStep",jobRepository)
                .<DartAccountEntity, DartAccountEntity> chunk(100,batchTransactionManager)
                .reader(dartAcctReader(null,null,null))
                .writer(dartAcctWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIItemReader<DartAccountEntity> dartAcctReader(@Value("#{jobParameters['corp_code']}") String corp_code,
                                                               @Value("#{jobParameters['bsns_year']}") String bsns_year,
                                                               @Value("#{jobParameters['reprt_code']}") String reprt_code) {

        if(corp_code.isEmpty() || bsns_year.isEmpty() || reprt_code.isEmpty())
            throw new RuntimeException("필수값이 빠져있습니다.");

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("corp_code", corp_code);
        params.add("bsns_year",bsns_year);
        params.add("reprt_code",reprt_code);

        return new OpenAPIItemReader<>(DartAccountEntity.class, params,"DART",PATH, DataType.DATA_JSON,null);
    }


    //쓰기
    @Bean
    @StepScope
    public JpaItemWriter<DartAccountEntity> dartAcctWriter(){

        JpaItemWriter<DartAccountEntity> writer = new JpaItemWriter<DartAccountEntity>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
