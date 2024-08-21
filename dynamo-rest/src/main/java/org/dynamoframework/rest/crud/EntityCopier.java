package org.dynamoframework.rest.crud;

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

import lombok.RequiredArgsConstructor;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.AttributeType;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.utils.ClassUtils;
import org.dynamoframework.utils.EntityModelUtils;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class used to create copies of an entity or list of entities
 * <p>
 * This is mainly used in order to "hide" attributes that are not relevant in
 * a given situation (because "visibleInGrid" or "visibleInForm" is false
 * for a particular attribute)
 * <p>
 * This is also used to get around infinite loops in the JSON structure,
 * because this any references from detail objects back to their parents
 */
@Component
@RequiredArgsConstructor
public class EntityCopier {

    /**
     * Creates a copy of the provided list of entities,
     * with all irrelevant fields omitted
     *
     * @param input the list of input entities
     * @param model the model
     * @return the copies
     */
    public <ID2, U extends AbstractEntity<ID2>> List<U> copyResults(List<U> input, EntityModel<U> model) {
        return input.stream().map(source ->
                createCopy(source, model,
                        am -> am.isVisibleInGrid() || am.isNeededInData(), false)).toList();
    }

    /**
     * Creates a copy of the provided entity
     * with all irrelevant fields omitted
     *
     * @param input the entity copy
     * @param model the entity model to use
     * @param <ID2> the type of the ID of the entity
     * @param <U>   the type of the entity
     * @return the copied entity
     */
    public <ID2, U extends AbstractEntity<ID2>> U copyResult(U input, EntityModel<U> model) {
        return createCopy(input, model, am -> am.isVisibleInForm() || am.isNeededInData(), true);
    }

    /**
     * Creates a copy of the provided entity based on the provided entity model
     *
     * @param source the entity to copy
     * @param model  the entity model
     * @param <ID2>  type parameter, type of the ID
     * @param <U>    type parameter, type of the entity
     * @return the created copy
     */
    @SuppressWarnings("unchecked")
    private <ID2, U extends AbstractEntity<ID2>> U createCopy(U source, EntityModel<U> model,
                                                              Predicate<AttributeModel> predicate,
                                                              boolean copyLobs) {

        U target = (U) ClassUtils.instantiateClass(source.getClass());
        List<String> ignoreFields = model.getAttributeModels(am -> !predicate.test(am))
                .map(AttributeModel::getName).toList();

        EntityModelUtils.copySimpleAttributes(source, target, model,
                copyLobs, ignoreFields.toArray(new String[0]));
        target.setId(source.getId());

        // clear any possible default values
        ignoreFields.forEach(field -> {
            if (ClassUtils.hasMethod(target, "set" + field)) {
                ClassUtils.setFieldValue(target, field, null);
            }
        });


        // recursively copy nested attributes
        List<AttributeModel> attributeModels = model.getAttributeModels(
                am -> isComplexAttribute(am) && predicate.test(am)).toList();
        for (AttributeModel am : attributeModels) {
            if (am.getAttributeType() == AttributeType.MASTER) {
                copyNestedEntity(source, target, am, predicate, copyLobs);
            } else if (am.getAttributeType() == AttributeType.DETAIL) {
                copyCollection(source, target, am, predicate, copyLobs);
            } else {
                // element collection
                Object value = ClassUtils.getFieldValue(source, am.getName());
                ClassUtils.setFieldValue(target, am.getName(), value);
            }
        }
        return target;
    }

    @SuppressWarnings("unchecked")
    private <T, ID2, U extends AbstractEntity<ID2>> void copyNestedEntity(T source, T target, AttributeModel am,
                                                                          Predicate<AttributeModel> predicate,
                                                                          boolean copyLobs) {

        U sourceU = (U) ClassUtils.getFieldValue(source, am.getName());
        if (sourceU == null) {
            return;
        }

        sourceU = (U) unproxy(sourceU);

        U targetU = createCopy(sourceU, (EntityModel<U>) am.getNestedEntityModel(), predicate, copyLobs);
        ClassUtils.setFieldValue(target, am.getName(), targetU);
    }

    public static Object unproxy(Object proxy) {
        LazyInitializer lazyInitializer = HibernateProxy.extractLazyInitializer(proxy);
        return lazyInitializer != null ? lazyInitializer.getImplementation() : proxy;
    }



    @SuppressWarnings("unchecked")
    private <T, ID2, U extends AbstractEntity<ID2>> void copyCollection(T source, T target, AttributeModel am,
                                                                        Predicate<AttributeModel> predicate,
                                                                        boolean copyLobs) {
        Collection<U> nestedEntities = (Collection<U>) ClassUtils.getFieldValue(source, am.getName());
        if (nestedEntities == null) {
            return;
        }

        Stream<U> nestedCopyStream = nestedEntities.stream().map(ne ->
                createCopy(ne, (EntityModel<U>) am.getNestedEntityModel(), predicate, copyLobs));
        Collection<U> nestedCopy;
        if (nestedEntities instanceof List) {
            nestedCopy = nestedCopyStream.toList();
        } else {
            nestedCopy = nestedCopyStream.collect(Collectors.toSet());
        }

        // clear any back-references to prevent endless looping
        ClassUtils.setFieldValue(target, am.getName(), nestedCopy);
        nestedCopy.forEach(nc -> am.getNestedEntityModel().getAttributeModels().forEach(nam -> {
            if (nam.getType().isAssignableFrom(source.getClass()) || nam.getType().equals(source.getClass())) {
                ClassUtils.setFieldValue(nc, nam.getName(), null);
            }
        }));
    }

    private boolean isComplexAttribute(AttributeModel am) {
        return (am.getAttributeType() == AttributeType.MASTER || am.getAttributeType() ==
                AttributeType.DETAIL || am.getAttributeType() == AttributeType.ELEMENT_COLLECTION);
    }

}
