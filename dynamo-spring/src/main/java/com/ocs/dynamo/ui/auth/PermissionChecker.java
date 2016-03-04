package com.ocs.dynamo.ui.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.ocs.dynamo.service.UserDetailsService;
import com.vaadin.spring.annotation.SpringView;

/**
 * Permission checker - checks if the user has the correct role to access a view
 * 
 * @author bas.rutten
 */
public class PermissionChecker {

	private static final Logger LOG = Logger.getLogger(PermissionChecker.class);

	@Inject
	private UserDetailsService userDetailsService;

	private Map<String, List<String>> permissions = new HashMap<>();

	// map indicating whether a certain view is an "edit only" view that may be
	// hidden if the user has no edit rights
	private Map<String, Boolean> editOnly = new HashMap<>();

	private String basePackage;

	public PermissionChecker(String basePackage) {
		this.basePackage = basePackage;
	}

	@PostConstruct
	public void postConstruct() {

		// scan the class path for all classes annotated with @SpringView
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
		        true);
		provider.addIncludeFilter(new AnnotationTypeFilter(SpringView.class));

		Set<BeanDefinition> views = provider.findCandidateComponents(basePackage);
		for (BeanDefinition d : views) {
			try {
				Class<?> clazz = Class.forName(d.getBeanClassName());

				SpringView view = clazz.getAnnotation(SpringView.class);

				// store the permissions both under the bean name and the view
				// name - unfortunately these
				// don't always have to match but there is no way to tell this
				// to the authentication framework!
				Authorized auth = clazz.getAnnotation(Authorized.class);
				if (auth != null && auth.roles().length > 0) {
					int p = d.getBeanClassName().lastIndexOf(".");
					permissions.put(d.getBeanClassName().substring(p + 1),
					        Arrays.asList(auth.roles()));
					editOnly.put(d.getBeanClassName().substring(p + 1), auth.editOnly());

					permissions.put(view.name(), Arrays.asList(auth.roles()));
					editOnly.put(view.name(), auth.editOnly());

				}
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Checks if the user is allowed to access a certain view
	 * 
	 * @param viewName
	 * @return
	 */
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
	 * Returns a list of all view names
	 * 
	 * @return
	 */
	public List<String> getViewNames() {
		return Collections.unmodifiableList(new ArrayList<String>(permissions.keySet()));
	}

	/**
	 * @param viewName
	 * @return
	 */
	public boolean isEditOnly(String viewName) {
		if (!editOnly.containsKey(viewName)) {
			return false;
		}
		return editOnly.get(viewName);
	}
}
