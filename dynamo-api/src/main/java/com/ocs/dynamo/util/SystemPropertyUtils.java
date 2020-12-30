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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.GroupTogetherMode;

/**
 * Utility methods for retrieving system property values
 * 
 * @author bas.rutten
 *
 */
public final class SystemPropertyUtils {

	private static final int DEFAULT_DECIMAL_PRECISION = 2;

	private static final String DEFAULT_FALSE_REPRESENTATION = "false";

	private static final int DEFAULT_LOOKUP_FIELD_MAX_ITEMS = 3;

	private static final int DEFAULT_MESSAGE_DISPLAY_TIME = 2000;

	private static final String DEFAULT_TRUE_REPRESENTATION = "true";

	private static final Logger LOG = LoggerFactory.getLogger(SystemPropertyUtils.class);

	private static Properties properties = new Properties();

	/**
	 * Load properties from file
	 */
	static {
		try {
			InputStream resourceAsStream = SystemPropertyUtils.class.getClassLoader()
					.getResourceAsStream("application.properties");
			if (resourceAsStream != null) {
				properties.load(resourceAsStream);
			}
		} catch (IOException ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	/**
	 * Returns whether export of grid contents to Excel/CSV is allowed. This system
	 * property can be used to either enable or disable this on the application
	 * level.
	 * 
	 * @return
	 */
	public static boolean allowListExport() {
		return getBooleanProperty(DynamoConstants.SP_ALLOW_LIST_EXPORT, null);
	}

	/**
	 * Looks up the value of a boolean property by scanning the system properties
	 * first and falling back to application.properties
	 * 
	 * @param propertyName the name of the property
	 * @param defaultValue the default value
	 * @return
	 */
	private static Boolean getBooleanProperty(String propertyName, Boolean defaultValue) {
		String sys = System.getProperty(propertyName);
		if (sys == null) {
			sys = properties.getProperty(propertyName, defaultValue == null ? null : defaultValue.toString());
		}
		return Boolean.valueOf(sys);
	}

	/**
	 *
	 * @return the CSV escape character
	 */
	public static String getCsvEscapeChar() {
		return getStringProperty(DynamoConstants.SP_EXPORT_CSV_ESCAPE, "\"\"");
	}

	/**
	 *
	 * @return the CSV quote character
	 */
	public static String getCsvQuoteChar() {
		return getStringProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "\"");
	}

	/**
	 * 
	 * @return the CSV separator character
	 */
	public static String getCsvSeparator() {
		return getStringProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ";");
	}

