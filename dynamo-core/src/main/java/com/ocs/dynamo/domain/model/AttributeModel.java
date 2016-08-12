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
     * @return the allowed extensions for a LOB attribute
     */
    Set<String> getAllowedExtensions();

    /**
     * @return the attribute type (e.g. BASIC, MASTER, DETAIL) of the attribute
     */
    AttributeType getAttributeType();

    /**
     * 
     * @return the date type (date, time, or timestamp) of the attribute
     */
    AttributeDateType getDateType();

    /**
     * 
     * @return the default value of the attribute
     */
    Object getDefaultValue();

    /**
     * 
     * @return the description of the attribute
     */
    String getDescription();

    /**
     * 
     * @return the display format of the attribute (determines how to format a data)
     */
    String getDisplayFormat();

    /**
     * 
     * @return the display name of the attribute
     */
    String getDisplayName();

    /**
     * @return the EntityModel for the entity that contains this attribute
     */
    EntityModel<?> getEntityModel();

    /**
     * 
     * @return the textual representation of a "false" value
     */
    String getFalseRepresentation();

    /**
     * The property of the field in which to store the file name of an uploaded file
     * 
     * @return
     */
    String getFileNameProperty();

    /**
     * 
     * @return the maximum allowed length of the attribute
     */
    Integer getMaxLength();

    /**
     * 
     * @return the member type of the collection, if this attribute holds a collection of values
     */
    Class<?> getMemberType();

    /**
     * 
     * @return the minimum allowed length of the attribute
     */
    Integer getMinLength();

    /**
     * 
     * @return the name/identifier of the attribute
     */
    String getName();

    /**
     * When this is a MASTER or DETAIL attribute then return the entity model for the nested entity
     * 
     * @return The nested entity model
     */
    EntityModel<?> getNestedEntityModel();

    /**
     * The order number of the attribute
     * 
     * @return
     */
    Integer getOrder();

    /**
     * @return the (nested) path to this attribute
     */
    String getPath();

    /**
     * The precision (number of decimals) to use when displaying a decimal number
     * 
     * @return
     */
    int getPrecision();

    /**
     * The input prompt to use for a field
     * 
     * @return
     */
    String getPrompt();

    /**
     * 
     * @return
     */
    String getQuickAddPropertyName();

    /**
     * Returns the path by which to replace the actual path when carrying out a search. This is
     * needed in very specific cases when an Entity has multiple detail relations that are mapped to
     * the same table
     * 
     * @return
     */
    String getReplacementSearchPath();

    /**
     * Determines which selection component to use in search mode
     * 
     * @return
     */
    AttributeSelectMode getSearchSelectMode();

    /**
     * Determines which selection component to use in edit mode
     * 
     * @return
     */
    AttributeSelectMode getSelectMode();

    /**
     * Returns the text field mode
     * 
     * @return
     */
    AttributeTextFieldMode getTextFieldMode();

    /**
     * Indicates a string value to use instead of "true"
     * 
     * @return
     */
    String getTrueRepresentation();

    /**
     * The Java type of the property
     * 
     * @return
     */
    Class<?> getType();

    /**
     * Indicates whether the attribute is a one-to-one or many-to-one attribute that can ben
     * selected in a form
     */
    boolean isComplexEditable();

    /**
     * Is this a currency?
     * 
     * @return
     */
    boolean isCurrency();

    /**
     * Indicates this field must get the focus on a detail table
     * 
     * @return
     */
    boolean isDetailFocus();

    /**
     * Is this an email field
     * 
     * @return
     */
    boolean isEmail();

    /**
     * Is this an embedded object
     * 
     * @return
     */
    boolean isEmbedded();

    /**
     * Whether this attribute should be presented as an image
     * 
     * @return
     */
    boolean isImage();

    /**
     * Indicates whether this is the main attribute
     * 
     * @return
     */
    boolean isMainAttribute();

    /**
     * Indicates whether this is a "multiple search" attribute
     * 
     * @return
     */
    boolean isMultipleSearch();

    /**
     * Is this a numeric attribute
     * 
     * @return
     */
    boolean isNumerical();

    /**
     * Indicates whether a numerical field is a percentage
     * 
     * @return
     */
    boolean isPercentage();

    /**
     * Indicates whether "quick edit" functionality is allowed. Quick edit functionality allows for
     * the inline addition of simple domain values
     */
    boolean isQuickAddAllowed();

    /**
     * Indicates whether the attribute is read only
     * 
     * @return
     */
    boolean isReadOnly();

    /**
     * Indicates whether this is a required attribute
     * 
     * @return
     */
    boolean isRequired();

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
     * Marks this attribute as the main attribute
     * 
     * @param main
     * @return
     */
    void setMainAttribute(boolean main);

}
