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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.QuickAddListSingleSelect;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Component creator that is used for creating a List box component for managing
 * an entity
 * 
 * @author BasRutten
 *
 * @param <ID> the primary key of the entity
 * @param <T>  the type of the entity
 */
public class ListBoxComponentCreator<ID extends Serializable, T extends AbstractEntity<ID>>
		implements EntityComponentCreator<ID, T> {

	@Override
	public boolean supports(AttributeModel am, FieldCreationContext context) {
		AttributeSelectMode selectMode = context.getAppropriateMode(am);

		// in search mode, use this unless multiple search is enabled
		if (context.isSearch()) {
			return !am.isMultipleSearch() && AbstractEntity.class.isAssignableFrom(am.getType())
					&& AttributeSelectMode.LIST.equals(selectMode);
		}

		return AbstractEntity.class.isAssignableFrom(am.getType()) && AttributeSelectMode.LIST.equals(selectMode);
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context, BaseService<ID, T> service,
			EntityModel<T> entityModel, SerializablePredicate<T> fieldFilter,
			DataProvider<T, SerializablePredicate<T>> sharedProvider) {
		SortOrder<?>[] sos = constructSortOrder(entityModel);
		return new QuickAddListSingleSelect<>(entityModel, am, service, mapPagingMode(am.getPagingMode()), fieldFilter,
				null, context.isSearch(), sos);
	}

}
