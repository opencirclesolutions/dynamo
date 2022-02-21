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
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;

/**
 * Component creator used for creating a text field for managing an attribute of
 * type String
 * 
 * @author BasRutten
 *
 */
public class TextFieldComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {

		// only for basic non URL attributes
		if (!AttributeType.BASIC.equals(attributeModel.getAttributeType()) || attributeModel.isUrl()) {
			return false;
		}

		// only for String and number
		if (!(String.class.equals(attributeModel.getType()) || NumberUtils.isNumeric(attributeModel.getType()))) {
			return false;
		}

		return context.isSearch() || context.isEditableGrid()
				|| AttributeTextFieldMode.TEXTFIELD.equals(attributeModel.getTextFieldMode());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		TextField tf = new TextField();
		return tf;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U, V> void addConverters(AttributeModel attributeModel, BindingBuilder<U, V> builder) {
		BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
		addTextFieldConverters(attributeModel, sBuilder);
	}

}
