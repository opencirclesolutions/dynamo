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

import java.time.ZoneId;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.FormatUtils;

import lombok.experimental.UtilityClass;

/**
 * Utilities for formatting values inside a grid. Mostly delegates to
 * FormatUtils
 * 
 * @author Bas Rutten
 *
 */
@UtilityClass
public final class GridFormatUtils {

	private static EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator()
			.getEntityModelFactory();

	private static MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * Extracts a field value from an object and formats it
	 * 
	 * @param am             the attribute model
	 * @param obj            the object from which to extract the value
	 * @param local          the locale
	 * @param zoneId         the time zone ID
	 * @param currencySymbol the currency symbol
	 * @return
	 */
	public static <T> String extractAndFormat(AttributeModel am, Object obj, Locale locale, ZoneId zoneId,
			String currencySymbol) {
		Object value = ClassUtils.getFieldValue(obj, am.getPath());
		String result = FormatUtils.formatPropertyValue(entityModelFactory, messageService, am, value, ", ", locale,
				zoneId, currencySymbol);
		return restrictToMaxLength(result, am);
	}

	/**
	 * Formats a property value
	 *
	 * @param entityModelFactory the entity model factory
	 * @param entityModel        the entity model
	 * @param model              the attribute model
	 * @param value              the property value
	 * @param locale             the locale to use
	 * @return
	 */
	public static <T> String formatPropertyValue(AttributeModel am, Object value, String separator, Locale locale,
			ZoneId zoneId, String currencySymbol) {
		String result = FormatUtils.formatPropertyValue(entityModelFactory, messageService, am, value, separator,
				locale, zoneId);
		return restrictToMaxLength(result, am);
	}

	/**
	 * Restricts a value to its maximum length defined in the attribute model
	 *
	 * @param input the input value
	 * @param am    the attribute model
	 * @return
	 */
	private static String restrictToMaxLength(String input, AttributeModel am) {
		if (am.getMaxLengthInGrid() != null && input != null && input.length() > am.getMaxLengthInGrid()) {
			return input.substring(0, am.getMaxLengthInGrid()) + "...";
		}
		return input;
	}

}
