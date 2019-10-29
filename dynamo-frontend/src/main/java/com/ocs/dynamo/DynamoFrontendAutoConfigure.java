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
