package app.monybatch.mony.infra.elasticsearch.report;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

public class CustomReportEsRepositoryImpl implements CustomReportEsReporsitory {

    private final ElasticsearchOperations elasticsearchOperations;

    public CustomReportEsRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

}
