package app.monybatch.mony.batch.dart.job;

import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.AppConst;
import app.monybatch.mony.common.constant.DartConst;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.core.utils.DateUtil;
import app.monybatch.mony.domian.dart.DartBasicEntity;
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

import java.util.Collections;

@Slf4j
@Configuration
@AllArgsConstructor
public class DartInfoJob {
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final String PATH = "/api/list.json";

    @Qualifier("batchEntityManager")
    private EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager batchTransactionManager;

    @Bean
    public DescriptiveJob fetchDartInfoJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(Collections.singletonList("basDd").toArray(new String[0]));
        validator.setOptionalKeys(new String[] { "forced_id"});

        Job job = new JobBuilder("fetchDartInfoJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(readDartInfoStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "DART 공시내역 배치 처리");
    }


    //실제 배치처리
    @Bean
    public Step readDartInfoStep() {

        return new StepBuilder("readDartInfoStep",jobRepository)
                .<DartBasicEntity, DartBasicEntity> chunk(100,batchTransactionManager)
                .reader(dartInfoReader(null))
                .writer(dartInfoWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIItemReader<DartBasicEntity> dartInfoReader(@Value("#{jobParameters['basDd']}") String basDd) {

        String sYmd = basDd.isEmpty() ? DateUtil.getDateYmd() : basDd;
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("corp_cls", AppConst.CON_Y);
        params.add("bgn_de",sYmd);
        params.add("end_de",sYmd);
        params.add("page_no", DartConst.PAGE_NO);
        params.add("page_count",DartConst.PAGE_COUNT);
        return new OpenAPIItemReader<>(DartBasicEntity.class, params,"DART",PATH, DataType.DATA_JSON,null);
    }


    //쓰기
    @Bean
    @StepScope
    public JpaItemWriter<DartBasicEntity> dartInfoWriter(){

        JpaItemWriter<DartBasicEntity> writer = new JpaItemWriter<DartBasicEntity>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
