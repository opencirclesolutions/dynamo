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

import lombok.experimental.UtilityClass;

/**
 * Various constants that are used by the Dynamo framework.
 *
 * @author bas.rutten
 */
@UtilityClass
public class DynamoConstants {
	
	/**
	 * CSS class for the main layout for a base view
	 */
	public static final String CSS_BASE_VIEW_PARENT = "baseViewParent";

	/**
	 * CSS class for the button that is used as the header for a collapsible panel
	 */
	public static final String CSS_COLLAPSIBLE_PANEL_BUTTON = "collapsiblePanelButton";

	/**
	 * CSS class for composition layout
	 */
	public static final String CSS_COMPOSITION_LAYOUT = "compositionLayout";

	/**
	 * CSS class for remove button in details edit layout
	 */
	public static final String CSS_DETAIL_EDIT_LAYOUT_REMOVE_BUTTON = "detailsEditLayoutRemoveButton";

	/**
	 * CSS class for details edit layout
	 */
	public static final String CSS_DETAILS_EDIT_LAYOUT = "detailsEditLayout";

	/**
	 * CSS class for button bar in details edit layout that appears below the
	 * components
	 */
	public static final String CSS_DETAILS_EDIT_LAYOUT_BUTTONBAR = "detailsEditLayoutButtonBar";

	/**
	 * CSS class for button bar in details edit layout that appears on same line as
	 * the components
	 */
	public static final String CSS_DETAILS_EDIT_LAYOUT_BUTTONBAR_SAME = "detailsEditLayoutButtonBarSame";

	/**
	 * CSS class for a row in a details edit layout
	 */
	public static final String CSS_DETAILS_EDIT_LAYOUT_ROW = "detailsEditLayoutRow";

	/**
	 * CSS class for modal dialog
	 */
	public static final String CSS_DIALOG = "dynamoDialog";

	/**
	 * CSS class for modal dialog title
	 */
	public static final String CSS_DIALOG_TITLE = "dynamoDialogTitle";

	/**
	 * CSS class for download button
	 */
	public static final String CSS_DOWNLOAD_BUTTON = "downloadButton";

	/**
	 * CSS class for flex layouts
	 */
	public static final String CSS_DYNAMO_FLEX_ROW = "dynamoFlexRow";

	/**
	 * CSS class for editable grid layout
	 */
	public static final String CSS_EDITABLE_GRID_LAYOUT = "editableGridLayout";

	/**
	 * CSS class for grid details panel
	 */
	public static final String CSS_GRID_DETAILS_PANEL = "gridDetailsPanel";

	/**
	 * CSS class for form layout that is used for grouping components together on
	 * same row
	 */
	public static final String CSS_GROUP_TOGETHER_LAYOUT = "groupTogether";

	/**
	 * CSS class for horizontal display layout
	 */
	public static final String CSS_HORIZONTAL_DISPLAY_LAYOUT = "horizontalDisplayLayout";

	/**
	 * CSS class for image previews
	 */
	public static final String CSS_IMAGE_PREVIEW = "dynamoImagePreview";

	/**
	 * CSS class for last visited menu item
	 */
	public static final String CSS_LAST_VISITED = "lastVisited";

	/**
	 * CSS class for the main edit layout in a SearchLayout
	 */
	public static final String CSS_MAIN_EDIT_LAYOUT = "mainEditLayout";

	/**
	 * CSS class for main search layout
	 */
	public static final String CSS_MAIN_SEARCH_LAYOUT = "mainSearchLayout";

	/**
	 * CSS class for ModelBasedEditForm
	 */
	public static final String CSS_MODEL_BASED_EDIT_FORM = "modelBasedEditForm";

	/**
	 * CSS class for edit form main layout
	 */
	public static final String CSS_MODEL_BASED_EDIT_FORM_MAIN = "modelBasedEditFormMain";

	/**
	 * CSS class for panel
	 */
	public static final String CSS_PANEL = "dynamoPanel";

	/**
	 * CSS class for dialog title
	 */
	public static final String CSS_PANEL_TITLE = "dynamoPanelTitle";

	/**
	 * CSS class for parent row in tree grid
	 */
	public static final String CSS_PARENT_ROW = "dynamoParentRow";

	/**
	 * CSS class for search form layout
	 */
	public static final String CSS_SEARCH_FORM_LAYOUT = "searchFormLayout";

	/**
	 * CSS class for search layout search form wrapper
	 */
	public static final String CSS_SEARCH_FORM_WRAPPER = "searchFormWrapper";

	/**
	 * CSS class for search results layout
	 */
	public static final String CSS_SEARCH_RESULTS_LAYOUT = "searchResultsLayout";

