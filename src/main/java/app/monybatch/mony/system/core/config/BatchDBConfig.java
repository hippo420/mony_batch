package app.monybatch.mony.system.core.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Slf4j
@Configuration
@EnableJpaRepositories(
        basePackages = "app.monybatch.mony.business.repository.jpa",
        entityManagerFactoryRef = "batchEntityManager",
        transactionManagerRef = "batchTransactionManager"
)
public class BatchDBConfig {

    @Bean(name = "batchDataSource")
    @ConfigurationProperties(prefix = "spring.datasource-batch")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean(name = "batchEntityManager")
    public LocalContainerEntityManagerFactoryBean batchEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(batchDataSource());
        em.setPackagesToScan("app.monybatch.mony.business.entity");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");       // 운영에서는 validate 권장
        properties.put("hibernate.show_sql", "false");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); // MySQL -> PostgreSQL

        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "batchTransactionManager")
    public PlatformTransactionManager batchTransactionManager(
            @Qualifier("batchEntityManager") EntityManagerFactory batchEntityManager) {
        return new JpaTransactionManager(batchEntityManager);
    }


}
