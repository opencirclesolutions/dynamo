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
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.QuickAddTokenSelect;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Component creator used for creating a Token Field for managing a collection of entities
 * 
 * @author BasRutten
 *
 * @param <ID> the primary key of the entity
 * @param <T>  the type of the entity
 */
public class TokenFieldComponentCreator<ID extends Serializable, T extends AbstractEntity<ID>>
		implements EntityComponentCreator<ID, T> {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		AttributeSelectMode mode = context.getAppropriateMode(attributeModel);

		// use token field when editing a collection or doing multiple search, and the
		// select mode is NOT lookup
		if (context.isSearch() && attributeModel.isMultipleSearch()) {
			return !AttributeSelectMode.LOOKUP.equals(mode);
		}

		return Collection.class.isAssignableFrom(attributeModel.getType()) && AttributeSelectMode.TOKEN.equals(mode);
	}

	@Override
	public Component createComponent(AttributeModel attributeModel, FieldCreationContext context,
			BaseService<ID, T> service, EntityModel<T> entityModel, SerializablePredicate<T> fieldFilter,
			DataProvider<T, SerializablePredicate<T>> sharedProvider) {
		SortOrder<String>[] sortOrder = constructSortOrder(entityModel);
		return new QuickAddTokenSelect<>(entityModel, attributeModel, service,
				mapPagingMode(attributeModel.getPagingMode()), fieldFilter, sharedProvider, context.isSearch(),
				sortOrder);
	}

}
