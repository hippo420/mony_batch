package app.monybatch.mony.business.batch;


import app.monybatch.mony.business.batch.reader.ExcelRowReader;
import app.monybatch.mony.business.entity.sample.ExcelEntity;
import app.monybatch.mony.business.repository.jpa.ExcelRepository;
import app.monybatch.mony.system.core.constant.BatchConstant;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;

@Configuration
@AllArgsConstructor
public class FiletoDBBatch {

    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;
    private ExcelRepository repository;

    @Bean
    public Job excelJob(){
        return new JobBuilder("excelJob",jobRepository)
                .start(excelStep())
                .build();
    }

    @Bean
    public Step excelStep(){
        return new StepBuilder("excelStep",jobRepository)
                .<Row, ExcelEntity>chunk(10,transactionManager)
                .reader(excelReader())
                .processor(excelProcessor())
                .writer(excelWriter())
                .build();
    }

    @Bean
    public ItemReader<Row> excelReader(){
        return new ExcelRowReader(BatchConstant.EXCEL_TO_DB_FILEPATH,1);
    }

    @Bean
    public ItemProcessor<Row,ExcelEntity> excelProcessor(){
        return new ItemProcessor<Row, ExcelEntity>() {
            @Override
            public ExcelEntity process(Row item) throws Exception {
                ExcelEntity entity = new ExcelEntity();
                entity.setIdxNm(item.getCell(0).getStringCellValue());
                entity.setClosePrice(BigDecimal.valueOf(item.getCell(1).getNumericCellValue()));
                entity.setComparison(BigDecimal.valueOf(item.getCell(2).getNumericCellValue()));
                entity.setFRate(BigDecimal.valueOf(item.getCell(3).getNumericCellValue()));
                entity.setOpenPrice(BigDecimal.valueOf(item.getCell(4).getNumericCellValue()));
                entity.setUpperPrice(BigDecimal.valueOf(item.getCell(5).getNumericCellValue()));
                entity.setLowerPrice(BigDecimal.valueOf(item.getCell(6).getNumericCellValue()));
                entity.setVolume(BigDecimal.valueOf(item.getCell(7).getNumericCellValue()));
                entity.setTranPrice(BigDecimal.valueOf(item.getCell(8).getNumericCellValue()));
                entity.setMktCapital(BigDecimal.valueOf(item.getCell(9).getNumericCellValue()));

                return entity;
            }
        };
    }

    @Bean
    public ItemWriter<ExcelEntity> excelWriter(){
        return new RepositoryItemWriterBuilder<ExcelEntity>()
                .repository(repository)
                .methodName("save")
                .build();
    }


}
