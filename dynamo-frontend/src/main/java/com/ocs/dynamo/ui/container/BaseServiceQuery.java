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
package com.ocs.dynamo.ui.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Container;
import com.vaadin.data.util.filter.And;

/**
 * A lazy container query that retrieves data using a service
 * 
 * @author patrick.deenen
 */
public abstract class BaseServiceQuery<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractBeanQuery<T> {

	private static final long serialVersionUID = 4128040933505878355L;

	// local variable used as a counter for assigning temporary IDs
	private int countDown;

	// the class of the primary key
	private Class<?> idClass;

	// the class of the entity
	private Class<T> entityClass;

	// message service
	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	// entity model factory
	private EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

	/**
	 * Constructor
	 * 
	 * @param queryDefinition
	 * @param queryConfiguration
	 */
	public BaseServiceQuery(ServiceQueryDefinition<ID, T> queryDefinition, Map<String, Object> queryConfiguration) {
		super(queryDefinition, queryConfiguration, null, null);
	}

	/**
	 * Creates an instance of the entity class
	 */
	@Override
	protected T constructBean() {

		// lazily load the entity class
		if (entityClass == null) {
			entityClass = getCustomQueryDefinition().getService().getEntityClass();
		}

		// lazily load the ID class
		if (idClass == null) {
			idClass = ClassUtils.getResolvedType(entityClass, DynamoConstants.ID);
		}

		// the lazy query container cannot deal with situations in which the
		// new object doesn't have an ID
		// to circumvent this, we give the object a temporary ID which we clear
		// before actually persisting the object
		T result = ClassUtils.instantiateClass(entityClass);

		// set the primary key
		if (Integer.class.equals(idClass)) {
			ClassUtils.setFieldValue(result, DynamoConstants.ID, Integer.MAX_VALUE - countDown);
		} else if (Long.class.equals(idClass)) {
			ClassUtils.setFieldValue(result, DynamoConstants.ID, Long.MAX_VALUE - countDown);
		}
		countDown--;
		return result;
	}

	/**
	 * Constructs the search filter
	 * 
	 * @return
	 */
	protected Filter constructFilter() {
		final List<Container.Filter> filters = new ArrayList<>();
		filters.addAll(getCustomQueryDefinition().getDefaultFilters());
		filters.addAll(getCustomQueryDefinition().getFilters());

		Container.Filter first;
		if (!filters.isEmpty()) {
			first = filters.remove(0);
		} else {
			first = null;
		}
		while (!filters.isEmpty()) {
			Container.Filter filter = filters.remove(0);
			first = new And(first, filter);
		}

		// look up the correct entity model for filter conversion
		EntityModel<T> em = getCustomQueryDefinition().getEntityModel();
		if (em == null) {
			em = entityModelFactory.getModel(getCustomQueryDefinition().getService().getEntityClass());
		}
		return new FilterConverter(em).convert(first);
	}

	/**
	 * Sets order clause of Service query according to query definition sort states.
	 * 
	 * @return an array containing the constructed Order objects
	 */
	protected SortOrder[] constructOrder() {
		Object[] sortPropertyIds;
		boolean[] sortPropertyAscendingStates;
		QueryDefinition queryDefinition = getCustomQueryDefinition();

		// look up the correct entity model for filter conversion
		EntityModel<T> em = getCustomQueryDefinition().getEntityModel();
		if (em == null) {
			em = entityModelFactory.getModel(getCustomQueryDefinition().getService().getEntityClass());
		}

		if (queryDefinition.getSortPropertyIds().length == 0) {
			sortPropertyIds = queryDefinition.getDefaultSortPropertyIds();
			sortPropertyAscendingStates = queryDefinition.getDefaultSortPropertyAscendingStates();
		} else {
			sortPropertyIds = queryDefinition.getSortPropertyIds();
			sortPropertyAscendingStates = queryDefinition.getSortPropertyAscendingStates();
		}

		final SortOrder[] orders = new SortOrder[sortPropertyIds.length];
		if (sortPropertyIds.length > 0) {
			for (int i = 0; i < sortPropertyIds.length; i++) {
				String prop = sortPropertyIds[i].toString();
				AttributeModel am = em.getAttributeModel(prop);
				if (am.getReplacementSortPath() != null) {
					prop = am.getReplacementSortPath();
				}
				orders[i] = new SortOrder(
						sortPropertyAscendingStates[i] ? SortOrder.Direction.ASC : SortOrder.Direction.DESC, prop);
			}
		}
		return orders;
	}

	@Override
	protected void saveBeans(List<T> addedBeans, List<T> modifiedBeans, List<T> removedBeans) {

		// it is possible to first add/edit an item and then remove it - weed
		// out the items that
		// have already been removed here
		modifiedBeans.removeAll(removedBeans);
		addedBeans.removeAll(removedBeans);

		// any beans that have not been persisted before don't actually have to
		// be removed - remove them from the collection before saving
		removedBeans.removeIf(t -> getCustomQueryDefinition().getService().findById(t.getId()) == null);

		// clear the IDs of the newly added bean and let the database assign
		// proper ones
		for (T added : addedBeans) {
			added.setId(null);
		}

		// reset the counter so we can start again
		countDown = 0;

		// add, update, and delete everything in a single transaction
		getCustomQueryDefinition().getService().update(modifiedBeans, addedBeans, removedBeans);
	}

	@SuppressWarnings("unchecked")
	protected ServiceQueryDefinition<ID, T> getCustomQueryDefinition() {
		return (ServiceQueryDefinition<ID, T>) super.getQueryDefinition();
	}

	public MessageService getMessageService() {
		return messageService;
	}

}
