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

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class StringUtils {

	private static final String EMAIL_PATTERN = "([\\w-\\.]+)@(?:[\\w]+\\.)(\\w){2,4}";

	private static final String HTTP = "http";

	public static final String HTML_LINE_BREAK = "<br/>";

	private StringUtils() {
		// private constructor
	}

	/**
	 * Converts CamelCase string to human friendly
	 *
	 * @param camelCaseString
	 *            the Camel Case string to convert
	 * @param capitalize
	 *            indicates whether to capitalize every word (if set to
	 *            <code>false</code> then only the first word will be
	 *            capitalized
	 * @return
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
		return Arrays.stream(parts).collect(Collectors.joining(" "));
	}

	/**
	 * Capitalize a String
	 *
	 * @param string
	 *            the String to capitalize
	 * @return
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

	public static String getHtmlLineBreak() {
		return HTML_LINE_BREAK;
	}

	/**
	 * Checks if an value is a valid email address - this is actually a very
	 * simple
	 * check that only checks for the @-sign
	 *
	 * @param value
	 *            the value to check
	 */
	public static boolean isValidEmail(String value) {
		if (value == null) {
			return true;
		}
		return value.matches(EMAIL_PATTERN);
	}

	/**
	 * Indicates whether a word ends at position i in the string
	 *
	 * @param camelCaseString
	 *            the string
	 * @param i
	 * @return
	 */
	private static boolean isWordComplete(String camelCaseString, int i) {
		if (i == 0) {
			// Word can't end at the beginning
			return false;
		} else if (!Character.isUpperCase(camelCaseString.charAt(i - 1))) {
			// Word ends if previous char wasn't upper case
			return true;
		} else if (i + 1 < camelCaseString.length() && !Character.isUpperCase(camelCaseString.charAt(i + 1))) {
			// Word ends if next char isn't upper case
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Prepends the default protocol ("http://") to a value that represents a
	 * URL
	 *
	 * @param value
	 *            the value
	 * @return
	 */
	public static String prependProtocol(String value) {
		if (value == null) {
			return value;
		}

		if (!value.startsWith(HTTP)) {
			return HTTP + "://" + value;
		}
		return value;
	}

	/**
	 * Converts a propertyId to a
	 *
	 * @param propertyId
	 *            the ID of the property
	 * @return
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
	 * Replaces all HTML line breaks by commas
	 *
	 * @param value
	 *            the string in which to replace all line breaks
	 * @return
	 */
	public static String replaceHtmlBreaks(String value) {
		if (value == null) {
			return null;
		}
		value = value.replaceAll("<br/>", ", ").trim();
		if (value.endsWith(",")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}

	/**
	 * Restricts a string value to the maximum length of a certain field
	 *
	 * @param value
	 * @param clazz
	 * @param fieldName
	 * @return
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
	 * @param camelCaseString
	 *            the String to split
	 * @return
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

}
