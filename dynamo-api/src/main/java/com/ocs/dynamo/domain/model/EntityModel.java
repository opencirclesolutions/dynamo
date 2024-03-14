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
package com.ocs.dynamo.domain.model;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * An interface representing a model that contains an entity's metadata
 * 
 * @author bas.rutten
 * @param <T> the type of the entity
 */
public interface EntityModel<T> {

	/**
	 * Auto-fill instructions for AI
	 */
	String AUTO_FILL_INSTRUCTIONS = "autoFillInstructions";

	/**
	 * Allowed extensions (for a file upload)
	 */
	String ALLOWED_EXTENSIONS = "allowedExtensions";

	/**
	 * Used to define an attribute group in the message bundle
	 */
	String ATTRIBUTE_GROUP = "attributeGroup";

	/**
	 * The names of the attributes inside an attribute group
	 */
	String ATTRIBUTE_NAMES = "attributeNames";

	/**
	 * The (default) order of the attributes
	 */
	String ATTRIBUTE_ORDER = "attributeOrder";

	/**
	 * Cascade attribute
	 */
	String CASCADE = "cascade";

	/**
	 * Cascade filter path (the attribute to filter on the receiving side of a
	 * cascade action)
	 */
	String CASCADE_FILTER_PATH = "cascadeFilterPath";

	/**
	 * Cascade mode (when to apply cascading - search, edit, or both)
	 */
	String CASCADE_MODE = "cascadeMode";

	/**
	 * To completely turn off cascading for an attribute
	 */
	String CASCADE_OFF = "cascadeOff";

	/**
	 * Whether the clear button is visible
	 */
	String CLEAR_BUTTON_VISIBLE = "clearButtonVisible";

	/**
	 * Whether the attribute (of type MASTER, DETAIL or ELEMENT_TABLE) can be edited
	 * when inside an edit form
	 */
	String COMPLEX_EDITABLE = "complexEditable";

	/**
	 * Whether an amount represents a currency
	 */
	String CURRENCY = "currency";

	/**
	 * The currency symbol to use
	 */
	String CURRENCY_SYMBOL = "currencySymbol";

	/**
	 * Custom setting
	 */
	String CUSTOM = "custom";

	/**
	 * Type of custom setting
	 */
	String CUSTOM_TYPE = "customType";

	/**
	 * Value of custom setting
	 */
	String CUSTOM_VALUE = "customValue";

	/**
	 * The date type (date, time, or time stamp) of a value from java.time.*
	 */
	String DATE_TYPE = "dateType";

	/**
	 * The name of the default group for all attributes for which no explicit group
	 * is specified
	 */
	String DEFAULT_GROUP = "ocs.default.attribute.group";

	/**
	 * Default value when creating new entity
	 */
	String DEFAULT_VALUE = "defaultValue";

	/**
	 * Description (used for tool tip)
	 */
	String DESCRIPTION = "description";

	/**
	 * Display format for date, time, or time stamp fields
	 */
	String DISPLAY_FORMAT = "displayFormat";

	/**
	 * Display name (used as title/caption)
	 */
	String DISPLAY_NAME = "displayName";

	/**
	 * Display name plural form (used as title above grids/lists)
	 */
	String DISPLAY_NAME_PLURAL = "displayNamePlural";

	/**
	 * The display property (used to determine what to display when using the entity
	 * inside a lookup component or a grid)
	 */
	String DISPLAY_PROPERTY = "displayProperty";

	/**
	 * The editable mode (EDITABLE, READ_ONLY, or CREATE_ONLY)
	 */
	String EDITABLE = "editable";

	/**
	 * Whether the property is embedded
	 */
	String EMBEDDED = "embedded";

	/**
	 * The String representation for the boolean "false" value
	 */
	String FALSE_REPRESENTATION = "falseRepresentation";

	/**
	 * The property to use for filtering when using paged mode for lookup
	 */
	String FILTER_PROPERTY = "filterProperty";

	/**
	 * Attribute order in grid
	 */
	String GRID_ATTRIBUTE_ORDER = "gridAttributeOrder";

	/**
	 * The type of component to use in editable grid
	 */
	String GRID_SELECT_MODE = "gridSelectMode";

	/**
	 * Names of other attributes that must appear on the same line inside an edit
	 * form
	 */
	String GROUP_TOGETHER_WITH = "groupTogetherWith";

	/**
	 * Whether to ignore the attribute when it should normally be added to a search
	 * filters
	 */
	String IGNORE_IN_SEARCH_FILTER = "ignoreInSearchFilter";

	/**
	 * Whether a (BLOB) field represents an image
	 */
	String IMAGE = "image";
	
	/**
	 * Whether to display button captions in lookup fields
	 */
	String LOOKUP_FIELD_CAPTIONS = "lookupFieldCaptions";

	/**
	 * Whether the attribute is the main attribute
	 */
	String MAIN = "main";

