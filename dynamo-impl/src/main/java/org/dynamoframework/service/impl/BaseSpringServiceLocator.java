package org.dynamoframework.service.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.service.BaseSearchService;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.service.ServiceLocator;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Static class for accessing the Spring container
 *
 * @author bas.rutten
 */
@Slf4j
public abstract class BaseSpringServiceLocator implements ServiceLocator {

	protected ApplicationContext ctx;

	protected abstract ApplicationContext loadContext();

	private ApplicationContext getContext() {
		if (ctx == null) {
			ctx = loadContext();
		}
		return ctx;
	}

	/**
	 * Retrieves a service of a certain type
	 *
	 * @param clazz the class of the service
	 * @return the service
	 */
	@Override
	public <T> T getService(Class<T> clazz) {
		Map<String, T> beansOfType = getContext().getBeansOfType(clazz);
		if (!beansOfType.isEmpty()) {
			return beansOfType.values().iterator().next();
		}
		return null;
	}

	@Override
	public <T> T getServiceByName(String name, Class<T> clazz) {
		Map<String, T> beansOfType = getContext().getBeansOfType(clazz);
		if (!beansOfType.isEmpty()) {
			for (Entry<String, T> entry : beansOfType.entrySet()) {
				if (entry.getKey().equals(name)) {
					return entry.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the message service from the context
	 *
	 * @return the message service
	 */
	@Override
	public MessageService getMessageService() {
		return getService(MessageService.class);
	}

	/**
	 * Retrieves the entity model factory from the context
	 *
	 * @return the entity model factory
	 */
	@Override
	public EntityModelFactory getEntityModelFactory() {
		return getService(EntityModelFactory.class);
	}

	/**
	 * Returns a service that is used to manage a certain type of entity
	 *
	 * @param entityClass the entity class
	 * @return the service
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public BaseService<?, ?> getServiceForEntity(Class<?> entityClass) {
		return getServiceForEntityInner(BaseService.class, entityClass);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public BaseSearchService<?, ?> getSearchServiceForEntity(Class<?> entityClass) {
		return getServiceForEntityInner(BaseSearchService.class, entityClass);
	}

	private <S extends BaseSearchService<?, ?>> S getServiceForEntityInner(Class<S> serviceClass, Class<?> entityClass) {
		Map<String, S> services = getContext().getBeansOfType(serviceClass, false, true);
		for (Entry<String, S> entry : services.entrySet()) {
			try {
				if (entry.getValue().getEntityClass() != null && entry.getValue().getEntityClass().equals(entityClass)) {
					return entry.getValue();
				}
			} catch (Exception ex) {
				log.error("Could not find service for {}", ex.getMessage());
				// skip
			}
		}
		return null;
	}

}
