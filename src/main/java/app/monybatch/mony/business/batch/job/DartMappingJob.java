package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.OpenAPIReader;
import app.monybatch.mony.business.entity.Stock;
import app.monybatch.mony.business.entity.dart.DartMapping;
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
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Configuration
@AllArgsConstructor
public class DartMappingJob {
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final StockRepository stockRepository;
    private final String PATH = "/api/corpCode.xml";

    @Qualifier("batchEntityManager")
    private EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager batchTransactionManager;

    @Bean
    public DescriptiveJob dartJob() throws DuplicateJobException {

        Job job = new JobBuilder("dartJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(readDartDataStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "KRX-DART 매핑 배치 처리");
    }


    //실제 배치처리
    @Bean
    public Step readDartDataStep() {
        List<Stock> stocks = stockRepository.findAll();
        return new StepBuilder("readDartDataStep",jobRepository)
                .<DartMapping, Stock> chunk(100,batchTransactionManager)
                .reader(dartApiReader())
                .processor(mappingProcessor(stocks))
                .writer(dartMergeWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIReader<DartMapping> dartApiReader() {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        return new OpenAPIReader<>(DartMapping.class, params,"DART",PATH, DataType.DATA_ZIP);
    }

    //배치처리
    @Bean
    public ItemProcessor<DartMapping, Stock> mappingProcessor(List<Stock> stocks) {

        return new ItemProcessor<>() {
            @Override
            public Stock process(DartMapping item) throws Exception {
                    if(StringUtils.isEmpty(item.getStock_code()))
                        return null;

                    for(Stock stock : stocks) {
                        if(item.getStock_code().equals(stock.getISU_SRT_CD()))
                        {
                            stock.setCORP_CODE(item.getCorp_code());
                            log.info("DART-KRX 매핑완료 : {}",stock);
                            return stock;
                        }
                    }
                return null;
            }
        };
    }


    //쓰기
    @Bean
    @StepScope
    public JpaItemWriter<Stock> dartMergeWriter(){

        JpaItemWriter<Stock> writer = new JpaItemWriter<Stock>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