	/**
	 *
	 *
	 * @return the default currency symbol to use for decimal fields that display a
	 *         currency
	 */
	public static String getDefaultCurrencySymbol() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_CURRENCY_SYMBOL, "€");
	}

	/**
	 * 
	 * @return the default format for formatting dates
	 */
	public static String getDefaultDateFormat() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_DATE_FORMAT, "dd-MM-yyyy");
	}

	/**
	 * 
	 * @return the locale used for the month names inside date picker components
	 */
	public static String getDefaultDateLocale() {
		return getStringProperty(DynamoConstants.SP_DATE_LOCALE, getDefaultLocale());
	}

	/**
	 * 
	 * @return the default date/time format (dd-MM-yyyy HH:mm:ss)
	 */
	public static String getDefaultDateTimeFormat() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_DATETIME_FORMAT, "dd-MM-yyyy HH:mm:ss");
	}

	/**
	 * 
	 * @return the default date/time format with time zone (dd-MM-yyyy HH:mm:ssZ)
	 */
	public static String getDefaultDateTimeWithTimezoneFormat() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_DATETIME_ZONE_FORMAT, "dd-MM-yyyy HH:mm:ssZ");
	}

	/**
	 * 
	 * @return the default decimal precision
	 */
	public static int getDefaultDecimalPrecision() {
		return getIntProperty(DynamoConstants.SP_DECIMAL_PRECISION, DEFAULT_DECIMAL_PRECISION);
	}

	/**
	 * 
	 * @return
	 */
	public static List<String> getDefaultEditColumnThresholds() {
		String temp = getStringProperty(DynamoConstants.SP_DEFAULT_EDIT_FORM_COLUMN_THRESHOLDS, "0px");
		return Lists.newArrayList(temp.split(","));
	}

	/**
	 * 
	 * @return the default edit grid height in pixels
	 */
	public static String getDefaultEditGridHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_EDIT_GRID_HEIGHT, "200px");
	}

	/**
	 * @return the default false representation
	 */
	public static String getDefaultFalseRepresentation() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION, DEFAULT_FALSE_REPRESENTATION);
	}

	/**
	 * Looks up the default false representation for the provided locale. This will
	 * look for a system property names ocs.default.false.representation.<lan> where
	 * <lan> is the language code of the locale, e.g. "en", "nl"
	 * 
	 * @param locale the locale
	 * @return
	 */
	public static String getDefaultFalseRepresentation(Locale locale) {
		return getStringProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION + "." + locale.getLanguage(), null);
	}

	/**
	 * 
	 * @return the default grid height in pixels
	 */
	public static String getDefaultGridHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_GRID_HEIGHT, "400px");
	}

	/**
	 * 
	 * @return the default "group together" mode. This determines how the grouped
	 *         together fields behave
	 */
	public static GroupTogetherMode getDefaultGroupTogetherMode() {
		String s = getStringProperty(DynamoConstants.SP_DEFAULT_GROUP_TOGETHER_MODE, "pixel");
		return GroupTogetherMode.valueOf(s.toUpperCase());
	}

	/**
	 * 
	 * @return the default threshold width for group together columns (when mode =
	 *         pixel)
	 */
	public static Integer getDefaultGroupTogetherWidth() {
		return getIntProperty(DynamoConstants.SP_DEFAULT_GROUP_TOGETHER_WIDTH, 300);
	}

	/**
	 *
	 * @return the default locale used for e.g. the decimal and thousands separators
	 */
	public static String getDefaultLocale() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_LOCALE, DynamoConstants.DEFAULT_LOCALE.toString());
	}

	/**
	 * 
	 * @return the default maximum edit form width
	 */
	public static String getDefaultMaxEditFormWidth() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_MAX_EDIT_FORM_WIDTH, "100%");
	}

	/**
	 * 
	 * @return the default maximum search form width
	 */
	public static String getDefaultMaxSearchFormWidth() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_MAX_SEARCH_FORM_WIDTH, "100%");
	}

	/**
	 * 
	 * @return the amount of time (in milliseconds) that an error or information
	 *         message will be displayed
	 */
	public static Integer getDefaultMessageDisplayTime() {
		return getIntProperty(DynamoConstants.SP_DEFAULT_MESSAGE_DISPLAY_TIME, DEFAULT_MESSAGE_DISPLAY_TIME);
	}

	/**
	 * 
	 * @return the default nesting depth for entity models
	 */
	public static int getDefaultNestingDepth() {
		return getIntProperty(DynamoConstants.SP_DEFAULT_NESTING_DEPTH, 2);
	}

	/**
	 * @return whether searches on text fields will be default be case insensitive
	 */
	public static boolean getDefaultSearchCaseSensitive() {
		return getBooleanProperty(DynamoConstants.SP_DEFAULT_SEARCH_CASE_SENSITIVE, null);
	}

	/**
	 * 
	 * @return the default thresholds for columns in a search form
	 */
	public static List<String> getDefaultSearchColumnThresholds() {
		String temp = getStringProperty(DynamoConstants.SP_DEFAULT_SEARCH_FORM_COLUMN_THRESHOLDS, "0px,650px,1300px");
		return Lists.newArrayList(temp.split(","));
	}

	/**
	 * 
	 * @return the default height of the results grid in a search dialog
	 */
	public static String getDefaultSearchDialogGridHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_SEARCH_DIALOG_GRID_HEIGHT, "300px");
	}

	/**
	 * @return default search prefix only. False if not specified
	 */
	public static boolean getDefaultSearchPrefixOnly() {
		return getBooleanProperty(DynamoConstants.SP_DEFAULT_SEARCH_PREFIX_ONLY, null);
	}

	/**
	 * @return the default height of a text area (e.g. "150px")
	 */
	public static String getDefaultTextAreaHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_TEXT_AREA_HEIGHT, "200px");
	}

	/**
	 * @return the default format for formatting attributes of type LocalTime or
	 *         Java 8 dates that only consist of a time stamp
	 */
	public static String getDefaultTimeFormat() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_TIME_FORMAT, "HH:mm:ss");
	}

	/**
	 * 
	 * @return the default true representation (if no further locale is specified)
	 */
	public static String getDefaultTrueRepresentation() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION, DEFAULT_TRUE_REPRESENTATION);
	}

	/**
	 * Looks up the default true representation for the provided locale. This will
	 * look for a system property names ocs.default.true.representation.<lan> where
	 * <lan> is the language code of the locale, e.g. "en", "nl"
	 * 
	 * @param locale
	 * @return
	 */
	public static String getDefaultTrueRepresentation(Locale locale) {
		return getStringProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION + "." + locale.getLanguage(), null);
	}

	/**
	 * Looks up the value for an integer property
	 * 
	 * @param propertyName the name of the property
	 * @param defaultValue the default value
	 * @return
	 */
	private static Integer getIntProperty(String propertyName, Integer defaultValue) {
		Integer sys = Integer.getInteger(propertyName);
		if (sys == null) {
			String s = properties.getProperty(propertyName, defaultValue == null ? null : defaultValue.toString());
			sys = s == null ? null : Integer.parseInt(s);
		}
		return sys;
	}

	/**
	 * @return the maximum number of selected items to display in a lookup field
	 *         description
	 */
	public static int getLookupFieldMaxItems() {
		return getIntProperty(DynamoConstants.SP_LOOKUP_FIELD_MAX_ITEMS, DEFAULT_LOOKUP_FIELD_MAX_ITEMS);
	}

	/**
	 *
	 * @return the name of the service locator to use. Used internally by the
	 *         framework, highly unlikely this needs to be modified directly
	 */
	public static String getServiceLocatorClassName() {
		return getStringProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME,
				"com.ocs.dynamo.ui.SpringWebServiceLocator");
	}

	/**
	 * Looks up the value of a String property by scanning the system properties
	 * first and falling back to application.properties
	 * 
	 * @param propertyName the name of the property
	 * @param defaultValue the default value
	 * @return
	 */
	private static String getStringProperty(String propertyName, String defaultValue) {
		String sys = System.getProperty(propertyName);
		if (sys == null) {
			sys = properties.getProperty(propertyName, defaultValue);
		}
		return sys;
	}

	/**
	 * 
	 * @return whether to capitalize every word in a property name
	 */
	public static boolean isCapitalizeWords() {
		return getBooleanProperty(DynamoConstants.SP_CAPITALIZE_WORDS, true);
	}

	/**
	 * 
	 * @return whether to indent grid and detail form components
	 */
	public static Boolean mustIndentGrids() {
		return getBooleanProperty(DynamoConstants.SP_INDENT_GRIDS_IN_FORM, true);
	}

	/**
	 * @return whether to use the display name of an attribute as the "prompt" value
	 *         (hint/placeholder) inside the component
	 */
	public static boolean useDefaultPromptValue() {
		return getBooleanProperty(DynamoConstants.SP_USE_DEFAULT_PROMPT_VALUE, true);
	}

	/**
	 * @return whether to include thousands grouping separators in edit mode
	 */
	public static boolean useThousandsGroupingInEditMode() {
		return getBooleanProperty(DynamoConstants.SP_THOUSAND_GROUPING, null);
	}

	/**
	 * 
	 * @return whether to use thousands grouping in XLS export
	 */
	public static boolean useXlsThousandsGrouping() {
		return getBooleanProperty(DynamoConstants.SP_XLS_THOUSANDS_GROUPING, false);
	}

	private SystemPropertyUtils() {
		// default constructor
	}
}
