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
package com.ocs.dynamo.util;

import java.util.Locale;

import com.ocs.dynamo.constants.DynamoConstants;

/**
 * Utility methods for retrieving system property values
 * 
 * @author bas.rutten
 *
 */
public final class SystemPropertyUtils {

	private static final int DEFAULT_DECIMAL_PRECISION = 2;

	private static final String DEFAULT_FALSE_REPRESENTATION = "false";

	private static final int DEFAULT_LISTSELECT_ROWS = 3;

	private static final int DEFAULT_LOOKUP_FIELD_MAX_ITEMS = 3;

	private static final int DEFAULT_TEXTAREA_ROWS = 3;

	private static final String DEFAULT_TRUE_REPRESENTATION = "true";

	/**
	 * Returns whether export of grid contents to Excel/CSV is allowed. This system
	 * property can be used to either enable or disable this on the application
	 * level. YOu
	 * 
	 * @return
	 */
	public static boolean allowListExport() {
		return Boolean.getBoolean(DynamoConstants.SP_ALLOW_LIST_EXPORT);
	}

	/**
	 *
	 * @return the CSV escape character
	 */
	public static String getCsvEscapeChar() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_ESCAPE, "\"\"");
	}

	/**
	 *
	 * @return the CSV quote character
	 */
	public static String getCsvQuoteChar() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "\"");
	}

	/**
	 * 
	 * @return the CSV separator character
	 */
	public static String getCsvSeparator() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ";");
	}

	/**
	 *
	 *
	 * @return the default currency symbol to use for decimal fields that display a
	 *         currency
	 */
	public static String getDefaultCurrencySymbol() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_CURRENCY_SYMBOL, "€");
	}

	/**
	 * Return the default format for formatting dates
	 * 
	 * @return
	 */
	public static String getDefaultDateFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_DATE_FORMAT, "dd-MM-yyyy");
	}

	/**
	 * 
	 * @return the locale used for the month names inside date picker components
	 */
	public static String getDefaultDateLocale() {
		return System.getProperty(DynamoConstants.SP_DATE_LOCALE, getDefaultLocale());
	}

	/**
	 * The default date/time format (dd-MM-yyyy HH:mm:ss)
	 * 
	 * @return
	 */
	public static String getDefaultDateTimeFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_DATETIME_FORMAT, "dd-MM-yyyy HH:mm:ss");
	}

	/**
	 * The default date/time format with time zone
	 * 
	 * @return
	 */
	public static String getDefaultDateTimeWithTimezoneFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_DATETIME_ZONE_FORMAT, "dd-MM-yyyy HH:mm:ssZ");
	}

	/**
	 * The default decimal precision
	 * 
	 * @return
	 */
	public static int getDefaultDecimalPrecision() {
		return Integer.getInteger(DynamoConstants.SP_DECIMAL_PRECISION, DEFAULT_DECIMAL_PRECISION);
	}

	/**
	 * @return the default false representation
	 */
	public static String getDefaultFalseRepresentation() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION, DEFAULT_FALSE_REPRESENTATION);
	}

	/**
	 * Looks up the default false representation for the provided locale. This will look for 
	 * a system property names ocs.default.false.representation.<lan> where <lan> is the language code
	 * of the locale, e.g. "en", "nl" 
	 * 
	 * @param locale
	 * @return
	 */
	public static String getDefaultFalseRepresentation(Locale locale) {
		return System.getProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION + "." + locale.getLanguage());
	}

	/**
	 * 
	 * @return the default field width of edit fields within a form
	 */
	public static Integer getDefaultFieldWidth() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_FIELD_WIDTH);
	}

	/**
	 * @return the default width (in pixels) of the title label above an edit from
	 */
	public static int getDefaultFormTitleWidth() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_FORM_TITLE_WIDTH, 0);
	}

	/**
	 * @return the default number of rows in a list select component. Also used as
	 *         the default for collection tables
	 */
	public static int getDefaultListSelectRows() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_LISTSELECT_ROWS, DEFAULT_LISTSELECT_ROWS);
	}

	/**
	 *
	 * @return the default locale used for e.g. the decimal and thousands separators
	 */
	public static String getDefaultLocale() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_LOCALE, DynamoConstants.DEFAULT_LOCALE.toString());
	}

	/**
	 * @return whether searches on text fields will be default be case insensitive
	 */
	public static boolean getDefaultSearchCaseSensitive() {
		return Boolean.getBoolean(DynamoConstants.SP_DEFAULT_SEARCH_CASE_SENSITIVE);
	}

	/**
	 * @return default search prefix only. False if not specified
	 */
	public static boolean getDefaultSearchPrefixOnly() {
		return Boolean.getBoolean(DynamoConstants.SP_DEFAULT_SEARCH_PREFIX_ONLY);
	}

	/**
	 * 
	 * @return the default number of rows for a textarea component
	 */
	public static int getDefaultTextAreaRows() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_TEXTAREA_ROWS, DEFAULT_TEXTAREA_ROWS);
	}

	/**
	 *
	 *
	 * @return the default format for formatting attributes of type LocalTime or
	 *         Java 7 dates that only consist of a time stamp
	 */
	public static String getDefaultTimeFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_TIME_FORMAT, "HH:mm:ss");
	}

	/**
	 * @return default true representation
	 */
	public static String getDefaultTrueRepresentation() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION, DEFAULT_TRUE_REPRESENTATION);
	}

	/**
	 * Looks up the default true representation for the provided locale. This will look for 
	 * a system property names ocs.default.true.representation.<lan> where <lan> is the language code
	 * of the locale, e.g. "en", "nl" 
	 * 
	 * @param locale
	 * @return
	 */
	public static String getDefaultTrueRepresentation(Locale locale) {
		return System.getProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION + "." + locale.getLanguage());
	}

	/**
	 * @return the maximum number of selected items to display in a lookup field
	 *         description
	 */
	public static int getLookupFieldMaxItems() {
		return Integer.getInteger(DynamoConstants.SP_LOOKUP_FIELD_MAX_ITEMS, DEFAULT_LOOKUP_FIELD_MAX_ITEMS);
	}

	/**
	 *
	 * @return the name of the service locator to use. Used internally by the
	 *         framework, highly unlikely this needs to be modified directly
	 */
	public static String getServiceLocatorClassName() {
		return System.getProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME,
				"com.ocs.dynamo.ui.SpringWebServiceLocator");
	}

	/**
	 * Whether to capitalize every word in a property name
	 * 
	 * @return
	 */
	public static boolean isCapitalizeWords() {
		String temp = System.getProperty(DynamoConstants.SP_CAPITALIZE_WORDS, "true");
		return Boolean.valueOf(temp);
	}

	/**
	 *
	 *
	 * @return whether to use the display name of an attribute as the "prompt" value
	 *         (hint/placeholder) inside the component
	 */
	public static boolean useDefaultPromptValue() {
		String temp = System.getProperty(DynamoConstants.SP_USE_DEFAULT_PROMPT_VALUE, "false");
		return Boolean.valueOf(temp);
	}

	/**
	 * @return whether to include thousands grouping separators in edit mode
	 */
	public static boolean useThousandsGroupingInEditMode() {
		return Boolean.getBoolean(DynamoConstants.SP_THOUSAND_GROUPING);
	}

	private SystemPropertyUtils() {
		// default constructor
	}
}
