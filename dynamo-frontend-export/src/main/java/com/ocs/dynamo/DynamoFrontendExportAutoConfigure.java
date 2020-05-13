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
