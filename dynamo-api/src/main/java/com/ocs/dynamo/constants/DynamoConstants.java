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
package com.ocs.dynamo.constants;

import java.util.Locale;

/**
 * Various constants that are used by the Dynamo framework.
 *
 * @author bas.rutten
 */
public final class DynamoConstants {

	/**
	 * Maximum cache size (for lazy query container)
	 */
	public static final int CACHE_SIZE = 10000;

	public static final String CSS_COLLAPSIBLE_PANEL_BUTTON = "collapsiblePanelButton";

	/**
	 * CSS style for flex layouts
	 */
	public static final String CSS_DYNAMO_FLEX_ROW = "dynamoFlexRow";

	/**
	 * CSS style for dialog
	 */
	public static final String CSS_DIALOG = "dynamoDialog";

	/**
	 * CSS style for dialog title
	 */
	public static final String CSS_DIALOG_TITLE = "dynamoDialogTitle";

	/**
	 * CSS style for dialog title
	 */
	public static final String CSS_PANEL_TITLE = "dynamoPanelTitle";

	/**
	 * CSS style for panel
	 */
	public static final String CSS_PANEL = "dynamoPanel";

	/**
	 * CSS style for image previews
	 */
	public static final String CSS_IMAGE_PREVIEW = "dynamoImagePreview";

	/**
	 * CSS class for last visited menu item
	 */
	public static final String CSS_LAST_VISITED = "lastVisited";

	/**
	 * 
	 */
	public static final String CSS_PARENT_ROW = "dynamoParentRow";

	/**
	 * Currency symbol
	 */
	public static final String CURRENCY_SYMBOL = "currencySymbol";

	/**
	 * The locale to use in Date components (can be different from the main locale)
	 */
	public static final String DATE_LOCALE = "dateLocale";

	/**
	 * The default locale
	 */
	public static final Locale DEFAULT_LOCALE = Locale.UK;

	/**
	 * 
	 */
	public static final String ERROR_VIEW = "errorView";

	/**
	 * The default ID field
	 */
	public static final String ID = "id";

	/**
	 * Additional ID field
	 */
	public static final String IDS = "ids";

	/**
	 * Intermediate precision for floating point calculations
	 */
	public static final int INTERMEDIATE_PRECISION = 10;

	/**
	 * The screen mode
	 */
	public static final String SCREEN_MODE = "screenMode";

	/**
	 * The name of the variable that keeps track of which tab is selected
	 */
	public static final String SELECTED_TAB = "selectedTab";

	/**
	 * Name of the system property that is used to determine if exporting of lists
	 * (inside grids) is allowed
	 */
	public static final String SP_ALLOW_LIST_EXPORT = "ocs.allow.list.export";

	/**
	 * Indicates whether to capitalize individual words in property names
	 */
	public static final String SP_CAPITALIZE_WORDS = "ocs.capitalize.words";

	/**
	 * Name of the system property that is used to set the locale used for month
	 * names in date components
	 */
	public static final String SP_DATE_LOCALE = "ocs.default.date.locale";

	/**
	 * Name of the system property that is used to determine the default decimal
	 * precision
	 */
	public static final String SP_DECIMAL_PRECISION = "ocs.default.decimal.precision";

	/**
	 * Name of the system property that is used to determined the default grid
	 * height
	 */
	public static final String SP_DEFAULT_GRID_HEIGHT = "ocs.default.grid.height";

	/**
	 * Name of the system property that is used to determine the default edit grid
	 * height
	 */
	public static final String SP_DEFAULT_EDIT_GRID_HEIGHT = "ocs.default.edit.grid.height";

	/**
	 * Name of the system property that is used to determined the default grid
	 * height in a search dialog
	 */
	public static final String SP_DEFAULT_SEARCH_DIALOG_GRID_HEIGHT = "ocs.default.search.dialog.grid.height";

	/**
	 * Name of the system property that is used to determine the default currency
	 * symbol
	 */
	public static final String SP_DEFAULT_CURRENCY_SYMBOL = "ocs.default.currency.symbol";

	/**
	 * Name of the system property that is used to determine the default date format
	 */
	public static final String SP_DEFAULT_DATE_FORMAT = "ocs.default.date.format";

	/**
	 * Name of the system property that is used to determine the default date/time
	 * (time stamp) format
	 */
	public static final String SP_DEFAULT_DATETIME_FORMAT = "ocs.default.datetime.format";

	/**
	 * Name of the system property that is used to determine the default date/time
	 * (timestamp) format with time zone
	 */
	public static final String SP_DEFAULT_DATETIME_ZONE_FORMAT = "ocs.default.datetime.zone.format";

