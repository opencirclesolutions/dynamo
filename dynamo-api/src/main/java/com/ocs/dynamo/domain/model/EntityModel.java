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
import java.util.Map;

/**
 * An interface representing a model that contains an entity's metadata
 * 
 * @author bas.rutten
 * @param <T>
 *            the type of the entity
 */
public interface EntityModel<T> {

	/**
	 * Allowed extensions (for a file upload)
	 */
	String ALLOWED_EXTENSIONS = "allowedExtensions";

	/**
	 * Used to define an attribute group in the message bundle
	 */
	String ATTRIBUTE_GROUP = "attributeGroup";

	/**
	 * Attribute names
	 */
	String ATTRIBUTE_NAMES = "attributeNames";

	/**
	 * Attribute order
	 */
	String ATTRIBUTE_ORDER = "attributeOrder";

	/**
	 * Cascade attribute
	 */
	String CASCADE = "cascade";

	/**
	 * To completely turn off cascading for an attribute
	 */
	String CASCADE_OFF = "cascadeOff";

	/**
	 * Cascade filter path (what to filter to receving end of the cascade on)
	 */
	String CASCADE_FILTER_PATH = "cascadeFilterPath";

	/**
	 * Cascade mode (when to apply cascading - search, edit, or both)
	 */
	String CASCADE_MODE = "cascadeMode";

	/**
	 * The "checkbox mode" for a boolean (either a checkbox or a toggle)
	 */
	String CHECKBOX_MODE = "checkboxMode";

	/**
	 * Whether the attribute (of type MASTER, DETAIL or ELEMENT_TABLE) can be edited
	 * in an edit form
	 */
	String COMPLEX_EDITABLE = "complexEditable";

	/**
	 * Whether an amount represents a currency
	 */
	String CURRENCY = "currency";

	/**
	 * The date type (date, time, or time stamp) of a value of type java.util.Date
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
	 * Display name plural form (used as title above tables/lists)
	 */
	String DISPLAY_NAME_PLURAL = "displayNamePlural";

	/**
	 * The display property (used to determine what to display when using the entity
	 * inside a lookup component or inside a table)
	 */
	String DISPLAY_PROPERTY = "displayProperty";

	/**
	 * Whether the property is embedded
	 */
	String EMBEDDED = "embedded";

	/**
	 * The String representation for the boolean "false" value
	 */
	String FALSE_REPRESENTATION = "falseRepresentation";

	/**
	 * Names of other attributes that must appear on the same line inside an edit
	 * form
	 */
	String GROUP_TOGETHER_WITH = "groupTogetherWith";

	/**
	 * Whether a field represents an image
	 */
	String IMAGE = "image";

	/**
	 * Whether locales are restricted
	 */
	String LOCALES_RESTRICTED = "localesRestricted";

	/**
	 * Whether the attribute is the main attribute
	 */
	String MAIN = "main";

	/**
	 * The maximum length of the items inside an element collection
	 */
	String MAX_LENGTH = "maxLength";

	/**
	 * The maximum string length of the textual representation inside a table
	 */
	String MAX_LENGTH_IN_TABLE = "maxLengthInTable";

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
	 * Whether to allow searching for multiple values in case of a MASTER attribute
	 */
	String MULTIPLE_SEARCH = "multipleSearch";

	/**
	 * Whether a link in a table is navigable
	 */
	String NAVIGABLE = "navigable";

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
	String QUICK_ADD_PROPERTY = "quickAddProperty";

	/**
	 * Indicates whether the property is read-only. This is deprecated but left for
	 * convenience - please use "editable" instead
	 */
	String READ_ONLY = "readOnly";

	/**
	 * The editable modus (EDITABLE, READ_ONLY, or CREATE_ONLY)
	 */
	String EDITABLE = "editable";

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
	 * Whether searching is case sensivite
	 */
	String SEARCH_CASE_SENSITIVE = "searchCaseSensitive";

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
	 * Whether the field is searchable
	 */
	String SEARCHABLE = "searchable";

	/**
	 * One or more styles for the field
	 */
	String STYLES = "styles";

	/**
	 * Indicates that a lookup field (rather than a combo box) must be used when
	 * selecting the component
	 */
	String SELECT_MODE = "selectMode";

