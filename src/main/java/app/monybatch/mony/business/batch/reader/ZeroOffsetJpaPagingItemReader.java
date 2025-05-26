package app.monybatch.mony.business.batch.reader;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.batch.item.database.orm.JpaQueryProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class ZeroOffsetJpaPagingItemReader <T> extends AbstractPagingItemReader<T> {
    @Setter
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private final Map<String, Object> jpaPropertyMap = new HashMap();
    @Setter
    private String queryString;
    @Setter
    private JpaQueryProvider queryProvider;
    @Setter
    private Map<String, Object> parameterValues;
    @Setter
    private boolean transacted = true;
    @Setter
    private Object object;
    private Field pkField;
    private Object lastPkValue;

    public ZeroOffsetJpaPagingItemReader() {
        this.setName(ClassUtils.getShortName(ZeroOffsetJpaPagingItemReader.class));
    }

    private Query createQuery() {
        return this.queryProvider == null ? this.entityManager.createQuery(this.queryString) : this.queryProvider.createQuery();
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (this.queryProvider == null) {
            Assert.state(this.entityManagerFactory != null, "EntityManager is required when queryProvider is null");
            Assert.state(StringUtils.hasLength(this.queryString), "Query string is required when queryProvider is null");
        }
        initializePkField();
    }

    protected void doOpen() throws Exception {
        super.doOpen();
        this.entityManager = this.entityManagerFactory.createEntityManager(this.jpaPropertyMap);
        if (this.entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain an EntityManager");
        } else {
            if (this.queryProvider != null) {
                this.queryProvider.setEntityManager(this.entityManager);
            }

        }
    }

    protected void doReadPage() {
        EntityTransaction tx = null;
        if (this.transacted) {
            tx = this.entityManager.getTransaction();
            tx.begin();
            this.entityManager.flush();
            this.entityManager.clear();
        }
        if (this.results == null) {
            this.results = new CopyOnWriteArrayList<>();
        } else {
            this.results.clear();
        }
        boolean isFirstPage = (this.lastPkValue == null);

        String modifiedQueryString = modifyQueryStringWithPkCondition(isFirstPage);
        Query query = this.entityManager.createQuery(modifiedQueryString)
                .setFirstResult(0)  // always start at 0
                .setMaxResults(this.getPageSize());
        log.info("실행 SQL : {}",modifiedQueryString);
        if (this.parameterValues != null) {
            for (Map.Entry<String, Object> me : this.parameterValues.entrySet()) {
                query.setParameter(me.getKey(), me.getValue());
            }
        }
        if (this.lastPkValue != null && !isFirstPage) {
            query.setParameter("lastPkValue", this.lastPkValue);
            log.info(this.lastPkValue.toString());
        }


        List<T> queryResult = query.getResultList();
        if (!queryResult.isEmpty()) {
            this.lastPkValue = extractPkValue(queryResult.get(queryResult.size() - 1));
        }

        if (this.transacted) {
            this.results.addAll(queryResult);
            tx.commit();
        } else {
            for (T entity : queryResult) {
                this.entityManager.detach(entity);
                this.results.add(entity);
            }
        }

    }

    private String modifyQueryStringWithPkCondition(boolean isFirstPage) {
        String alias = extractAlias(this.queryString);
        String pkName = pkField.getName();
        if (isFirstPage) {
            return this.queryString + " ORDER BY " + alias + "." + pkName;
        }
        String pkCondition = alias + "." + pkName + " > :lastPkValue";

        StringBuilder modifiedQueryString = new StringBuilder(this.queryString);
        String lowerQueryString = this.queryString.toLowerCase();

        int whereIndex = lowerQueryString.indexOf(" where ");
        if (whereIndex != -1) {
            modifiedQueryString.insert(whereIndex + 7, pkCondition + " AND ");
        } else {
            modifiedQueryString.append(" WHERE ").append(pkCondition);
        }

        modifiedQueryString.append(" ORDER BY ").append(alias).append(".").append(pkName);
        return modifiedQueryString.toString();
    }

    private String extractAlias(String queryString) {
        String lowerQuery = queryString.toLowerCase();
        int fromIndex = lowerQuery.indexOf("from");
        if (fromIndex == -1) {
            throw new IllegalArgumentException("Query must have a FROM clause");
        }

        String afterFrom = queryString.substring(fromIndex + 4).trim();
        int spaceIndex = afterFrom.indexOf(' ');

        if (spaceIndex != -1) {
            return afterFrom.substring(spaceIndex).trim().split(" ")[0];
        }

        throw new IllegalArgumentException("Alias not found in query");
    }

    private Object extractPkValue(T entity) {
        try {
            return pkField.get(entity);
        } catch (Exception e) {
            log.error("Failed to extract PK value", e);
            return null;
        }
    }

    protected void doClose() throws Exception {
        this.entityManager.close();
        super.doClose();
    }
    private void initializePkField() {
        if (this.object == null) {
            throw new IllegalArgumentException("Entity instance cannot be null.");
        }

        Class<?> entityClass = this.object.getClass();
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                this.pkField = field;
                this.pkField.setAccessible(true);
                break;
            }
        }

        if (this.pkField == null) {
            throw new IllegalArgumentException("No @Id field found in entity class: " + entityClass.getName());
        }
    }
}