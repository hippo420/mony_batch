package app.monybatch.mony.batch.support.writer;

import app.monybatch.mony.infra.elasticsearch.news.NewsArticle;
import app.monybatch.mony.infra.elasticsearch.news.NewsArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ListFlattenWriter implements ItemWriter<List<NewsArticle>> {

    private final NewsArticleRepository repository;

    @Override
    public void write(Chunk<? extends List<NewsArticle>> chunk) throws Exception {
        List<NewsArticle> flatList = new ArrayList<>();

        // 이중 리스트 풀기 (List<List<NewsArticle>> -> List<NewsArticle>)
        for (List<NewsArticle> list : chunk) {
            if (list != null) {
                flatList.addAll(list);
            }
        }

        if (!flatList.isEmpty()) {
            repository.saveAll(flatList); // 순수 저장 로직
        }
    }
}
