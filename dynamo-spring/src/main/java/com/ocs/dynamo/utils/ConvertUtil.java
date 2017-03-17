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
package com.ocs.dynamo.utils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import com.ocs.dynamo.ui.converter.WeekCodeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;

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
	 * @param attributeModel
	 *            the attribute model
	 * @param input
	 *            the input value
	 * @return
	 */
	public static Object convertToPresentationValue(AttributeModel attributeModel, Object input) {
		if (input == null) {
			return null;
		}

		Locale locale = VaadinUtils.getLocale();
		boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();

		if (attributeModel.isWeek()) {
			WeekCodeConverter converter = new WeekCodeConverter();
			return converter.convertToPresentation((Date) input, String.class, locale);
		} else if (Integer.class.equals(attributeModel.getType())) {
			return VaadinUtils.integerToString(grouping, (Integer) input);
		} else if (Long.class.equals(attributeModel.getType())) {
			return VaadinUtils.longToString(grouping, (Long) input);
		} else if (BigDecimal.class.equals(attributeModel.getType())) {
			return VaadinUtils.bigDecimalToString(attributeModel.isCurrency(), attributeModel.isPercentage(), grouping,
			        attributeModel.getPrecision(), (BigDecimal) input, locale);
		}
		return input;
	}

	/**
	 * Converts the search value from the presentation to the model
	 * 
	 * @param attributeModel
	 *            the attribute model that governs the conversion
	 * @param input
	 *            the search value to convert
	 * @return
	 */
	public static Object convertSearchValue(AttributeModel attributeModel, Object input) {
		if (input == null) {
			return null;
		}

		boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();
		Locale locale = VaadinUtils.getLocale();

		if (attributeModel.isWeek()) {
			WeekCodeConverter converter = new WeekCodeConverter();
			return converter.convertToModel((String) input, Date.class, locale);
		} else if (NumberUtils.isInteger(attributeModel.getType())) {
			if (NumberSelectMode.TEXTFIELD.equals(attributeModel.getNumberSelectMode())) {
				return VaadinUtils.stringToInteger(grouping, (String) input, locale);
			}
		} else if (NumberUtils.isLong(attributeModel.getType())) {
			if (NumberSelectMode.TEXTFIELD.equals(attributeModel.getNumberSelectMode())) {
				return VaadinUtils.stringToLong(grouping, (String) input, locale);
			}
		} else if (BigDecimal.class.equals(attributeModel.getType())) {
			if (NumberSelectMode.TEXTFIELD.equals(attributeModel.getNumberSelectMode())) {
				return VaadinUtils.stringToBigDecimal(attributeModel.isPercentage(), grouping,
				        attributeModel.isCurrency(), attributeModel.getPrecision(), (String) input, locale);
			}
		}
		return input;
	}
}
