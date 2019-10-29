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
package com.ocs.dynamo.ui.converter;

import java.math.BigDecimal;
import java.time.ZoneId;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToLongConverter;

public final class ConverterFactory {

	private static MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	private ConverterFactory() {
		// hidden constructor
	}

	/**
	 * Creates a BigDecimalConverter
	 * 
	 * @param currency       whether the field is a currency field
	 * @param percentage     whether to include a percentage sign
	 * @param useGrouping    whether to uses a thousands grouping
	 * @param precision      the desired decimal precision
	 * @param currencySymbol the currency symbol to include
	 * @return
	 */
	public static BigDecimalConverter createBigDecimalConverter(boolean currency, boolean percentage,
			boolean useGrouping, int precision, String currencySymbol) {
		String msg = messageService.getMessage("ocs.cannot.convert", VaadinUtils.getLocale());
		if (currency) {
			return new CurrencyBigDecimalConverter(msg, precision, useGrouping, currencySymbol);
		} else if (percentage) {
			return new PercentageBigDecimalConverter(msg, precision, useGrouping);
		} else {
			return new BigDecimalConverter(msg, precision, useGrouping);
		}
	}

	/**
	 * Create a converter for a certain type
	 * 
	 * @param clazz          the type
	 * @param attributeModel the attribute model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Converter<String, T> createConverterFor(Class<T> clazz, AttributeModel attributeModel,
			boolean grouping) {
		if (NumberUtils.isInteger(attributeModel.getType())) {
			return (Converter<String, T>) createIntegerConverter(grouping, attributeModel.isPercentage());
		} else if (NumberUtils.isLong(attributeModel.getType())) {
			return (Converter<String, T>) createLongConverter(grouping, attributeModel.isPercentage());
		} else if (NumberUtils.isDouble(attributeModel.getType())) {
			return (Converter<String, T>) createDoubleConverter(attributeModel.isCurrency(), grouping,
					attributeModel.isPercentage(), attributeModel.getPrecision(),
					SystemPropertyUtils.getDefaultCurrencySymbol());
		} else if (clazz.equals(BigDecimal.class)) {
			return (Converter<String, T>) createBigDecimalConverter(attributeModel.isCurrency(),
					attributeModel.isPercentage(), grouping, attributeModel.getPrecision(),
					SystemPropertyUtils.getDefaultCurrencySymbol());
		}
		return null;
	}

	/**
	 * Creates a converter for converting between integer and String
	 * 
	 * @param useGrouping whether to use the thousands grouping separator
	 * @return
	 */
	public static StringToIntegerConverter createIntegerConverter(boolean useGrouping, boolean percentage) {
		String msg = messageService.getMessage("ocs.cannot.convert", VaadinUtils.getLocale());
		return percentage ? new PercentageIntegerConverter(msg, useGrouping)
				: new GroupingStringToIntegerConverter(msg, useGrouping);
	}

	/**
	 * Creates a converter for converting between long and String
	 * 
	 * @param useGrouping whether to include a thousands grouping separator
	 * @param percentage  whether to include a percentage sign
	 * @return
	 */
	public static StringToLongConverter createLongConverter(boolean useGrouping, boolean percentage) {
		String msg = messageService.getMessage("ocs.cannot.convert", VaadinUtils.getLocale());
		return percentage ? new PercentageLongConverter(msg, useGrouping)
				: new GroupingStringToLongConverter(msg, useGrouping);
	}

	/**
	 * Creates a converter for converting between a double and a String
	 * 
	 * @param currency    whether to include a currency sign
	 * @param useGrouping whether to include a thousands grouping separator
	 * @param percentage  whether to include a percentage sign
	 * @param precision   the precision to use
	 * @return
	 */
	public static StringToDoubleConverter createDoubleConverter(boolean currency, boolean percentage,
			boolean useGrouping, int precision, String currencySymbol) {
		String msg = messageService.getMessage("ocs.cannot.convert", VaadinUtils.getLocale());
		if (currency) {
			return new CurrencyDoubleConverter(msg, precision, useGrouping, currencySymbol);
		} else if (percentage) {
			return new PercentageDoubleConverter(msg, precision, useGrouping);
		} else {
			return new GroupingStringToDoubleConverter(msg, precision, useGrouping);
		}
	}

	/**
	 * Creates a converter for a ZonedDateTime
	 * 
	 * @return
	 */
	public static ZonedDateTimeToLocalDateTimeConverter createZonedDateTimeConverter(ZoneId zoneId) {
		return new ZonedDateTimeToLocalDateTimeConverter(zoneId);
	}
}
