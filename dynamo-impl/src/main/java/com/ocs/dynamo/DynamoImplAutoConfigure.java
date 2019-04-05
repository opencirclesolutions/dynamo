package com.ocs.dynamo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.impl.MessageServiceImpl;

/**
 * Auto configuration for Dynamo implementation classes
 * @author Bas Rutten
 *
 */
@Configuration
public class DynamoImplAutoConfigure {

    @Bean
    @ConditionalOnMissingBean(value = MessageService.class)
    public MessageService messageService() {
        return new MessageServiceImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(value = EntityModelFactory.class)
    public EntityModelFactory entityModelFactory() {
        return new EntityModelFactoryImpl();
    }
}
