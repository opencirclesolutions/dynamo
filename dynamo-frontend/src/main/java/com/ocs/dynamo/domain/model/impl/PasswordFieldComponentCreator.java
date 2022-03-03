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

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.ui.converter.TrimSpacesConverter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;

/**
 * Component creator for creating a password field
 * 
 * @author BasRutten
 *
 */
public class PasswordFieldComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		if (context.isSearch()) {
			return false;
		}

		return AttributeType.BASIC.equals(attributeModel.getAttributeType())
				&& AttributeTextFieldMode.PASSWORD.equals(attributeModel.getTextFieldMode())
				&& String.class.equals(attributeModel.getType());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		PasswordField field = new PasswordField();
		field.setRevealButtonVisible(am.isShowPassword());
		return field;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U, V> void addConverters(AttributeModel attributeModel, BindingBuilder<U, V> builder) {
		BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
		if (attributeModel.isTrimSpaces()) {
			sBuilder.withConverter(new TrimSpacesConverter());
		}
		sBuilder.withNullRepresentation("");
	}
}
