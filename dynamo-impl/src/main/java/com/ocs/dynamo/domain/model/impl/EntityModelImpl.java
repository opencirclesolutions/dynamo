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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * An implementation of an entity model - holds metadata about an entity
 * 
 * @author bas.rutten
 * @param <T> the class of the entity
 */
public class EntityModelImpl<T> implements EntityModel<T> {

	// use a linked hash map to guarantee the ordering
	private final Map<String, List<AttributeModel>> attributeModels = new LinkedHashMap<>();

	private String defaultDescription;

	private String defaultDisplayName;

	private String defaultDisplayNamePlural;

	private Map<String, Optional<String>> displayNames = new ConcurrentHashMap<>();

	private Map<String, Optional<String>> displayNamesPlural = new ConcurrentHashMap<>();

	private Map<String, Optional<String>> descriptions = new ConcurrentHashMap<>();

	private String displayProperty;

	private Class<T> entityClass;

	private AttributeModel idAttributeModel;

	private String reference;

	private Map<AttributeModel, Boolean> sortOrder = new LinkedHashMap<>();

	public EntityModelImpl() {
	}

	@Override
	public void addAttributeGroup(String attributeGroup) {
		if (!attributeModels.containsKey(attributeGroup)) {
			attributeModels.put(attributeGroup, new ArrayList<>());
		}
	}

	public void addAttributeModel(String attributeGroup, AttributeModel model) {
		attributeModels.get(attributeGroup).add(model);
	}

	@Override
	public void addAttributeModel(String attributeGroup, AttributeModel model, AttributeModel existingModel) {
		List<AttributeModel> group = attributeModels.get(attributeGroup);
		if (group.contains(existingModel)) {
			group.add(group.indexOf(existingModel), model);
		} else {
			group.add(model);
		}
	}

	/**
	 * Constructs a stream of all attribute models
	 * 
	 * @return
	 */
	private Stream<AttributeModel> constructAttributeModelStream() {
		return attributeModels.values().stream().flatMap(List::stream)
				.sorted(Comparator.comparing(AttributeModel::getOrder));
	}

	private List<AttributeModel> filterAttributeModels(Predicate<AttributeModel> p) {
		return Collections.unmodifiableList(constructAttributeModelStream().filter(p).collect(Collectors.toList()));
	}

	private AttributeModel findAttributeModel(Predicate<AttributeModel> p) {
		return constructAttributeModelStream().filter(p).findFirst().orElse(null);
	}

	@Override
	public List<String> getAttributeGroups() {
		return new ArrayList<>(attributeModels.keySet());
	}

