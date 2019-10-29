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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.impl.FieldFactoryImpl;
import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.service.impl.DefaultUserDetailsServiceImpl;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.auth.PermissionChecker;
import com.ocs.dynamo.ui.auth.impl.DefaultPermissionCheckerImpl;
import com.ocs.dynamo.ui.menu.MenuService;

/**
 * Spring Boot auto configuration for the UI module
 * 
 * @author Bas Rutten
 *
 */
@Configuration
public class DynamoFrontendAutoConfigure {

    @Bean
    @ConditionalOnMissingBean(value = FieldFactory.class)
    public FieldFactory fieldFactory() {
        return new FieldFactoryImpl();
    }

    @Bean
    @ConditionalOnMissingBean(value = MenuService.class)
    public MenuService menuService() {
        return new MenuService();
    }

    @Bean
    @ConditionalOnMissingBean(value = UIHelper.class)
    public UIHelper uiHelper() {
        return new UIHelper();
    }

    @Bean
    @ConditionalOnMissingBean(value = PermissionChecker.class)
    @ConditionalOnProperty(name = DynamoConstants.SP_ENABLE_VIEW_AUTHENTICATION, havingValue = "true")
    public PermissionChecker permissionChecker(@Value("${ocs.view.package:}") String basePackage) {
        return new DefaultPermissionCheckerImpl(basePackage);
    }

    @Bean
    @ConditionalOnMissingBean(value = UserDetailsService.class)
    public UserDetailsService userDetailsService() {
        return new DefaultUserDetailsServiceImpl();
    }
}
