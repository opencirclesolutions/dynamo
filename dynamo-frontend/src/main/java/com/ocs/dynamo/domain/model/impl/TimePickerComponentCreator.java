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

import java.time.LocalTime;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.timepicker.TimePicker;

/**
 * Component creator used for creating a TimePicker field for managing a
 * LocalDate
 * 
 * @author BasRutten
 *
 */
public class TimePickerComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		return AttributeType.BASIC.equals(attributeModel.getAttributeType())
				&& LocalTime.class.equals(attributeModel.getType());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		Locale dateLoc = VaadinUtils.getDateLocale();
		TimePicker tf = new TimePicker();
		tf.setLocale(dateLoc);
		return tf;
	}
}
