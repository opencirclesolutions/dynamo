package com.ocs.dynamo.utils;

import java.text.DecimalFormat;
import java.util.Locale;

import org.springframework.util.StringUtils;

/**
 * Utility class for dealing with common functionality when pasting into fields
 * 
 * @author bas.rutten
 * 
 */
public class PasteUtils {

	private PasteUtils() {
	}

	/**
	 * Splits an input object into its separate values - the values can be
	 * separated by any kind of whitespace
	 * 
	 * @param input
	 * @return
	 */
	public static String[] split(Object input) {
		if (input == null) {
			return null;
		}
		String temp = input.toString();
		//
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < temp.length(); i++) {
			String s = new String(new char[] { temp.charAt(i) });
			if (org.apache.commons.lang.StringUtils.isWhitespace(s)) {
				if (i == 0) {
					b.append("#");
				} else {
					String t = new String(new char[] { temp.charAt(i - 1) });
					if (org.apache.commons.lang.StringUtils.isWhitespace(t)) {
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
	 * Converts a string to an integer, conveniently removing all grouping
	 * separators
	 * 
	 * @param input
	 * @return
	 */
	public static Integer toInt(String input) {
		if (input == null) {
			return null;
		}
		input = input.replaceAll(",", "").replaceAll("\\.", "");
		return StringUtils.isEmpty(input) ? null : Integer.parseInt(input);
	}

	/**
	 * Translates the decimal separator in the input to the format that is
	 * appropriate for the provided Locale
	 * 
	 * Note - this only works for input that does not contain any grouping
	 * separators!
	 * 
	 * @param input
	 * @param locale
	 * @return
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
	 * @param input
	 * @return
	 */
	public static String stripSeparators(String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("\\.", "").replaceAll(",", "");
	}
}
