package org.dynamoframework.domain.model.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.domain.model.*;
import org.dynamoframework.domain.model.annotation.SearchMode;
import org.dynamoframework.utils.NumberUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the AttributeModel interface - simple container for
 * properties
 *
 * @author bas.rutten
 */
@Data
@EqualsAndHashCode(callSuper = false, of = {"entityModel", "name"})
@ToString
public class AttributeModelImpl implements AttributeModel {

	private final DynamoProperties dynamoProperties;

	private Set<String> allowedExtensions = new HashSet<>();

	private boolean alreadyGrouped;

	private AttributeType attributeType;

	private String autofillInstructions;

	private AttributeBooleanFieldMode booleanFieldMode;

	private final Map<String, String> cascadeAttributes = new HashMap<>();

	private Map<String, CascadeMode> cascadeModes = new HashMap<>();

	private String collectionTableFieldName;

	private String collectionTableName;

	private String currencyCode;

	private Map<String, Object> customSettings = new HashMap<>();

	private AttributeDateType dateType;

	private String defaultDescription;

	private String defaultDisplayFormat;

	private String defaultDisplayName;

	private String defaultFalseRepresentation;

	private String defaultPrompt;

	private String defaultTrueRepresentation;

	private Object defaultValue;

	private Object defaultSearchValue;

	private Object defaultSearchValueFrom;

	private Object defaultSearchValueTo;

	private Map<String, Optional<String>> descriptions = new ConcurrentHashMap<>();

	private Map<String, Optional<String>> displayFormats = new ConcurrentHashMap<>();

	private Map<String, Optional<String>> displayNames = new ConcurrentHashMap<>();

	private boolean downloadAllowed;

	private EditableType editableType;

	private ElementCollectionMode elementCollectionMode;

	private boolean email;

	private AttributeEnumFieldMode enumFieldMode;

	@ToString.Exclude
	private EntityModel<?> entityModel;

	private Map<String, Optional<String>> falseRepresentations = new ConcurrentHashMap<>();

	private String fileNameProperty;

	private Integer gridOrder;

	private final List<String> groupTogetherWith = new ArrayList<>();

	private boolean ignoreInSearchFilter;

	private boolean image;

	private String lookupEntityReference;

	private QueryType lookupQueryType;

	private Integer maxCollectionSize;

	private Integer maxLength;

	private Integer maxLengthInGrid;

	private BigDecimal maxValue;

	private Class<?> memberType;

	private Integer minCollectionSize;

	private Integer minLength;

	private BigDecimal minValue;

	private boolean multipleSearch;

	private String name;

	private boolean neededInData;

	private boolean navigable;

	private String navigationLink;

	private boolean nestedDetails;

	private EntityModel<?> nestedEntityModel;

	private NumberFieldMode numberFieldMode;

	private Integer numberFieldStep;

	private Integer order;

	private boolean percentage;

	private int precision;

	private Map<String, Optional<String>> prompts = new ConcurrentHashMap<>();

	private boolean quickAddAllowed;

	private String replacementSearchPath;

	private String replacementSortPath;

	private boolean required;

	private boolean requiredForSearching;

	private boolean searchCaseSensitive;

	private boolean searchDateOnly;

	private boolean searchForExactValue;

	private SearchMode searchMode;

	private Integer searchOrder;

	private boolean searchPrefixOnly;

	private AttributeSelectMode searchSelectMode;

	private AttributeSelectMode selectMode;

	private boolean showDetailsPaginator;

	private boolean showPassword;

	private boolean sortable;

	private AttributeTextFieldMode textFieldMode;

	private boolean trimSpaces;

	private Map<String, Optional<String>> trueRepresentations = new ConcurrentHashMap<>();

	private Class<?> type;

	private boolean url;

	private boolean visibleInForm;

	private boolean visibleInGrid;

	private boolean setterMethod;

	public AttributeModelImpl(DynamoProperties dynamoProperties) {
		this.dynamoProperties = dynamoProperties;
	}

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
	public int compareTo(final AttributeModel other) {
		return this.getOrder() - other.getOrder();
	}

	@Override
	public String getActualSearchPath() {
		return replacementSearchPath != null ? replacementSearchPath : getPath();
	}

