package app.monybatch.mony.business.batch;

import app.monybatch.mony.business.batch.writer.ExcelRowWriter;
import app.monybatch.mony.business.entity.sample.ExcelEntity;
import app.monybatch.mony.business.repository.jpa.ExcelRepository;
import app.monybatch.mony.system.core.constant.BatchConstant;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@AllArgsConstructor
public class DBtoFileBatch {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private ExcelRepository repository;

    @Bean
    public Job dbJob(){
        return new JobBuilder("dbJob",jobRepository)
                .start(dbStep())
                .build();
    }

    @Bean
    public Step dbStep(){
        return new StepBuilder("dbStep",jobRepository)
                .<ExcelEntity, ExcelEntity>chunk(10,transactionManager)
                .reader(dbReader())
                .processor(dbProcessor())
                .writer(dbWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<ExcelEntity> dbReader(){
        RepositoryItemReader<ExcelEntity>  reader = new RepositoryItemReaderBuilder<ExcelEntity>()
                .name("dbReader")
                .pageSize(100)
                .methodName("findAll")
                .repository(repository)
                .sorts(Map.of("idxNm", Sort.Direction.ASC))
                .build();

        //실패시 처음부터 다시 수행
        reader.setSaveState(false);

        return reader;
    }

    @Bean
    public ItemProcessor<ExcelEntity,ExcelEntity> dbProcessor(){
        return new ItemProcessor<ExcelEntity, ExcelEntity>() {
            @Override
            public ExcelEntity process(ExcelEntity item) throws Exception {
                return item;
            }
        };
    }

    @Bean
    public ItemStreamWriter<ExcelEntity> dbWriter(){
        int size = (ExcelEntity.class).getClass().getDeclaredFields().length;
        return new ExcelRowWriter(BatchConstant.DB_TO_FILE_FILEPATH,1, size);
    }

}
