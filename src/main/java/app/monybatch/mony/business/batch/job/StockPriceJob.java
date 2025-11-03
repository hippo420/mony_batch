package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.OpenAPIReader;
import app.monybatch.mony.business.entity.StockPrice;
import app.monybatch.mony.business.repository.jpa.StockRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockPriceJob {

    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;

    private final StockRepository stockRepository;
    private final String PATH = "/svc/apis/sto/stk_bydd_trd";

    @PersistenceUnit // 💡 EntityManagerFactory를 주입받습니다.
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public DescriptiveJob priceJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(Collections.singletonList("basDd").toArray(new String[0]));
        validator.setOptionalKeys(new String[] { "param1","param2" });

        Job job = new JobBuilder("priceJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(batchPriceStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "주식종목 종가배치 처리");
    }


    //실제 배치처리
    @Bean
    public Step batchPriceStep() {

        return new StepBuilder("batchPriceStep",jobRepository)
                .<StockPrice, StockPrice> chunk(100,jpaTransactionManager(entityManagerFactory))
                .reader(stockPriceApiReader(null))
                .processor(stockPriceProcessor())
                .writer(stockPriceWriter())
                .transactionManager(jpaTransactionManager(entityManagerFactory))
                .build();
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIReader<StockPrice> stockPriceApiReader(@Value("#{jobParameters['basDd']}") String basDd) {


        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("basDd",basDd);

        return new OpenAPIReader<>(StockPrice.class, params,"KRX",PATH);
    }

    //배치처리
    @Bean
    public ItemProcessor<StockPrice, StockPrice> stockPriceProcessor(){
        return new ItemProcessor<>() {
            @Override
            public StockPrice process(StockPrice item) throws Exception {

                return item;
            }
        };
    }


    //쓰기
    @Bean
    @StepScope
    public JpaItemWriter<StockPrice> stockPriceWriter(){

        JpaItemWriter<StockPrice> writer = new JpaItemWriter<StockPrice>();
        log.info("트랜잭션활성화: {}", TransactionSynchronizationManager.isActualTransactionActive());
        // 안전하게 EntityManagerFactory를 설정하여 트랜잭션이 바인딩되도록 합니다.
        writer.setEntityManagerFactory(entityManagerFactory);
        // 💡 참고: JpaItemWriter는 merge() 전략을 기본으로 사용합니다.
        return writer;
    }
}
