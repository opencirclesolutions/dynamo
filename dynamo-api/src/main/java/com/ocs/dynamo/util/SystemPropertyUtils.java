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
import com.ocs.dynamo.domain.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility methods for retrieving system property values
 * 
 * @author bas.rutten
 *
 */
@Slf4j
public final class SystemPropertyUtils {

	private static final int DEFAULT_DECIMAL_PRECISION = 2;

	private static final String DEFAULT_FALSE_REPRESENTATION = "false";

	private static final int DEFAULT_LOOKUP_FIELD_MAX_ITEMS = 3;

	private static final int DEFAULT_MESSAGE_DISPLAY_TIME = 2000;

	private static final String DEFAULT_TRUE_REPRESENTATION = "true";

	private static final Properties properties = new Properties();

	static {
		try {
			InputStream resourceAsStream = SystemPropertyUtils.class.getClassLoader()
					.getResourceAsStream("application.properties");
			if (resourceAsStream != null) {
				properties.load(resourceAsStream);
			}
		} catch (IOException ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	/**
	 * 
	 * @return whether export of grid contents to Excel/CSV is allowed. This system
	 *         property can be used to either enable or disable this on the
	 *         application level.
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
	 * @return the value of the property
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
	 * @return the locale used for localization of date picker components
	 */
	public static Locale getDefaultDateLocale() {
		String localeString =  getStringProperty(DynamoConstants.SP_DEFAULT_DATE_LOCALE,
				DynamoConstants.DEFAULT_LOCALE.toString());
		return constructLocale(localeString);
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
		return getIntProperty(DynamoConstants.SP_DEFAULT_DECIMAL_PRECISION, DEFAULT_DECIMAL_PRECISION);
	}

	/**
	 * 
	 * @return whether a details grid is sortable by default
	 */
	public static boolean getDefaultDetailsGridSortable() {
		return getBooleanProperty(DynamoConstants.SP_DEFAULT_DETAILS_GRID_SORTABLE, false);
	}

	/**
	 * 
	 * @return the default column thresholds to use for determining the number of
	 *         columns in an edit form
	 */
	public static List<String> getDefaultEditColumnThresholds() {
		String temp = getStringProperty(DynamoConstants.SP_DEFAULT_EDIT_FORM_COLUMN_THRESHOLDS, "0px");
		return List.of(temp.split(","));
	}

	/**
	 * 
	 * @return the default edit grid height in pixels
	 */
	public static String getDefaultEditGridHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_EDIT_GRID_HEIGHT, "200px");
	}

