package com.ocs.dynamo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ocs.dynamo.ui.composite.export.ExportDelegate;
import com.ocs.dynamo.ui.composite.export.impl.ExportDelegateImpl;

/**
 * Auto configuration for the frontend-export module. Responsible for registering an export delegate that is used
 * by the 
 * @author Bas Rutten
 *
 */
@Configuration
public class DynamoFrontendExportAutoConfigure {

    @Bean
    @ConditionalOnMissingBean(value = ExportDelegate.class)
    public ExportDelegate exportDelegate() {
        return new ExportDelegateImpl();
    }
}
