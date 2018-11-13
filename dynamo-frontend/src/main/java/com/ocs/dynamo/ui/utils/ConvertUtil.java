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
package com.ocs.dynamo.ui.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.IntToDoubleConverter;
import com.ocs.dynamo.ui.converter.LocalDateWeekCodeConverter;
import com.ocs.dynamo.ui.converter.LongToDoubleConverter;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.converter.StringToLongConverter;

/**
 * Utility for converting between data types
 * 
 * @author bas.rutten
 *
 */
public final class ConvertUtil {

	private ConvertUtil() {
	}

	/**
	 * Converts a value to its presentation value
	 * 
	 * @param attributeModel the attribute model
	 * @param input          the input value
	 * @return
	 */
	public static Object convertToPresentationValue(AttributeModel attributeModel, Object input) {
		if (input == null) {
			return null;
		}

		Locale locale = VaadinUtils.getLocale();
		boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();
		boolean percentage = attributeModel.isPercentage();

		if (attributeModel.isWeek()) {
			LocalDateWeekCodeConverter converter = new LocalDateWeekCodeConverter();
			return converter.convertToPresentation((LocalDate) input, new ValueContext(locale));
		} else if (Integer.class.equals(attributeModel.getType())) {
			return VaadinUtils.integerToString(grouping, percentage, (Integer) input);
		} else if (Long.class.equals(attributeModel.getType())) {
			return VaadinUtils.longToString(grouping, percentage, (Long) input);
		} else if (BigDecimal.class.equals(attributeModel.getType())) {
			return VaadinUtils.bigDecimalToString(attributeModel.isCurrency(), attributeModel.isPercentage(), grouping,
					attributeModel.getPrecision(), (BigDecimal) input, locale);
		} else if (ZonedDateTime.class.equals(attributeModel.getType())) {
			ZonedDateTime zdt = (ZonedDateTime) input;
			return zdt.toLocalDateTime();
		}
		return input;
	}

	/**
	 * Converts the search value from the presentation to the model
	 * 
	 * @param attributeModel the attribute model that governs the conversion
	 * @param input          the search value to convert
	 * @return
	 */
	public static Result<? extends Object> convertToModelValue(AttributeModel am, Object value) {
		if (value == null) {
			return Result.ok(null);
		}

		boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();
		Locale locale = VaadinUtils.getLocale();

		if (am.isWeek()) {
			LocalDateWeekCodeConverter converter = new LocalDateWeekCodeConverter();
			Result<LocalDate> locDate = converter.convertToModel((String) value, new ValueContext(locale));
			return locDate;
		} else if (NumberUtils.isInteger(am.getType())) {
			if (value instanceof String) {
				StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping, false);
				return converter.convertToModel((String) value, new ValueContext(locale));
			} else if (value instanceof Double) {
				return new IntToDoubleConverter().convertToModel((Double) value, new ValueContext(locale));
			}
		} else if (NumberUtils.isLong(am.getType())) {
			if (value instanceof String) {
				StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
				return converter.convertToModel((String) value, new ValueContext(locale));
			} else if (value instanceof Double) {
				return new LongToDoubleConverter().convertToModel((Double) value, new ValueContext(locale));
			}
		} else if (BigDecimal.class.equals(am.getType())) {
			if (NumberSelectMode.TEXTFIELD.equals(am.getNumberSelectMode())) {
				BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(am.isCurrency(),
						am.isPercentage(), grouping, am.getPrecision(), SystemPropertyUtils.getDefaultCurrencySymbol());
				return converter.convertToModel((String) value, new ValueContext(locale));
			}
		} else if (ZonedDateTime.class.equals(am.getType())) {
			LocalDateTime ldt = (LocalDateTime) value;
			return Result.ok(ldt.atZone(ZoneId.systemDefault()));
		}
		return Result.ok(value);
	}
}
