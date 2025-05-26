package app.monybatch.mony.business.batch.reader.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemReadListener;

public class LoggingItemReadListener<T> implements ItemReadListener<T> {
    private static final Logger logger = LoggerFactory.getLogger(LoggingItemReadListener.class);
    private static Long CNT=0L;
    @Override
    public void beforeRead() {
        // 읽기 전에 로그를 남기고 싶다면 여기에 작성합니다.
    }

    @Override
    public void afterRead(T item) {
        // 각 아이템이 읽힐 때마다 로그를 남깁니다.
        logger.info("Item read: {}", CNT++);
    }

    @Override
    public void onReadError(Exception ex) {
        logger.error("Error occurred while reading item", ex);
    }
}

