package app.monybatch.mony.batch.stock.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockItemJobConfig {

    private final JobRepository jobRepository;

    private final Step truncateStockTempStep;
    private final Step prebatchStep;
    private final Step markAllDelistStep;
    private final Step kospiBatchStep;
    private final Step kosdaqBatchStep;
    private final Step konexBatchStep;
    private final Step dartMappingStep;

    @Bean
    public Job stockItemJob() {

        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{"basDd"});
        validator.setOptionalKeys(new String[]{"param1","param2"});

        return new JobBuilder("stockItemJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(truncateStockTempStep)   // info_stock_temp 초기화
                .next(prebatchStep)             // info_stock 전체 → info_stock_temp
                .next(markAllDelistStep)        // info_stock 전체 DELIST_YN=Y 선마킹 (완전 상폐 검출용)
                .next(kospiBatchStep)           // KOSPI API → 대사 → info_stock (생존 종목 N/관리종목 Y)
                .next(kosdaqBatchStep)          // KOSDAQ API → 대사 → info_stock
                .next(konexBatchStep)           // KONEX API → 대사 → info_stock
                .next(dartMappingStep)          // DART 데이터 보완
                .build();
    }
}