	/**
	 * Name of the system property that is used to determine the default decimal
	 * precision
	 */
	public static final String SP_DEFAULT_DECIMAL_PRECISION = "ocs.default.decimal.precision";

	/**
	 * Name of the system property that is used to determine the representation of
	 * the value false
	 */
	public static final String SP_DEFAULT_FALSE_REPRESENTATION = "ocs.default.false.representation";

	/**
	 * Name of the system property that is used to set the default locale
	 */
	public static final String SP_DEFAULT_LOCALE = "ocs.default.locale";

	/**
	 * Name of the system property that determines how long a message will remain
	 * visible (in milliseconds)
	 */
	public static final String SP_DEFAULT_MESSAGE_DISPLAY_TIME = "ocs.default.message.display.time";

	/**
	 * Name of the system property that is used to determine the default case
	 * sensitiveness for search
	 */
	public static final String SP_DEFAULT_SEARCH_CASE_SENSITIVE = "ocs.default.search.case.sensitive";

	/**
	 * Name of the system property that is used to determine whether search is
	 * prefix only
	 */
	public static final String SP_DEFAULT_SEARCH_PREFIX_ONLY = "ocs.default.search.prefix.only";

	/**
	 * Name of the system property that is used to determine the default time format
	 */
	public static final String SP_DEFAULT_TIME_FORMAT = "ocs.default.time.format";

	/**
	 * Name of the system property that is used to determine the default text area
	 * height
	 */
	public static final String SP_DEFAULT_TEXT_AREA_HEIGHT = "ocs.default.text.area.height";

	/**
	 * Name of the system property that is used to determine the representation of
	 * the value true
	 */
	public static final String SP_DEFAULT_TRUE_REPRESENTATION = "ocs.default.true.representation";

	/**
	 * Name of the system property that is used as the CSV escape character when
	 * exporting
	 */
	public static final String SP_EXPORT_CSV_ESCAPE = "ocs.export.csv.escape";

	/**
	 * Name of the system property that is used as the CSV quote char when exporting
	 */
	public static final String SP_EXPORT_CSV_QUOTE = "ocs.export.csv.quote";

	/**
	 * Name of the system property that is used as the CSV separator when exporting
	 */
	public static final String SP_EXPORT_CSV_SEPARATOR = "ocs.export.csv.separator";

	/**
	 * Name of the property that is used to enable view authorization
	 */
	public static final String SP_ENABLE_VIEW_AUTHORIZATION = "ocs.enable.view.authorization";

	/**
	 * The minimum screen width (e.g. in pixels) that there must be for two columns
	 * to be displayed in forms
	 */
	public static final String SP_MINIMUM_TWO_COLUMN_WIDTH = "ocs.minimum.two.column.width";

	/**
	 * Name of the system property that indicates the maximum number of items to
	 * display in an entity lookup field in multiple select mode
	 */
	public static final String SP_LOOKUP_FIELD_MAX_ITEMS = "ocs.default.lookupfield.max.items";

	/**
	 * Class name for the service locator (override to create a different service
	 * locator, e.g. to use a separate service locator for integration tests)
	 */
	public static final String SP_SERVICE_LOCATOR_CLASS_NAME = "ocs.service.locator.classname";

	/**
	 * System property that indicates whether to use the thousands grouping
	 * separator in edit mode
	 */
	public static final String SP_THOUSAND_GROUPING = "ocs.edit.thousands.grouping";

	/**
	 * Indicates whether to use the display name as the input prompt by default
	 */
	public static final String SP_USE_DEFAULT_PROMPT_VALUE = "ocs.use.default.prompt.value";

	/**
	 * The name of the variable that is used to store the user
	 */
	public static final String USER = "user";

	/**
	 * The name of the variable that is used to store the user name in the session
	 */
	public static final String USER_NAME = "userName";

	public static final String CSS_MODEL_BASED_EDIT_FORM = "modelBasedEditForm";

	public static final String CSS_MODEL_BASED_EDIT_FORM_MAIN = "modelBasedEditFormMain";
	
	public static final String CSS_SIMPLE_EDIT_LAYOUT = "simpleEditLayout";

	public static final String CSS_EDITABLE_GRID_LAYOUT = "editableGridLayout";

	public static final String CSS_SPLIT_LAYOUT = "splitLayout";

	public static final String CSS_MAIN_SEARCH_LAYOUT = "mainSearchLayout";

	public static final String CSS_SEARCH_RESULTS_LAYOUT = "searchResultsLayout";

	public static final String CSS_COMPOSITION_LAYOUT = "compositionLayout";

	public static final String CSS_TAB_LAYOUT = "tabLayout";

	/**
	 * Constructor for OCSConstants.
	 */
	private DynamoConstants() {
	}
}
