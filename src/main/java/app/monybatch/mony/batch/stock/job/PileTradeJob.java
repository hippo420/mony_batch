package app.monybatch.mony.batch.stock.job;

import app.monybatch.mony.batch.stock.partitioner.StockPartitioner;
import app.monybatch.mony.batch.stock.processor.PileTradeProcessor;
import app.monybatch.mony.batch.stock.tasklet.CalculateSectorTradeTasklet;
import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.ratelimit.KisApiRateLimiter;
import app.monybatch.mony.batch.support.reader.CompositeItemReader;
import app.monybatch.mony.batch.support.reader.OpenAPIItemReader;
import app.monybatch.mony.common.constant.DataType;
import app.monybatch.mony.common.constant.StockConstant;
import app.monybatch.mony.domian.stock.dto.InvestorTradeRaw;
import app.monybatch.mony.domian.stock.entity.Stock;
import app.monybatch.mony.domian.stock.repository.StockRepository;
import app.monybatch.mony.domian.trade.entity.Trade;
import app.monybatch.mony.domian.trade.repository.SectorTradeRepository;
import app.monybatch.mony.domian.trade.repository.TradeRepository;
import app.monybatch.mony.system.token.KisTokenManager;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
public class PileTradeJob {

    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final StockRepository stockRepository;
    private final TradeRepository tradeRepository;
    private final SectorTradeRepository sectorTradeRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final PlatformTransactionManager batchTransactionManager;
    private final KisTokenManager tokenManager;
    private final KisApiRateLimiter kisApiRateLimiter;

    private static final String PATH = "/uapi/domestic-stock/v1/quotations/investor-trade-by-stock-daily";
    private static final int GRID_SIZE = 5;

    // ─── Job ────────────────────────────────────────────────────────────────

    @Bean
    public DescriptiveJob pileTradeJobDef() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(Collections.singletonList("basDd").toArray(new String[0]));
        validator.setOptionalKeys(new String[]{"forced_id"});

        Job job = new JobBuilder("pileTradeJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                //.start(pileTradePartitionMasterStep())
                .start(calculateSectorTradeVolumeStep(batchTransactionManager))
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "종목별 투자자 매매동향 배치 처리 (pile_trade)");
    }

    // ─── Master Step (Partition) ─────────────────────────────────────────────
    @Bean
    public Step pileTradePartitionMasterStep() {
        return new StepBuilder("pileTradePartitionMasterStep", jobRepository)
                .partitioner("pileTradeSlaveStep", pileTradeStockPartitioner())
                .step(pileTradeSlaveStep())
                .taskExecutor(pileTradeTaskExecutor())
                .gridSize(GRID_SIZE)
                .build();
    }

    @Bean
    public TaskExecutor pileTradeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(GRID_SIZE);
        executor.setMaxPoolSize(GRID_SIZE);
        executor.setQueueCapacity(0);
        executor.setThreadNamePrefix("pile-trade-");
        executor.initialize();
        return executor;
    }

    @Bean
    public StockPartitioner pileTradeStockPartitioner() {
        return new StockPartitioner(stockRepository);
    }

    // ─── Slave Step ──────────────────────────────────────────────────────────

    @Bean
    public Step pileTradeSlaveStep() {
        return new StepBuilder("pileTradeSlaveStep", jobRepository)
                .<InvestorTradeRaw, Trade>chunk(100, batchTransactionManager)
                .reader(pileTradePartitionReader(null, null, null))
                .processor(pileTradeProcessor())
                .writer(pileTradeWriter())
                .transactionManager(batchTransactionManager)
                .build();
    }

    // ─── Reader (파티션별 종목 슬라이스) ───────────────────────────────────────

    @Bean
    @StepScope
    public ItemReader<InvestorTradeRaw> pileTradePartitionReader(
            @Value("#{jobParameters['basDd']}") String basDd,
            @Value("#{stepExecutionContext['startIndex']}") Integer startIndex,
            @Value("#{stepExecutionContext['endIndex']}") Integer endIndex) {

        ConcurrentHashMap<String, String> headers = new ConcurrentHashMap<>();
        headers.put("content-type", "application/json; charset=utf-8");
        headers.put("authorization", tokenManager.getAccessToken());
        headers.put("appkey", StockConstant.KIS_API_KEY);
        headers.put("appsecret", StockConstant.KIS_API_SECRET);
        headers.put("tr_id", "FHPTJ04160001");
        headers.put("custtype", "P");

        MultiValueMap<String, String> baseParams = new LinkedMultiValueMap<>();
        baseParams.add("FID_INPUT_DATE_1", basDd);
        baseParams.add("FID_ORG_ADJ_PRC", "");
        baseParams.add("FID_ETC_CLS_CODE", "1");

        List<Stock> partition = stockRepository.findKospiKosdaqStocks().subList(startIndex, endIndex);
        log.info("파티션 [{}-{}] : {}건 처리 시작", startIndex, endIndex, partition.size());

        List<ItemReader<InvestorTradeRaw>> readerList = new ArrayList<>();
        for (Stock stock : partition) {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>(baseParams);
            params.add("FID_COND_MRKT_DIV_CODE", "J");
            params.add("FID_INPUT_ISCD", stock.getISU_SRT_CD());
            // KisApiRateLimiter를 공유 — 모든 파티션의 API 호출이 합산 10건/초 이내로 제어됨
            log.info("[{}] 조회",stock.getISU_SRT_CD());
            readerList.add(new OpenAPIItemReader<>(InvestorTradeRaw.class, params, "KIS", PATH,
                    DataType.DATA_JSON, headers, stock.getISU_SRT_CD(), kisApiRateLimiter));
        }

        return new CompositeItemReader<>(readerList);
    }

    @Bean
    public Step calculateSectorTradeVolumeStep(
            @Qualifier("transactionManager") PlatformTransactionManager metaTransactionManager) {
        return new StepBuilder("calculateSectorTradeVolumeStep", jobRepository)
                .tasklet(calculateSectorTradeTasklet(null), metaTransactionManager)
                .build();
    }

    @Bean
    @StepScope
    public CalculateSectorTradeTasklet calculateSectorTradeTasklet(
            @Value("#{jobParameters['basDd']}") String basDd) {
        return new CalculateSectorTradeTasklet(basDd, tradeRepository, sectorTradeRepository);
    }

    // ─── Processor ───────────────────────────────────────────────────────────

    @Bean
    @StepScope
    public ItemProcessor<InvestorTradeRaw, Trade> pileTradeProcessor() {
        PileTradeProcessor delegate = new PileTradeProcessor(tradeRepository);
        return delegate::processInternal;
    }

    // ─── Writer ──────────────────────────────────────────────────────────────

    @Bean
    @StepScope
    public JpaItemWriter<Trade> pileTradeWriter() {
        JpaItemWriter<Trade> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }
}
