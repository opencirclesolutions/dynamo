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

	private SystemPropertyUtils() {
	}

	/**
	 * Whether to allow data export from tables
	 * 
	 * @return
	 */
	public static boolean allowTableExport() {
		return Boolean.getBoolean(DynamoConstants.SP_ALLOW_TABLE_EXPORT);
	}

	/**
	 * 
	 * @return the CSV separator character
	 */
	public static String getExportCsvSeparator() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ";");
	}

	/**
	 * 
	 * @return the CSV quote character
	 */
	public static String getExportCsvQuoteChar() {
		return System.getProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "\"");
	}

	/**
	 * The default currency symbol
	 * 
	 * @return
	 */
	public static String getDefaultCurrencySymbol() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_CURRENCY_SYMBOL, "€");
	}

	/**
	 * The default date format
	 * 
	 * @return
	 */
	public static String getDefaultDateFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_DATE_FORMAT, "dd-MM-yyyy");
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
	 * The default decimal precision
	 * 
	 * @return
	 */
	public static int getDefaultDecimalPrecision() {
		return Integer.getInteger(DynamoConstants.SP_DECIMAL_PRECISION, DEFAULT_DECIMAL_PRECISION);
	}

	/**
	 * The default number of rows in a list select component. Also used as the default for
	 * collection tables
	 * 
	 * @return
	 */
	public static int getDefaultListSelectRows() {
		return Integer.getInteger(DynamoConstants.SP_DEFAULT_LISTSELECT_ROWS, DEFAULT_LISTSELECT_ROWS);
	}

	/**
	 * The default locale - this is used mainly for number formatting. We use the German locale here
	 * since this ensure the use of the comma as the decimal separator
	 * 
	 * @return
	 */
	public static String getDefaultLocale() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_LOCALE, "de");
	}

	/**
	 * The locale used for determining month names inside date components
	 * 
	 * @return
	 */
	public static String getDateLocale() {
		return System.getProperty(DynamoConstants.SP_DATE_LOCALE, "en");
	}

	/**
	 * The default date/time format
	 * 
	 * @return
	 */
	public static String getDefaultTimeFormat() {
		return System.getProperty(DynamoConstants.SP_DEFAULT_TIME_FORMAT, "HH:mm:ss");
	}

	/**
	 * The default maximum number of items to display in an entity lookup field when it is in
	 * multiple select mode
	 * 
	 * @return
	 */
	public static int getLookupFieldMaxItems() {
		return Integer.getInteger(DynamoConstants.SP_LOOKUP_FIELD_MAX_ITEMS, DEFAULT_LOOKUP_FIELD_MAX_ITEMS);
	}

	/**
	 * Maximum number of rows in a non-streaming export
	 * 
	 * @return
	 */
	public static int getMaximumExportRowsNonStreaming() {
		return Integer.getInteger(DynamoConstants.SP_MAX_ROWS_NON_STREAMING, 15000);
	}

	/**
	 * Maximum number of rows in a streaming export
	 * 
	 * @return
	 */
	public static int getMaximumExportRowsStreaming() {
		return Integer.getInteger(DynamoConstants.SP_MAX_ROWS_STREAMING, 10000);
	}

	/**
	 * Maximum number of rows in a streaming export of a pivoted data set
	 * 
	 * @return
	 */
	public static int getMaximumExportRowsStreamingPivot() {
		return Integer.getInteger(DynamoConstants.SP_MAX_ROWS_STREAMING_PIVOTED, 30000);
	}

	/**
	 * Whether to include thousands groupings in edit mode
	 * 
	 * @return
	 */
	public static boolean useThousandsGroupingInEditMode() {
		return Boolean.getBoolean(DynamoConstants.SP_THOUSAND_GROUPING);
	}

}