	/**
	 * Type of number input field to use instead of a text field
	 */
	String NUMBER_SELECT_MODE = "numberSelectMode";

	/**
	 * Whether to show an attribute inside a table
	 */
	String SHOW_IN_TABLE = "showInTable";

	/**
	 * Default sort order for an entity
	 */
	String SORT_ORDER = "sortOrder";

	/**
	 * Whether it is possible to sort on an attribute
	 */
	String SORTABLE = "sortable";

	/**
	 * The text field mode - indicates whether to use a text field or text area for
	 * editing a String field
	 */
	String TEXTFIELD_MODE = "textFieldMode";

	/**
	 * Indicates whether to use thousand grouping characters in view mode
	 */
	String THOUSANDS_GROUPING = "thousandsGrouping";

	/**
	 * The textual representation of the boolean "TRUE" value
	 */
	String TRUE_REPRESENTATION = "trueRepresentation";

	/**
	 * Indicates that a value must be represented in a table as a clickable URL
	 */
	String URL = "url";

	/**
	 * Indicated whether the field is visible
	 */
	String VISIBLE = "visible";

	/**
	 * Indicates whether to format a date field as a week code
	 */
	String WEEK = "week";

	/**
	 * Indicates whether direct navigation to this linked entity is allowed
	 */
	String DIRECT_NAVIGATION = "directNavigation";

	/**
	 * Adds an attribute group
	 *
	 * @param attributeGroup
	 *            the name of the attribute group
	 */
	void addAttributeGroup(String attributeGroup);

	/**
	 * Adds a new attribute model on the position of the given existing attribute
	 * model. The existing model will shift one position to the back of the list.
	 * When the existing model is not found the attribute will added on the end of
	 * the list.
	 * 
	 * @param attributeGroup
	 *            The group to which the attribute model should be registered
	 * @param model
	 *            The model of the attribute
	 * @param existingModel
	 *            The existing attribute model
	 */
	void addAttributeModel(String attributeGroup, AttributeModel model, AttributeModel existingModel);

	/**
	 * Returns the attribute groups that are defined for this entity
	 * 
	 * @return
	 */
	List<String> getAttributeGroups();

	/**
	 * Looks up an attribute model by its name
	 * 
	 * @param attributeName
	 *            the name of the attribute
	 * @return
	 */
	AttributeModel getAttributeModel(String attributeName);

	/**
	 * Returns an ordered list of all attribute models
	 * 
	 * @return
	 */
	List<AttributeModel> getAttributeModels();

	/**
	 * Returns the attribute models for a certain group
	 * 
	 * @param group
	 *            the caption of the group
	 * @return
	 */
	List<AttributeModel> getAttributeModelsForGroup(String group);

	/**
	 * Returns the attribute models for a certain attribute type and type. Just one
	 * of the parameters is mandatory, when both are given both will be used in a
	 * boolean AND. Will also look at the generic type of a attribute, e.g.
	 * List<some generic type>.
	 * 
	 * @param attributeType
	 * @param type
	 * @return
	 */
	List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type);

	/**
	 * 
	 * @return the attribute models for which cascading has been defined
	 */
	List<AttributeModel> getCascadeAttributeModels();

	/**
	 * Textual description of the entity
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * The display name of the entity
	 * 
	 * @return
	 */
	String getDisplayName();

	/**
	 * The display name (plural) of the entity
	 * 
	 * @return
	 */
	String getDisplayNamePlural();

	/**
	 * The name of the property that is used when displaying the entity inside a
	 * select component (like a combo box) or a table
	 * 
	 * @return
	 */
	String getDisplayProperty();

	/**
	 * The class of the entity that this model is based on
	 * 
	 * @return
	 */
	Class<T> getEntityClass();

	/**
	 * @return The attribute model of the id
	 */
	AttributeModel getIdAttributeModel();

	/**
	 * Returns the main attribute
	 * 
	 * @return
	 */
	AttributeModel getMainAttributeModel();

	/**
	 * @retun the full reference of this attribute model
	 */
	String getReference();

	/**
	 * 
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
	 * @param group
	 *            the attribute group
	 * @return
	 */
	boolean isAttributeGroupVisible(String group, boolean readOnly);

	/**
	 * @return whether only the default attribute group is used
	 */
	boolean usesDefaultGroupOnly();
}
