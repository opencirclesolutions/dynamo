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
package com.ocs.dynamo.ui.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.ObjectUtils;

import com.google.common.collect.Sets;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.ui.DefaultFieldFactory;

/**
 * @author bas.rutten
 */
public final class EntityModelUtil {

    private static final Set<String> ALWAYS_IGNORE = Sets.newHashSet("createdOn", "createdBy", "changedBy",
            "changedOn");

    private EntityModelUtil() {
        // private constructor
    }

    /**
     * Compares two entities based on the entity model and reports a list of the differences
     * 
     * @param oldEntity
     *            the old entity
     * @param newEntity
     *            the new entity
     * @param model
     *            the entity model
     * @param entityModelFactory
     * @param messageService
     *            the message service
     * @param ignore
     *            the names of the fields to ignore
     */
    public static List<String> compare(Object oldEntity, Object newEntity, EntityModel<?> model,
            EntityModelFactory entityModelFactory, MessageService messageService, String... ignore) {
        List<String> results = new ArrayList<>();

        Set<String> toIgnore = new HashSet<>();
        if (ignore != null) {
            toIgnore = Sets.newHashSet(ignore);
        }
        toIgnore.addAll(ALWAYS_IGNORE);

        String noValue = messageService.getMessage("ocs.no.value", VaadinUtils.getLocale());

        for (AttributeModel am : model.getAttributeModels()) {
            if ((AttributeType.BASIC.equals(am.getAttributeType())
                    || AttributeType.MASTER.equals(am.getAttributeType())) && !toIgnore.contains(am.getName())) {

                Object oldValue = ClassUtils.getFieldValue(oldEntity, am.getName());
                Object newValue = ClassUtils.getFieldValue(newEntity, am.getName());

                if (!ObjectUtils.equals(oldValue, newValue)) {
                    String oldValueStr = FormatUtils.formatPropertyValue(entityModelFactory, am, oldValue);
                    String newValueStr = FormatUtils.formatPropertyValue(entityModelFactory, am, newValue);
                    results.add(messageService.getMessage("ocs.value.changed", VaadinUtils.getLocale(),
                            am.getDisplayName(), oldValue == null ? noValue : oldValueStr,
                            newValue == null ? noValue : newValueStr));
                }
            } else if (AttributeType.DETAIL.equals(am.getAttributeType())) {
                Collection<?> ocol = (Collection<?>) ClassUtils.getFieldValue(oldEntity, am.getName());
                Collection<?> ncol = (Collection<?>) ClassUtils.getFieldValue(newEntity, am.getName());

                for (Object o : ncol) {
                    if (!ocol.contains(o)) {
                        results.add(messageService.getMessage("ocs.value.added", VaadinUtils.getLocale(),
                                getDescription(o, am.getNestedEntityModel()), am.getDisplayName()));
                    }
                }

                for (Object o : ocol) {
                    if (!ncol.contains(o)) {
                        results.add(messageService.getMessage("ocs.value.removed", VaadinUtils.getLocale(),
                                getDescription(o, am.getNestedEntityModel()), am.getDisplayName()));
                    }
                }

                for (Object o : ocol) {
                    for (Object o2 : ncol) {
                        if (o.equals(o2)) {
                            List<String> nested = compare(o, o2, am.getNestedEntityModel(), entityModelFactory,
                                    messageService, ignore);
                            results.addAll(nested);
                        }
                    }
                }

            }
        }
        return results;
    }

    /**
     * Copies all simple attribute values from one entity to the other
     * 
     * @param source
     *            the source entity
     * @param target
     *            the target entity
     * @param model
     */
    public static <T> void copySimpleAttributes(T source, T target, EntityModel<T> model, String... ignore) {
        Set<String> toIgnore = new HashSet<>();
        if (ignore != null) {
            toIgnore = Sets.newHashSet(ignore);
        }
        toIgnore.addAll(ALWAYS_IGNORE);

        for (AttributeModel am : model.getAttributeModels()) {
            if ((AttributeType.BASIC.equals(am.getAttributeType()) || AttributeType.LOB.equals(am.getAttributeType()))
                    && !toIgnore.contains(am.getName())) {
                if (!DynamoConstants.ID.equals(am.getName())) {
                    Object value = ClassUtils.getFieldValue(source, am.getName());
                    if (ClassUtils.canSetProperty(target, am.getName())) {
                        if (value != null) {
                            ClassUtils.setFieldValue(target, am.getName(), value);
                        } else {
                            ClassUtils.clearFieldValue(target, am.getName(), am.getType());
                        }
                    }

                }
            }
        }
    }

    /**
     * Gets the description for a certain entity
     * 
     * @param o
     * @param model
     * @return
     */
    private static String getDescription(Object o, EntityModel<?> model) {
        if (o instanceof AbstractEntity && model.getDisplayProperty() != null) {
            String property = model.getDisplayProperty();
            return ClassUtils.getFieldValueAsString(o, property);
        }
        return o.toString();
    }

    /**
     * Returns a comma separated String containing the display properties of the specified entities
     * 
     * @param entities
     *            the entities
     * @param model
     *            the model
     * @param maxItems
     *            the maximum number of items before the description is truncated
     * @param messageService
     *            message service
     * @return
     */
    public static <T> String getDisplayPropertyValue(Collection<T> entities, EntityModel<T> model, int maxItems,
            MessageService messageService) {
        String property = model.getDisplayProperty();
        StringBuilder result = new StringBuilder();

        int i = 0;
        for (T t : entities) {
            if (result.length() > 0) {
                result.append(", ");
            }

            if (i < maxItems) {
                result.append(ClassUtils.getFieldValueAsString(t, property));
            } else {
                result.append(messageService.getMessage("ocs.and.others", VaadinUtils.getLocale(),
                        entities.size() - maxItems));
                break;
            }
            i++;
        }
        return result.toString();
    }

    /**
     * Returns the value of the main attribute of an entity
     * 
     * @param entity
     *            the entity
     * @param model
     *            the entity model
     * @return
     */
    public static <T> String getDisplayPropertyValue(T entity, EntityModel<T> model) {
        String property = model.getDisplayProperty();
        return ClassUtils.getFieldValueAsString(entity, property);
    }

    /**
     * Returns the value of the main attribute of an entity
     * 
     * @param entity
     *            the entity
     * @param model
     *            the entity model
     * @return
     */
    public static <T> String getMainAttributeValue(T entity, EntityModel<T> model) {
        AttributeModel main = model.getMainAttributeModel();
        if (main != null) {
            return ClassUtils.getFieldValueAsString(entity, main.getName());
        }
        return null;
    }

    /**
     * Returns the caption for a given field name using the configured strategy:<br/>
     * vaadin - Match camel case for words. Each word starts with a capital.<br/>
     * other - Match camel case for words. Only first word starts with a capital.<br/>
     *
     * @param fieldName
     *            java property name.
     * @return the caption for the given field.
     * @see SystemPropertyUtils
     */
    public static String getCaptionByPropertyId(String fieldName) {
        String displayName = DefaultFieldFactory.createCaptionByPropertyId(fieldName);
        if (SystemPropertyUtils.getDefaultCaptionFormat().equals(DynamoConstants.SP_DEFAULT_CAPTION_FORMAT_VAADIN)) {
            return displayName;
        } else {
            StringBuilder formatted = new StringBuilder();
            StringTokenizer st = new StringTokenizer(displayName, " ");
            if (st.hasMoreTokens()) {
                formatted.append(st.nextToken());
            }
            while (st.hasMoreTokens()) {
                formatted.append(" " + st.nextToken().toLowerCase());
            }
            return formatted.toString();
        }
    }
}
