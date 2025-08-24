package com.hackaton.simulacaocredito.configurations;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EnableJpaRepositories(
        basePackages = "com.hackaton.simulacaocredito.repositories.sqlserver",
        entityManagerFactoryRef = "sqlServerEntityManagerFactory",
        transactionManagerRef = "sqlServerTransactionManager"
)
@EnableConfigurationProperties
@EnableTransactionManagement
public class SqlServerConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSource sqlServerDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean sqlServerEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("sqlServerDataSource") DataSource ds) {

        return builder
                .dataSource(ds)
                .packages("com.hackaton.simulacaocredito.models.sqlserver")
                .persistenceUnit("sqlServerPU")
                .build();
    }

    @Bean
    public PlatformTransactionManager sqlServerTransactionManager(
            @Qualifier("sqlServerEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
