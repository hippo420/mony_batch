package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.CompositeNewsReader;
import app.monybatch.mony.business.batch.reader.RssItemReader;
import app.monybatch.mony.business.batch.service.GeminiApiClient;
import app.monybatch.mony.business.batch.writer.NewsElasticsearchItemWriter;
import app.monybatch.mony.business.entity.news.News;
import app.monybatch.mony.business.entity.news.NewsArticle;
import app.monybatch.mony.business.repository.es.NewsArticleRepository;
import app.monybatch.mony.system.utils.DateUtil;
import app.monybatch.mony.system.utils.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
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
    private final NewsArticleRepository newsArticleRepository;
    private final PlatformTransactionManager batchTransactionManager;
    private final GeminiApiClient geminiApiClient;

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

        // 구글 뉴스
        //readers.add(new RssItemReader("https://news.google.com/rss?hl=ko&gl=KR&ceid=KR:ko"));

        // JTBC
        readers.add(new RssItemReader("https://news-ex.jtbc.co.kr/v1/get/rss/newsflesh"));
        readers.add(new RssItemReader("https://news-ex.jtbc.co.kr/v1/get/rss/program/NG10000013"));
        readers.add(new RssItemReader("https://news-ex.jtbc.co.kr/v1/get/rss/section/economy"));
        readers.add(new RssItemReader("https://news-ex.jtbc.co.kr/v1/get/rss/section/international"));
        readers.add(new RssItemReader("https://news-ex.jtbc.co.kr/v1/get/rss/section/society"));

        // 연합뉴스
        readers.add(new RssItemReader("https://www.yna.co.kr/rss/economy.xml"));
        readers.add(new RssItemReader("https://www.yna.co.kr/rss/society.xml"));
        readers.add(new RssItemReader("https://www.yna.co.kr/rss/international.xml"));

        // SBS 뉴스
        readers.add(new RssItemReader("https://news.sbs.co.kr/news/headlineRssFeed.do?plink=RSSREADER"));
        readers.add(new RssItemReader("https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=02&plink=RSSREADER"));

        // 더벨
        readers.add(new RssItemReader("https://www.thevaluenews.co.kr/rss_view.php?code=m6481nr"));
        readers.add(new RssItemReader("https://www.thevaluenews.co.kr/rss_view.php?code=m76i0t2"));
        readers.add(new RssItemReader("https://www.thevaluenews.co.kr/rss_view.php?code=m71n1f6"));

        // 한국경제
        readers.add(new RssItemReader("https://help.wowtv.co.kr/serviceinfo/newsstand/FeedRss/stock"));
        readers.add(new RssItemReader("https://help.wowtv.co.kr/serviceinfo/newsstand/FeedRss/init"));
        readers.add(new RssItemReader("https://help.wowtv.co.kr/serviceinfo/newsstand/FeedRss/politics"));

        return new CompositeNewsReader(readers);
    }

    // --- ItemProcessor (AI 요약 및 변환) ---
    @Bean
    @StepScope
    public ItemProcessor<News, NewsArticle> newsArticleProcessor() {
        return new ItemProcessor<>() {
            @Override
            public NewsArticle process(News item) throws Exception {
                log.info("Processing news: {}", item.getTitle());

                // TODO: DB 조회 로직 추가 (이미 수집된 뉴스인지 확인)
                // if (newsArticleRepository.existsByOriginallink(item.getOriginallink())) {
                //     return null; // 이미 존재하면 건너뜀
                // }

                // AI 요약 요청 (단건 처리로 변경됨에 따라 로직 수정 필요할 수 있음)
                // 현재 구조상 Chunk 단위로 묶어서 AI에 보내는 것이 효율적일 수 있으나,
                // 여기서는 개별 처리를 가정하고 TODO로 남김.
                // 실제로는 Processor에서 모아서 처리하거나, Writer에서 처리하는 방식을 고려해야 함.
                
                // 임시로 데이터 매핑만 수행
                NewsArticle newsArticle = new NewsArticle();
                newsArticle.setId(HashUtil.generateMD5Hash(item.getOriginallink()));
                newsArticle.setTitle(item.getTitle());
                newsArticle.setPublishedDate(item.getPubDate());
                newsArticle.setContent(item.getDescription()); // 요약 전 원문(또는 description)
                if(item.getDescription().contains("속보"))
                {
                    newsArticle.setCategory("속보");
                }
                else {
                    newsArticle.setCategory("일반");
                }
                // TODO: Gemini API 호출하여 요약, 감정분석, 키워드 추출 수행
                // String result = geminiApiClient.requestSummaryAndSentiment(List.of(item));
                // ... 파싱 및 매핑 로직 ...

                return newsArticle;
            }
        };
    }

    // --- ItemWriter (Elasticsearch 저장) ---
    @Bean
    @StepScope
    public NewsElasticsearchItemWriter newsArticleWriter() {
        return new NewsElasticsearchItemWriter(newsArticleRepository);
    }
}
