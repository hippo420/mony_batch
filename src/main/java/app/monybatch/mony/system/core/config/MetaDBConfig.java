package app.monybatch.mony.system.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class MetaDBConfig {

    @Primary
    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix ="spring.datasource-meta")
    public DataSource metaDataSource() {
        return DataSourceBuilder.create().build();
    }


    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dataSource")DataSource metaDataSource) {
        return new DataSourceTransactionManager(metaDataSource);
    }
    @Bean
    public JdbcTemplate batchJdbcTemplate(@Qualifier("dataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
