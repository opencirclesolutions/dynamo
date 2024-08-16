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
package org.dynamoframework.domain.model;

import org.dynamoframework.domain.model.annotation.SearchMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * An attribute model represents how a certain attribute of an entity will
 * behave in the user interface. This includes e.g. whether the attribute is
 * searchable, sortable, what kind of user interface component is used to edit
 * the attribute, any many additional aspects
 * 
 * @author bas.rutten
 *
 */
public interface AttributeModel extends Comparable<AttributeModel> {

	/**
	 * Adds a cascade setting
	 * 
	 * @param cascadeTo  the path to the attribute to cascade to
	 * @param filterPath the path used to filter on
	 * @param mode       the mode (search, edit, or both)
	 */
	void addCascade(String cascadeTo, String filterPath, CascadeMode mode);

	/**
	 * Adds a "group together with" attribute. These attributes mentioned as the
	 * "group together with" attributes will be rendered on the same line as the
	 * attribute for which this model is defined
	 * 
	 * @param path the path to the attribute to group with
	 */
	void addGroupTogetherWith(String path);

	/**
	 * @return the field-specific auto-fill instructions
	 */
	String getAutofillInstructions();

	/**
	 * @return the actual path to search on. This uses the replacementSearchPath
	 *         when set or the default search path otherwise
	 */
	String getActualSearchPath();

	/**
	 * @return the actual path to sort on. This uses the replacementSortPath when
	 *         set or the default sort path otherwise
	 */
	String getActualSortPath();

	/**
	 * @return The allowed extensions for a LOB attribute
	 */
	Set<String> getAllowedExtensions();

	/**
	 * @return The attribute type (e.g. BASIC, MASTER, DETAIL) of the attribute
	 */
	AttributeType getAttributeType();

	/**
	 * @return the boolean field mode to use
	 */
	AttributeBooleanFieldMode getBooleanFieldMode();

	/**
	 * @return the attributes to cascade to when the value of this attribute changes
	 */
	Set<String> getCascadeAttributes();

	/**
	 * Returns the path to filter on when applying a cascade operation
	 * 
	 * @param cascadeTo the path of the attribute to which to apply cascading
	 * @return the path to filter on
	 */
	String getCascadeFilterPath(String cascadeTo);

	/**
	 * Returns the cascade mode for an attribute
	 * 
	 * @param cascadeTo the path of the attribute to which to apply cascading
	 * @return when to apply cascading - in search mode, edit mode, or both
	 */
	CascadeMode getCascadeMode(String cascadeTo);

	/**
	 *
	 * @return the name of the field in the collection table that is used to search
	 *         on when building a token search field for values in a collection
	 *         table
	 */
	String getCollectionTableFieldName();

	/**
	 * 
	 * @return the name of the collection table that is used when building a token
	 *         search field for values in a collection table
	 */
	String getCollectionTableName();

	/**
	 *
	 * @return the currency code to use
	 */
	String getCurrencyCode();

	/**
	 * Returns the value for a custom setting
	 * 
	 * @param name the name of the custom setting
	 * @return the value of the custom setting
	 */
	Object getCustomSetting(String name);

	/**
	 * 
	 * @return the date type (date, time, or time stamp) of the attribute
	 */
	AttributeDateType getDateType();

	/**
	 * @return The default value of the attribute
	 */
	Object getDefaultValue();

	/**
	 *
	 * @return the default search value of the attribute
	 */
	Object getDefaultSearchValue();

	/**
	 * @return the default search value (lower bound)
	 */
	Object getDefaultSearchValueFrom();

	/**
	 * @return the default search value (upper bound)
	 */
	Object getDefaultSearchValueTo();

	/**
	 * Returns the description of the attribute for a certain locale
	 * 
	 * @param locale the locale
	 * @return the description of the attribute. This is used as the tool tip in
	 *         tables
	 */
	String getDescription(Locale locale);

	/**
	 * 
	 * @return the display format of the attribute (used in date formatting)
	 */
	String getDisplayFormat(Locale locale);

	/**
	 * Returns the display name of the attribute for a certain locale
	 * 
	 * @param locale the locale
	 * @return The display name of the attribute (used as the caption for the input
	 *         component)
	 */
	String getDisplayName(Locale locale);

