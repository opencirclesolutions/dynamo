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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.EntityModelUtils;
import com.ocs.dynamo.utils.FormatUtils;
import lombok.experimental.UtilityClass;

import java.time.ZoneId;
import java.util.*;

/**
 * Utility methods for comparing two entities
 *
 * @author BasRutten
 */
@UtilityClass
public final class CompareUtils {

    /**
     * Compares two entities based on the entity model and reports a list of the
     * differences
     *
     * @param oldEntity          the old entity
     * @param newEntity          the new entity
     * @param model              the entity model
     * @param entityModelFactory the entity model factory
     * @param messageService     the message service
     * @param ignore             the names of the fields to ignore
     */
    public static List<String> compare(Object oldEntity, Object newEntity, EntityModel<?> model,
                                       EntityModelFactory entityModelFactory, MessageService messageService, String... ignore) {
        List<String> results = new ArrayList<>();

        Set<String> toIgnore = new HashSet<>();
        if (ignore != null) {
            toIgnore = new HashSet<>(Arrays.asList(ignore));
        }
        toIgnore.addAll(EntityModelUtils.ALWAYS_IGNORE);

        Locale locale = VaadinUtils.getLocale();
        ZoneId zoneId = VaadinUtils.getTimeZoneId();
        String noValue = messageService.getMessage("ocs.no.value", locale);

        for (AttributeModel am : model.getAttributeModels()) {
            if ((AttributeType.BASIC.equals(am.getAttributeType())
                    || AttributeType.MASTER.equals(am.getAttributeType())) && !toIgnore.contains(am.getName())) {

                Object oldValue = ClassUtils.getFieldValue(oldEntity, am.getName());
                Object newValue = ClassUtils.getFieldValue(newEntity, am.getName());

                if (!Objects.equals(oldValue, newValue)) {
                    String oldValueStr = FormatUtils.formatPropertyValue(entityModelFactory, messageService, am,
                            oldValue, ", ", locale, zoneId);
                    String newValueStr = FormatUtils.formatPropertyValue(entityModelFactory, messageService, am,
                            newValue, ", ", locale, zoneId);
                    results.add(messageService.getMessage("ocs.value.changed", VaadinUtils.getLocale(),
                            am.getDisplayName(VaadinUtils.getLocale()), oldValue == null ? noValue : oldValueStr,
                            newValue == null ? noValue : newValueStr));
                }
            } else if (AttributeType.DETAIL.equals(am.getAttributeType())) {
                compareCollections(oldEntity, newEntity, entityModelFactory, messageService, results, am, ignore);
            }
        }
        return results;
    }

    /**
     * Compares two collections and reports a list of differences
     *
     * @param oldEntity          the old entity
     * @param newEntity          the new entity
     * @param entityModelFactory the entity model factory
     * @param messageService     the message service
     * @param results            the list of results
     * @param am                 the attribute model of the collection property
     * @param ignore             names of the properties to ignore
     */
    private static void compareCollections(Object oldEntity, Object newEntity, EntityModelFactory entityModelFactory,
                                           MessageService messageService, List<String> results, AttributeModel am, String... ignore) {
        Collection<?> oldCollection = (Collection<?>) ClassUtils.getFieldValue(oldEntity, am.getName());
        Collection<?> newCollection = (Collection<?>) ClassUtils.getFieldValue(newEntity, am.getName());
        if (oldCollection == null) {
            oldCollection = Collections.emptyList();
        }
        if (newCollection == null) {
            newCollection = Collections.emptyList();
        }

        // check for added values
        for (Object o : newCollection) {
            if (!oldCollection.contains(o)) {
                results.add(messageService.getMessage("ocs.value.added", VaadinUtils.getLocale(),
                        getDescription(o, am.getNestedEntityModel()), am.getDisplayName(VaadinUtils.getLocale())));
            }
        }
        // check for removed values
        for (Object o : oldCollection) {
            if (!newCollection.contains(o)) {
                results.add(messageService.getMessage("ocs.value.removed", VaadinUtils.getLocale(),
                        getDescription(o, am.getNestedEntityModel()), am.getDisplayName(VaadinUtils.getLocale())));
            }
        }

        // check for changes to entities that are present in old and new situation
        for (Object o : oldCollection) {
            for (Object o2 : newCollection) {
                if (o.equals(o2)) {
                    List<String> nested = compare(o, o2, am.getNestedEntityModel(), entityModelFactory, messageService,
                            ignore);
                    results.addAll(nested);
                }
            }
        }
    }

    /**
     * Gets the description for a certain entity. This uses the display property if
     * one is set and the toString value otherwise
     *
     * @param entity the entity
     * @param model  the entity model
     * @return the description
     */
    private static String getDescription(Object entity, EntityModel<?> model) {
        if (entity instanceof AbstractEntity && model.getDisplayProperty() != null) {
            String property = model.getDisplayProperty();
            return ClassUtils.getFieldValueAsString(entity, property);
        }
        return entity.toString();
    }
}
