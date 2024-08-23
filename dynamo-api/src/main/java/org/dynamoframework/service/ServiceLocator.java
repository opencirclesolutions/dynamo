package org.dynamoframework.service;

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

import org.dynamoframework.domain.model.EntityModelFactory;

/**
 * Service locator
 * 
 * @author bas.rutten
 *
 */
public interface ServiceLocator {

	/**
	 * Retrieves a service of a certain type
	 * 
	 * @param clazz the class of the service
	 * @return the service
	 */
	<T> T getService(Class<T> clazz);

	/**
	 * Returns a service bean based on name and class
	 * @param name the name
	 * @param clazz the class
	 * @return the service
	 */
	<T> T getServiceByName(String name, Class<T> clazz);
	
	/**
	 * Retrieves the message service from the context
	 * 
	 * @return the message service
	 */
	MessageService getMessageService();

	/**
	 * Retrieves the entity model factory from the context
	 * 
	 * @return the entity model factory
	 */
	EntityModelFactory getEntityModelFactory();

	/**
	 * Retrieves a service that is used to manage a certain type of entity
	 * 
	 * @param entityClass the entity class
	 * @return the service used for managing the specified entity type
	 */
	BaseService<?, ?> getServiceForEntity(Class<?> entityClass);

	/**
	 * Retrieves a service that is used to search for a certain type of entity
	 * @param entityClass the entity class
	 * @return the service used for searching
	 */
	BaseSearchService<?, ?> getSearchServiceForEntity(Class<?> entityClass);

}
