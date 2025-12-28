package app.monybatch.mony.business.batch.job;

import app.monybatch.mony.business.batch.reader.OpenAPIListReader;
import app.monybatch.mony.business.batch.service.GeminiApiClient;
import app.monybatch.mony.business.batch.writer.NewsElasticsearchItemWriter;
import app.monybatch.mony.business.entity.news.News;
import app.monybatch.mony.business.entity.news.NewsArticle;
import app.monybatch.mony.business.repository.jpa.NewsArticleRepository;
import app.monybatch.mony.system.core.constant.DataType;
import app.monybatch.mony.system.utils.DateUtil;
import app.monybatch.mony.system.utils.HashUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NewsJob {

    // --- 주입되는 컴포넌트 ---
    private final JobRepository jobRepository;
    private final JobRegistry jobRegistry;
    private final NewsArticleRepository newsArticleRepository;
    private final PlatformTransactionManager batchTransactionManager;
    private final GeminiApiClient geminiApiClient;;
    // --- Job 정의 ---
    @Bean
    public DescriptiveJob newsCollectionJob() throws DuplicateJobException {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        // required: basDd (기준일), maxArticlesToSummarize (일일 최대 요약 개수)
        validator.setRequiredKeys(new String[]{"basDd", "keyword"});
        //validator.setOptionalKeys(new String[] { "keyword","param2" });

        Job job = new JobBuilder("newsCollectionJob", jobRepository) // Job 이름 변경
                .validator(validator)
                .incrementer(new RunIdIncrementer())
                .start(batchNewsStep())
                .build();
        jobRegistry.register(new ReferenceJobFactory(job));
        return new DescriptiveJob(job, "AI 요약 뉴스 수집 및 Elasticsearch 저장 배치");
    }

    // --- Step 정의 ---
    @Bean
    public Step batchNewsStep() { // Writer 주입

        return new StepBuilder("batchNewsStep",jobRepository)
                .<List<News>, List<NewsArticle>> chunk(10, batchTransactionManager)
                .reader(newsApiReader(DateUtil.getDateYmd(),"SK하이닉스+반도체+HBM+AI")) // Reader 연결
                .processor(newsArticleProcessor()) // Processor 연결
                .writer(newsArticleWriter()) // Writer 연결
                .transactionManager(batchTransactionManager)
                .build();
    }



    // --- ItemReader (뉴스 API 호출) ---
    @Bean
    @StepScope
    public ItemReader<List<News>> newsApiReader(@Value("#{jobParameters['basDd']}") String basDd,
                                                    @Value("#{jobParameters['keyword'] ?: ''}") String keyword) {

        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();

        params.add("query", "SK하이닉스|반도체|HBM|AI"); // 검색 키워드 파라미터 추가 가정
        params.add("display", "10"); // 검색 키워드 파라미터 추가 가정
        params.add("start", "1"); // 검색 키워드 파라미터 추가 가정
        params.add("sort", "date"); // 검색 키워드 파라미터 추가 가정
        log.info("params - basDd :{}, query: {}", basDd, keyword);
        // return new NewsApiItemReader(NewsArticle.class, params,"NAVER",API_PATH, DataType.DATA_JSON);
        // 실제 구현체는 API를 호출하고 NewsArticle 리스트를 반환해야 합니다.
        return new OpenAPIListReader<>(News.class, params,"NEWS","", DataType.DATA_JSON);
    }

    // --- ItemProcessor (AI 요약 및 토큰 제한) ---
    @Bean
    @StepScope
    public ItemProcessor<List<News>, List<NewsArticle>> newsArticleProcessor(){ // Job Parameter로 제한 개수 주입

        final AtomicInteger processedCount = new AtomicInteger(0);

        return new ItemProcessor<>() {
            @Override
            public List<NewsArticle> process(List<News> item) throws Exception {

                for (News news : item) {
                    log.info("processing news: {}", news);
                }
                List<NewsArticle> insertData = new ArrayList<>();

//                StringBuilder sb = new StringBuilder();
//                for (News news : item) {
//                    sb.append(news.getTitle());
//                    sb.append("|");
//                    sb.append(news.getDescription());
//                    sb.append("|");
//                    sb.append(news.getPubDate());
//                    sb.append("^");
//                }
//
//                String rawData = sb.toString();

                //TODO AI요약
                String result = geminiApiClient.requestSummaryAndSentiment(item);
                String[] nextLine;
                String[] parseData;
                if(result.indexOf("^")>-1)
                {
                    nextLine = result.split("\\^");
                    for (int i = 0; i < nextLine.length; i++) {
                        parseData = nextLine[i].split("\\|");
                        NewsArticle newsArticle = new NewsArticle();
                        newsArticle.setId(HashUtil.generateMD5Hash(item.get(i).getOriginallink()));
                        newsArticle.setPublishedDate(parseData[0]);
                        newsArticle.setTitle(parseData[1]);
                        newsArticle.setContent(parseData[2]);
                        newsArticle.setOpinion(parseData[3]);
                        newsArticle.setKeywords(parseData[4]);
                        newsArticle.setRationale(parseData[5]);
                        insertData.add(newsArticle);

                    }
                }else{
                    parseData = result.split("\\|");
                    NewsArticle newsArticle = new NewsArticle();
                    newsArticle.setId(HashUtil.generateMD5Hash(item.getFirst().getOriginallink()));
                    newsArticle.setPublishedDate(parseData[0]);
                    newsArticle.setTitle(parseData[1]);
                    newsArticle.setContent(parseData[2]);
                    newsArticle.setOpinion(parseData[3]);
                    newsArticle.setKeywords(parseData[4]);
                    newsArticle.setRationale(parseData[5]);
                    insertData.add(newsArticle);
                }


                insertData.stream().forEach(newsArticle -> {
                    log.info("✅ 요약 처리 완료 [발행일:{}, 제목:{}, 내용 :{}, 감정분석: {}",newsArticle.getPublishedDate(),newsArticle.getTitle(),newsArticle.getContent() , newsArticle.getOpinion());
                });


                return insertData; // 다음 단계(Writer)로 전달
            }



        };
    }

    // --- ItemWriter (Elasticsearch 저장) ---
    @Bean
    @StepScope
    // ⭐ JpaItemWriter를 NewsElasticsearchItemWriter로 대체
    public NewsElasticsearchItemWriter newsArticleWriter(){
        // NewsElasticsearchItemWriter는 내부적으로 NewsArticleRepository를 사용해 저장합니다.
        // JPA 관련 설정(EntityManagerFactory, 트랜잭션 활성화 로그)은 필요 없습니다.
        return new NewsElasticsearchItemWriter(newsArticleRepository);
    }
}