	/**
	 * CSS class for simple edit layout
	 */
	public static final String CSS_SIMPLE_EDIT_LAYOUT = "simpleEditLayout";

	/**
	 * CSS class for simple search layout
	 */
	public static final String CSS_SIMPLE_SEARCH_LAYOUT = "simpleSearchLayout";

	/**
	 * CSS class for split layout
	 */
	public static final String CSS_SPLIT_LAYOUT = "splitLayout";

	/**
	 * CSS class split layout left side
	 */
	public static final String CSS_SPLIT_LAYOUT_LEFT = "splitLayoutLeft";

	/**
	 * CSS class split layout right side
	 */
	public static final String CSS_SPLIT_LAYOUT_RIGHT = "splitLayoutRight";

	/**
	 * CSS class for tab layout 
	 */
	public static final String CSS_TAB_LAYOUT = "tabLayout";

	/**
	 * CSS class for the displayed page in a tab layout
	 */
	public static final String CSS_TAB_LAYOUT_DISPLAY_PAGE = "tabLayoutDisplayPage";

	/**
	 * CSS class for the detail layout in a split layout
	 */
	public static final String CSS_SPLIT_LAYOUT_DETAIL_LAYOUT = "splitLayoutDetailLayout";

	/**
	 * CSS class for edit panel in split layout
	 */
	public static final String CSS_SPLIT_LAYOUT_EDIT_PANEL = "splitLayoutEditPanel";

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
	 * ID of the error view
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
	 * Name of the system property that is used to determine whether a clear button
	 * is visible for a component
	 */
	public static final String SP_DEFAULT_CLEAR_BUTTON_VISIBLE = "ocs.default.clear.button.visible";

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
	 * Name of the system property that is used to set the locale used for month
	 * names in date components
	 */
	public static final String SP_DEFAULT_DATE_LOCALE = "ocs.default.date.locale";

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
	 * Name of the system property that is used to determine whether columns in
	 * details edit grid are sortable
	 */
	public static final String SP_DEFAULT_DETAILS_GRID_SORTABLE = "ocs.default.details.grid.sortable";

	/**
	 * The default responsive step thresholds for edit forms
	 */
	public static final String SP_DEFAULT_EDIT_FORM_COLUMN_THRESHOLDS = "ocs.default.edit.form.column.thresholds";

	/**
	 * Name of the system property that is used to determine the default edit grid
	 * height
	 */
	public static final String SP_DEFAULT_EDIT_GRID_HEIGHT = "ocs.default.edit.grid.height";

	/**
	 * Name of the system property that is used to determine the representation of
	 * the value false
	 */
	public static final String SP_DEFAULT_FALSE_REPRESENTATION = "ocs.default.false.representation";

	/**
	 * Name of the system property that is used to determine the default grid
	 * height
	 */
	public static final String SP_DEFAULT_GRID_HEIGHT = "ocs.default.grid.height";

	/**
	 * Name of the system property that is used to determine the default group
	 * together mode
	 */
	public static final String SP_DEFAULT_GROUP_TOGETHER_MODE = "ocs.default.group.together.mode";

	/**
	 * Name of the system property that is used to determine the column width from
	 * grouping together
	 */
	public static final String SP_DEFAULT_GROUP_TOGETHER_WIDTH = "ocs.default.group.together.width";

	/**
	 * Name of the system property that is used to set the default locale
	 */
	public static final String SP_DEFAULT_LOCALE = "ocs.default.locale";

	/**
	 * Name of the system property that is used to determine the default maximum
	 * edit form width
	 */
	public static final String SP_DEFAULT_MAX_EDIT_FORM_WIDTH = "ocs.default.max.edit.form.width";

	/**
	 * Name of the system property that is used to determine the default maximum
	 * form width
	 */
	public static final String SP_DEFAULT_MAX_SEARCH_FORM_WIDTH = "ocs.default.max.search.form.width";

	/**
	 * Name of the system property that determines how long a message will remain
	 * visible (in milliseconds)
	 */
	public static final String SP_DEFAULT_MESSAGE_DISPLAY_TIME = "ocs.default.message.display.time";

	/**
	 * Name of the system property that is used to determine the default nesting
	 * depth
	 */
	public static final String SP_DEFAULT_NESTING_DEPTH = "ocs.default.entity.nesting.depth";

	/**
	 * Name of the system property that is used to specify the default number field
	 * mode
	 */
	public static final String SP_DEFAULT_NUMBER_FIELD_MODE = "ocs.default.number.field.mode";

	/**
	 * Name of the system property that is used to determine the default paging mode
	 */
	public static final String SP_DEFAULT_PAGING_MODE = "ocs.default.paging.mode";