	/**
	 * The maximum length of the items inside an element collection
	 */
	String MAX_LENGTH = "maxLength";

	/**
	 * The maximum string length of the textual representation inside a grid
	 */
	String MAX_LENGTH_IN_GRID = "maxLengthInGrid";

	/**
	 * The maximum value of the numeric items inside an element collection
	 */
	String MAX_VALUE = "maxValue";

	/**
	 * Message key
	 */
	String MESSAGE_KEY = "messageKey";

	/**
	 * The minimum length of the numeric items inside an element collection
	 */
	String MIN_LENGTH = "minLength";

	/**
	 * The minimum value of the items inside an element collection
	 */
	String MIN_VALUE = "minValue";

	/**
	 * The multiple select mode to use inside a search dialog
	 */
	String MULTI_SELECT_MODE = "multiSelectMode";

	/**
	 * Whether to allow searching for multiple values in case of a MASTER attribute
	 */
	String MULTIPLE_SEARCH = "multipleSearch";

	/**
	 * Whether this property is navigable (in view mode or inside a grid, a link
	 * will be rendered)
	 */
	String NAVIGABLE = "navigable";

	/**
	 * The default nesting depth for nested entity models
	 */
	String NESTING_DEPTH = "nestingDepth";

	/**
	 * The number field mode
	 */
	String NUMBER_FIELD_MODE = "numberFieldMode";

	/**
	 * The number field step
	 */
	String NUMBER_FIELD_STEP = "numberFieldStep";

	/**
	 * The paging type for selection components
	 */
	String PAGING_MODE = "pagingMode";

	/**
	 * Whether to include a percentage sign to a numerical field (cosmetic only)
	 */
	String PERCENTAGE = "percentage";

	/**
	 * The decimal precision
	 */
	String PRECISION = "precision";

	/**
	 * The prompt value that appears inside empty input fields
	 */
	String PROMPT = "prompt";

	/**
	 * Indicates that quick add functionality is enabled
	 */
	String QUICK_ADD_ALLOWED = "quickAddAllowed";

	/**
	 * Indicates whether the property is read-only. This is deprecated but left for
	 * convenience - please use "editable" instead
	 */
	String READ_ONLY = "readOnly";

	/**
	 * The path to actually search for (replaces the standard search path)
	 */
	String REPLACEMENT_SEARCH_PATH = "replacementSearchPath";

	/**
	 * The path to actually use when sorting
	 */
	String REPLACEMENT_SORT_PATH = "replacementSortPath";

	/**
	 * Whether this attribute is required when performing a search
	 */
	String REQUIRED_FOR_SEARCHING = "requiredForSearching";

	/**
	 * Attribute order in search form
	 */
	String SEARCH_ATTRIBUTE_ORDER = "searchAttributeOrder";

	/**
	 * Whether searching is case-sensitive
	 */
	String SEARCH_CASE_SENSITIVE = "searchCaseSensitive";

	/**
	 * Whether to search on just the date
	 */
	String SEARCH_DATE_ONLY = "searchDateOnly";

	/**
	 * Whether to search for exact values (in case of numbers and dates)
	 */
	String SEARCH_EXACT_VALUE = "searchForExactValue";

	/**
	 * Whether to only search on prefix values
	 */
	String SEARCH_PREFIX_ONLY = "searchPrefixOnly";

	/**
	 * The select mode in a search screen
	 */
	String SEARCH_SELECT_MODE = "searchSelectMode";

	/**
	 * Whether the field appears inside a search form
	 */
	String SEARCHABLE = "searchable";

	/**
	 * Indicates that a lookup field (rather than a combo box) must be used when
	 * selecting the component
	 */
	String SELECT_MODE = "selectMode";

	/**
	 * Whether to show the password in a password field
	 */
	String SHOW_PASSWORD = "showPassword";

	/**
	 * Default sort order for an entity
	 */
	String SORT_ORDER = "sortOrder";

	/**
	 * Whether it is possible to sort on an attribute
	 */
	String SORTABLE = "sortable";

	/**
	 * Text area height
	 */
	String TEXT_AREA_HEIGHT = "textAreaHeight";

	/**
	 * The text field mode - indicates whether to use a text field or text area for
	 * editing a String field
	 */
	String TEXTFIELD_MODE = "textFieldMode";

	/**
	 * Indicates whether when to use the thousand separator when formatting
	 * floating point numbers
	 */
	String THOUSANDS_GROUPING_MODE = "thousandsGroupingMode";

	/**
	 * Whether to trim excess spaces for text and text area fields
	 */
	String TRIM_SPACES = "trimSpaces";

	/**
	 * The textual representation of the boolean "TRUE" value
	 */
	String TRUE_REPRESENTATION = "trueRepresentation";

	/**
	 * Indicates that a value must be represented as a clickable URL (to an outside
	 * destination)
	 */
	String URL = "url";

	/**
	 * Indicated whether the field is visible
	 */
	String VISIBLE = "visible";

