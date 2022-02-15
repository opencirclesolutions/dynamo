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

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.MessageService;
//import com.ocs.dynamo.ui.component.DateTimePicker;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;

/**
 * Component creator that is used for creating date/time picker components for
 * timestamps without time zone
 * 
 * @author BasRutten
 *
 */
public class DateTimePickerComponentCreator implements SimpleComponentCreator {

	@Autowired
	private MessageService messageService;

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		if (!AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
			return false;
		}

		boolean searchDateOnly = context.isSearch() && attributeModel.isSearchDateOnly();
		if (LocalDateTime.class.equals(attributeModel.getType()) && !searchDateOnly) {
			return true;
		}

		return false;
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		Locale dateLoc = VaadinUtils.getDateLocale();
		DateTimePicker dateTimePicker = new DateTimePicker();
		dateTimePicker.setDatePickerI18n(getDatePickerLocalization(messageService, dateLoc));
		return dateTimePicker;
	}
}
