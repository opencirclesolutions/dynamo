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
import java.util.Set;

/**
 * An attribute model represents how a certain attribute of an entity will be
 * behave in the user interface. This includes e.g. whether the attribute is
 * searchable, sortable, what kind of user interface component is used to edit
 * the attribute any many other aspects
 * 
 * @author bas.rutten
 *
 */
public interface AttributeModel extends Comparable<AttributeModel> {

	/**
	 * Adds a cascade option
	 * 
	 * @param cascadeTo
	 *            the path to the attribute to cascade to
	 * @param filterPath
	 *            the path used to filter on
	 * @param mode
	 *            the mode (search, edit, or both)
	 */
	void addCascade(String cascadeTo, String filterPath, CascadeMode mode);

	/**
	 * Adds a "group together with" attribute. These attributes mentioned as the
	 * "group together with" attributes will be rendered on the same line as the
	 * attribute for which this model is defined
	 * 
	 * @param path
	 *            the path to the attribute to group with
	 */
	void addGroupTogetherWith(String path);

	/**
	 * @return The allowed extensions for a LOB attribute
	 */
	Set<String> getAllowedExtensions();

	/**
	 * @return The attribute type (e.g. BASIC, MASTER, DETAIL) of the attribute
	 */
	AttributeType getAttributeType();

	/**
	 * 
	 * @return the attributes to cascade to when the value of this attribute
	 *         changes
	 */
	Set<String> getCascadeAttributes();

	/**
	 * Returns the path to filter on when applying a cascade
	 * 
	 * @param cascadeTo
	 *            the path of the property to which to apply cascading
	 * @return the path to filter on
	 */
	String getCascadeFilterPath(String cascadeTo);

	/**
	 * Returns the cascade mode
	 * 
	 * @param cascadeTo
	 *            the path of the property to which to apply cascading
	 * @return when to apply cascading - in search mode, edit mode, or both
	 */
	CascadeMode getCascadeMode(String cascadeTo);

	/**
	 * 
	 * @return the desired check box mode (defaults to simply a check box)
	 */
	CheckboxMode getCheckboxMode();

	/**
	 *
	 * @return the name of the field in the collection table that is used to
	 *         search
	 *         on when building a token search field for values in a
	 *         collection
	 *         table
	 */
	String getCollectionTableFieldName();

	/**
	 * 
	 * @return the name of the collection table that is used when building a
	 *         token
	 *         search field for values in a collection table
	 */
	String getCollectionTableName();

	/**
	 * 
	 * @return The date type (date, time, or time stamp) of the attribute
	 */
	AttributeDateType getDateType();

	/**
	 * 
	 * @return The default value of the attribute
	 */
	Object getDefaultValue();

	/**
	 * 
	 * @return The description of the attribute. This is used as the tool tip in
	 *         tables
	 */
	String getDescription();

	/**
	 * 
	 * @return The display format of the attribute (used in date formatting)
	 */
	String getDisplayFormat();

	/**
	 * 
	 * @return The display name of the attribute
	 */
	String getDisplayName();

	/**
	 * @return When the attribute can be edited
	 */
	EditableType getEditableType();

	/**
	 * @return The EntityModel for the entity that contains this attribute
	 */
	EntityModel<?> getEntityModel();

	/**
	 * The expansion factor for sizing components that are rendered on the same row
	 *
	 * @return
	 */
	float getExpansionFactor();

	/**
	 * 
	 * @return The textual representation of a "false" value
	 */
	String getFalseRepresentation();

	/**
	 * 
	 * @return The name of the property in which to store the file name of an
	 *         uploaded file
	 */
	String getFileNameProperty();

	/**
	 * 
	 * @return the paths to the other attributes that must be appear on the same
	 *         line in an edit form
	 */
	List<String> getGroupTogetherWith();

	/**
	 * 
	 * @return The maximum allowed length of the attribute (inside a collection
	 *         table)
	 */
	Integer getMaxLength();

	/**
	 * The maximum length of the text representation inside a table
	 * 
	 * @return
	 */
	Integer getMaxLengthInTable();

	/**
	 * 
	 * @return the maximum allowed value of the attribute (inside a collection
	 *         table)
	 */
	Long getMaxValue();

	/**
	 * 
	 * @return the member type of the collection, if this attribute holds a
	 *         collection of values
	 */
	Class<?> getMemberType();

	/**
	 * 
	 * @return the minimum allowed length of the attribute (inside a collection
	 *         table)
	 */
	Integer getMinLength();

	/**
	 * 
	 * @return the minimum allowed value of the attribute (inside a collection
	 *         table)
	 */
	Long getMinValue();

	/**
	 * 
	 * @return The name/identifier of the attribute
	 */
	String getName();

	/**
	 * 
	 * @return The nested entity model for this attribute
	 */
	EntityModel<?> getNestedEntityModel();

	/**
	 * @return The normalized type of the attribute (this is the same as the
	 *         <code>type</code> in case of a singular attribute, and the member
	 *         type of the collection case of collection attribute
	 */
	Class<?> getNormalizedType();

	/**
	 * Returns the number select mode
	 * 
	 * @return the number select mode (can be used to switch between text field and
	 *         slider)
	 */
	NumberSelectMode getNumberSelectMode();

	/**
	 * 
	 * 
	 * @return The order number (used to internally order the attribute models)
	 */
	Integer getOrder();

