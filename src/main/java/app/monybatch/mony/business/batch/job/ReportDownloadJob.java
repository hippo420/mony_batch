package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.ReportWebReader;
import app.monybatch.mony.business.batch.service.GeminiApiClient;
import app.monybatch.mony.business.batch.writer.ReportFileWriter;
import app.monybatch.mony.business.entity.report.ReportDto;
import app.monybatch.mony.business.repository.jpa.ReportRepository;
import app.monybatch.mony.system.utils.MinioUtil;
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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportDownloadJob {
    private final GeminiApiClient geminiApiClient;
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final MinioUtil minioUtil;
    private final PlatformTransactionManager batchTransactionManager;
    private final ReportRepository reportRepository;
    @Bean
    public DescriptiveJob reportCollectionJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{"basDd"}); // 기준일

        Job job = new JobBuilder("reportCollectionJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(downloadReportStep())
                .build();

        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "증권사 리포트 자동 수집 및 파일 저장 배치");
    }

    @Bean
    public Step downloadReportStep() {
        return new StepBuilder("downloadReportStep", jobRepository)
                .<List<ReportDto>, List<ReportDto>> chunk(1, batchTransactionManager) // 페이지 단위 처리
                .reader(reportWebReader(null))
                .writer(reportFileWriter(minioUtil))
                .transactionManager(batchTransactionManager)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<List<ReportDto>> reportWebReader(@Value("#{jobParameters['basDd']}") String basDd) {
        // 기본적으로 1페이지만 읽도록 설정 (필요시 파라미터화 가능)
        return new ReportWebReader(basDd);
    }

    @Bean
    @StepScope
    public ItemWriter<List<ReportDto>> reportFileWriter(MinioUtil minioUtil) {
        return new ReportFileWriter(minioUtil,reportRepository);
    }


}
