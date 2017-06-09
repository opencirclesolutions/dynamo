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
package com.ocs.dynamo.ui;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

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
				ctx = new ClassPathXmlApplicationContext("classpath:META-INF/testApplicationContext.xml");
			}
		}
	}

	/**
	 * Retrieves a service of a certain type
	 * 
	 * @param clazz
	 *            the class of the service
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

	/**
	 * Returns a service that is used to manage a certain type of entity
	 * 
	 * @param entityClass
	 *            the entity class
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static BaseService<?, ?> getServiceForEntity(Class<?> entityClass) {
		Map<String, BaseService> services = getContext().getBeansOfType(BaseService.class, false, true);
		for (Entry<String, BaseService> e : services.entrySet()) {
			if (e.getValue().getEntityClass() != null && e.getValue().getEntityClass().equals(entityClass)) {
				return (BaseService<?, ?>) e.getValue();
			}
		}
		return null;
	}

}
