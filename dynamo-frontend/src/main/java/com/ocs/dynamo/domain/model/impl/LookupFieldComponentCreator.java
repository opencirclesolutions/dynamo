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

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.MultiSelectMode;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.composite.layout.SearchOptions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * 
 * Component creator that is used for creating a lookup field for managing an
 * entity or collection of entities.
 * 
 * @author BasRutten
 *
 * @param <ID> the primary key of the entity
 * @param <T>  the type of the entity
 */
public class LookupFieldComponentCreator<ID extends Serializable, T extends AbstractEntity<ID>>
		implements EntityComponentCreator<ID, T> {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		AttributeSelectMode mode = context.getAppropriateMode(attributeModel);

		// use a lookup field when editing a collection or doing multiple search, and the
		// select mode is LOOKUP
		if (context.isSearch() && attributeModel.isMultipleSearch()) {
			return AttributeSelectMode.LOOKUP.equals(mode);
		}

		return isCollectionOrEntity(attributeModel) && AttributeSelectMode.LOOKUP.equals(mode);
	}

	@Override
	public Component createComponent(AttributeModel attributeModel, FieldCreationContext context,
			BaseService<ID, T> service, EntityModel<T> entityModel, SerializablePredicate<T> fieldFilter,
			DataProvider<T, SerializablePredicate<T>> sharedProvider) {
		SortOrder<String>[] sortOrder = constructSortOrder(entityModel);
		boolean multiSelect = (context.isSearch() && attributeModel.isMultipleSearch())
				|| Collection.class.isAssignableFrom(attributeModel.getType());

		SearchOptions options = SearchOptions.builder().advancedSearchMode(false).multiSelect(multiSelect)
				.searchImmediately(true)
				.useCheckboxesForMultiSelect(MultiSelectMode.CHECKBOX.equals(attributeModel.getMultiSelectMode()))
				.build();
		return new EntityLookupField<>(service, entityModel, attributeModel, fieldFilter, context.isSearch(), options,
				sortOrder.length == 0 ? null : Lists.newArrayList(sortOrder));
	}
}
