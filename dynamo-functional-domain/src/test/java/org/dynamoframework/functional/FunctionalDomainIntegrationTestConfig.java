//package org.dynamoframework.functional;
//
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//
//import org.dynamoframework.IntegrationTestConfig;
//import org.dynamoframework.dao.BaseDao;
//import org.dynamoframework.dao.impl.DefaultDaoImpl;
//import org.dynamoframework.functional.domain.Currency;
//import org.dynamoframework.functional.domain.QCurrency;
//import org.dynamoframework.service.BaseService;
//import impl.service.org.dynamoframework.DefaultServiceImpl;
//
///**
// * Configuration for integration tests in functional domain module
// *
// * @author Bas Rutten
// *
// */
//@TestConfiguration
//@ComponentScan(basePackages = "org.dynamoframework")
//@SpringBootApplication
//public class FunctionalDomainIntegrationTestConfig extends IntegrationTestConfig {
//
//	@Bean
//	public BaseDao<Integer, Currency> currencyDao() {
//		return new DefaultDaoImpl<>(QCurrency.currency, Currency.class);
//	}
//
//	@Bean
//	public BaseService<Integer, Currency> currencyService(BaseDao<Integer, Currency> dao) {
//		return new DefaultServiceImpl<>(dao, "code");
//	}
//
//}
