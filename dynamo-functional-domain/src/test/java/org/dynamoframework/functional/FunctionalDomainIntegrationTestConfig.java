//package org.dynamoframework.functional;
//
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//
//import org.dynamoframework.IntegrationTestConfig;
//import dao.org.dynamoframework.BaseDao;
//import impl.dao.org.dynamoframework.DefaultDaoImpl;
//import domain.functional.org.dynamoframework.Currency;
//import domain.functional.org.dynamoframework.QCurrency;
//import service.org.dynamoframework.BaseService;
//import impl.service.org.dynamoframework.DefaultServiceImpl;
//
///**
// * Configuration for integration tests in functional domain module
// *
// * @author Bas Rutten
// *
// */
//@TestConfiguration
//@ComponentScan(basePackages = "com.ocs.dynamo")
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
