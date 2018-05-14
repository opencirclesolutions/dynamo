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
package com.ocs.dynamo.domain.model.impl;

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.CheckboxMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the AttributeModel interface - simple container for
 * properties
 * 
 * @author bas.rutten
 */
public class AttributeModelImpl implements AttributeModel {

	private Set<String> allowedExtensions = new HashSet<>();

	private boolean alreadyGrouped;

	private AttributeType attributeType;

	private Map<String, String> cascadeAttributes = new HashMap<>();

	private Map<String, CascadeMode> cascadeModes = new HashMap<>();

	private CheckboxMode checkboxMode;

	private String collectionTableFieldName;

	private String collectionTableName;

	private boolean complexEditable;

	private boolean currency;

	private AttributeDateType dateType;

	private Object defaultValue;

	private String description;

	private boolean directNavigation;

	private String displayFormat;

	private String displayName;

	private EditableType editableType;

	private boolean email;

	private EntityModel<?> entityModel;

	private float expansionFactor;

	private String falseRepresentation;

	private String fileNameProperty;

	private List<String> groupTogetherWith = new ArrayList<>();

	private boolean image;

	private boolean mainAttribute;

	private Integer maxLength;

	private Integer maxLengthInTable;

	private Long maxValue;

	private Class<?> memberType;

	private Integer minLength;

	private Long minValue;

	private boolean multipleSearch;

	private String name;

	private boolean navigable;

	private EntityModel<?> nestedEntityModel;

	private NumberSelectMode numberSelectMode = NumberSelectMode.TEXTFIELD;

	private Integer order;

	private boolean percentage;

	private int precision;

	private String prompt;

	private boolean quickAddAllowed;

	private String quickAddPropertyName;

	private String replacementSearchPath;

	private boolean required;

	private boolean requiredForSearching;

	private boolean searchable;

	private boolean searchCaseSensitive;

	private boolean searchForExactValue;

	private boolean searchPrefixOnly;

	private AttributeSelectMode searchSelectMode;

	private AttributeSelectMode selectMode;

	private String styles;

	private boolean sortable;

	private AttributeTextFieldMode textFieldMode;

	private boolean trans;

	private String trueRepresentation;

	private Class<?> type;

	private boolean url;

	private boolean useThousandsGrouping;

	private boolean visible;

	private boolean visibleInTable;

	private boolean week;

	@Override
	public void addCascade(String cascadeTo, String filterPath, CascadeMode mode) {
		this.cascadeAttributes.put(cascadeTo, filterPath);
		this.cascadeModes.put(cascadeTo, mode);
	}

	@Override
	public void addGroupTogetherWith(String path) {
		groupTogetherWith.add(path);
	}

	@Override
	public int compareTo(AttributeModel o) {
		return this.getOrder() - o.getOrder();
	}

