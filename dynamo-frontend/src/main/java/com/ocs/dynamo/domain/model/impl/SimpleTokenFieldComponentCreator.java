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
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Component creator that is used to create a token field for searching on
 * simple values
 * 
 * @author BasRutten
 *
 * @param <ID>
 * @param <T>
 */
public class SimpleTokenFieldComponentCreator<ID extends Serializable, T extends AbstractEntity<ID>>
		implements EntityComponentCreator<ID, T> {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		boolean elementCollectionSearch = context.isSearch()
				&& AttributeType.ELEMENT_COLLECTION.equals(attributeModel.getAttributeType());

		boolean simpleSearch = context.isSearch()
				&& AttributeSelectMode.TOKEN.equals(attributeModel.getSearchSelectMode())
				&& AttributeType.BASIC.equals(attributeModel.getAttributeType());

		return elementCollectionSearch || simpleSearch;
	}

	@Override
	public Component createComponent(AttributeModel attributeModel, FieldCreationContext context,
			BaseService<ID, T> service, EntityModel<T> entityModel, SerializablePredicate<T> fieldFilter,
			DataProvider<T, SerializablePredicate<T>> sharedProvider) {

		String distinctField = attributeModel.getPath().substring(attributeModel.getPath().lastIndexOf('.') + 1);
		return new SimpleTokenFieldSelect<>(service, entityModel, attributeModel, fieldFilter, distinctField,
				String.class, AttributeType.ELEMENT_COLLECTION.equals(attributeModel.getAttributeType()));
	}

}
