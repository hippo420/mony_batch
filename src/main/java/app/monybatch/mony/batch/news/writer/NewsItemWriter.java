package app.monybatch.mony.batch.news.writer;

import app.monybatch.mony.domian.news.dto.NewsAnalysis;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticle;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticleRepository;
import app.monybatch.mony.infra.llm.OllamaModelClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class NewsItemWriter implements ItemWriter<NewsArticle> {

    private final NewsArticleRepository newsRepository;
    private final OllamaModelClient agent;
    @Override
    public void write(Chunk<? extends NewsArticle> chunk) throws Exception {
        log.info("Writing {} articles", chunk.size());
        List<String> list = new ArrayList<>();
        for (NewsArticle article : chunk) {
            if(article.getContent().isEmpty())
                list.add(article.getTitle());
            else
                list.add(article.getTitle().concat(":").concat(article.getContent()));
            //log.info("Writing article: {}", article.getTitle());
        }

        String data = agent.extractKeyWord(list,1);
        ObjectMapper mapper = new ObjectMapper();
        List<NewsAnalysis> result = mapper.readValue(data, new TypeReference<List<NewsAnalysis>>() {});
        result.forEach(f -> log.info("키워드:{}, 투자의견:{}, 근거:{}",f.getKeyword(),f.getSentiment(),f.getReason()));

        if(result.size() == 10)
        {
            for (int i = 0; i < chunk.getItems().size(); i++) {
                chunk.getItems().get(i).setKeywords(result.get(i).getKeyword());
                chunk.getItems().get(i).setOpinion(result.get(i).getSentiment());
                //chunk.getItems().get(i).setReason(info[2]);
            }
        }
        chunk.getItems().forEach(f->log.info(" 내용:{}, 키워드:{}, 투자의견:{}",f.getContent(),f.getKeywords(),f.getOpinion()));
        newsRepository.saveAll(chunk.getItems());
        
        //log.info("Elasticsearch에 {}개의 기사 Chunk 저장 완료.", chunk.size());
    }
}
