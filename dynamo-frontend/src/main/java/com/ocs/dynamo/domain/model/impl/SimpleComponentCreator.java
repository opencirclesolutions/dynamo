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
import java.util.List;
import java.util.Locale;

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

	/**
	 * Creates a component based on an attribute model and context
	 * @param am the attribute model
	 * @param context the context
	 * @return the component
	 */
	Component createComponent(AttributeModel am, FieldCreationContext context);

	/**
	 * Adds date picker localization to a component
	 * 
	 * @param messageService the message service used for localization
	 * @param dateLocale the locale to us
	 * @return the date picker localization object
	 */
	default DatePickerI18n getDatePickerLocalization(MessageService messageService, Locale dateLocale) {
		DatePickerI18n dpi = new DatePickerI18n();
		String weekdays = messageService.getMessage("ocs.calendar.days", dateLocale);
		if (weekdays != null) {
			dpi.setWeekdays(List.of(weekdays.split(",")));
		}

		String weekdaysShort = messageService.getMessage("ocs.calendar.days.short", dateLocale);
		if (weekdaysShort != null) {
			dpi.setWeekdaysShort(List.of(weekdaysShort.split(",")));
		}

		String months = messageService.getMessage("ocs.calendar.months", dateLocale);
		if (months != null) {
			dpi.setMonthNames(List.of(months.split(",")));
		}

		dpi.setCancel(messageService.getMessage("ocs.calendar.cancel", dateLocale));
		dpi.setFirstDayOfWeek(Integer.parseInt(messageService.getMessage("ocs.calendar.first", dateLocale)));

		return dpi;
	}

	/**
	 * Adds converters for a TextField
	 * 
	 * @param <U>            the type of the value to convert to
	 * @param attributeModel the attribute model that is used to construct the
	 *                       component
	 * @param builder       the binding builder that is used to bind the component
	 */
	default <U> void addTextFieldConverters(AttributeModel attributeModel, BindingBuilder<U, String> builder) {
		builder.withNullRepresentation("");
		if (attributeModel.getType().equals(BigDecimal.class)) {
			builder.withConverter(ConverterFactory.createBigDecimalConverter(attributeModel.isCurrency(),
					attributeModel.isPercentage(), attributeModel.useThousandsGroupingInEditMode(),
					attributeModel.getPrecision(), attributeModel.getCurrencySymbol()));
		} else if (NumberUtils.isInteger(attributeModel.getType())) {
			builder.withConverter(ConverterFactory.createIntegerConverter(
					attributeModel.useThousandsGroupingInEditMode(), attributeModel.isPercentage()));
		} else if (NumberUtils.isLong(attributeModel.getType())) {
			builder.withConverter(ConverterFactory.createLongConverter(attributeModel.useThousandsGroupingInEditMode(),
					attributeModel.isPercentage()));
		} else if (NumberUtils.isDouble(attributeModel.getType())) {
			builder.withConverter(ConverterFactory.createDoubleConverter(attributeModel.isCurrency(),
					attributeModel.useThousandsGroupingInEditMode(), attributeModel.isPercentage(),
					attributeModel.getPrecision(), attributeModel.getCurrencySymbol()));
		} else if (String.class.equals(attributeModel.getType()) && attributeModel.isTrimSpaces()) {
			builder.withConverter(new TrimSpacesConverter());
		}
	}
}
