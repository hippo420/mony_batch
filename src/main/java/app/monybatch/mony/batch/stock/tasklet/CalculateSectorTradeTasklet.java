package app.monybatch.mony.batch.stock.tasklet;

import app.monybatch.mony.domian.trade.entity.SectorTrade;
import app.monybatch.mony.domian.trade.repository.SectorTradeRepository;
import app.monybatch.mony.domian.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CalculateSectorTradeTasklet implements Tasklet {

    private final String basDd;
    private final TradeRepository tradeRepository;
    private final SectorTradeRepository sectorTradeRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("섹터 수급 집계 시작 - basDd={}", basDd);

        List<Object[]> rows = tradeRepository.findTradeWithIndustryByDate(basDd);

        // industryCode → [totalVol, totalTrPbmn, frgnNtbyQty, frgnNtbyTrPbmn,
        //                  orgnNtbyQty, orgnNtbyTrPbmn, prsnNtbyQty, prsnNtbyTrPbmn,
        //                  fundNtbyTrPbmn, peFundNtbyTrPbmn]
        Map<String, long[]> acc = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String industryCode = (String) row[1];
            long[] sums = acc.computeIfAbsent(industryCode, k -> new long[10]);
            sums[0] += parseLong(row[2]);
            sums[1] += parseLong(row[3]);
            sums[2] += parseLong(row[4]);
            sums[3] += parseLong(row[5]);
            sums[4] += parseLong(row[6]);
            sums[5] += parseLong(row[7]);
            sums[6] += parseLong(row[8]);
            sums[7] += parseLong(row[9]);
            sums[8] += parseLong(row[10]);
            sums[9] += parseLong(row[11]);
        }

        List<SectorTrade> results = new ArrayList<>();
        acc.forEach((industryCode, sums) -> results.add(SectorTrade.builder()
                .stckBsopDate(basDd)
                .industryCode(industryCode)
                .totalVol(sums[0])
                .totalTrPbmn(sums[1])
                .frgnNtbyQty(sums[2])
                .frgnNtbyTrPbmn(sums[3])
                .orgnNtbyQty(sums[4])
                .orgnNtbyTrPbmn(sums[5])
                .prsnNtbyQty(sums[6])
                .prsnNtbyTrPbmn(sums[7])
                .fundNtbyTrPbmn(sums[8])
                .peFundNtbyTrPbmn(sums[9])
                .build()));

        sectorTradeRepository.saveAll(results);
        log.info("섹터 수급 집계 완료 - basDd={}, 섹터 수={}", basDd, results.size());
        return RepeatStatus.FINISHED;
    }

    private long parseLong(Object val) {
        if (val == null) return 0L;
        String s = val.toString().trim();
        if (s.isEmpty()) return 0L;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
