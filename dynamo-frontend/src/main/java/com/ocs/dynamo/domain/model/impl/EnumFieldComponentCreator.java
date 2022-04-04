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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ListDataProvider;

/**
 * Component creator used for creating a combo box for managing an enumeration
 * 
 * @author BasRutten
 *
 */
public class EnumFieldComponentCreator implements SimpleComponentCreator {

	@Autowired
	private MessageService messageService;

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		return Enum.class.isAssignableFrom(attributeModel.getType());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		return constructEnumComboBox(am.getType().asSubclass(Enum.class));
	}

	@SuppressWarnings("rawtypes")
	private <E extends Enum> ComboBox<E> constructEnumComboBox(Class<E> enumClass) {
		ComboBox<E> cb = new ComboBox<>();

		// sort on the description
		List<E> list = Lists.newArrayList(enumClass.getEnumConstants());
		list.sort((a, b) -> {
			String msg1 = messageService.getEnumMessage(enumClass, a, VaadinUtils.getLocale());
			String msg2 = messageService.getEnumMessage(enumClass, b, VaadinUtils.getLocale());
			return msg1.compareToIgnoreCase(msg2);

		});

		cb.setDataProvider(new ListDataProvider<>(list));
		cb.setItemLabelGenerator(e -> messageService.getEnumMessage(enumClass, e, VaadinUtils.getLocale()));
		return cb;
	}

}
