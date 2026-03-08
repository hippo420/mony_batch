package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.CompositeNewsReader;
import app.monybatch.mony.business.batch.reader.RssItemReader;
import app.monybatch.mony.business.batch.service.OllamaModelClient;
import app.monybatch.mony.business.batch.writer.NewsItemWriter;
import app.monybatch.mony.business.entity.news.News;
import app.monybatch.mony.business.entity.news.NewsArticle;
import app.monybatch.mony.business.entity.news.NewsRss;
import app.monybatch.mony.business.repository.es.NewsArticleRepository;
import app.monybatch.mony.business.repository.jpa.NewsRSSRepository;
import app.monybatch.mony.system.utils.DateUtil;
import app.monybatch.mony.system.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NewsJob {

    // --- 주입되는 컴포넌트 ---
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final JobLauncher jobLauncher; // JobLauncher 주입
    private final NewsArticleRepository newsRepository;
    private final NewsRSSRepository newsRssRepository;
    private final PlatformTransactionManager batchTransactionManager;
    private final OllamaModelClient ollamaModelClient;

    // --- 스케줄러 (5분마다 실행) ---
    @Scheduled(cron = "0 */5 * * * *") // 5분마다 실행
    public void runNewsCollectionJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("basDd", DateUtil.getDateYmd())
                    .addLong("time", System.currentTimeMillis()) // JobInstance를 다르게 하기 위해 현재 시간을 파라미터로 추가
                    .toJobParameters();
            jobLauncher.run(newsCollectionJob().getJob(), jobParameters);
        } catch (Exception e) {
            log.error("Error running newsCollectionJob", e);
        }
    }


    // --- Job 정의 ---
    @Bean
    public DescriptiveJob newsCollectionJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        // required: basDd (기준일)
        validator.setRequiredKeys(new String[]{"basDd"});
        validator.setOptionalKeys(new String[]{"time"}); // 'time' 파라미터 추가

        Job job = new JobBuilder("newsCollectionJob", jobRepository)
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(batchNewsStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "RSS 뉴스 수집 및 AI 요약 배치");
    }

    // --- Step 정의 ---
    @Bean
    public Step batchNewsStep() {
        return new StepBuilder("batchNewsStep", jobRepository)
                .<News, NewsArticle>chunk(10, batchTransactionManager) // Chunk size 10
                .reader(newsRssReader()) // RSS Reader 연결
                .processor(newsArticleProcessor()) // Processor 연결
                .writer(newsArticleWriter()) // Writer 연결
                .transactionManager(batchTransactionManager)
                .build();
    }

    // --- ItemReader (RSS Feed 호출) ---
    @Bean
    @StepScope
    public ItemReader<News> newsRssReader() {
        List<ItemReader<News>> readers = new ArrayList<>();

        List<NewsRss> url = newsRssRepository.findByUseYn("Y");

        for (NewsRss item : url) {
            log.info("RSS: {}",item.getLink());
            readers.add(new RssItemReader(item));
        }


        return new CompositeNewsReader(readers);
    }

    // --- ItemProcessor (AI 요약 및 변환) ---
    @Bean
    @StepScope
    public ItemProcessor<News, NewsArticle> newsArticleProcessor() {
        return new ItemProcessor<>() {
            @Override
            public NewsArticle process(News item) throws Exception {
                //log.info("Processing news: {}", item.getTitle());

                // TODO: DB 조회 로직 추가 (이미 수집된 뉴스인지 확인)
                // if (newsArticleRepository.existsByOriginallink(item.getOriginallink())) {
                //     return null; // 이미 존재하면 건너뜀
                // }

                // 임시로 데이터 매핑만 수행
                NewsArticle news = new NewsArticle();
                news.setId(HashUtil.generateMD5Hash(item.getOriginallink()));
                news.setTitle(item.getTitle());
                news.setPublishedDate(item.getPubDate());
                news.setContent(item.getDescription()); // 요약 전 원문(또는 description)
                news.setCategory(item.getCategory());
                news.setCompany(item.getCompany());
                news.setLink(item.getOriginallink());



                return news;
            }
        };
    }

    // --- ItemWriter (Elasticsearch 저장) ---
    @Bean
    @StepScope
    public NewsItemWriter newsArticleWriter() {
        return new NewsItemWriter(newsRepository,ollamaModelClient);
    }
}
