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

import java.math.BigDecimal;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.TrimSpacesConverter;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;

/**
 * Component creator that is used for creating a component that manages a simple
 * value (rather than an entity or collection of entities)
 * 
 * @author BasRutten
 *
 */
public interface SimpleComponentCreator extends ComponentCreator {

	Component createComponent(AttributeModel am, FieldCreationContext context);

	/**
	 * Adds date picker localization to a component
	 * 
	 * @param messageService
	 * @param dateLoc
	 * @return
	 */
	default DatePickerI18n getDatePickerLocalization(MessageService messageService, Locale dateLoc) {
		DatePickerI18n dpi = new DatePickerI18n();

		String weekdays = messageService.getMessage("ocs.calendar.days", dateLoc);
		if (weekdays != null) {
			dpi.setWeekdays(Lists.newArrayList(weekdays.split(",")));
		}

		String weekdaysShort = messageService.getMessage("ocs.calendar.days.short", dateLoc);
		if (weekdaysShort != null) {
			dpi.setWeekdaysShort(Lists.newArrayList(weekdaysShort.split(",")));
		}

		String months = messageService.getMessage("ocs.calendar.months", dateLoc);
		if (months != null) {
			dpi.setMonthNames(Lists.newArrayList(months.split(",")));
		}

		dpi.setCancel(messageService.getMessage("ocs.calendar.cancel", dateLoc));
		dpi.setFirstDayOfWeek(Integer.parseInt(messageService.getMessage("ocs.calendar.first", dateLoc)));

		return dpi;
	}

	/**
	 * Adds converters for a TextField
	 * 
	 * @param <U>            the of the value to convert to
	 * @param attributeModel the attribute model that is used to construct the
	 *                       component
	 * @param sBuilder       the binding builder that is used to bind the component
	 */
	default <U> void addTextFieldConverters(AttributeModel attributeModel, BindingBuilder<U, String> sBuilder) {
		sBuilder.withNullRepresentation("");
		if (attributeModel.getType().equals(BigDecimal.class)) {
			sBuilder.withConverter(ConverterFactory.createBigDecimalConverter(attributeModel.isCurrency(),
					attributeModel.isPercentage(), attributeModel.useThousandsGroupingInEditMode(),
					attributeModel.getPrecision(), attributeModel.getCurrencySymbol()));
		} else if (NumberUtils.isInteger(attributeModel.getType())) {
			sBuilder.withConverter(ConverterFactory.createIntegerConverter(
					attributeModel.useThousandsGroupingInEditMode(), attributeModel.isPercentage()));
		} else if (NumberUtils.isLong(attributeModel.getType())) {
			sBuilder.withConverter(ConverterFactory.createLongConverter(attributeModel.useThousandsGroupingInEditMode(),
					attributeModel.isPercentage()));
		} else if (NumberUtils.isDouble(attributeModel.getType())) {
			sBuilder.withConverter(ConverterFactory.createDoubleConverter(attributeModel.isCurrency(),
					attributeModel.useThousandsGroupingInEditMode(), attributeModel.isPercentage(),
					attributeModel.getPrecision(), attributeModel.getCurrencySymbol()));
		} else if (String.class.equals(attributeModel.getType()) && attributeModel.isTrimSpaces()) {
			sBuilder.withConverter(new TrimSpacesConverter());
		}
	}
}