	/**
	 * Whether to show an attribute inside a grid
	 */
	String VISIBLE_IN_GRID = "visibleInGrid";

	/**
	 * Indicates whether to format a date field as a week code
	 */
	String WEEK = "week";

	/**
	 * Adds an attribute group
	 *
	 * @param attributeGroup the name of the attribute group
	 */
	void addAttributeGroup(String attributeGroup);

	/**
	 * Adds a new attribute model on the position of the given existing attribute
	 * model. The existing model will shift one position to the back of the list.
	 * When the existing model is not found the attribute will be added to the end of
	 * the list.
	 * 
	 * @param attributeGroup The group to which the attribute model should be
	 *                       registered
	 * @param model          The model of the attribute
	 * @param existingModel  The existing attribute model
	 */
	void addAttributeModel(String attributeGroup, AttributeModel model, AttributeModel existingModel);

	/**
	 * @return the attribute groups that are defined for this entity
	 */
	List<String> getAttributeGroups();

	/**
	 * Looks up an attribute model by its name
	 * 
	 * @param attributeName the name of the attribute
	 * @return the attribute model, or null if this is not found
	 */
	AttributeModel getAttributeModel(String attributeName);

	/**
	 * Returns an attribute model based on the value of its actualSortPath
	 * 
	 * @param actualSortPath the actual sort path
	 * @return the attribute model, or null if this is not found
	 */
	AttributeModel getAttributeModelByActualSortPath(String actualSortPath);

	/**
	 * @return an ordered list of all attribute models
	 */
	List<AttributeModel> getAttributeModels();

	/**
	 * Returns the attribute models for a certain group
	 * 
	 * @param group the caption of the group
	 * @return the attribute models that belong to this group
	 */
	List<AttributeModel> getAttributeModelsForGroup(String group);

	/**
	 * Returns the attribute models for a certain attribute type and type. Just one
	 * of the parameters is mandatory, when both are given both will be used in a
	 * boolean AND. Will also look at the generic type of the attribute, e.g.
	 * List<some generic type>.
	 * 
	 * @param attributeType the attribute type
	 * @param type          the type
	 * @return the list of attribute models
	 */
	List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type);

	/**
	 * @return an ordered list of all attribute models for display in the grid
	 */
	List<AttributeModel> getAttributeModelsSortedForGrid();

	/**
	 * @return an ordered list of all attribute models for use in a search form
	 */
	List<AttributeModel> getAttributeModelsSortedForSearch();

	/**
	 * @return the attribute models for which cascading has been defined
	 */
	List<AttributeModel> getCascadeAttributeModels();

	/**
	 * Return the textual description of an entity for the specified locale
	 * 
	 * @param locale the locale
	 * @return the description in the specified locale
	 */
	String getDescription(Locale locale);

	/**
	 * Returns the display name for a certain locale
	 * 
	 * @param locale the locale
	 * @return the display name in the specified locale
	 */
	String getDisplayName(Locale locale);

	/**
	 * Return the display name plural form of an entity for the specified locale
	 * 
	 * @param locale the locale
	 * @return the display name
	 */
	String getDisplayNamePlural(Locale locale);

	/**
	 * The name of the property that is used when displaying the entity inside a
	 * select component (like a combo box) or a grid
	 * 
	 * @return the name of the display property
	 */
	String getDisplayProperty();

	/**
	 * @return The class of the entity that this model is based on
	 */
	Class<T> getEntityClass();

	/**
	 * The name of the property that is used to filter inside a combo box or token
	 * component
	 * 
	 * @return the name of the filter property
	 */
	String getFilterProperty();

	/**
	 * @return the main attribute for this entity model
	 */
	AttributeModel getMainAttributeModel();

	/**
	 * 
	 * @return the maximum nesting depth up to which to process attributes
	 */
	int getNestingDepth();

	/**
	 * @return the full reference of this attribute model
	 */
	String getReference();

	/**
	 * @return all attribute models for all attributes that are required for
	 *         searching
	 */
	List<AttributeModel> getRequiredForSearchingAttributeModels();

	/**
	 * Get the default sort order
	 * 
	 * @return a map of attribute models for which sort orders are set
	 */
	Map<AttributeModel, Boolean> getSortOrder();

	/**
	 * Indicates whether an attribute group should be visible
	 * 
	 * @param group    the attribute group
	 * @param readOnly whether the group is in read-only mode
	 * @return <code>true</code> if the group is visible, false otherwise
	 */
	boolean isAttributeGroupVisible(String group, boolean readOnly);

	/**
	 * Indicates whether this is the "base" entity model for the class
	 * 
	 * @return true if this is the case, false otherwise
	 */
	boolean isBaseEntityModel();

	/**
	 * @return whether only the default attribute group is used
	 */
	boolean usesDefaultGroupOnly();

	/**
	 *
	 * @return the auto-fill instructions
	 */
	String getAutofillInstructions();
}
