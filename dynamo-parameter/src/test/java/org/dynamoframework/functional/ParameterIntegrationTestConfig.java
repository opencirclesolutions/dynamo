package org.dynamoframework.functional;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.functional.domain.Parameter;
import com.ocs.dynamo.functional.domain.QParameter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.impl.DefaultServiceImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.ocs.dynamo.IntegrationTestConfig;
import org.springframework.data.repository.query.Param;

/**
 * Configuration for integration tests in functional domain module
 * 
 * @author Bas Rutten
 *
 */
@TestConfiguration
@ComponentScan(basePackages = "com.ocs.dynamo")
@SpringBootApplication
public class ParameterIntegrationTestConfig extends IntegrationTestConfig {


}