	/**
	 * @return The (nested) path to this attribute
	 */
	String getPath();

	/**
	 * @return The precision (number of decimals) to use when displaying a
	 *         decimal
	 *         number
	 */
	int getPrecision();

	/**
	 * @return The value to display as the input prompt value inside an edit
	 *         field
	 */
	String getPrompt();

	/**
	 * 
	 * @return the name of the property to which to assign a value in case of a
	 *         "quick addition"
	 */
	String getQuickAddPropertyName();

	/**
	 * 
	 * @return The path by which to replace the actual path when carrying out a
	 *         search. This is needed in very specific cases when an Entity has
	 *         multiple detail relations that are mapped to the same table
	 */
	String getReplacementSearchPath();

	/**
	 * 
	 * @return The path to use for sorting on this attribute when it does not
	 *         normally support sorting
	 */
	String getReplacementSortPath();

	/**
	 * 
	 * 
	 * @return The search select mode (determines which component to render in
	 *         search screens)
	 */
	AttributeSelectMode getSearchSelectMode();

	/**
	 * 
	 * 
	 * @return The select mode (determines which component to render in edit
	 *         screens)
	 */
	AttributeSelectMode getSelectMode();

	/**
	 * @return the styles to apply for this component
	 */
	String getStyles();

	/**
	 * 
	 * 
	 * @return The text field mode (text fiel or text area)
	 */
	AttributeTextFieldMode getTextFieldMode();

	/**
	 * @return The textual representation of a "true" value
	 */
	String getTrueRepresentation();

	/**
	 * 
	 * @return The Java type of the property
	 */
	Class<?> getType();

	/**
	 * 
	 * @return true if the attribute is already included in a
	 *         "groupTogetherWith"
	 *         clause
	 */
	boolean isAlreadyGrouped();

	/**
	 * 
	 * @return Whether the property is present inside an edit form. By default
	 *         this
	 *         is switched off for complex (i.e. MASTER or DETAIL) objects
	 */
	boolean isComplexEditable();

	/**
	 * 
	 * @return Whether this property represents a currency
	 */
	boolean isCurrency();

	/**
	 * Indicates whether this attribute allows direct navigation to entity edit
	 * screen
	 */
	boolean isDirectNavigation();

	/**
	 * 
	 * 
	 * @return Whether the attribute is an email address
	 */
	boolean isEmail();

	/**
	 * 
	 * @return Whether this is an embedded attribute
	 */
	boolean isEmbedded();

	/**
	 * @return Whether this attribute represents an image
	 */
	boolean isImage();

	/**
	 * @return Whether translated fields can and must be used for only the required locales
	 */
	boolean isLocalesRestricted();

	/**
	 * @return Whether this is the "main" attribute
	 */
	boolean isMainAttribute();

	/**
	 * 
	 * @return Whether "multiple search" is supported for this attribute
	 */
	boolean isMultipleSearch();

	/**
	 *
	 * @return
	 */
	boolean isNavigable();

	/**
	 * 
	 * @return Whether this is a numeric attribute
	 */
	boolean isNumerical();

	/**
	 * 
	 * @return Whether the attribute represents a percentage
	 */
	boolean isPercentage();

	/**
	 * @ return whether "quick edit" functionality is allowed. Quick edit
	 * functionality allows for the inline addition of simple domain values
	 */
	boolean isQuickAddAllowed();

	/**
	 * @return whether the attribute is a required attribute (entity can only be
	 *         saved if values for all required attributes have been provided)
	 */
	boolean isRequired();

	/**
	 * 
	 * @return whether it is required to fill in a value for this attribute
	 *         before
	 *         you can carry out a search
	 */
	boolean isRequiredForSearching();

	/**
	 * @return whether it is possible to search on this attribute
	 */
	boolean isSearchable();

	/**
	 * @return whether searching on this attribute is case sensitive (only
	 *         applies
	 *         if this is a String attribute)
	 */
	boolean isSearchCaseSensitive();

	/**
	 * @return whether searching for this value is by exact match (rather than
	 *         using
	 *         a range). Only applicable to numerical and date field
	 */
	boolean isSearchForExactValue();

	/**
	 * 
	 * 
	 * @return whether searching should only match on prefixes rather than on a
	 *         substring occurring anywhere
	 */
	boolean isSearchPrefixOnly();

	/**
	 * @return whether the attribute is sortable
	 */
	boolean isSortable();

	/**
	 *
	 * @return whether the attribute is transient
	 */
	boolean isTransient();

	/**
	 * Indicates whether this represents a (clickable) URL
	 * 
	 * @return
	 */
	boolean isUrl();

	/**
	 * Indicates whether to use a thousands grouping character
	 * 
	 * @return
	 */
	boolean isUseThousandsGrouping();

	/**
	 * Indicates whether the attribute is visible
	 * 
	 * @return
	 */
	boolean isVisible();

	/**
	 * Indicates whether the attribute must be shown in a table
	 * 
	 * @return
	 */
	boolean isVisibleInTable();

	/**
	 * Indicates whether this attribute represents a weekly recurring date
	 * 
	 * @return
	 */
	boolean isWeek();

	/**
	 * Removes all cascading
	 */
	void removeCascades();

	/**
	 * Marks the attribute as the main attribute
	 * 
	 * @param main
	 *            whether the attribute is the main attribute
	 * @return
	 */
	void setMainAttribute(boolean main);

}
