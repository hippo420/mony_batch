package app.monybatch.mony.business.repository.es;

import app.monybatch.mony.business.document.ReportIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReportEsRepository extends ElasticsearchRepository<ReportIndex,Long>, CustomReportEsReporsitory {

}
