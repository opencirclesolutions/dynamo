package com.ocs.dynamo.functional;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.ocs.dynamo.IntegrationTestConfig;
import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.functional.domain.Currency;
import com.ocs.dynamo.functional.domain.QCurrency;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.impl.DefaultServiceImpl;

/**
 * Configuration for integration tests in functional domain module
 * 
 * @author Bas Rutten
 *
 */
@TestConfiguration
@ComponentScan(basePackages = "com.ocs.dynamo")
@SpringBootApplication
public class FunctionalDomainIntegrationTestConfig extends IntegrationTestConfig {

	@Bean
	public BaseDao<Integer, Currency> currencyDao() {
		return new DefaultDaoImpl<>(QCurrency.currency, Currency.class);
	}

	@Bean
	public BaseService<Integer, Currency> currencyService(BaseDao<Integer, Currency> dao) {
		return new DefaultServiceImpl<>(dao, "code");
	}

}