	@Override
	public String getActualSortPath() {
		return replacementSortPath != null ? replacementSortPath : getPath();
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
	public Object getCustomSetting(String name) {
		return customSettings.get(name);
	}

	@Override
	public String getDescription(Locale locale) {
		// lookup description, falling back to overridden display name or otherwise to
		// the default description
		String displayNameNoDefault = lookupNoDefault(displayNames, locale, EntityModel.DISPLAY_NAME);
		return lookup(descriptions, locale, EntityModel.DESCRIPTION, displayNameNoDefault, defaultDescription);
	}

	@Override
	public String getDisplayFormat(Locale locale) {
		return lookup(displayFormats, locale, EntityModel.DISPLAY_FORMAT, defaultDisplayFormat, null);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return lookup(displayNames, locale, EntityModel.DISPLAY_NAME, defaultDisplayName, null);
	}

	@Override
	public String getFalseRepresentation(Locale locale) {
		return lookup(falseRepresentations, locale, EntityModel.FALSE_REPRESENTATION,
			dynamoProperties.getDefaults().getFalseRepresentations().get(locale.toString()), defaultFalseRepresentation);
	}

	@Override
	public List<String> getGroupTogetherWith() {
		return Collections.unmodifiableList(groupTogetherWith);
	}

	@Override
	public Class<?> getNormalizedType() {
		return getMemberType() != null ? getMemberType() : getType();
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
	public String getPrompt(Locale locale) {
		if (!dynamoProperties.getDefaults().isUsePromptValue()) {
			return null;
		}

		// look up prompt. If not defined, look up display name
		String displayNameNoDefault = lookupNoDefault(displayNames, locale, EntityModel.DISPLAY_NAME);
		return lookup(prompts, locale, EntityModel.PROMPT, displayNameNoDefault, defaultPrompt);
	}

	@Override
	public String getTrueRepresentation(Locale locale) {
		return lookup(trueRepresentations, locale, EntityModel.TRUE_REPRESENTATION,
			dynamoProperties.getDefaults().getTrueRepresentations().get(locale.toString()), defaultTrueRepresentation);
	}

	@Override
	public boolean isBoolean() {
		return Boolean.class.equals(type) || boolean.class.equals(type);
	}

	@Override
	public boolean isEmbedded() {
		return AttributeType.EMBEDDED.equals(attributeType);
	}

	@Override
	public boolean isNumerical() {
		return NumberUtils.isNumeric(type);
	}

	@Override
	public boolean isIntegral() {
		return NumberUtils.isInteger(type) || NumberUtils.isLong(type);
	}

	public boolean isSearchable() {
		return SearchMode.ADVANCED.equals(searchMode) || SearchMode.ALWAYS.equals(searchMode);
	}

	/**
	 * Looks up the translation of a value for a certain locale
	 *
	 * @param source         the translation cache
	 * @param locale         the locale
	 * @param key            the message key
	 * @param fallBack       the first fallback value
	 * @param secondFallBack the second fallback value
	 * @return the translation for the specified key
	 */
	private String lookup(Map<String, Optional<String>> source, Locale locale, String key, String fallBack,
						  String secondFallBack) {
		if (!source.containsKey(locale.toString())) {
			try {
				ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
				String str = rb.getString(getEntityModel().getReference() + "." + getPath() + "." + key);
				source.put(locale.toString(), Optional.of(str));
			} catch (MissingResourceException ex) {
				source.put(locale.toString(), Optional.empty());
			}
		}

		Optional<String> optional = source.get(locale.toString());
		return optional.orElse(fallBack != null ? fallBack : secondFallBack);
	}

	/**
	 * Looks up a key from the message bundle, returning an empty optional if
	 * nothing can be found
	 *
	 * @param source the cache
	 * @param locale the locale
	 * @param key    the message key
	 * @return the value  that the key resolves to
	 */
	private String lookupNoDefault(Map<String, Optional<String>> source, Locale locale, String key) {
		if (!source.containsKey(locale.toString())) {
			try {
				// resource bundle has not been checked yet, check it now
				ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
				String str = rb.getString(getEntityModel().getReference() + "." + getPath() + "." + key);
				source.put(locale.toString(), Optional.of(str));
			} catch (MissingResourceException ex) {
				// nothing in resource bundle, store empty value
				source.put(locale.toString(), Optional.empty());
			}
		}
		return source.get(locale.toString()).orElse(null);
	}

	@Override
	public void removeCascades() {
		this.cascadeAttributes.clear();
	}

	@Override
	public void setCustomSetting(String name, Object value) {
		this.customSettings.put(name, value);
	}

	@Override
	public boolean hasSetterMethod() {
		return setterMethod;
	}

	public void setHasSetterMethod(boolean hasSetterMethod) {
		this.setterMethod = hasSetterMethod;
	}
}
