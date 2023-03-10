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
package com.ocs.dynamo.domain.model.impl;

import java.io.Serializable;
import java.util.Collection;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.PagingMode;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.service.BaseService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Interface that must be implemented by component creators that create
 * components that are used for managing an entity or collections of entitie
 * 
 * @author BasRutten
 *
 * @param <ID> the primary key of the entity
 * @param <T>  the type of the entity
 */
public interface EntityComponentCreator<ID extends Serializable, T extends AbstractEntity<ID>>
		extends ComponentCreator {

	/**
	 * Creates the component
	 * 
	 * @param attributeModel the attribute model to base the component on
	 * @param context        the creation context to based the component on
	 * @param service        service for contacting the database
	 * @param entityModel    the entity model
	 * @param fieldFilter    the field filter used for limiting the results
	 * @param sharedProvider shared data provider
	 * @return the created component
	 */
	Component createComponent(AttributeModel attributeModel, FieldCreationContext context, BaseService<ID, T> service,
			EntityModel<T> entityModel, SerializablePredicate<T> fieldFilter,
			DataProvider<T, SerializablePredicate<T>> sharedProvider);

	/**
	 * Constructs the sort order to be used by the component
	 * 
	 * @param entityModel the entity model that defines the sort order
	 * @return the sort order
	 */
	@SuppressWarnings("unchecked")
	default SortOrder<String>[] constructSortOrder(EntityModel<?> entityModel) {

		final SortOrder<String>[] sortOrders = new SortOrder[entityModel.getSortOrder().size()];
		int i = 0;
		for (AttributeModel am : entityModel.getSortOrder().keySet()) {
			sortOrders[i++] = new SortOrder<>(am.getName(),
					entityModel.getSortOrder().get(am) ? SortDirection.ASCENDING : SortDirection.DESCENDING);
		}
		return sortOrders;
	}

	/**
	 * Maps the paging mode from the attribute model to a select mode for the
	 * component
	 * 
	 * @param mode the paging mode to map
	 * @return the associated SelectMode
	 */
	default SelectMode mapPagingMode(PagingMode mode) {
		return switch (mode) {
			case NON_PAGED -> SelectMode.FILTERED_ALL;
			case PAGED -> SelectMode.FILTERED_PAGED;
			default -> null;
		};
	}

	/**
	 * Checks whether the attribute model defines an entity or a collection
	 * 
	 * @param attributeModel the attribute model to check
	 * @return true if this is the case, false otherwise
	 */
	default boolean isCollectionOrEntity(AttributeModel attributeModel) {
		return Collection.class.isAssignableFrom(attributeModel.getType())
				|| (AbstractEntity.class.isAssignableFrom(attributeModel.getType()));
	}
}
