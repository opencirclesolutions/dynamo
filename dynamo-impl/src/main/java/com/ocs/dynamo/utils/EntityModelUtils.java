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
package com.ocs.dynamo.utils;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.MessageService;

import java.util.*;

/**
 * @author bas.rutten
 */
public final class EntityModelUtils {

    /**
     * Copies all simple attribute values from one entity to the other
     *
     * @param source the source entity
     * @param target the target entity
     * @param model  the entity model
     * @param ignore the name of the properties to ignore
     */
    public static <T> void copySimpleAttributes(T source, T target, EntityModel<T> model,
                                                boolean copyLobs, String... ignore) {
        Set<String> toIgnore = new HashSet<>();
        if (ignore != null) {
            toIgnore.addAll(Arrays.asList(ignore));
        }
        for (AttributeModel am : model.getAttributeModels()) {

            boolean copy = AttributeType.BASIC.equals(am.getAttributeType());
            if (AttributeType.LOB.equals(am.getAttributeType())) {
                copy = copyLobs;
            }

            if (copy && !toIgnore.contains(am.getName())) {
                if (!DynamoConstants.ID.equals(am.getName())) {
                    Object value = ClassUtils.getFieldValue(source, am.getName());
                    if (ClassUtils.canSetProperty(target, am.getName())) {

                        if (value instanceof String str && am.isTrimSpaces()) {
                            value = str.trim();
                        }

                        if (value != null) {
                            ClassUtils.setFieldValue(target, am.getName(), value);
                        } else {
                            ClassUtils.clearAttributeValue(target, am.getName(), am.getType());
                        }
                    }

                }
            }
        }
    }

    /**
     * Returns a comma separated String containing the display properties of the
     * specified entities
     *
     * @param entities       the entities
     * @param model          the model
     * @param maxItems       the maximum number of items before the description is
     *                       truncated
     * @param messageService message service
     * @return the resulting display value
     */
    public static <T> String getDisplayPropertyValue(Collection<T> entities, EntityModel<T> model, int maxItems,
                                                     MessageService messageService, Locale locale) {
        String property = model.getDisplayProperty();
        StringBuilder result = new StringBuilder();

        int i = 0;
        for (T entity : entities) {
            if (!result.isEmpty()) {
                result.append(", ");
            }

            if (i < maxItems) {
                result.append(ClassUtils.getFieldValueAsString(entity, property));
            } else {
                result.append(messageService.getMessage("ocs.and.others", locale, entities.size() - maxItems));
                break;
            }
            i++;
        }
        return result.toString();
    }

    /**
     * Returns the value of the display property of an entity
     *
     * @param entity the entity
     * @param model  the entity model
     * @return the resulting display value
     */
    public static <T> String getDisplayPropertyValue(T entity, EntityModel<?> model) {
        if (entity == null) {
            return null;
        }
        String property = model.getDisplayProperty();
        if (property == null) {
            return entity.toString();
        }
        return ClassUtils.getFieldValueAsString(entity, property);
    }

    private EntityModelUtils() {
        // private constructor
    }

}
