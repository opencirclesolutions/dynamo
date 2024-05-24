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

import java.text.DecimalFormat;
import java.util.Locale;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for dealing with common functionality when pasting into fields
 * 
 * @author bas.rutten
 */
@UtilityClass
public final class PasteUtils {

	/**
	 * Splits an input object into its separate values - the values can be separated
	 * by any kind of whitespace
	 * 
	 * @param input the input string
	 * @return
	 */
	public static String[] split(Object input) {
		if (input == null) {
			return null;
		}
		String temp = input.toString();
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < temp.length(); i++) {
			String s = String.valueOf(temp.charAt(i));
			if (StringUtils.isWhitespace(s)) {
				if (i == 0) {
					b.append("#");
				} else {
					String value = String.valueOf(temp.charAt(i - 1));
					if (StringUtils.isWhitespace(value)) {
						b.append("#");
					}
				}
			}
			b.append(temp.charAt(i));
		}

		String[] result = b.toString().replaceAll("\\s+", " ").split(" ");
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].replace('#', ' ').trim();
		}
		return result;
	}

	/**
	 * Converts a string to an integer, removing all grouping
	 * separators
	 * 
	 * @param input the input string
	 * @return the resulting integer
	 */
	public static Integer toInt(String input) {
		if (input == null) {
			return null;
		}
		input = input.replace(",", "").replace(".", "");
		return StringUtils.isEmpty(input) ? null : Integer.parseInt(input);
	}

	/**
	 * Translates the decimal separator in the input to the format that is
	 * appropriate for the provided Locale Note - this only works for an input that
	 * does not contain any grouping separators
	 * 
	 * @param input the input string
	 * @param locale the locale
	 * @return the result of the conversion
	 */
	public static String translateSeparators(String input, Locale locale) {
		if (input == null) {
			return null;
		}

		DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(locale);
		char decimalSeparator = format.getDecimalFormatSymbols().getDecimalSeparator();
		if (decimalSeparator == '.') {
			input = input.replace(',', '.');
		} else {
			input = input.replace('.', ',');
		}
		return input;
	}

	/**
	 * Strips all separators (dots and commas) from the input
	 * 
	 * @param input the input string
	 * @return the result of the strip operation
	 */
	public static String stripSeparators(String input) {
		if (input == null) {
			return null;
		}
		return input.replace(".", "").replace(",", "");
	}
}
