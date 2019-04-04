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
package com.ocs.dynamo.ui.auth.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.ui.auth.Authorized;
import com.ocs.dynamo.ui.auth.PermissionChecker;
import com.vaadin.spring.annotation.SpringView;

/**
 * Default permission checker - checks if the user has the correct role to
 * access a view
 * 
 * @author bas.rutten
 */
@Service
@ConditionalOnMissingBean(name = "com.ocs.dynamo.ui.auth.PermissionChecker")
@ConditionalOnProperty(name = DynamoConstants.SP_ENABLE_VIEW_AUTHENTICATION, havingValue = "true")
public class DefaultPermissionCheckerImpl implements PermissionChecker {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPermissionCheckerImpl.class);

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Map from view name to list of roles that are allowed to access the view
     */
    private Map<String, List<String>> permissions = new HashMap<>();

    // map indicating whether a certain view is an "edit only" view that may be
    // hidden if the user has no edit rights
    private Map<String, Boolean> editOnly = new HashMap<>();

    /**
     * The base package to scan for views
     */
    private String basePackage;

    /**
     * Constructor
     * 
     * @param basePackage the base package to scan for views
     */
    public DefaultPermissionCheckerImpl(@Value("${ocs.view.package:}") String basePackage) {
        if (StringUtils.isEmpty(basePackage)) {
            throw new OCSRuntimeException("No base package configure. Please configure it using the ocs.view.package application property");
        }
        this.basePackage = basePackage;
    }

    /**
     * Returns a list of all view names
     * 
     * @return
     */
    @Override
    public List<String> getViewNames() {
        return Collections.unmodifiableList(new ArrayList<>(permissions.keySet()));
    }

    /**
     * Checks if the user is allowed to access a certain view
     * 
     * @param viewName the name of the view
     * @return
     */
    @Override
    public boolean isAccessAllowed(String viewName) {
        List<String> roles = permissions.get(viewName);
        if (roles == null) {
            // if no roles are defined on the view, everybody has access
            return true;
        } else {
            for (String s : roles) {
                if (userDetailsService.isUserInRole(s)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Checks whether the view is an "edit only" view that can be hidden if the user
     * doesn't have certain edit rights
     * 
     * @param viewName the name of the view
     * @return
     */
    @Override
    public boolean isEditOnly(String viewName) {
        if (!editOnly.containsKey(viewName)) {
            return false;
        }
        return editOnly.get(viewName);
    }

    @PostConstruct
    public void postConstruct() {

        // scan the class path for all classes annotated with @SpringView
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AnnotationTypeFilter(SpringView.class));

        Set<BeanDefinition> views = provider.findCandidateComponents(basePackage);
        for (BeanDefinition d : views) {
            try {
                final String beanClassName = d.getBeanClassName();
                Class<?> clazz = Class.forName(beanClassName);

                SpringView view = clazz.getAnnotation(SpringView.class);

                // store the permissions both under the bean name and the view
                // name - unfortunately these
                // don't always have to match but there is no way to tell this
                // to the authentication framework!
                Authorized auth = clazz.getAnnotation(Authorized.class);
                if (auth != null && auth.roles().length > 0) {
                    if (beanClassName != null) {
                        int p = beanClassName.lastIndexOf('.');
                        permissions.put(beanClassName.substring(p + 1), Arrays.asList(auth.roles()));
                        editOnly.put(beanClassName.substring(p + 1), auth.editOnly());
                    }
                    permissions.put(view.name(), Arrays.asList(auth.roles()));
                    editOnly.put(view.name(), auth.editOnly());
                }
            } catch (ClassNotFoundException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
