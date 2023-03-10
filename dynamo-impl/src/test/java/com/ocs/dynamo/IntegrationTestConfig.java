package com.ocs.dynamo;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.service.impl.UserDetailsServiceMockImpl;
import com.ocs.dynamo.test.BaseIntegrationTestConfig;

/**
 * Integration test config for backend tests. Sets up in memory database and
 * transaction manager
 * 
 * @author Bas Rutten
 *
 */
@TestConfiguration
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ocs.dynamo", entityManagerFactoryRef = "entityManager")
public class IntegrationTestConfig extends BaseIntegrationTestConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceMockImpl();
    }

    @Bean
    public HibernateJpaVendorAdapter vendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.H2Dialect");
        adapter.setGenerateDdl(true);
        adapter.setShowSql(false);
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManager(DataSource dataSource, JpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPackagesToScan("com.ocs.dynamo");
        emf.setJpaVendorAdapter(adapter);
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(adapter);
        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
