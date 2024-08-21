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

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.domain.model.*;
import org.dynamoframework.utils.ClassUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An implementation of an entity model - holds metadata about an entity
 *
 * @param <T> the class of the entity
 * @author bas.rutten
 */
@Data
@EqualsAndHashCode(callSuper = false, of = {"reference", "entityClass"})
@Builder(toBuilder = true)
@ToString
public class EntityModelImpl<T> implements EntityModel<T> {

    private String autofillInstructions;

    @Builder.Default
    @ToString.Exclude
    private final Map<String, List<AttributeModel>> attributeModels = new LinkedHashMap<>();

    private String defaultDescription;

    private String defaultDisplayName;

    private String defaultDisplayNamePlural;

    @Builder.Default
    private Map<String, Optional<String>> descriptions = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, Optional<String>> displayNames = new ConcurrentHashMap<>();

    @Builder.Default
    private Map<String, Optional<String>> displayNamesPlural = new ConcurrentHashMap<>();

    private String displayProperty;

    private Class<T> entityClass;

    private boolean gridOrderSet;

    private int nestingDepth;

    private String reference;

    private boolean searchOrderSet;

    private boolean listAllowed;

    private boolean exportAllowed;

    private boolean updateAllowed;

    private boolean deleteAllowed;

    private boolean createAllowed;

    private boolean searchAllowed;

    private String fileUploadEntityName;

    private String fileUploadAttributeName;

    private String fileUploadEntityIdPath;

    private List<FetchJoinInformation> fetchJoins;

    private List<FetchJoinInformation> detailJoins;

    private int maxSearchResults;

    private List<EntityModelAction> entityModelActions;

    @Builder.Default
    private Map<AttributeModel, Boolean> sortOrder = new LinkedHashMap<>();

    @Builder.Default
    private List<String> readRoles = new ArrayList<>();

    @Builder.Default
    private List<String> writeRoles = new ArrayList<>();

    @Builder.Default
    private List<String> deleteRoles = new ArrayList<>();

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

    private Stream<AttributeModel> constructAttributeModelStream(Comparator<AttributeModel> comp) {
        return attributeModels.values().stream().flatMap(List::stream).sorted(comp);
    }

    private List<AttributeModel> filterAttributeModels(Predicate<AttributeModel> p) {
        return Collections
                .unmodifiableList(constructAttributeModelStream(Comparator.comparing(AttributeModel::getOrder))
                        .filter(p).toList());
    }

    private AttributeModel findAttributeModel(Predicate<AttributeModel> p) {
        return constructAttributeModelStream(Comparator.comparing(AttributeModel::getOrder)).filter(p).findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getAttributeGroups() {
        return new ArrayList<>(attributeModels.keySet());
    }

    @Override
    public AttributeModel getAttributeModel(String attributeName) {
        if (!StringUtils.isEmpty(attributeName)) {

            AttributeModel attributeModel = findAttributeModel(am -> am.getName().equals(attributeName));
            if (attributeModel != null) {
                return attributeModel;
            }

            // check for nested property
            String[] names = attributeName.split("\\.");
            if (names.length > 1) {
                // Find Attribute attributeModel
                AttributeModel am = getAttributeModel(names[0]);
                if (am != null) {
                    // Find nested entity attributeModel
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
    public AttributeModel getAttributeModelByActualSortPath(String actualSortPath) {
        return findAttributeModel(m -> m.getActualSortPath().equals(actualSortPath));
    }

    @Override
    public List<AttributeModel> getAttributeModels() {
        List<AttributeModel> list = constructAttributeModelStream(Comparator.comparing(AttributeModel::getOrder))
                .toList();
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
    public List<AttributeModel> getAttributeModelsSortedForGrid() {
        if (!gridOrderSet) {
            return getAttributeModels();
        }
        List<AttributeModel> list = constructAttributeModelStream(Comparator.comparing(AttributeModel::getGridOrder))
                .toList();
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<AttributeModel> getAttributeModelsSortedForSearch() {
        if (!searchOrderSet) {
            return getAttributeModels();
        }
        List<AttributeModel> list = constructAttributeModelStream(Comparator.comparing(AttributeModel::getSearchOrder))
                .toList();
        return Collections.unmodifiableList(list);
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
    public List<AttributeModel> getRequiredForSearchingAttributeModels() {
        List<AttributeModel> result = constructAttributeModelStream(Comparator.comparing(AttributeModel::getOrder))
                .map(m -> {
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
                }).flatMap(List::stream).toList();
        return Collections.unmodifiableList(result);
    }

    @Override
    public boolean isAttributeGroupVisible(String group, boolean readOnly) {
        return attributeModels.get(group).stream()
                .anyMatch(am -> am.isVisibleInForm() && (readOnly || !am.getEditableType().equals(EditableType.READ_ONLY)));
    }

    /**
     * Looks up a text message from a resource bundle
     *
     * @param source   the message cache
     * @param locale   the desired locale
     * @param key      the message key
     * @param fallBack value to fall back to if no match is found
     * @return the translation of the key
     */
    private String lookup(Map<String, Optional<String>> source, Locale locale, String key, String fallBack) {
        // look up in message bundle and add to cache
        if (!source.containsKey(locale.toString())) {
            try {
                ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
                String str = rb.getString(reference + "." + key);
                source.put(locale.toString(), Optional.of(str));
            } catch (MissingResourceException ex) {
                source.put(locale.toString(), Optional.empty());
            }
        }

        // look up or return fallback value
        Optional<String> optional = source.get(locale.toString());
        return optional.orElse(fallBack);
    }

    @Override
    public boolean usesDefaultGroupOnly() {
        return attributeModels.keySet().size() == 1
                && attributeModels.keySet().iterator().next().equals(EntityModel.DEFAULT_GROUP);
    }

    @Override
    public Stream<AttributeModel> getAttributeModels(Predicate<AttributeModel> predicate) {
        return getAttributeModels().stream().filter(predicate::test);
    }

    @Override
    public EntityModelAction findAction(String actionId) {
        return entityModelActions.stream().filter(action -> action.getId().equals(actionId))
                .findFirst().orElse(null);
    }

    @Override
    public boolean hasEmbeddedAttributeModel(String property) {
        return !filterAttributeModels(am -> am.getName().startsWith(property + ".")).isEmpty();
    }

}
