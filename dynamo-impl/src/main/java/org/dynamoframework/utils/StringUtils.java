package org.dynamoframework.utils;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.experimental.UtilityClass;

import java.text.Normalizer;
import java.util.Locale;

/**
 * String utility functions
 *
 * @author Bas Rutten
 */
@UtilityClass
public final class StringUtils {

	private static final String EMAIL_PATTERN = "([\\w-\\.]+)@(?:[\\w]+\\.)(\\w){2,4}";

	private static final String HTTP = "http";

	/**
	 * Converts CamelCase string to human friendly
	 *
	 * @param camelCaseString the Camel Case string to convert
	 * @param capitalize      indicates whether to capitalize every word (if set to
	 *                        <code>false</code> then only the first word will be
	 *                        capitalized
	 * @return the result of the conversions
	 */
	public static String camelCaseToHumanFriendly(String camelCaseString, boolean capitalize) {
		if (camelCaseString == null) {
			return null;
		}

		String[] parts = splitCamelCase(camelCaseString);
		for (int i = 0; i < parts.length; i++) {
			if (capitalize) {
				parts[i] = capitalize(parts[i]);
			} else {
				parts[i] = i > 0 ? parts[i].toLowerCase() : capitalize(parts[i]);
			}
		}
		return String.join(" ", parts);
	}

	/**
	 * Capitalize a String
	 *
	 * @param string the String to capitalize
	 * @return the result of the capitalization
	 */
	private static String capitalize(String string) {
		if (string == null) {
			return null;
		}

		if (string.length() <= 1) {
			return string.toUpperCase();
		}

		return string.substring(0, 1).toUpperCase(Locale.ENGLISH) + string.substring(1);
	}

	/**
	 * Checks if a value is a valid email address - this is actually a very simple
	 * check that only checks for the @-sign
	 *
	 * @param value the value to check
	 */
	public static boolean isValidEmail(String value) {
		if (value == null) {
			return true;
		}
		return value.matches(EMAIL_PATTERN);
	}

	/**
	 * Indicates whether a word ends at position index in the string
	 *
	 * @param camelCaseString the string
	 * @param index           the index
	 * @return true if this is the case, false otherwise
	 */
	private static boolean isWordComplete(String camelCaseString, int index) {
		if (index == 0) {
			// Word can't end at the beginning
			return false;
		} else { // Word ends if next char isn't upper case
			if (!Character.isUpperCase(camelCaseString.charAt(index - 1))) {
				// Word ends if previous char wasn't upper case
				return true;
			} else {
				return index + 1 < camelCaseString.length() && !Character.isUpperCase(camelCaseString.charAt(index + 1));
			}
		}
	}

	/**
	 * Prepends the default protocol ("http://") to a value that represents a URL
	 *
	 * @param value the value
	 * @return the result of the action
	 */
	public static String prependProtocol(String value) {
		if (value == null) {
			return null;
		}

		if (!value.startsWith(HTTP)) {
			return HTTP + "://" + value;
		}
		return value;
	}

	/**
	 * Converts a propertyId to a human-friendly representation
	 *
	 * @param propertyId the ID of the property
	 * @return the result of the conversion
	 */
	public static String propertyIdToHumanFriendly(Object propertyId, boolean capitalize) {
		String string = propertyId.toString();
		if (string.isEmpty()) {
			return "";
		}

		// For nested properties, only use the last part
		int dotLocation = string.lastIndexOf('.');
		if (dotLocation > 0 && dotLocation < string.length() - 1) {
			string = string.substring(dotLocation + 1);
		}

		return camelCaseToHumanFriendly(string, capitalize);
	}

	/**
	 * Restricts a string value to the maximum length of a certain field
	 *
	 * @param value     the value to restrict
	 * @param clazz     the clazz on which the field is located
	 * @param fieldName the name of the field
	 * @return the result of the restriction
	 */
	public static String restrictToMaxFieldLength(String value, Class<?> clazz, String fieldName) {
		if (value == null) {
			return null;
		} else {
			int maxLength = ClassUtils.getMaxLength(clazz, fieldName);
			if (maxLength >= 0 && value.length() > maxLength) {
				value = value.substring(0, maxLength);
			}
		}
		return value;
	}

	/**
	 * Splits a CamelCase String into words
	 *
	 * @param camelCaseString the String to split
	 * @return the result of the split action
	 */
	private static String[] splitCamelCase(String camelCaseString) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < camelCaseString.length(); i++) {
			char c = camelCaseString.charAt(i);
			if (Character.isUpperCase(c) && isWordComplete(camelCaseString, i)) {
				sb.append(' ');
			}
			sb.append(c);
		}
		return sb.toString().split(" ");
	}

	public static String removeAccents(String input) {
		if (input == null) {
			return null;
		}

		String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
		return temp.replaceAll("[^\\p{ASCII}]", "");
	}

}
