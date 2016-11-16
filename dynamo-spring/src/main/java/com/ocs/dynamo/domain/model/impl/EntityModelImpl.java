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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * An implementation of an entity model - holds metadata about an entity
 * 
 * @author bas.rutten
 * @param <T>
 *            the class of the entity
 */
public class EntityModelImpl<T> implements EntityModel<T> {

    // use a linked hash map to guarantee the ordering
    private final Map<String, List<AttributeModel>> attributeModels = new LinkedHashMap<>();

    private final String description;

    private final String displayName;

    private final String displayNamePlural;

    private final String displayProperty;

    private final Class<T> entityClass;

    private AttributeModel idAttributeModel;

    private final String reference;

    private Map<AttributeModel, Boolean> sortOrder = new LinkedHashMap<>();

    /**
     * Constructor
     * 
     * @param entityClass
     * @param displayName
     * @param displayNamePlural
     * @param description
     */
    public EntityModelImpl(Class<T> entityClass, String reference, String displayName,
            String displayNamePlural, String description, String displayProperty) {
        this.entityClass = entityClass;
        this.displayName = displayName;
        this.displayNamePlural = displayNamePlural;
        this.description = description;
        this.displayProperty = displayProperty;
        this.reference = reference;
    }

    public void addAttributeGroup(String attributeGroup) {
        if (!attributeModels.containsKey(attributeGroup)) {
            attributeModels.put(attributeGroup, new ArrayList<AttributeModel>());
        }
    }

    public void addAttributeModel(String attributeGroup, AttributeModel model) {
        attributeModels.get(attributeGroup).add(model);
    }

    @Override
    public void addAttributeModel(String attributeGroup, AttributeModel model,
            AttributeModel existingModel) {
        List<AttributeModel> group = attributeModels.get(attributeGroup);
        if (group.contains(existingModel)) {
            group.add(group.indexOf(existingModel), model);
        } else {
            group.add(model);
        }
    }

    @Override
    public List<String> getAttributeGroups() {
        return new ArrayList<>(attributeModels.keySet());
    }

    @Override
    public AttributeModel getAttributeModel(String attributeName) {
        if (!StringUtils.isEmpty(attributeName)) {

            // check for direct property (note: in case of an embedded property,
            // the attribute
            // can have a name that contain a "." and still be a direct
            // attribute )
            for (List<AttributeModel> list : attributeModels.values()) {
                for (AttributeModel model : list) {
                    if (model.getName().equals(attributeName)) {
                        return model;
                    }
                }
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
                        return nem
                                .getAttributeModel(attributeName.substring(names[0].length() + 1));
                    }
                }
            }
        }
        return null;
    }

    @Override
    public List<AttributeModel> getAttributeModels() {
        List<AttributeModel> result = new ArrayList<>();
        for (List<AttributeModel> list : attributeModels.values()) {
            result.addAll(list);
        }
        Collections.sort(result, new Comparator<AttributeModel>() {

            @Override
            public int compare(AttributeModel o1, AttributeModel o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }

        });

        return Collections.unmodifiableList(result);
    }

    @Override
    public List<AttributeModel> getRequiredAttributeModels() {
        List<AttributeModel> requiredModels = new ArrayList<AttributeModel>();
        for (AttributeModel model : getAttributeModels()) {
            if (model.isRequired()) {
                requiredModels.add(model);
            }
        }
        return requiredModels;
    }

    @Override
    public List<AttributeModel> getRequiredForSearchingAttributeModels() {
        List<AttributeModel> requiredModels = new ArrayList<AttributeModel>();
        for (AttributeModel model : getAttributeModels()) {
            if (model.isSearchable() && model.isRequiredForSearching()) {
                requiredModels.add(model);
            }
        }
        return requiredModels;
    }

    @Override
    public List<AttributeModel> getAttributeModelsForGroup(String group) {
        return Collections.unmodifiableList(attributeModels.get(group));
    }

    @Override
    public List<AttributeModel> getAttributeModelsForType(AttributeType attributeType, Class<?> type) {
        List<AttributeModel> result = new ArrayList<>();
        if (attributeType != null || type != null) {
            for (List<AttributeModel> list : attributeModels.values()) {
                for (AttributeModel model : list) {
                    Class<?> rt = ClassUtils.getResolvedType(getEntityClass(), model.getName(), 0);
                    if ((attributeType == null || attributeType.equals(model.getAttributeType()))
                            && (type == null || type.isAssignableFrom(model.getType()) || (rt != null && type
                                    .isAssignableFrom(rt)))) {
                        result.add(model);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDisplayNamePlural() {
        return displayNamePlural;
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
        for (List<AttributeModel> list : attributeModels.values()) {
            for (AttributeModel model : list) {
                if (model.isMainAttribute()) {
                    return model;
                }
            }
        }
        return null;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Map<AttributeModel, Boolean> getSortOrder() {
        return sortOrder;
    }

    @Override
    public boolean isAttributeGroupVisible(String group, boolean readOnly) {
        List<AttributeModel> attributes = attributeModels.get(group);
        for (AttributeModel model : attributes) {
            if (AttributeType.BASIC.equals(model.getAttributeType())
                    || AttributeType.LOB.equals(model.getAttributeType())
                    || model.isComplexEditable()) {
                // attribute must be visible and not read-only (when in edit
                // mode)
                if (model.isVisible() && (readOnly || !model.isReadOnly())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the attribute model of the id
     * 
     * @param idAttributeModel
     *            the idAttributeModel to set
     */
    void setIdAttributeModel(AttributeModel idAttributeModel) {
        this.idAttributeModel = idAttributeModel;
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
