package com.ocs.dynamo.ui;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.ocs.dynamo.IntegrationTestConfig;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;

@TestConfiguration
@SpringBootApplication
public class FrontendIntegrationTestConfig extends IntegrationTestConfig {

    @Bean(name = "fieldFactory")
    public FieldFactory fieldFactory() {
        return new FieldFactoryImpl();
    }

}
