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

import com.ocs.dynamo.constants.DynamoConstants;

/**
 * Utility methods for retrieving system property values
 * 
 * @author bas.rutten
 *
 */
public final class SystemPropertyUtils {

	private static final int DEFAULT_DECIMAL_PRECISION = 2;

	private static final int DEFAULT_LISTSELECT_ROWS = 3;

	private static final int DEFAULT_LOOKUP_FIELD_MAX_ITEMS = 3;

	private static final String DEFAULT_TRUE_REPRESENTATION = "true";

	private static final String DEFAULT_FALSE_REPRESENTATION = "false";

	/**
	 * 
	 * 
	 * @return whether export to Excel or CSV is allowed for all tables. If set
	 *         to <code>false</code> it will disable exporting for all tables.
	 *         You can selectively enable it for somet tables using the
	 *         FormOptions object
	 */
	public static boolean allowTableExport() {
		return Boolean.getBoolean(DynamoConstants.SP_ALLOW_TABLE_EXPORT);
	}

	/**
	 *Returns the default caption format
	 * 
	 * @return the default caption format
	 */
	public static String getDefaultCaptionFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_CAPTION_FORMAT,
				DynamoConstants.SP_DEFAULT_CAPTION_FORMAT_VAADIN);
	}

	/**
	 *
	 *
	 * @return the default currency symbol to use for decimale fields that display a
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
	 * The default date/time format
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
	 * 
	 * @return the default field width of edit fields within a form
	 */
	public static Integer getDefaultFieldWidth() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_FIELD_WIDTH);
	}

	/**
	 *
	 * @return the default width (in pixels) of the title label above an edit from
	 */
	public static int getDefaultFormTitleWidth() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_FORM_TITLE_WIDTH, 0);
	}

	/**
	 *
	 *
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
	 * 
	 * @return the default validation mode
	 */
	public static ValidationMode getDefaultValidationMode() {
		return ValidationMode
				.valueOf(System.getProperty(DynamoConstants.SP_DEFAULT_VALIDATION_MODE, "VALIDATE_DIRECTLY"));
	}

	/**
	 *
	 * @return the CSV quote character
	 */
	public static String getExportCsvQuoteChar() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "\"");
	}

	/**
	 * 
	 * @return the CSV separator character
	 */
	public static String getExportCsvSeparator() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ";");
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
	 * @return number of rows in a non-streaming export
	 */
	public static int getMaximumExportRowsNonStreaming() {
		return Integer.getInteger(DynamoConstants.SP_MAX_ROWS_NON_STREAMING, 15000);
	}

	/**
	 * 
	 * 
	 * @return maximum number of rows in a streaming export
	 */
	public static int getMaximumExportRowsStreaming() {
		return Integer.getInteger(DynamoConstants.SP_MAX_ROWS_STREAMING, 100_000);
	}

	/**
	 * 
	 * 
	 * @return maximum number of rows to include in a streaming pivoted export
	 */
	public static int getMaximumExportRowsStreamingPivot() {
		return Integer.getInteger(DynamoConstants.SP_MAX_ROWS_STREAMING_PIVOTED, 30000);
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
	 *         (hint) inside the component
	 */
	public static boolean isUseDefaultPromptValue() {
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
