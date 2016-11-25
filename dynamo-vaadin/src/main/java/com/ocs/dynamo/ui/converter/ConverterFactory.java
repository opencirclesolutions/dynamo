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

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.StringToIntegerConverter;
import com.vaadin.data.util.converter.StringToLongConverter;

public final class ConverterFactory {

	private ConverterFactory() {
		// hidden constructor
	}

	/**
	 * Create a converter for a certain type
	 * 
	 * @param clazz
	 *            the type
	 * @param attributeModel
	 *            the attribute model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Converter<String, T> createConverterFor(Class<T> clazz, AttributeModel attributeModel,
	        boolean grouping) {
		if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
			return (Converter<String, T>) createIntegerConverter(grouping);
		} else if (clazz.equals(Long.class) || clazz.equals(long.class)) {
			return (Converter<String, T>) createLongConverter(grouping);
		} else if (clazz.equals(BigDecimal.class)) {
			return (Converter<String, T>) createBigDecimalConverter(attributeModel.isCurrency(),
			        attributeModel.isPercentage(), grouping, attributeModel.getPrecision(),
			        SystemPropertyUtils.getDefaultCurrencySymbol());
		}
		return null;
	}

	/**
	 * Creates a BigDecimalConverter
	 * 
	 * @param currency
	 *            whether the field is a currency field
	 * @param percentage
	 * @param useGrouping
	 * @param precision
	 * @param currencySymbol
	 * @return
	 */
	public static BigDecimalConverter createBigDecimalConverter(boolean currency, boolean percentage,
	        boolean useGrouping, int precision, String currencySymbol) {
		if (currency) {
			return new CurrencyBigDecimalConverter(precision, useGrouping, currencySymbol);
		}
		if (percentage) {
			return new PercentageBigDecimalConverter(precision, useGrouping);
		}
		return new BigDecimalConverter(precision, useGrouping);
	}

	/**
	 * Creates a converter for converting between integer and String
	 * 
	 * @param useGrouping
	 * @return
	 */
	public static StringToIntegerConverter createIntegerConverter(boolean useGrouping) {
		return new GroupingStringToIntegerConverter(useGrouping);
	}

	/**
	 * Creates a converter for converting between long and String
	 * 
	 * @param useGrouping
	 * @return
	 */
	public static StringToLongConverter createLongConverter(boolean useGrouping) {
		return new GroupingStringToLongConverter(useGrouping);
	}
}