	/**
	 * @return when the attribute can be edited (never, always, or only when
	 *         creating new entities)
	 */
	EditableType getEditableType();

	/**
	 * @return the EntityModel for the entity that contains this attribute
	 */
	EntityModel<?> getEntityModel();

	/**
	 * @return the component to use for rendering an enum
	 */
	AttributeEnumFieldMode getEnumFieldMode();

	/**
	 * @return the desired mode to use for element collection fields
	 */
	ElementCollectionMode getElementCollectionMode();

	/**
	 * Returns the textual representation of a "false" value for a certain locale
	 * 
	 * @param locale the locale
	 * @return The textual representation of a "false" value
	 */
	String getFalseRepresentation(Locale locale);

	/**
	 * 
	 * @return The name of the attribute in which to store the file name after a
	 *         successful file upload
	 */
	String getFileNameProperty();

	/**
	 * 
	 * @return the index of the attribute within the ordering inside a grid
	 */
	Integer getGridOrder();

	/**
	 * 
	 * @return the paths to the other attributes that must appear on the same line
	 *         in an edit form
	 */
	List<String> getGroupTogetherWith();

	/**
	 * @return the entity model reference used in lookup components
	 */
	String getLookupEntityReference();

	/**
	 *
	 * @return the maximum collection size for element collection
	 */
	Integer getMaxCollectionSize();

	/**
	 * 
	 * @return The maximum allowed length of a String attribute
	 */
	Integer getMaxLength();

	/**
	 * 
	 * @return the maximum length of the text when displaying the attribute inside a
	 *         grid
	 */
	Integer getMaxLengthInGrid();

	/**
	 * 
	 * @return the maximum allowed value of a numeric attribute
	 */
	BigDecimal getMaxValue();

	/**
	 * 
	 * @return the member type of the collection, if this attribute holds a
	 *         collection of values
	 */
	Class<?> getMemberType();

	/**
	 *
	 * @return the minimum collection size for element collections
	 */
	Integer getMinCollectionSize();

	/**
	 * 
	 * @return the minimum allowed length of a String attribute
	 */
	Integer getMinLength();

	/**
	 * 
	 * @return the minimum allowed value of a numeric attribute
	 */
	BigDecimal getMinValue();

	/**
	 * 
	 * @return the name/identifier of the attribute
	 */
	String getName();

	/**
	 * @return the internal navigation link
	 */
	String getNavigationLink();

	/**
	 * 
	 * @return the nested entity model for this attribute
	 */
	EntityModel<?> getNestedEntityModel();

	/**
	 * @return the normalized type of the attribute (this is the same as the
	 *         <code>type</code> in case of a singular attribute, and the member
	 *         type of the collection case of collection attribute
	 */
	Class<?> getNormalizedType();

	/**
	 * 
	 * @return the number field mode
	 */
	NumberFieldMode getNumberFieldMode();

	/**
	 * 
	 * @return the number field step
	 */
	Integer getNumberFieldStep();

	/**
	 * @return The order number (used to internally order the attribute models)
	 */
	Integer getOrder();

	/**
	 * @return The (nested) path to this attribute
	 */
	String getPath();

	/**
	 * @return The precision (number of decimals) to use when displaying a decimal
	 *         number
	 */
	int getPrecision();

	/**
	 * @return The value to display as the input prompt value inside an edit field
	 */
	String getPrompt(Locale locale);

	/**
	 * 
	 * @return the path by which to replace the actual path when carrying out a
	 *         search. This is needed in very specific cases when an Entity has
	 *         multiple detail relations that are mapped to the same table
	 */
	String getReplacementSearchPath();

	/**
	 * 
	 * @return the path to use for sorting on this attribute when it does not
	 *         normally support sorting
	 */
	String getReplacementSortPath();

	/**
	 * @return the search mode
	 */
	SearchMode getSearchMode();

	/**
	 * 
	 * @return the index of the attribute within a search form
	 */
	Integer getSearchOrder();

	/**
	 * @return The search select mode (determines which component to render in
	 *         search screens)
	 */
	AttributeSelectMode getSearchSelectMode();

	/**
	 * @return The select mode (determines which component to render in edit
	 *         screens)
	 */
	AttributeSelectMode getSelectMode();

