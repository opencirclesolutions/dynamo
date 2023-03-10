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

import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.ui.validator.URLValidator;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;

/**
 * Component creator used for creating a URL field. This is only supported
 * for String fields that have the "url" setting at "true"
 * 
 * @author BasRutten
 *
 */
public class UrlFieldComponentCreator implements SimpleComponentCreator {

	@Autowired
	private MessageService messageService;

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		if (!AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
			return false;
		}

		return String.class.equals(attributeModel.getType()) && attributeModel.isUrl();
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		TextField textField = new TextField();
		textField.setSizeFull();
		return new URLField(textField, am, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U, V> void addConverters(AttributeModel attributeModel, BindingBuilder<U, V> builder) {
		BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
		sBuilder.withNullRepresentation("").withValidator(
				new URLValidator(messageService.getMessage("ocs.no.valid.url", VaadinUtils.getLocale())));
	}
}
