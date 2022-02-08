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

import java.time.LocalDate;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.ui.converter.LocalDateWeekCodeConverter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;

/**
 * Component creator for creating a week field for a LocalDate
 * 
 * @author BasRutten
 *
 */
public class WeekFieldComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {

		// only for basic non URL attributes
		if (!AttributeType.BASIC.equals(attributeModel.getAttributeType()) || attributeModel.isUrl()) {
			return false;
		}

		return LocalDate.class.equals(attributeModel.getType()) && attributeModel.isWeek();
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		return new TextField();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U, V> void addConverters(AttributeModel attributeModel, BindingBuilder<U, V> builder) {
		BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
		sBuilder.withConverter(new LocalDateWeekCodeConverter());
	}
}
