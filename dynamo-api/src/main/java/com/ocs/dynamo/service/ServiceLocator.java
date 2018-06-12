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
package com.ocs.dynamo.service;

import java.util.Collection;

import com.ocs.dynamo.domain.model.EntityModelFactory;

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
	 * @param clazz
	 *            the class of the service
	 * @return
	 */
	<T> T getService(Class<T> clazz);

	/**
	 * 
	 * @param clazz
	 *            the class of the service
	 * @return
	 */
	<T> Collection<T> getServices(Class<T> clazz);

	/**
	 * Retrieves the message service from the context
	 * 
	 * @return
	 */
	MessageService getMessageService();

	/**
	 * Retrieves the entity model factory from the context
	 * 
	 * @return
	 */
	EntityModelFactory getEntityModelFactory();

	/**
	 * Returns a service that is used to manage a certain type of entity
	 * 
	 * @param entityClass
	 *            the entity class
	 * @return
	 */
	BaseService<?, ?> getServiceForEntity(Class<?> entityClass);

}
