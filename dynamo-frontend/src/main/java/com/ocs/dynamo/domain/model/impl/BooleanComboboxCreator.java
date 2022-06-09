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

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.ListDataProvider;

/**
 * Component creator for creating a boolean check box (featuring true, false,
 * and undefined as possible values).
 * 
 * @author BasRutten
 *
 */
public class BooleanComboboxCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		return context.isSearch() && attributeModel.isBoolean();
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		ComboBox<Boolean> cb = new ComboBox<>();
		ListDataProvider<Boolean> provider = new ListDataProvider<>(Lists.newArrayList(Boolean.TRUE, Boolean.FALSE));
		cb.setItems(provider);
		cb.setItemLabelGenerator(b -> Boolean.TRUE.equals(b) ? am.getTrueRepresentation(VaadinUtils.getLocale())
				: am.getFalseRepresentation(VaadinUtils.getLocale()));
		cb.setRequiredIndicatorVisible(context.isSearch() ? am.isRequiredForSearching() : am.isRequired());
		return cb;
	}

}
