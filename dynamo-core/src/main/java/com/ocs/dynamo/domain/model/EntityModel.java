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
 * An interface representating a model that contains an entity's metadata
 * 
 * @author bas.rutten
 * @param <T>
 */
public interface EntityModel<T> {

	String ALLOWED_EXTENSIONS = "allowedExtensions";

	String ATTRIBUTE_GROUP = "attributeGroup";

	String ATTRIBUTE_NAMES = "attributeNames";

	String ATTRIBUTE_ORDER = "attributeOrder";

	String COMPLEX_EDITABLE = "complexEditable";

	String CURRENCY = "currency";

	String DATE_TYPE = "dateType";

	String DEFAULT_GROUP = "defaultGroup";

	int DEFAULT_PRECISION = 2;

	String DEFAULT_VALUE = "defaultValue";

	String DESCRIPTION = "description";

	String DETAIL_FOCUS = "detailFocus";

	String DISPLAY_FORMAT = "displayFormat";

	String DISPLAY_NAME = "displayName";

	String DISPLAY_NAME_PLURAL = "displayNamePlural";

	String DISPLAY_PROPERTY = "displayProperty";

	String EMBEDDED = "embedded";

	String FALSE_REPRESENTATION = "falseRepresentation";

	String IMAGE = "image";

	String MAIN = "main";

	String MAX_LENGTH = "maxLength";

	String MIN_LENGTH = "minLength";

	String PERCENTAGE = "percentage";

	String PRECISION = "precision";

	String PROMPT = "prompt";

	String READ_ONLY = "readOnly";

	String SEARCH_CASE_SENSITIVE = "searchCaseSensitive";

	String SEARCH_PREFIX_ONLY = "searchPrefixOnly";

	String SEARCHABLE = "searchable";

	/**
	 * Indicates that a lookup field (rather than a combo box) must be used when
	 * selecting the component
	 */
	String SELECT_MODE = "selectMode";

	String SHOW_IN_TABLE = "showInTable";

	String SORT_ORDER = "sortOrder";

	String SORTABLE = "sortable";

	String TEXTFIELD_MODE = "textFieldMode";

	String TRUE_REPRESENTATION = "trueRepresentation";

	String VISIBLE = "visible";

	String WEEK = "week";

	/**
	 * Adds a new attribute model on the position of the given existing
	 * attribute model. The existing model will shift a position backwards. When
	 * the existing model is not found the attribute will added on the end of
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
	 * @return
	 */
	List<AttributeModel> getAttributeModelsForGroup(String group);

	/**
	 * Returns the attribute models for a certain attribute type and type. Just
	 * one of the parameters is mandatory, when both are given both will be used
	 * in a boolean AND. Will also look at the generic type of a attribute, e.g.
	 * List<some generic type>.
	 * 
	 * @param attributeType
	 * @param type
	 * @return
	 */
	List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type);

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
	 * The name of the property that is used when displaying the object in a
	 * select component (like a combobox) or a table
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
	 * Returns the primary attribute
	 * 
	 * @return
	 */
	AttributeModel getMainAttributeModel();

	/**
     * 
     */
	String getReference();

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
	 * @return
	 */
	boolean isAttributeGroupVisible(String group, boolean readOnly);

	/**
	 * @return
	 */
	boolean usesDefaultGroupOnly();
}
