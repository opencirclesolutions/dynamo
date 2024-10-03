package org.dynamoframework.functional;

import org.dynamoframework.dao.BaseDao;
import org.dynamoframework.dao.impl.DefaultDaoImpl;
import org.dynamoframework.functional.domain.Parameter;
import org.dynamoframework.functional.domain.QParameter;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.impl.DefaultServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.dynamoframework.IntegrationTestConfig;
import org.springframework.data.repository.query.Param;

/**
 * Configuration for integration tests in functional domain module
 *
 * @author Bas Rutten
 */
@TestConfiguration
@ComponentScan(basePackages = "com.ocs.dynamo")
@SpringBootApplication
public class ParameterIntegrationTestConfig extends IntegrationTestConfig {


}
