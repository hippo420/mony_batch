package app.monybatch.mony.infra.elasticsearch.report;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReportEsRepository extends ElasticsearchRepository<ReportIndex,Long>, CustomReportEsReporsitory {

}
