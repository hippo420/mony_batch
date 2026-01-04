package app.monybatch.mony.business.repository.es;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

public class CustomReportEsRepositoryImpl implements CustomReportEsReporsitory {

    private final ElasticsearchOperations elasticsearchOperations;

    public CustomReportEsRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

}
