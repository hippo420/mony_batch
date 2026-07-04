package app.monybatch.mony.batch.news.job;

import app.monybatch.mony.batch.news.parser.NewsParserFactory;
import app.monybatch.mony.batch.news.processor.NewsRuleFilterProcessor;
import app.monybatch.mony.batch.news.reader.CompositeNewsReader;
import app.monybatch.mony.batch.news.reader.RssItemReader;
import app.monybatch.mony.batch.news.writer.NewsItemWriter;
import app.monybatch.mony.batch.support.parameter.DescriptiveJob;
import app.monybatch.mony.batch.support.parameter.JobParamSpec;
import app.monybatch.mony.common.core.utils.HashUtil;
import app.monybatch.mony.domian.news.entity.NewsDto;
import app.monybatch.mony.domian.news.entity.NewsRss;
import app.monybatch.mony.domian.news.repository.NewsRSSRepository;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticle;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticleRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
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
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    private final NewsArticleRepository newsRepository;
    private final NewsRSSRepository newsRssRepository;
    private final PlatformTransactionManager batchTransactionManager;
    private final OllamaModelClient ollamaModelClient;
    private final VectorStore vectorStore; // VectorStore 주입 추가
    private final NewsParserFactory parserFactory; // VectorStore 주입 추가

    // 실행 주기는 모니터링 화면의 스케줄 설정(batch_schedule_config)에서 관리 — DynamicBatchScheduler 참고

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
        return new DescriptiveJob(job, "RSS 뉴스 수집 및 AI 요약 배치", List.of(
                new JobParamSpec("basDd", "기준일자 (빈값이면 오늘)", "yyyyMMdd", "", false)
        ));
    }

    // --- Step 정의 ---
    @Bean
    public Step batchNewsStep() {
        return new StepBuilder("batchNewsStep", jobRepository)
                .<NewsDto, NewsArticle>chunk(10, batchTransactionManager) // Chunk size 10
                .reader(newsRssReader()) // RSS Reader 연결
                .processor(compositeNewsProcessor()) // Composite Processor 연결
                .writer(newsArticleWriter()) // Writer 연결
                .transactionManager(batchTransactionManager)
                .build();
    }

    // --- ItemReader (RSS Feed 호출) ---
    @Bean
    @StepScope
    public ItemReader<NewsDto> newsRssReader() {
        List<ItemReader<NewsDto>> readers = new ArrayList<>();

        List<NewsRss> url = newsRssRepository.findByUseYn("Y");

        for (NewsRss item : url) {
            //log.info("RSS: {}",item.getLink());
            readers.add(new RssItemReader(item,parserFactory));
        }


        return new CompositeNewsReader(readers);
    }

    // --- Composite ItemProcessor (필터링 -> 변환) ---
    @Bean
    @StepScope
    public ItemProcessor<NewsDto, NewsArticle> compositeNewsProcessor() {
        CompositeItemProcessor<NewsDto, NewsArticle> processor = new CompositeItemProcessor<>();
        List<ItemProcessor<?, ?>> delegates = new ArrayList<>();

        delegates.add(newsRuleFilterProcessor());
        delegates.add(newsDtoToArticleProcessor());

        processor.setDelegates(delegates);
        return processor;
    }

    @Bean
    @StepScope
    public ItemProcessor<NewsDto, NewsDto> newsRuleFilterProcessor() {
        return new NewsRuleFilterProcessor();
    }

    // --- ItemProcessor (변환) ---
    @Bean
    @StepScope
    public ItemProcessor<NewsDto, NewsArticle> newsDtoToArticleProcessor() {
        return new ItemProcessor<>() {
            @Override
            public NewsArticle process(NewsDto item) throws Exception {
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
        return new NewsItemWriter(); // vectorStore 주입
    }
}
