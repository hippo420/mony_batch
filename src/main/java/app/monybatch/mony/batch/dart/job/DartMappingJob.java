package app.monybatch.mony.batch.dart.job;

import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.domian.dart.DartCorpInfo;
import app.monybatch.mony.domian.dart.DartMapping;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.repository.StockRepository;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@AllArgsConstructor
public class DartMappingJob {
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final StockRepository stockRepository;

    private final String PATH = "/api/corpCode.xml";
    private final String PATH1 = "/api/company.json";

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
                .processor(mappingProcessor())
                .writer(dartMergeWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }


    //데이터 읽기
    @Bean
    @StepScope
    public OpenAPIItemReader<DartMapping> dartApiReader() {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        return new OpenAPIItemReader<>(DartMapping.class, params,"DART",PATH, DataType.DATA_ZIP);
    }

    //배치처리
    @Bean
    public ItemProcessor<DartMapping, Stock> mappingProcessor()  {

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();

        List<Stock> stocks = stockRepository.findAll();

        List<DartCorpInfo> infos = new ArrayList<>();

        OpenAPIItemReader<DartCorpInfo> api  = new OpenAPIItemReader(DartCorpInfo.class,params,"DART",PATH1,DataType.DATA_JSON);
        try{
            infos= Collections.singletonList(api.read());
        }catch (Exception e)
        {
            log.error("다트 수신오류");
        }

        Map<String, Stock> stockMap = stocks.stream()
                .collect(Collectors.toMap(
                        Stock::getISU_SRT_CD,
                        s -> s
                ));

        Map<String, DartCorpInfo> infoMap = infos.stream()
                .collect(Collectors.toMap(
                        DartCorpInfo::getStockCode,
                        s -> s
                ));

        return item -> {

            if(!StringUtils.hasText(item.getStock_code()))
                return null;

            Stock stock = stockMap.get(item.getStock_code());
            DartCorpInfo info = infoMap.get(item.getStock_code());
            if(stock == null)
                return null;
            if(info == null)
                return null;

            stock.setCORP_CODE(item.getCorp_code());
            stock.setIndustryCode(null);
            stock.setBIZR_NO(info.getBizrNo());
            stock.setADRES(info.getAdres());
            stock.setCEO_NM(info.getCeoNm());
            stock.setEST_DT(info.getEstDt());
            stock.setFAX_NO(info.getFaxNo());
            stock.setACC_MT(info.getAccMt());
            stock.setHM_URL(info.getHmUrl());
            stock.setIR_URL(info.getIrUrl());

            return stock;
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
