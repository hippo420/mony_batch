package app.monybatch.mony.business.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StockPriceJob {

    private static final int MAX_RETRY = 5;
    private final PlatformTransactionManager transactionManager;

}