	/**
	 * 
	 * @return The text field mode (text field or text area)
	 */
	AttributeTextFieldMode getTextFieldMode();

    /**
	 * @return The textual representation of a "true" value
	 */
	String getTrueRepresentation(Locale locale);

	/**
	 * 
	 * @return The Java type of the attribute
	 */
	Class<?> getType();

	/**
	 * 
	 * @return true if the attribute is already included in a "groupTogetherWith"
	 *         clause
	 */
	boolean isAlreadyGrouped();

	/**
	 * 
	 * @return whether the attribute value is a Boolean
	 */
	boolean isBoolean();

	/**
	 * @return whether image download is allowed
	 */
	boolean isDownloadAllowed();

    /**
	 * 
	 * @return whether the attribute represents an email address
	 */
	boolean isEmail();

	/**
	 * 
	 * @return Whether the attribute is an embedded attribute
	 */
	boolean isEmbedded();

	/**
	 * 
	 * @return whether to ignore the attribute when creating a search filter (i.e.
	 *         the attribute is there only for cascading)
	 */
	boolean isIgnoreInSearchFilter();

	/**
	 * @return whether the attribute represents an image
	 */
	boolean isImage();

	/**
	 * 
	 * @return whether "multiple search" is supported for this attribute
	 */
	boolean isMultipleSearch();

	/**
	 *
	 * @return whether the attribute is used for navigation within the application
	 */
	boolean isNavigable();

	/**
	 *
	 * @return whether the detail collection is nested directly inside the parent object
	 */
	boolean isNestedDetails();

	/**
	 * 
	 * @return whether the attribute value is numerical
	 */
	boolean isNumerical();

	/**
	 *
	 * @return whether the attribute value is an integral number (integer or long)
	 */
	boolean isIntegral();

	/**
	 * 
	 * @return whether the attribute represents a percentage
	 */
	boolean isPercentage();

	/**
	 * @ return whether "quick edit" functionality is allowed. Quick edit
	 * functionality allows for the in-line addition of simple domain values
	 */
	boolean isQuickAddAllowed();

	/**
	 * @return whether the attribute is a required attribute (entity can only be
	 *         saved if values for all required attributes have been provided)
	 */
	boolean isRequired();

	/**
	 * 
	 * @return whether it is required to fill in a value for this attribute before
	 *         you can carry out a search
	 */
	boolean isRequiredForSearching();

	/**
	 * 
	 * @return whether the attribute appears within a search form
	 */
	boolean isSearchable();

	/**
	 * @return whether searching on this attribute is case-sensitive (only applies
	 *         if this is a String attribute)
	 */
	boolean isSearchCaseSensitive();

	/**
	 * @return whether searching on a time stamp field should only use the date part
	 */
	boolean isSearchDateOnly();

	/**
	 * @return whether searching for this value is by exact match (rather than using
	 *         a range). Only applicable to numerical and date field
	 */
	boolean isSearchForExactValue();

	/**
	 * @return whether searching should only match on prefixes rather than on a
	 *         substring occurring anywhere
	 */
	boolean isSearchPrefixOnly();

	/**
	 * @return whether the password reveal button is visible
	 */
	boolean isShowPassword();

	/**
	 * @return whether the attribute is sortable
	 */
	boolean isSortable();

	/**
	 * @return whether to trim extra spaces from text input
	 */
	boolean isTrimSpaces();

	/**
	 * @return whether the attribute value represent a URL
	 */
	boolean isUrl();

	/**
	 * @return whether the attribute is visible in a form
	 */
	boolean isVisibleInForm();

	/**
	 * @return whether the attribute must be shown in a grid or table
	 */
	boolean isVisibleInGrid();

	/**
	 * Removes all cascading search settings for the attribute
	 */
	void removeCascades();

	/**
	 * Adds a custom setting for the attribute
	 * 
	 * @param name  the name of the custom setting
	 * @param value the value of the custom setting
	 */
	void setCustomSetting(String name, Object value);

	/**
	 * @return whether the attribute has a setter method
	 */
	boolean hasSetterMethod();

	/**
	 * @return whether this attribute is needed for correct working of other attributes
	 */
	boolean isNeededInData();

	/**
	 *
	 * @return the query type to use in case of a lookup
	 */
	QueryType getLookupQueryType();

}
