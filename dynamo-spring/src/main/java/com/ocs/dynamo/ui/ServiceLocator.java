package com.ocs.dynamo.ui;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.vaadin.spring.server.SpringVaadinServlet;

/**
 * Static class for accessing the Spring container
 * 
 * @author bas.rutten
 */
public final class ServiceLocator {

	private static ApplicationContext ctx;

	private ServiceLocator() {
		// hidden constructor
	}

	/**
	 * @return
	 */
	private static ApplicationContext getContext() {
		if (ctx == null) {
			loadCtx();
		}
		return ctx;
	}

	/**
	 * Lazily loads the context
	 */
	private static synchronized void loadCtx() {
		if (ctx == null) {
			if (SpringVaadinServlet.getCurrent() != null) {
				ServletContext sc = SpringVaadinServlet.getCurrent().getServletContext();
				ctx = WebApplicationContextUtils.getWebApplicationContext(sc);
			} else {
				ctx = new ClassPathXmlApplicationContext(
				        "classpath:META-INF/testApplicationContext.xml");
			}
		}
	}

	/**
	 * Retrieves a service of a certain type
	 * 
	 * @param clazz
	 * @return
	 */
	public static <T> T getService(Class<T> clazz) {
		return getContext().getBean(clazz);
	}

	/**
	 * Retrieves the message service from the context
	 * 
	 * @return
	 */
	public static MessageService getMessageService() {
		return getService(MessageService.class);
	}

	/**
	 * Retrieves the entity model factory from the context
	 * 
	 * @return
	 */
	public static EntityModelFactory getEntityModelFactory() {
		return getService(EntityModelFactory.class);
	}

	public static ProducerTemplate getProducerTemplate() {
		CamelContext context = ServiceLocator.getService(CamelContext.class);
		return context.createProducerTemplate();
	}

	/**
	 * Returns a service that is used to manage a certain type of entity
	 * 
	 * @param entityClass
	 *            the entity class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> BaseService<?, ?> getServiceForEntity(Class<T> entityClass) {
		Map<String, BaseService> services = getContext().getBeansOfType(BaseService.class, false,
		        true);
		for (Entry<String, BaseService> e : services.entrySet()) {
			if (e.getValue().getEntityClass() != null
			        && e.getValue().getEntityClass().equals(entityClass)) {
				return (BaseService<?, ?>) e.getValue();
			}
		}
		return null;
	}

}