	/**
	 * Name of the system property that is used to determine the default case
	 * sensitiveness for search
	 */
	public static final String SP_DEFAULT_SEARCH_CASE_SENSITIVE = "ocs.default.search.case.sensitive";

	/**
	 * Name of the system property that is used to determine the default grid
	 * height in a search dialog
	 */
	public static final String SP_DEFAULT_SEARCH_DIALOG_GRID_HEIGHT = "ocs.default.search.dialog.grid.height";

	/**
	 * The default responsive step thresholds for search forms
	 */
	public static final String SP_DEFAULT_SEARCH_FORM_COLUMN_THRESHOLDS = "ocs.default.search.form.column.thresholds";

	/**
	 * Name of the system property that is used to determine whether search is
	 * prefix only
	 */
	public static final String SP_DEFAULT_SEARCH_PREFIX_ONLY = "ocs.default.search.prefix.only";

	/**
	 * Name of the system property that is used to determine the default text area
	 * height
	 */
	public static final String SP_DEFAULT_TEXT_AREA_HEIGHT = "ocs.default.text.area.height";

	/**
	 * Name of the system property that is used to determine the default time format
	 */
	public static final String SP_DEFAULT_TIME_FORMAT = "ocs.default.time.format";

	/**
	 * Name of the system property that is used to determine the representation of
	 * the value true
	 */
	public static final String SP_DEFAULT_TRUE_REPRESENTATION = "ocs.default.true.representation";

	/**
	 * Name of the property that is used to enable view authorization
	 */
	public static final String SP_ENABLE_VIEW_AUTHORIZATION = "ocs.enable.view.authorization";

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
	 * System property that indicates whether to indent grids in input forms
	 */
	public static final String SP_INDENT_GRIDS_IN_FORM = "ocs.indent.grids";

	/**
	 * Name of the system property that indicates the default height of a list
	 * select component
	 */
	public static final String SP_LIST_SELECT_HEIGHT = "ocs.default.list.select.height";

	/**
	 * Name of the system property that indicates whether to display captions on the buttons
	 * in a lookup field
	 */
	public static final String SP_LOOKUP_FIELD_CAPTIONS = "ocs.default.lookupfield.captions";
	
	/**
	 * Name of the system property that indicates the maximum number of items to
	 * display in an entity lookup field in multiple select mode
	 */
	public static final String SP_LOOKUP_FIELD_MAX_ITEMS = "ocs.default.lookupfield.max.items";

	/**
	 * The number of rows that must be present in a result set before resorting to a
	 * streaming approach for Excel export
	 */
	public static final String SP_MAX_ROWS_BEFORE_STREAMING = "ocs.max.rows.before.streaming";

	/**
	 * Class name for the service locator (override to create a different service
	 * locator, e.g. to use a separate service locator for integration tests)
	 */
	public static final String SP_SERVICE_LOCATOR_CLASS_NAME = "ocs.service.locator.classname";

	/**
	 * System property that indicates whether to use the thousands grouping
	 * separator
	 */
	public static final String SP_THOUSAND_GROUPING = "ocs.default.thousands.grouping";

	/**
	 * The name of the system property that is used to determine whether to trim
	 * white space for text inputs
	 */
	public static final String SP_TRIM_SPACES = "ocs.trim.spaces";

	/**
	 * The name of the database function used to replace accents
	 */
	public static final String SP_UNACCENT_FUNCTION_NAME = "ocs.unaccent.function.name";

	/**
	 * Indicates whether to use the browser time zone for formatting zoned date
	 * times
	 */
	public static final String SP_USE_BROWSER_TIME_ZONE = "ocs.use.browser.time.zone";

	/**
	 * Indicates whether to use the display name as the input prompt by default
	 */
	public static final String SP_USE_DEFAULT_PROMPT_VALUE = "ocs.use.default.prompt.value";

	/**
	 * Indicates whether to use selection check boxes for multiple selection in grid
	 */
	public static final String SP_USE_GRID_SELECTION_CHECK_BOXES = "ocs.use.grid.selection.checkboxes";
	
	/**
	 * Whether to use thousands grouping in XLS files
	 */
	public static final String SP_XLS_THOUSANDS_GROUPING = "ocs.xls.thousands.grouping";

	/**
	 * Whether the auto-filling of forms by using an AI service is supported
	 */
	public static final String SP_FORM_AUTO_FILL_ENABLED = "ocs.form.auto-fill.enabled";

	/**
	 * The default AI service to use for form filling
	 */
	public static final String SP_DEFAULT_AI_SERVICE = "ocs.default.ai.service";

}
