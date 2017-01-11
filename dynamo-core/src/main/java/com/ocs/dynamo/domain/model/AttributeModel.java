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
 * An attribute model represents how a certain attribute of an entity will be behave in the user
 * interface. This includes e.g. whether the attribute is searchable, sortable, what kind of user
 * interface component is used to edit the attribute etc
 * 
 * @author bas.rutten
 *
 */
public interface AttributeModel extends Comparable<AttributeModel> {

	/**
	 * Adds a "group together with" attribute
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
	 * @return the name of the field in the collection table
	 */
	String getCollectionTableFieldName();

	/**
	 * 
	 * @return the name of the collection table (in case of an element collection)
	 */
	String getCollectionTableName();

	/**
	 * 
	 * @return The date type (date, time, or timestamp) of the attribute
	 */
	AttributeDateType getDateType();

	/**
	 * 
	 * @return The default value of the attribute
	 */
	Object getDefaultValue();

	/**
	 * 
	 * @return The description of the attribute. This is used as the tool tip in tables
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
	 * @return The EntityModel for the entity that contains this attribute
	 */
	EntityModel<?> getEntityModel();

	/**
	 * 
	 * @return The textual representation of a "false" value
	 */
	String getFalseRepresentation();

	/**
	 * 
	 * @return The name of the property in which to store the file name of an uploaded file
	 */
	String getFileNameProperty();

	/**
	 * 
	 * @return the names of/paths to the other attributes that must be appear on the same line in an
	 *         edit form
	 */
	List<String> getGroupTogetherWith();

	/**
	 * 
	 * @return The maximum allowed length of the attribute (inside a collection table)
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
	 * @return the maximum allowed value of the attribute (inside a collection table)
	 */
	Long getMaxValue();

	/**
	 * 
	 * @return the member type of the collection, if this attribute holds a collection of values
	 */
	Class<?> getMemberType();

	/**
	 * 
	 * @return the minimum allowed length of the attribute (inside a collection table)
	 */
	Integer getMinLength();

	/**
	 * 
	 * @return the minimum allowed value of the attribute (inside a collection table)
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
	 * @return The normalized type of the attribute (this is the same as the <code>type</code> in
	 *         case of a singular attribute, and the member type of the collection case of
	 *         collection attribute
	 */
	Class<?> getNormalizedType();

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
	 * @return The precision (number of decimals) to use when displaying a decimal number
	 */
	int getPrecision();

	/**
	 * @return The value to display as the input prompt value inside an edit field
	 */
	String getPrompt();

	/**
	 * 
	 * @return the name of the property to which to assign a value in case of a "quick addition"
	 */
	String getQuickAddPropertyName();

	/**
	 * 
	 * @return The path by which to replace the actual path when carrying out a search. This is
	 *         needed in very specific cases when an Entity has multiple detail relations that are
	 *         mapped to the same table
	 */
	String getReplacementSearchPath();

	/**
	 * 
	 * 
	 * @return The search select mode (determines which component to render in search screens)
	 */
	AttributeSelectMode getSearchSelectMode();

	/**
	 * 
	 * 
	 * @return The select mode (determines which component to render in edit screens)
	 */
	AttributeSelectMode getSelectMode();

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
	 * @return true if the attribute is already included in a "groupTogetherWith" clause
	 */
	boolean isAlreadyGrouped();

	/**
	 * 
	 * @return Whether the property is present inside an edit form. By default this is switched off
	 *         for complex (i.e. MASTER or DETAIL) objects
	 */
	boolean isComplexEditable();

	/**
	 * 
	 * @return Whether this property represents a currency
	 */
	boolean isCurrency();

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
	 * @return Whether this is a numeric attribute
	 */
	boolean isNumerical();

	/**
	 * 
	 * @return Whether the attribute represents a percentage
	 */
	boolean isPercentage();

	/**
	 * Indicates whether "quick edit" functionality is allowed. Quick edit functionality allows for
	 * the inline addition of simple domain values
	 */
	boolean isQuickAddAllowed();

	/**
	 * @return Whether the attribute is read only
	 */
	boolean isReadOnly();

	/**
	 * Indicates whether this is a required attribute
	 * 
	 * @return
	 */
	boolean isRequired();

	/**
	 * Indicates whether the field is required when searching
	 * 
	 * @return
	 */
	boolean isRequiredForSearching();

	/**
	 * Indicates whether it is possible to search on this attribute
	 * 
	 * @return
	 */
	boolean isSearchable();

	/**
	 * Indicates whether searching on this fields is case sensitive
	 * 
	 * @return
	 */
	boolean isSearchCaseSensitive();

	/**
	 * Indicates whether searching for this value is by exact match (rather than using a range).
	 * Only applicable to numerical and date field
	 * 
	 * @return
	 */
	boolean isSearchForExactValue();

	/**
	 * Indicates whether to only match the prefix when performing a search
	 * 
	 * @return
	 */
	boolean isSearchPrefixOnly();

	/**
	 * Indicates whether the attribute is sortable
	 * 
	 * @return
	 */
	boolean isSortable();

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
	 * Marks the attribute as the main attribute
	 * 
	 * @param main
	 *            whether the attribute is the main attribute
	 * @return
	 */
	void setMainAttribute(boolean main);

}
