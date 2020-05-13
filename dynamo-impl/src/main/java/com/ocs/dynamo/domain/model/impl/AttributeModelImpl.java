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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.util.SystemPropertyUtils;

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

	private final Map<String, String> cascadeAttributes = new HashMap<>();

	private Map<String, CascadeMode> cascadeModes = new HashMap<>();

	private String collectionTableFieldName;

	private String collectionTableName;

	private boolean complexEditable;

	private boolean currency;

	private Map<String, Object> customSettings = new HashMap<>();

	private AttributeDateType dateType;

	private String defaultDescription;

	private String defaultDisplayName;

	private String defaultFalseRepresentation;

	private String defaultPrompt;

	private String defaultTrueRepresentation;

	private Object defaultValue;

	private Map<String, Optional<String>> descriptions = new ConcurrentHashMap<>();

	private String displayFormat;

	private Map<String, Optional<String>> displayNames = new ConcurrentHashMap<>();

	private EditableType editableType;

	private boolean email;

	private EntityModel<?> entityModel;

	private Map<String, Optional<String>> falseRepresentations = new ConcurrentHashMap<>();

	private String fileNameProperty;

	private AttributeSelectMode gridSelectMode;

	private final List<String> groupTogetherWith = new ArrayList<>();

	private boolean ignoreInSearchFilter;

	private boolean image;

	private boolean mainAttribute;

	private Integer maxLength;

	private Integer maxLengthInGrid;

	private Long maxValue;

	private Class<?> memberType;

	private Integer minLength;

	private Long minValue;

	private boolean multipleSearch;

	private String name;

	private boolean navigable;

	private EntityModel<?> nestedEntityModel;

	private Integer order;

	private boolean percentage;

	private int precision;

	private Map<String, Optional<String>> prompts = new ConcurrentHashMap<>();

	private boolean quickAddAllowed;

	private String quickAddPropertyName;

	private String replacementSearchPath;

	private String replacementSortPath;

	private boolean required;

	private boolean requiredForSearching;

	private boolean searchCaseSensitive;

	private boolean searchDateOnly;

	private boolean searchForExactValue;

	private SearchMode searchMode;

	private boolean searchPrefixOnly;

	private AttributeSelectMode searchSelectMode;

	private AttributeSelectMode selectMode;

	private boolean sortable;

	private String styles;

	private AttributeTextFieldMode textFieldMode;

	private boolean thousandsGrouping;

	private Map<String, Optional<String>> trueRepresentations = new ConcurrentHashMap<>();

	private Class<?> type;

	private boolean url;

	private boolean visible;

	private boolean visibleInGrid;

	private boolean week;

	@Override
	public void addCascade(final String cascadeTo, final String filterPath, final CascadeMode mode) {
		this.cascadeAttributes.put(cascadeTo, filterPath);
		this.cascadeModes.put(cascadeTo, mode);
	}

	@Override
	public void addGroupTogetherWith(final String path) {
		groupTogetherWith.add(path);
	}

	@Override
	public int compareTo(final AttributeModel o) {
		return this.getOrder() - o.getOrder();
	}

	@Override
	public String getActualSortPath() {
		return replacementSortPath != null ? replacementSortPath : getPath();
	}

	@Override
	public Set<String> getAllowedExtensions() {
		return allowedExtensions;
	}

	@Override
	public AttributeType getAttributeType() {
		return attributeType;
	}

	@Override
	public Set<String> getCascadeAttributes() {
		return cascadeAttributes.keySet();
	}

	@Override
	public String getCascadeFilterPath(final String cascadeTo) {
		return this.cascadeAttributes.get(cascadeTo);
	}

	@Override
	public CascadeMode getCascadeMode(final String cascadeTo) {
		return this.cascadeModes.get(cascadeTo);
	}

	@Override
	public String getCollectionTableFieldName() {
		return collectionTableFieldName;
	}

	@Override
	public String getCollectionTableName() {
		return collectionTableName;
	}

	@Override
	public Object getCustomSetting(String name) {
		return customSettings.get(name);
	}

	@Override
	public AttributeDateType getDateType() {
		return dateType;
	}

	public String getDefaultDescription() {
		return defaultDescription;
	}

	public String getDefaultDisplayName() {
		return defaultDisplayName;
	}

	public String getDefaultTrueRepresentation() {
		return defaultTrueRepresentation;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getDescription(Locale locale) {
		// lookup description, falling back to overridden display name or otherwise to
		// the default description
		String displayNameNoDefault = lookupNoDefault(displayNames, locale, EntityModel.DISPLAY_NAME);
		return lookup(descriptions, locale, EntityModel.DESCRIPTION, displayNameNoDefault, defaultDescription);
	}

	@Override
	public String getDisplayFormat() {
		return displayFormat;
	}

	@Override
	public String getDisplayName(Locale locale) {
		return lookup(displayNames, locale, EntityModel.DISPLAY_NAME, defaultDisplayName, null);
	}

	@Override
	public EditableType getEditableType() {
		return editableType;
	}

	@Override
	public EntityModel<?> getEntityModel() {
		return entityModel;
	}

	@Override
	public String getFalseRepresentation(Locale locale) {
		return lookup(falseRepresentations, locale, EntityModel.FALSE_REPRESENTATION,
				SystemPropertyUtils.getDefaultFalseRepresentation(locale), defaultFalseRepresentation);
	}

	@Override
	public String getFileNameProperty() {
		return fileNameProperty;
	}

	public AttributeSelectMode getGridSelectMode() {
		return gridSelectMode;
	}

	@Override
	public List<String> getGroupTogetherWith() {
		return Collections.unmodifiableList(groupTogetherWith);
	}

	@Override
	public Integer getMaxLength() {
		return maxLength;
	}

	@Override
	public Integer getMaxLengthInGrid() {
		return maxLengthInGrid;
	}

	@Override
	public Long getMaxValue() {
		return maxValue;
	}

	@Override
	public Class<?> getMemberType() {
		return memberType;
	}

	@Override
	public Integer getMinLength() {
		return minLength;
	}

	@Override
	public Long getMinValue() {
		return minValue;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public EntityModel<?> getNestedEntityModel() {
		return nestedEntityModel;
	}

	@Override
	public Class<?> getNormalizedType() {
		return getMemberType() != null ? getMemberType() : getType();
	}

	@Override
	public Integer getOrder() {
		return order;
	}

	@Override
	public String getPath() {
		final String reference = entityModel.getReference();
		final int p = reference.indexOf('.');

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

	@Override
	public String getPrompt(Locale locale) {
		if (!SystemPropertyUtils.useDefaultPromptValue()) {
			return null;
		}

		// look up prompt. If not defined, look up display name
		String displayNameNoDefault = lookupNoDefault(displayNames, locale, EntityModel.DISPLAY_NAME);
		return lookup(prompts, locale, EntityModel.PROMPT, displayNameNoDefault, defaultPrompt);
	}

	@Override
	public String getQuickAddPropertyName() {
		return quickAddPropertyName;
	}

	@Override
	public String getReplacementSearchPath() {
		return replacementSearchPath;
	}

	public String getReplacementSortPath() {
		return replacementSortPath;
	}

	public SearchMode getSearchMode() {
		return searchMode;
	}

	@Override
	public AttributeSelectMode getSearchSelectMode() {
		return searchSelectMode;
	}

	@Override
	public AttributeSelectMode getSelectMode() {
		return selectMode;
	}

	/**
	 * @return the styles
	 */
	@Override
	public String getStyles() {
		return styles;
	}

	@Override
	public AttributeTextFieldMode getTextFieldMode() {
		return textFieldMode;
	}

	@Override
	public String getTrueRepresentation(Locale locale) {
		return lookup(trueRepresentations, locale, EntityModel.TRUE_REPRESENTATION,
				SystemPropertyUtils.getDefaultTrueRepresentation(locale), defaultTrueRepresentation);
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public boolean isAlreadyGrouped() {
		return alreadyGrouped;
	}

	@Override
	public boolean isComplexEditable() {
		return complexEditable;
	}

	@Override
	public boolean isCurrency() {
		return currency;
	}

	@Override
	public boolean isEmail() {
		return email;
	}

	@Override
	public boolean isEmbedded() {
		return AttributeType.EMBEDDED.equals(attributeType);
	}

	public boolean isIgnoreInSearchFilter() {
		return ignoreInSearchFilter;
	}

	@Override
	public boolean isImage() {
		return image;
	}

	@Override
	public boolean isMainAttribute() {
		return mainAttribute;
	}

	@Override
	public boolean isMultipleSearch() {
		return multipleSearch;
	}

	@Override
	public boolean isNavigable() {
		return navigable;
	}

	@Override
	public boolean isNumerical() {
		return Number.class.isAssignableFrom(type);
	}

	@Override
	public boolean isPercentage() {
		return percentage;
	}

	@Override
	public boolean isQuickAddAllowed() {
		return quickAddAllowed;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isRequiredForSearching() {
		return requiredForSearching;
	}

	public boolean isSearchable() {
		return SearchMode.ADVANCED.equals(searchMode) || SearchMode.ALWAYS.equals(searchMode);
	}

	@Override
	public boolean isSearchCaseSensitive() {
		return searchCaseSensitive;
	}

	@Override
	public boolean isSearchDateOnly() {
		return searchDateOnly;
	}

	@Override
	public boolean isSearchForExactValue() {
		return searchForExactValue;
	}

	@Override
	public boolean isSearchPrefixOnly() {
		return searchPrefixOnly;
	}

	@Override
	public boolean isSortable() {
		return sortable;
	}

	@Override
	public boolean isThousandsGrouping() {
		return thousandsGrouping;
	}

	@Override
	public boolean isUrl() {
		return url;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean isVisibleInGrid() {
		return visibleInGrid;
	}

	@Override
	public boolean isWeek() {
		return week;
	}

	/**
	 * Looks up the translations of a value for a certain locale
	 * 
	 * @param source         the translation cache
	 * @param locale         the locale
	 * @param key            the message key
	 * @param fallBack       the first fallback value
	 * @param secondFallBack the second fallback value
	 * @return
	 */
	private String lookup(Map<String, Optional<String>> source, Locale locale, String key, String fallback,
			String secondFallBack) {
		if (!source.containsKey(locale.toString())) {
			try {
				ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
				String str = rb.getString(getEntityModel().getReference() + "." + getPath() + "." + key);
				source.put(locale.toString(), Optional.ofNullable(str));
			} catch (MissingResourceException ex) {
				source.put(locale.toString(), Optional.empty());
			}
		}

		Optional<String> optional = source.get(locale.toString());
		return optional.orElse(fallback != null ? fallback : secondFallBack);
	}

	/**
	 * Looks up a key from the message bundle, returning an empty optional if
	 * nothing can be found
	 * 
	 * @param source the cache
	 * @param locale the locale
	 * @param key    the message key
	 * @return
	 */
	private String lookupNoDefault(Map<String, Optional<String>> source, Locale locale, String key) {
		if (!source.containsKey(locale.toString())) {
			try {
				// resource bundle has not been checked yet, check it now
				ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
				String str = rb.getString(getEntityModel().getReference() + "." + getPath() + "." + key);
				source.put(locale.toString(), Optional.ofNullable(str));
			} catch (MissingResourceException ex) {
				// nothing in resource bundle, store empty value
				source.put(locale.toString(), Optional.empty());
			}
		}

		// look up from cached
		Optional<String> optional = source.get(locale.toString());
		return optional.orElse(null);
	}

	@Override
	public void removeCascades() {
		this.cascadeAttributes.clear();
	}

	public void setAllowedExtensions(Set<String> allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}

	public void setAlreadyGrouped(boolean alreadyGrouped) {
		this.alreadyGrouped = alreadyGrouped;
	}

	public void setAttributeType(AttributeType attributeType) {
		this.attributeType = attributeType;
	}

	public void setCollectionTableFieldName(String collectionTableFieldName) {
		this.collectionTableFieldName = collectionTableFieldName;
	}

	public void setCollectionTableName(String collectionTableName) {
		this.collectionTableName = collectionTableName;
	}

	public void setComplexEditable(boolean complexEditable) {
		this.complexEditable = complexEditable;
	}

	public void setCurrency(boolean currency) {
		this.currency = currency;
	}

	@Override
	public void setCustomSetting(String name, Object value) {
		this.customSettings.put(name, value);
	}

	public void setDateType(AttributeDateType dateType) {
		this.dateType = dateType;
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	public void setDefaultDisplayName(String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	public void setDefaultFalseRepresentation(String defaultFalseRepresentation) {
		this.defaultFalseRepresentation = defaultFalseRepresentation;
	}

	public void setDefaultPrompt(String defaultPrompt) {
		this.defaultPrompt = defaultPrompt;
	}

	public void setDefaultTrueRepresentation(String defaultTrueRepresentation) {
		this.defaultTrueRepresentation = defaultTrueRepresentation;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setDisplayFormat(String displayFormat) {
		this.displayFormat = displayFormat;
	}

	public void setEditableType(EditableType editableType) {
		this.editableType = editableType;
	}

	public void setEmail(boolean email) {
		this.email = email;
	}

	public void setEntityModel(EntityModel<?> entityModel) {
		this.entityModel = entityModel;
	}

	public void setFileNameProperty(String fileNameProperty) {
		this.fileNameProperty = fileNameProperty;
	}

	public void setGridSelectMode(AttributeSelectMode gridSelectMode) {
		this.gridSelectMode = gridSelectMode;
	}

	public void setIgnoreInSearchFilter(boolean ignoreInSearchFilter) {
		this.ignoreInSearchFilter = ignoreInSearchFilter;
	}

	public void setImage(boolean image) {
		this.image = image;
	}

	@Override
	public void setMainAttribute(boolean main) {
		this.mainAttribute = main;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public void setMaxLengthInGrid(Integer maxLengthInGrid) {
		this.maxLengthInGrid = maxLengthInGrid;
	}

	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}

	public void setMemberType(Class<?> memberType) {
		this.memberType = memberType;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}

	public void setMultipleSearch(boolean multipleSearch) {
		this.multipleSearch = multipleSearch;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNavigable(boolean navigable) {
		this.navigable = navigable;
	}

	public void setNestedEntityModel(EntityModel<?> nestedEntityModel) {
		this.nestedEntityModel = nestedEntityModel;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public void setPercentage(boolean percentage) {
		this.percentage = percentage;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public void setQuickAddAllowed(boolean quickAddAllowed) {
		this.quickAddAllowed = quickAddAllowed;
	}

	public void setQuickAddPropertyName(String quickAddPropertyName) {
		this.quickAddPropertyName = quickAddPropertyName;
	}

	public void setReplacementSearchPath(String replacementSearchPath) {
		this.replacementSearchPath = replacementSearchPath;
	}

	public void setReplacementSortPath(String replacementSortPath) {
		this.replacementSortPath = replacementSortPath;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setRequiredForSearching(boolean requiredForSearching) {
		this.requiredForSearching = requiredForSearching;
	}

	public void setSearchCaseSensitive(boolean searchCaseSensitive) {
		this.searchCaseSensitive = searchCaseSensitive;
	}

	public void setSearchDateOnly(boolean searchDateOnly) {
		this.searchDateOnly = searchDateOnly;
	}

	public void setSearchForExactValue(boolean searchForExactValue) {
		this.searchForExactValue = searchForExactValue;
	}

	public void setSearchMode(SearchMode searchMode) {
		this.searchMode = searchMode;
	}

	public void setSearchPrefixOnly(boolean searchPrefixOnly) {
		this.searchPrefixOnly = searchPrefixOnly;
	}

	public void setSearchSelectMode(AttributeSelectMode searchSelectMode) {
		this.searchSelectMode = searchSelectMode;
	}

	public void setSelectMode(AttributeSelectMode selectMode) {
		this.selectMode = selectMode;
	}

	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}

	public void setStyles(String styles) {
		this.styles = styles;
	}

	public void setTextFieldMode(AttributeTextFieldMode textFieldMode) {
		this.textFieldMode = textFieldMode;
	}

	public void setThousandsGrouping(boolean thousandsGrouping) {
		this.thousandsGrouping = thousandsGrouping;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public void setUrl(boolean url) {
		this.url = url;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setVisibleInGrid(boolean visibleInGrid) {
		this.visibleInGrid = visibleInGrid;
	}

	public void setWeek(boolean week) {
		this.week = week;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "entityModel");
	}
}
