package app.monybatch.mony.batch.stock.job;

import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.domian.stock.dto.InvestorTradeRaw;
import app.monybatch.mony.batch.stock.processor.InvestorTradeProcessor;
import app.monybatch.mony.batch.support.reader.CompositeItemReader;
import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.domian.stock.entity.InvestorTradeFeature;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.repository.StockRepository;
import app.monybatch.mony.system.token.KisTokenManager;
import app.monybatch.mony.common.constant.DataType;
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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InvestorTradeJob {

    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;

    private final StockRepository stockRepository;
    private final String PATH = "/uapi/domestic-stock/v1/quotations/investor-trade-by-stock-daily";
    private final String Path1 = "https://kind.krx.co.kr/corpgeneral/corpList.do";

    //@Qualifier("batchEntityManager")
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager batchTransactionManager;
    private final KisTokenManager tokenManager;

    @Bean
    public DescriptiveJob investorTradeInfoJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(Collections.singletonList("basDd").toArray(new String[0]));
        validator.setOptionalKeys(new String[] { "param1","param2","forced_id" });

        Job job = new JobBuilder("investorTradeInfoJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(batchInvestorTradeStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "기관별_외국인 투자동향 배치 처리");
    }


    //실제 배치처리
    @Bean
    public Step batchInvestorTradeStep() {

        return new StepBuilder("batchInvestorTradeStep",jobRepository)
                .<InvestorTradeRaw, InvestorTradeFeature> chunk(100, batchTransactionManager)
                .reader(investorTradeApiReader(null))
                .processor(investorTradeProcessor(null))
                .writer(investorTradeWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }




    //데이터 읽기
    @Bean
    @StepScope
    public ItemReader<InvestorTradeRaw> investorTradeApiReader(@Value("#{jobParameters['basDd']}") String basDd) {

        OpenAPIItemReader<InvestorTradeRaw> reader;
        List<ItemReader<InvestorTradeRaw>> list = new ArrayList<>();

        ConcurrentHashMap<String,String> headers = new ConcurrentHashMap<>();
        headers.put("content-type","application/json; charset=utf-8");
        headers.put("authorization",tokenManager.getAccessToken());
        headers.put("appkey", StockConstant.KIS_API_KEY);
        headers.put("appsecret",StockConstant.KIS_API_SECRET);
        headers.put("tr_id","FHPTJ04160001");
        headers.put("custtype","P");

        // 첫 번째 Reader: FID_DIV_CLS_CODE = "1"
        MultiValueMap<String,String> params1 = new LinkedMultiValueMap<>();
        params1.add("FID_COND_MRKT_DIV_CODE","J");
        params1.add("FID_INPUT_DATE_1",basDd);
        params1.add("FID_ORG_ADJ_PRC","");
        params1.add("FID_ETC_CLS_CODE","");
        //params1.add("FID_INPUT_ISCD","001070");


        List<Stock> stocks = stockRepository.findAll();
        log.info("종목수: {}",stocks.size());

        for (int i = 0; i < stocks.size(); i++) {
            // 파라미터 복사하여 사용 (안전성 확보)
            MultiValueMap<String, String> currentParams = new LinkedMultiValueMap<>(params1);
            currentParams.add("FID_INPUT_ISCD", stocks.get(i).getISU_SRT_CD());
            
            // OpenAPIItemReader 생성자에 마지막 인자로 종목코드를 넘기고 있음. 
            // OpenAPIItemReader에 해당 생성자가 있는지 확인 필요하지만, 일단 사용자 코드 유지.
            // 만약 생성자가 없다면 컴파일 에러 발생할 것임.
            //log.info("종목조회 : {}",stocks.get(i).getISU_SRT_CD());
            reader  = new OpenAPIItemReader<>(InvestorTradeRaw.class, currentParams,"KIS",PATH, DataType.DATA_JSON,headers,stocks.get(i).getISU_SRT_CD());

            list.add(reader);
        }


        return new CompositeItemReader<>(list);
    }

    //배치처리
    @Bean
    @StepScope
    public ItemProcessor<InvestorTradeRaw, InvestorTradeFeature> investorTradeProcessor(@Value("#{jobParameters['basDd']}") String basDd) {

        InvestorTradeProcessor delegate = new InvestorTradeProcessor(basDd);

        return new ItemProcessor<>() {
            @Override
            public InvestorTradeFeature process(InvestorTradeRaw item) {
                return delegate.processInternal(item);
            }
        };
    }


    //쓰기
    @Bean
    @StepScope
    public JpaItemWriter<InvestorTradeFeature> investorTradeWriter(){

        JpaItemWriter<InvestorTradeFeature> writer = new JpaItemWriter<InvestorTradeFeature>();
        //log.info("트랜잭션활성화: {}", TransactionSynchronizationManager.isActualTransactionActive());
        // 안전하게 EntityManagerFactory를 설정하여 트랜잭션이 바인딩되도록 합니다.
        writer.setEntityManagerFactory(entityManagerFactory);
        // 💡 참고: JpaItemWriter는 merge() 전략을 기본으로 사용합니다.
        return writer;
    }
}