	@Override
	public AttributeModel getAttributeModel(String attributeName) {
		if (!StringUtils.isEmpty(attributeName)) {

			AttributeModel model = findAttributeModel(m -> m.getName().equals(attributeName));
			if (model != null) {
				return model;
			}

			// check for nested property
			String[] names = attributeName.split("\\.");
			if (names.length > 1) {
				// Find Attribute model
				AttributeModel am = getAttributeModel(names[0]);
				if (am != null) {
					// Find nested entity model
					EntityModel<?> nem = am.getNestedEntityModel();
					if (nem != null) {
						return nem.getAttributeModel(attributeName.substring(names[0].length() + 1));
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<AttributeModel> getAttributeModels() {
		List<AttributeModel> list = constructAttributeModelStream().collect(Collectors.toList());
		return Collections.unmodifiableList(list);
	}

	@Override
	public List<AttributeModel> getAttributeModelsForGroup(String group) {
		return Collections.unmodifiableList(attributeModels.get(group));
	}

	@Override
	public List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type) {
		return filterAttributeModels(model -> {
			Class<?> rt = ClassUtils.getResolvedType(getEntityClass(), model.getName(), 0);
			return (attributeType == null || attributeType.equals(model.getAttributeType())) && (type == null
					|| type.isAssignableFrom(model.getType()) || (rt != null && type.isAssignableFrom(rt)));
		});
	}

	@Override
	public List<AttributeModel> getCascadeAttributeModels() {
		List<AttributeModel> result = new ArrayList<>();
		for (AttributeModel model : getAttributeModels()) {
			if (!model.getCascadeAttributes().isEmpty()) {
				result.add(model);
			}

			// add nested models
			if (model.getNestedEntityModel() != null) {
				List<AttributeModel> nested = model.getNestedEntityModel().getCascadeAttributeModels();
				result.addAll(nested);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public String getDefaultDisplayName() {
		return defaultDisplayName;
	}

	public String getDefaultDisplayNamePlural() {
		return defaultDisplayNamePlural;
	}

	@Override
	public String getDescription(Locale locale) {
		return lookup(descriptions, locale, EntityModel.DESCRIPTION, defaultDescription);
	}

	@Override
	public String getDisplayName(Locale locale) {
		return lookup(displayNames, locale, EntityModel.DISPLAY_NAME, defaultDisplayName);
	}

	@Override
	public String getDisplayNamePlural(Locale locale) {
		return lookup(displayNamesPlural, locale, EntityModel.DISPLAY_NAME_PLURAL, defaultDisplayNamePlural);
	}

	@Override
	public String getDisplayProperty() {
		return displayProperty;
	}

	@Override
	public Class<T> getEntityClass() {
		return entityClass;
	}

	@Override
	public AttributeModel getIdAttributeModel() {
		return idAttributeModel;
	}

	@Override
	public AttributeModel getMainAttributeModel() {
		return findAttributeModel(AttributeModel::isMainAttribute);
	}

	@Override
	public String getReference() {
		return reference;
	}

	@Override
	public List<AttributeModel> getRequiredForSearchingAttributeModels() {
		List<AttributeModel> result = constructAttributeModelStream().map(m -> {
			List<AttributeModel> list = new ArrayList<>();
			if (m.isSearchable() && m.isRequiredForSearching()) {
				list.add(m);
			}
			// add nested models
			if (m.getNestedEntityModel() != null) {
				List<AttributeModel> nested = m.getNestedEntityModel().getRequiredForSearchingAttributeModels();
				list.addAll(nested);
			}
			return list;
		}).flatMap(List::stream).collect(Collectors.toList());
		return Collections.unmodifiableList(result);
	}

	@Override
	public Map<AttributeModel, Boolean> getSortOrder() {
		return sortOrder;
	}

	@Override
	public boolean isAttributeGroupVisible(String group, boolean readOnly) {
		return attributeModels.get(group).stream()
				.filter(m -> AttributeType.BASIC.equals(m.getAttributeType())
						|| AttributeType.LOB.equals(m.getAttributeType()) || m.isComplexEditable())
				.anyMatch(m -> m.isVisible() && (readOnly || !m.getEditableType().equals(EditableType.READ_ONLY)));
	}

	/**
	 * Looks up a text message from a resource bundle
	 * 
	 * @param source   the message cache
	 * @param locale   the desired locale
	 * @param key      the message key
	 * @param fallBack value to fall back to when nothing found
	 * @return
	 */
	private String lookup(Map<String, Optional<String>> source, Locale locale, String key, String fallBack) {
		if (!source.containsKey(locale.toString())) {
			try {
				ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
				String str = rb.getString(reference + "." + key);
				source.put(locale.toString(), Optional.ofNullable(str));
			} catch (MissingResourceException ex) {
				source.put(locale.toString(), Optional.empty());
			}
		}

		Optional<String> optional = source.get(locale.toString());
		return optional.orElse(fallBack);
	}

	public void setDefaultDescription(String defaultDescription) {
		this.defaultDescription = defaultDescription;
	}

	public void setDefaultDisplayName(String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	public void setDefaultDisplayNamePlural(String defaultDisplayNamePlural) {
		this.defaultDisplayNamePlural = defaultDisplayNamePlural;
	}

	public void setDisplayProperty(String displayProperty) {
		this.displayProperty = displayProperty;
	}

	public void setEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	void setIdAttributeModel(AttributeModel idAttributeModel) {
		this.idAttributeModel = idAttributeModel;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public void setSortOrder(Map<AttributeModel, Boolean> sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "attributeModels");
	}

	@Override
	public boolean usesDefaultGroupOnly() {
		return attributeModels.keySet().size() == 1
				&& attributeModels.keySet().iterator().next().equals(EntityModel.DEFAULT_GROUP);
	}

}
