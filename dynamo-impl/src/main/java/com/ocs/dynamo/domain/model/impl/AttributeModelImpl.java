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

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.MultiSelectMode;
import com.ocs.dynamo.domain.model.NumberFieldMode;
import com.ocs.dynamo.domain.model.PagingMode;
import com.ocs.dynamo.domain.model.ThousandsGroupingMode;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.util.SystemPropertyUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Implementation of the AttributeModel interface - simple container for
 * properties
 * 
 * @author bas.rutten
 */
@Data
@EqualsAndHashCode(callSuper = false, of = { "entityModel", "name" })
@ToString
public class AttributeModelImpl implements AttributeModel {

	private Set<String> allowedExtensions = new HashSet<>();

	private boolean alreadyGrouped;

	private AttributeType attributeType;

	private final Map<String, String> cascadeAttributes = new HashMap<>();

	private Map<String, CascadeMode> cascadeModes = new HashMap<>();

	private boolean clearButtonVisible;

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

	@ToString.Exclude
	private EntityModel<?> entityModel;

	private Map<String, Optional<String>> falseRepresentations = new ConcurrentHashMap<>();

	private String fileNameProperty;

	private Integer gridOrder;

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

	private MultiSelectMode multiSelectMode;

	private String name;

	private boolean navigable;

	private EntityModel<?> nestedEntityModel;

	private NumberFieldMode numberFieldMode;

	private Integer numberFieldStep;

	private Integer order;

	private PagingMode pagingMode;

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

	private Integer searchOrder;

	private boolean searchPrefixOnly;

	private AttributeSelectMode searchSelectMode;

	private AttributeSelectMode selectMode;

	private boolean sortable;

	private AttributeTextFieldMode textFieldMode;

	private ThousandsGroupingMode thousandsGroupingMode;

	private boolean trimSpaces;

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
	public String getDisplayName(Locale locale) {
		return lookup(displayNames, locale, EntityModel.DISPLAY_NAME, defaultDisplayName, null);
	}

	@Override
	public String getFalseRepresentation(Locale locale) {
		return lookup(falseRepresentations, locale, EntityModel.FALSE_REPRESENTATION,
				SystemPropertyUtils.getDefaultFalseRepresentation(locale), defaultFalseRepresentation);
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
		if (!SystemPropertyUtils.useDefaultPromptValue()) {
			return null;
		}

		// look up prompt. If not defined, look up display name
		String displayNameNoDefault = lookupNoDefault(displayNames, locale, EntityModel.DISPLAY_NAME);
		return lookup(prompts, locale, EntityModel.PROMPT, displayNameNoDefault, defaultPrompt);
	}

	@Override
	public String getTrueRepresentation(Locale locale) {
		return lookup(trueRepresentations, locale, EntityModel.TRUE_REPRESENTATION,
				SystemPropertyUtils.getDefaultTrueRepresentation(locale), defaultTrueRepresentation);
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
		return com.ocs.dynamo.utils.NumberUtils.isNumeric(type);
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
				source.put(locale.toString(), Optional.ofNullable(str));
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
	public boolean useThousandsGroupingInEditMode() {
		return ThousandsGroupingMode.ALWAYS.equals(thousandsGroupingMode)
				|| ThousandsGroupingMode.EDIT.equals(thousandsGroupingMode);
	}

	@Override
	public boolean useThousandsGroupingInViewMode() {
		return ThousandsGroupingMode.ALWAYS.equals(thousandsGroupingMode)
				|| ThousandsGroupingMode.VIEW.equals(thousandsGroupingMode);
	}

}
