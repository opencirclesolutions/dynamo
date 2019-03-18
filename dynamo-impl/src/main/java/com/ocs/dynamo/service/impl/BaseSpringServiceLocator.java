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
package com.ocs.dynamo.service.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.ApplicationContext;

import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;

/**
 * Static class for accessing the Spring container
 * 
 * @author bas.rutten
 */
public abstract class BaseSpringServiceLocator implements ServiceLocator {

	protected ApplicationContext ctx;

	protected abstract ApplicationContext loadCtx();

	/**
	 * @return
	 */
	private ApplicationContext getContext() {
		if (ctx == null) {
			ctx = loadCtx();
		}
		return ctx;
	}

	/**
	 * Retrieves a service of a certain type
	 * 
	 * @param clazz the class of the service
	 * @return
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
	 * @return
	 */
	@Override
	public MessageService getMessageService() {
		return getService(MessageService.class);
	}

	/**
	 * Retrieves the entity model factory from the context
	 * 
	 * @return
	 */
	@Override
	public EntityModelFactory getEntityModelFactory() {
		return getService(EntityModelFactory.class);
	}

	/**
	 * Returns a service that is used to manage a certain type of entity
	 * 
	 * @param entityClass the entity class
	 * @return
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public BaseService<?, ?> getServiceForEntity(Class<?> entityClass) {
		Map<String, BaseService> services = getContext().getBeansOfType(BaseService.class, false, true);
		for (Entry<String, BaseService> e : services.entrySet()) {
			if (e.getValue().getEntityClass() != null && e.getValue().getEntityClass().equals(entityClass)) {
				return e.getValue();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.ocs.dynamo.service.ServiceLocator#getServices(java.lang.Class)
	 */
	@Override
	public <T> Collection<T> getServices(Class<T> clazz) {
		Map<String, T> beans = getContext().getBeansOfType(clazz);
		if (beans != null && !beans.isEmpty()) {
			return beans.values();
		}
		return null;
	}
}
