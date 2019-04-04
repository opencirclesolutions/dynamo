package com.ocs.dynamo.ui;

import org.springframework.context.annotation.Bean;

import com.ocs.dynamo.IntegrationTestConfig;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;

public class FrontendIntegrationTestConfig extends IntegrationTestConfig {

    @Bean(name = "fieldFactory")
    public FieldFactory fieldFactory() {
        return new FieldFactoryImpl();
    }
}
