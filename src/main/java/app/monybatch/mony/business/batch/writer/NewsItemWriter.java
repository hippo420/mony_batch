package app.monybatch.mony.business.batch.writer;

import app.monybatch.mony.business.batch.service.OllamaModelClient;
import app.monybatch.mony.business.entity.news.NewsArticle;
import app.monybatch.mony.business.repository.es.NewsArticleRepository;
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
                list.add(article.getContent());
            //log.info("Writing article: {}", article.getTitle());
        }

        String data = agent.extractKeyWord(list,1);
        String[] news = data.split("\\|");
        log.info("keywords: {}", news);
        if(news.length == 10)
        {
            for (int i = 0; i < chunk.getItems().size(); i++) {
                String[] info = news[i].split(",");
                if(news[i].contains(","))
                {
                    chunk.getItems().get(i).setKeywords(info[0]);
                    chunk.getItems().get(i).setOpinion(info[1]);
                }
            }
        }
        chunk.getItems().forEach(f->log.info("언론사: {}, 내용:{}, 키워드:{}, 투자의견:{}",f.getCompany(),f.getContent(),f.getKeywords(),f.getOpinion()));
        newsRepository.saveAll(chunk.getItems());
        
        //log.info("Elasticsearch에 {}개의 기사 Chunk 저장 완료.", chunk.size());
    }
}