	@Override
	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}

	public void setAllowedExtensions(Set<String> allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}

	@Override
	public AttributeType getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(AttributeType attributeType) {
		this.attributeType = attributeType;
	}

	@Override
	public Set<String> getCascadeAttributes() {
		return cascadeAttributes.keySet();
	}

	@Override
	public String getCascadeFilterPath(String cascadeTo) {
		return this.cascadeAttributes.get(cascadeTo);
	}

	@Override
	public CascadeMode getCascadeMode(String cascadeTo) {
		return this.cascadeModes.get(cascadeTo);
	}

	@Override
	public CheckboxMode getCheckboxMode() {
		return checkboxMode;
	}

	public void setCheckboxMode(CheckboxMode checkboxMode) {
		this.checkboxMode = checkboxMode;
	}

	@Override
	public String getCollectionTableFieldName() {
		return collectionTableFieldName;
	}

	public void setCollectionTableFieldName(String collectionTableFieldName) {
		this.collectionTableFieldName = collectionTableFieldName;
	}

	@Override
	public String getCollectionTableName() {
		return collectionTableName;
	}

	public void setCollectionTableName(String collectionTableName) {
		this.collectionTableName = collectionTableName;
	}

	@Override
	public AttributeDateType getDateType() {
		return dateType;
	}

	public void setDateType(AttributeDateType dateType) {
		this.dateType = dateType;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDisplayFormat() {
		return displayFormat;
	}

	public void setDisplayFormat(String displayFormat) {
		this.displayFormat = displayFormat;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public EditableType getEditableType() {
		return editableType;
	}

	public void setEditableType(EditableType editableType) {
		this.editableType = editableType;
	}

	@Override
	public EntityModel<?> getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel<?> entityModel) {
		this.entityModel = entityModel;
	}

	@Override
	public float getExpansionFactor() {
		return expansionFactor;
	}

	public void setExpansionFactor(float expansionFactor) {
		this.expansionFactor = expansionFactor;
	}

	@Override
	public String getFalseRepresentation() {
		return falseRepresentation;
	}

	public void setFalseRepresentation(String falseRepresentation) {
		this.falseRepresentation = falseRepresentation;
	}

	@Override
	public String getFileNameProperty() {
		return fileNameProperty;
	}

	public void setFileNameProperty(String fileNameProperty) {
		this.fileNameProperty = fileNameProperty;
	}

	@Override
	public List<String> getGroupTogetherWith() {
		return Collections.unmodifiableList(groupTogetherWith);
	}

	@Override
	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public Integer getMaxLengthInTable() {
		return maxLengthInTable;
	}

	public void setMaxLengthInTable(Integer maxLengthInTable) {
		this.maxLengthInTable = maxLengthInTable;
	}

	@Override
	public Long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}

	@Override
	public Class<?> getMemberType() {
		return memberType;
	}

	public void setMemberType(Class<?> memberType) {
		this.memberType = memberType;
	}

	@Override
	public Integer getMinLength() {
		return minLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	@Override
	public Long getMinValue() {
		return minValue;
	}

	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public EntityModel<?> getNestedEntityModel() {
		return nestedEntityModel;
	}

	public void setNestedEntityModel(EntityModel<?> nestedEntityModel) {
		this.nestedEntityModel = nestedEntityModel;
	}

	@Override
	public Class<?> getNormalizedType() {
		return getMemberType() != null ? getMemberType() : getType();
	}

	@Override
	public NumberSelectMode getNumberSelectMode() {
		return numberSelectMode;
	}

	public void setNumberSelectMode(NumberSelectMode numberSelectMode) {
		this.numberSelectMode = numberSelectMode;
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	@Override
	public String getPath() {
		String reference = entityModel.getReference();
		int p = reference.indexOf('.');

		if (p <= 0) {
			return name;
		} else {
			return reference.substring(p + 1) + "." + name;
		}
	}

	@Override
	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	@Override
	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	@Override
	public String getQuickAddPropertyName() {
		return quickAddPropertyName;
	}

	public void setQuickAddPropertyName(String quickAddPropertyName) {
		this.quickAddPropertyName = quickAddPropertyName;
	}

	@Override
	public String getReplacementSearchPath() {
		return replacementSearchPath;
	}

	public void setReplacementSearchPath(String replacementSearchPath) {
		this.replacementSearchPath = replacementSearchPath;
	}

	@Override
	public AttributeSelectMode getSearchSelectMode() {
		return searchSelectMode;
	}

	public void setSearchSelectMode(AttributeSelectMode searchSelectMode) {
		this.searchSelectMode = searchSelectMode;
	}

	@Override
	public AttributeSelectMode getSelectMode() {
		return selectMode;
	}

	public void setSelectMode(AttributeSelectMode selectMode) {
		this.selectMode = selectMode;
	}

	/**
	 * @return the styles
	 */
	@Override
	public String getStyles() {
		return styles;
	}

	/**
	 * @param styles the styles to set
	 */
	public void setStyles(String styles) {
		this.styles = styles;
	}

	@Override
	public AttributeTextFieldMode getTextFieldMode() {
		return textFieldMode;
	}

	public void setTextFieldMode(AttributeTextFieldMode textFieldMode) {
		this.textFieldMode = textFieldMode;
	}

	@Override
	public String getTrueRepresentation() {
		return trueRepresentation;
	}

	public void setTrueRepresentation(String trueRepresentation) {
		this.trueRepresentation = trueRepresentation;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	@Override
	public boolean isAlreadyGrouped() {
		return alreadyGrouped;
	}

	public void setAlreadyGrouped(boolean alreadyGrouped) {
		this.alreadyGrouped = alreadyGrouped;
	}

	@Override
	public boolean isComplexEditable() {
		return complexEditable;
	}

	public void setComplexEditable(boolean complexEditable) {
		this.complexEditable = complexEditable;
	}

	@Override
	public boolean isCurrency() {
		return currency;
	}

	public void setCurrency(boolean currency) {
		this.currency = currency;
	}

	@Override
	public boolean isDirectNavigation() {
		return directNavigation;
	}

	public void setDirectNavigation(boolean directNavigation) {
		this.directNavigation = directNavigation;
	}

	@Override
	public boolean isEmail() {
		return email;
	}

	public void setEmail(boolean email) {
		this.email = email;
	}

	@Override
	public boolean isEmbedded() {
		return AttributeType.EMBEDDED.equals(attributeType);
	}

	@Override
	public boolean isImage() {
		return image;
	}

	public void setImage(boolean image) {
		this.image = image;
	}

	@Override
	public boolean isMainAttribute() {
		return mainAttribute;
	}

	@Override
	public void setMainAttribute(boolean mainAttribute) {
		this.mainAttribute = mainAttribute;
	}

	@Override
	public boolean isMultipleSearch() {
		return multipleSearch;
	}

	public void setMultipleSearch(boolean multipleSearch) {
		this.multipleSearch = multipleSearch;
	}

	@Override
	public boolean isNavigable() {
		return navigable;
	}

	public void setNavigable(boolean navigable) {
		this.navigable = navigable;
	}

	@Override
	public boolean isNumerical() {
		return Number.class.isAssignableFrom(type);
	}

	@Override
	public boolean isPercentage() {
		return percentage;
	}

	public void setPercentage(boolean percentage) {
		this.percentage = percentage;
	}

	@Override
	public boolean isQuickAddAllowed() {
		return quickAddAllowed;
	}

	public void setQuickAddAllowed(boolean quickAddAllowed) {
		this.quickAddAllowed = quickAddAllowed;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	@Override
	public boolean isRequiredForSearching() {
		return requiredForSearching;
	}

	public void setRequiredForSearching(boolean requiredForSearching) {
		this.requiredForSearching = requiredForSearching;
	}

	@Override
	public boolean isSearchable() {
		return searchable;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	@Override
	public boolean isSearchCaseSensitive() {
		return searchCaseSensitive;
	}

	public void setSearchCaseSensitive(boolean searchCaseSensitive) {
		this.searchCaseSensitive = searchCaseSensitive;
	}

	@Override
	public boolean isSearchForExactValue() {
		return searchForExactValue;
	}

	public void setSearchForExactValue(boolean searchForExactValue) {
		this.searchForExactValue = searchForExactValue;
	}

	@Override
	public boolean isSearchPrefixOnly() {
		return searchPrefixOnly;
	}

	public void setSearchPrefixOnly(boolean searchPrefixOnly) {
		this.searchPrefixOnly = searchPrefixOnly;
	}

	@Override
	public boolean isSortable() {
		return sortable;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public boolean isTransient() {
		return trans;
	}

	public void setTransient(boolean trans) {
		this.trans = trans;
	}

	@Override
	public boolean isUrl() {
		return url;
	}

	public void setUrl(boolean url) {
		this.url = url;
	}

	@Override
	public boolean isUseThousandsGrouping() {
		return useThousandsGrouping;
	}

	public void setUseThousandsGrouping(boolean useThousandsGrouping) {
		this.useThousandsGrouping = useThousandsGrouping;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isVisibleInTable() {
		return visibleInTable;
	}

	public void setVisibleInTable(boolean visibleInTable) {
		this.visibleInTable = visibleInTable;
	}

	@Override
	public boolean isWeek() {
		return week;
	}

	public void setWeek(boolean week) {
		this.week = week;
	}

	@Override
	public void removeCascades() {
		this.cascadeAttributes.clear();
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "entityModel");
	}

}
