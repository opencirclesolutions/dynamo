/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
