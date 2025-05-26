package app.monybatch.mony.business.batch;

import app.monybatch.mony.business.entity.sample.AsIsEntity;
import app.monybatch.mony.business.entity.sample.TobeEntity;
import app.monybatch.mony.business.repository.jpa.AsIsRepository;
import app.monybatch.mony.business.repository.jpa.TobeRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
@AllArgsConstructor
public class MigBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AsIsRepository asIsRepository;
    private final TobeRepository tobeRepository;

    @Bean
    public Job migBatchJob() {
        return new JobBuilder("migBatchJob",jobRepository)
                .start(migBatchStep())
                .build();
    }

    //실제 배치처리
    @Bean
    public Step migBatchStep() {
        return new StepBuilder("migBatchStep",jobRepository)
                .<AsIsEntity, TobeEntity> chunk(100,transactionManager)
                .reader(asisReader())
                .processor(migProcessor())
                .writer(tobeWriter())
                .build();
    }

    //데이터 읽기
    @Bean
    public RepositoryItemReader<AsIsEntity> asisReader(){
        return new RepositoryItemReaderBuilder<AsIsEntity>()
                .name("asisReader")
                .pageSize(100)
                .methodName("findAll")
                .repository(asIsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    //배치처리
    @Bean
    public ItemProcessor<AsIsEntity, TobeEntity> migProcessor(){
        return new ItemProcessor<AsIsEntity, TobeEntity>() {
            @Override
            public TobeEntity process(AsIsEntity item) throws Exception {
                TobeEntity tobeEntity = new TobeEntity();
                tobeEntity.setItem(item.getItem());
                tobeEntity.setItemNm(item.getItemNm()+"_이행");
                tobeEntity.setPrice(item.getPrice());
                return tobeEntity;
            }
        };
    }

    //쓰기
    @Bean
    public RepositoryItemWriter<TobeEntity> tobeWriter(){

        return new RepositoryItemWriterBuilder<TobeEntity>()
                .repository(tobeRepository)
                .methodName("save")
                .build();
    }
}
