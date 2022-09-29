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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.google.common.collect.Lists;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.UserDetailsService;
import com.ocs.dynamo.ui.auth.Authorized;
import com.ocs.dynamo.ui.auth.PermissionChecker;
import com.vaadin.flow.router.Route;

import lombok.extern.slf4j.Slf4j;

/**
 * Default permission checker - checks if the user has the correct role to
 * access a view
 * 
 * @author bas.rutten
 */
@Slf4j
public class DefaultPermissionCheckerImpl implements PermissionChecker {

	@Autowired
	private UserDetailsService userDetailsService;

	/**
	 * Map from view name to list of roles that are allowed to access the view
	 */
	private Map<String, List<String>> permissions = new HashMap<>();

	/**
	 * Map indicating whether a certain view is an "edit only" view that may be
	 * hidden if the user has no edit rights
	 */
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
	public DefaultPermissionCheckerImpl(String basePackage) {
		if (StringUtils.isEmpty(basePackage)) {
			throw new OCSRuntimeException(
					"No base package configure. Please configure it using the ocs.view.package application property");
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
		return roles == null ? true : userDetailsService.isUserInRole(roles.toArray(new String[0]));
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
		provider.addIncludeFilter(new AnnotationTypeFilter(Route.class));

		Set<BeanDefinition> views = provider.findCandidateComponents(basePackage);
		for (BeanDefinition definition : views) {
			try {
				String beanClassName = definition.getBeanClassName();
				Class<?> clazz = Class.forName(beanClassName);

				Route route = clazz.getAnnotation(Route.class);

				// store the permissions both under the bean name and the view
				// name - unfortunately these
				// don't always have to match but there is no way to tell this
				// to the authentication framework!
				Authorized auth = clazz.getAnnotation(Authorized.class);
				if (auth != null && auth.roles().length > 0) {
					if (beanClassName != null) {
						int p = beanClassName.lastIndexOf('.');
						permissions.put(beanClassName.substring(p + 1), Lists.newArrayList(auth.roles()));
						editOnly.put(beanClassName.substring(p + 1), auth.editOnly());
					}
					permissions.put(route.value(), Lists.newArrayList(auth.roles()));
					editOnly.put(route.value(), auth.editOnly());
				}
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		}
	}
}