	/**
	 * @return the default textual representation of the boolean literal "false"
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
	 * @return the default false representation
	 */
	public static String getDefaultFalseRepresentation(Locale locale) {
		return getStringProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION + "." + locale.getLanguage(), null);
	}

	/**
	 * 
	 * @return the default height of a search results grid
	 */
	public static String getDefaultGridHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_GRID_HEIGHT, "400px");
	}

	/**
	 * 
	 * @return the default "group together" mode. This determines how the fields
	 *         that are group together behave with respect to responsiveness
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
	 * @return the default height of a list select component
	 */
	public static String getDefaultListSelectHeight() {
		return getStringProperty(DynamoConstants.SP_LIST_SELECT_HEIGHT, "100px");
	}

	/**
	 *
	 * @return the default locale used for e.g. the decimal and thousands separators
	 */
	public static Locale getDefaultLocale() {
		String localeString = getStringProperty(DynamoConstants.SP_DEFAULT_LOCALE, DynamoConstants.DEFAULT_LOCALE.toString());
		return constructLocale(localeString);
	}

	private static Locale constructLocale(String localeString) {
		int split = localeString.indexOf("_");
		if (split > -1) {
			return new Locale.Builder().setLanguage(localeString.substring(0,split)
			).setRegion(localeString.substring(split+1)).build();
		}
		return new Locale.Builder().setLanguage(localeString).build();
	}

	/**
	 * @return the maximum number of selected items to display in a lookup field
	 *         description
	 */
	public static int getDefaultLookupFieldMaxItems() {
		return getIntProperty(DynamoConstants.SP_LOOKUP_FIELD_MAX_ITEMS, DEFAULT_LOOKUP_FIELD_MAX_ITEMS);
	}
	
	/**
	 * 
	 * @return the default setting for displaying button captions in lookup fields 
	 */
	public static VisibilityType getDefaultLookupFieldCaptions() {
		String s = getStringProperty(DynamoConstants.SP_LOOKUP_FIELD_CAPTIONS, "HIDE");
		return VisibilityType.valueOf(s);
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
	 *         message will be displayed by default
	 */
	public static Integer getDefaultMessageDisplayTime() {
		return getIntProperty(DynamoConstants.SP_DEFAULT_MESSAGE_DISPLAY_TIME, DEFAULT_MESSAGE_DISPLAY_TIME);
	}

	/**
	 * @return the default nesting depth for entity models
	 */
	public static int getDefaultNestingDepth() {
		return getIntProperty(DynamoConstants.SP_DEFAULT_NESTING_DEPTH, 2);
	}

	/**
	 * @return the default number field mode
	 */
	public static NumberFieldMode getDefaultNumberFieldMode() {
		String s = getStringProperty(DynamoConstants.SP_DEFAULT_NUMBER_FIELD_MODE, "TEXTFIELD");
		return NumberFieldMode.valueOf(s.toUpperCase());
	}

	/**
	 * 
	 * @return the default "paging" mode. This determines how to retrieve items in
	 *         UI components that maintain a list of items like combo boxes
	 */
	public static PagingMode getDefaultPagingMode() {
		String s = getStringProperty(DynamoConstants.SP_DEFAULT_PAGING_MODE, "NON_PAGED");
		return PagingMode.valueOf(s.toUpperCase());
	}

	/**
	 * @return whether searches on text fields will be case-sensitive by default
	 */
	public static boolean getDefaultSearchCaseSensitive() {
		return getBooleanProperty(DynamoConstants.SP_DEFAULT_SEARCH_CASE_SENSITIVE, false);
	}

	/**
	 * 
	 * @return the default thresholds for columns in a search form
	 */
	public static List<String> getDefaultSearchColumnThresholds() {
		String temp = getStringProperty(DynamoConstants.SP_DEFAULT_SEARCH_FORM_COLUMN_THRESHOLDS, "0px,650px,1300px");
		return List.of(temp.split(","));
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
		return getBooleanProperty(DynamoConstants.SP_DEFAULT_SEARCH_PREFIX_ONLY, false);
	}

	/**
	 * @return the default height of a text area (e.g. "1px")
	 */
	public static String getDefaultTextAreaHeight() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_TEXT_AREA_HEIGHT, "200px");
	}

	/**
	 * @return whether to include thousands grouping separators in edit mode
	 */
	public static ThousandsGroupingMode getDefaultThousandsGroupingMode() {
		return ThousandsGroupingMode
				.valueOf(getStringProperty(DynamoConstants.SP_THOUSAND_GROUPING, "ALWAYS").toUpperCase());
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
	 * @param locale the locale
	 * @return the true representation
	 */
	public static String getDefaultTrueRepresentation(Locale locale) {
		return getStringProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION + "." + locale.getLanguage(), null);
	}

	/**
	 * Looks up the value for an integer property
	 * 
	 * @param propertyName the name of the property
	 * @param defaultValue the default value
	 * @return the property value
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
	 * @return the maximum number of rows that a result set is allowed to have
	 *         before resorting to a streaming approach when doing Excel exports
	 */
	public static Integer getMaxExportRowsBeforeStreaming() {
		return getIntProperty(DynamoConstants.SP_MAX_ROWS_BEFORE_STREAMING, 1000);
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
	 * @return the property
	 */
	private static String getStringProperty(String propertyName, String defaultValue) {
		String sys = System.getProperty(propertyName);
		if (sys == null) {
			sys = properties.getProperty(propertyName, defaultValue);
		}
		return sys;
	}

	/**
	 * @return whether to capitalize every word in a property name
	 */
	public static boolean isCapitalizeWords() {
		return getBooleanProperty(DynamoConstants.SP_CAPITALIZE_WORDS, true);
	}

	/**
	 * 
	 * @return whether the clear button for a component is visible by default
	 */
	public static boolean isDefaultClearButtonVisible() {
		return getBooleanProperty(DynamoConstants.SP_DEFAULT_CLEAR_BUTTON_VISIBLE, false);
	}

	/**
	 * @return whether to trim spaces in text and text area fields
	 */
	public static boolean isDefaultTrimSpaces() {
		return getBooleanProperty(DynamoConstants.SP_TRIM_SPACES, false);
	}

	/**
	 * @return whether to indent grid and detail form components
	 */
	public static Boolean mustIndentGrids() {
		return getBooleanProperty(DynamoConstants.SP_INDENT_GRIDS_IN_FORM, true);
	}

	/**
	 * @return whether to use the browser time zone for formatting zoned date times
	 */
	public static boolean useBrowserTimezone() {
		return getBooleanProperty(DynamoConstants.SP_USE_BROWSER_TIME_ZONE, false);
	}

	/**
	 * @return whether to use the display name of an attribute as the "prompt" value
	 *         (hint/placeholder) inside the component
	 */
	public static boolean useDefaultPromptValue() {
		return getBooleanProperty(DynamoConstants.SP_USE_DEFAULT_PROMPT_VALUE, true);
	}

	/**
	 * @return whether to use check boxes for multiple selection in grids
	 */
	public static boolean useGridSelectionCheckBoxes() {
		return getBooleanProperty(DynamoConstants.SP_USE_GRID_SELECTION_CHECK_BOXES, true);
	}

	/**
	 * 
	 * @return whether to use thousands grouping in XLS export
	 */
	public static boolean useXlsThousandsGrouping() {
		return getBooleanProperty(DynamoConstants.SP_XLS_THOUSANDS_GROUPING, false);
	}
	
	public static String getUnAccentFunctionName() {
		return getStringProperty(DynamoConstants.SP_UNACCENT_FUNCTION_NAME, "");
	}

	public static boolean isFormAutofillEnabled() {
		return getBooleanProperty(DynamoConstants.SP_FORM_AUTO_FILL_ENABLED, false);
	}

	public static String getDefaultAiService() {
		return getStringProperty(DynamoConstants.SP_DEFAULT_AI_SERVICE, "CHAT_GPT");
	}

	private SystemPropertyUtils() {
		// default constructor
	}
}
