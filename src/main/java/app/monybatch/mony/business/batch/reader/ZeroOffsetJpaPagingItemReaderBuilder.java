package app.monybatch.mony.business.batch.reader;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.orm.JpaQueryProvider;
import org.springframework.util.Assert;

import java.util.Map;

public class ZeroOffsetJpaPagingItemReaderBuilder <T> {
    private int pageSize = 10;
    private EntityManagerFactory entityManagerFactory;
    private Map<String, Object> parameterValues;
    private boolean transacted = true;
    private String queryString;
    private JpaQueryProvider queryProvider;
    private boolean saveState = true;
    private String name;
    private int maxItemCount = Integer.MAX_VALUE;
    private int currentItemCount;
    private Object object;

    public ZeroOffsetJpaPagingItemReaderBuilder() {
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> saveState(boolean saveState) {
        this.saveState = saveState;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> maxItemCount(int maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> currentItemCount(int currentItemCount) {
        this.currentItemCount = currentItemCount;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> pageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> parameterValues(Map<String, Object> parameterValues) {
        this.parameterValues = parameterValues;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> queryProvider(JpaQueryProvider queryProvider) {
        this.queryProvider = queryProvider;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> queryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> transacted(boolean transacted) {
        this.transacted = transacted;
        return this;
    }

    public ZeroOffsetJpaPagingItemReaderBuilder<T> entityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        return this;
    }
    public ZeroOffsetJpaPagingItemReaderBuilder<T> object(Object object) {
        this.object = object;
        return this;
    }


    public ZeroOffsetJpaPagingItemReader<T> build() {
        Assert.isTrue(this.pageSize > 0, "pageSize must be greater than zero");
        Assert.notNull(this.entityManagerFactory, "An EntityManagerFactory is required");
        if (this.saveState) {
            Assert.hasText(this.name, "A name is required when saveState is set to true");
        }

        if (this.queryProvider == null) {
            Assert.hasLength(this.queryString, "Query string is required when queryProvider is null");
        }

        ZeroOffsetJpaPagingItemReader<T> reader = new ZeroOffsetJpaPagingItemReader<>();
        reader.setQueryString(this.queryString);
        reader.setPageSize(this.pageSize);
        reader.setParameterValues(this.parameterValues);
        reader.setEntityManagerFactory(this.entityManagerFactory);
        reader.setQueryProvider(this.queryProvider);
        reader.setTransacted(this.transacted);
        reader.setCurrentItemCount(this.currentItemCount);
        reader.setMaxItemCount(this.maxItemCount);
        reader.setSaveState(this.saveState);
        reader.setName(this.name);
        reader.setObject(this.object);
        return reader;
    }
}

