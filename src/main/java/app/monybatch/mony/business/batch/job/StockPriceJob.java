package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.OpenAPIItemReader;
import app.monybatch.mony.business.entity.StockTrade;
import app.monybatch.mony.business.repository.jpa.StockRepository;
import app.monybatch.mony.system.core.constant.DataType;
import jakarta.persistence.EntityManagerFactory;
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

    //@Qualifier("batchEntityManager")
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager batchTransactionManager;

    @Bean
    public DescriptiveJob priceJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(Collections.singletonList("basDd").toArray(new String[0]));
        validator.setOptionalKeys(new String[] { "param1","param2","forced_id" });

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
                .<StockTrade, StockTrade> chunk(100, batchTransactionManager)
                .reader(stockPriceApiReader(null))
                .processor(stockPriceProcessor())
                .writer(stockPriceWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIItemReader<StockTrade> stockPriceApiReader(@Value("#{jobParameters['basDd']}") String basDd) {


        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("basDd",basDd);

        return new OpenAPIItemReader<>(StockTrade.class, params,"KRX",PATH, DataType.DATA_JSON);
    }

    //배치처리
    @Bean
    public ItemProcessor<StockTrade, StockTrade> stockPriceProcessor(){
        return new ItemProcessor<>() {
            @Override
            public StockTrade process(StockTrade item) throws Exception {

                return item;
            }
        };
    }


    //쓰기
    @Bean
    @StepScope
    public JpaItemWriter<StockTrade> stockPriceWriter(){

        JpaItemWriter<StockTrade> writer = new JpaItemWriter<StockTrade>();
        log.info("트랜잭션활성화: {}", TransactionSynchronizationManager.isActualTransactionActive());
        // 안전하게 EntityManagerFactory를 설정하여 트랜잭션이 바인딩되도록 합니다.
        writer.setEntityManagerFactory(entityManagerFactory);
        // 💡 참고: JpaItemWriter는 merge() 전략을 기본으로 사용합니다.
        return writer;
    }
}
