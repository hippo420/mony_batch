package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.OpenAPIReader;
import app.monybatch.mony.business.batch.writer.CustomJPAWriter;
import app.monybatch.mony.business.entity.Stock;
import app.monybatch.mony.business.entity.StockTemp;
import app.monybatch.mony.business.repository.jpa.StockRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

import static app.monybatch.mony.business.constant.ColumnConst.BASDD;

@Configuration
@AllArgsConstructor
public class StockItemJob {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StockRepository stockRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public DescriptiveJob itemJob() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(Collections.singletonList("basDd").toArray(new String[0]));
        validator.setOptionalKeys(new String[] { "param1","param2" });

        Job job = new JobBuilder("itemJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(prebatchStep())
                .next(batchStep())
                .build();

        return new DescriptiveJob(job, "주식종목 동기화배치 처리");
    }


    //실제 배치처리
    @Bean
    public Step prebatchStep() {

        return new StepBuilder("prebatchStep",jobRepository)
                .<StockTemp, StockTemp> chunk(100,transactionManager)
                .reader(stockTempApiReader(null))
                .processor(stockTempProcessor())
                .writer(stockTempWriter())
                .build();
    }

    //실제 배치처리
    @Bean
    public Step batchStep() {

        return new StepBuilder("batchStep",jobRepository)
                .<Stock, Stock> chunk(100,transactionManager)
                .reader(stockApiReader(null))
                .processor(stockProcessor())
                .writer(stockWriter())
                .build();
    }

    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIReader<StockTemp> stockTempApiReader(@Value("#{jobParameters['basDd']}") String basDd) {


        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add(BASDD,basDd);

        return new OpenAPIReader<>(StockTemp.class, params,"KRX");
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIReader<Stock> stockApiReader(@Value("#{jobParameters['basDd']}") String basDd) {


        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add(BASDD,basDd);

        return new OpenAPIReader<>(Stock.class, params,"KRX");
    }

    //배치처리
    @Bean
    public ItemProcessor<Stock, Stock> stockProcessor(){
        return new ItemProcessor<>() {
            @Override
            public Stock process(Stock item) throws Exception {

                return item;
            }
        };
    }

    //배치처리
    @Bean
    public ItemProcessor<StockTemp, StockTemp> stockTempProcessor(){
        return new ItemProcessor<>() {
            @Override
            public StockTemp process(StockTemp item) throws Exception {

                return item;
            }
        };
    }

    //쓰기
    @Bean
    @StepScope
    public CustomJPAWriter<Stock> stockWriter(){

        return new CustomJPAWriter<>(entityManager);
    }

    //쓰기
    @Bean
    @StepScope
    public CustomJPAWriter<StockTemp> stockTempWriter(){

        return new CustomJPAWriter<>(entityManager);
    }
}
