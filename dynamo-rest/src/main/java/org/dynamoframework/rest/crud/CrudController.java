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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.constants.DynamoConstants;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.*;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.exception.OcsNotFoundException;
import org.dynamoframework.rest.BaseController;
import org.dynamoframework.rest.crud.search.SearchModel;
import org.dynamoframework.rest.crud.search.SearchResultsModel;
import org.dynamoframework.service.*;
import org.dynamoframework.utils.ClassUtils;
import org.dynamoframework.utils.EntityModelUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/crud")
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "CRUD", description = "Dynamo CRUD controller")
public class CrudController<ID, T extends AbstractEntity<ID>> extends BaseController {

    private final ObjectMapper objectMapper;

    private final SearchService searchService;

    private final EntityCopier entityCopier;

    private final UserDetailsService userDetailsService;

    private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

    @PostMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @SneakyThrows
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new entity")
    public T post(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName, @RequestBody
    String request, @RequestParam(required = false) String reference) {

        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = findEntityModel(reference, clazz);
        validateMethodAllowed(model, EntityModel::isCreateAllowed);
        userDetailsService.validateWriteAllowed(model);

        T source = objectMapper.readerFor(clazz).readValue(request);
        T copy = ClassUtils.instantiateClass(clazz);

        mergeSimpleValues(source, copy, model, false);

        // clear any ID that is present (this is a new source)
        copy.setId(null);
        mergeComplexValues(source, copy, model, false, false);

        BaseService<ID, T> service = findService(clazz);
        return entityCopier.copyResult(service.save(copy), model);
    }

    /**
     * Executes an action defined in the entity model
     *
     * @param entityName the name of the entity
     * @param actionId   the ID of the action to execute
     * @param request    the request body
     * @param reference  optional entity model reference
     * @param id         optional ID of the entity to update (in case of update actions)
     * @return the result of the action (the updated or created entity)
     */
    @PostMapping(value = "/{entityName}/action/{actionId}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Executes an action defined in the entity model")
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <U extends AbstractEntity<ID>> T executeAction(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                                                          @PathVariable("actionId") @Parameter(description = "The id of the action to execute") String actionId,
                                                          @RequestBody @Parameter(description = "The request body") String request,
                                                          @RequestParam(required = false) @Parameter(description = "Entity model reference") String reference,
                                                          @RequestParam(required = false) @Parameter(description = "ID of the entity to update (in case of update actions)")String id) {

        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = findEntityModel(reference, clazz);

        EntityModelAction action = model.findAction(actionId);
        if (action == null) {
            throw new OCSValidationException("Action with ID %s cannot be found"
                    .formatted(actionId));
        }

        if (!userDetailsService.isUserInRole(action.getRoles().toArray(new String[0]))) {
            throw new OCSValidationException("User is not allowed to carry out action %s"
                    .formatted(actionId));
        }

        EntityModel<U> actionModel = (EntityModel<U>) action.getEntityModel();

        U source = objectMapper.readerFor(action.getEntityClass()).readValue(request);
        ID convertedId = convertId(clazz, id);

        U copy = ClassUtils.instantiateClass(actionModel.getEntityClass());

        mergeSimpleValues(source, copy, actionModel, false);

        copy.setId(convertedId);
        mergeComplexValues(source, copy, actionModel, false, false);

        BaseService<ID, T> service = findService(clazz);
        Method method = service.getClass().getDeclaredMethod(action.getMethodName(),
                action.getEntityClass());

        try {
            Object result = method.invoke(service, copy);
            return entityCopier.copyResult((T) result, model);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    /**
     * Updates an existing entity
     *
     * @param entityName the name of the entity
     * @param id         the ID of the entity
     * @param request    the message body
     * @param reference  optional reference to specify the entity model to use
     * @return the entity after the update
     */
    @PutMapping(value = "/{entityName}/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @SneakyThrows
    @Operation(summary = "Updates an existing entity")
    public T put(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                 @PathVariable("id") @Parameter(description = "The ID of the entity") String id,
                 @RequestBody @Parameter(description = "The message body") String request,
                 @RequestParam(required = false)@Parameter(description = "Reference to specify the entity model to use")  String reference) {

        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = findEntityModel(reference, clazz);
        validateMethodAllowed(model, EntityModel::isUpdateAllowed);
        userDetailsService.validateWriteAllowed(model);

        T existingEntity = findService(clazz).fetchById(convertId(clazz, id));
        if (existingEntity == null) {
            throw new OcsNotFoundException("Entity of type %s with ID %s cannot be found"
                    .formatted(entityName, id));
        }

        T source = objectMapper.readerFor(clazz).readValue(request);

        mergeSimpleValues(source, existingEntity, model, true);
        mergeComplexValues(source, existingEntity, model, true, false);

        return entityCopier.copyResult(findService(clazz).save(existingEntity), model);
    }

    /**
     * Retrieve the details of a single entity
     *
     * @param entityName the name of the entity
     * @param id         the ID of the entity
     * @param reference  the entity model reference
     * @return the entity
     */
    @GetMapping(value = "/{entityName}/{id}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.ALL_VALUE)
    @Operation(summary = "Retrieve the details of a single entity")
    public T get(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                 @PathVariable("id") @Parameter(description = "The ID of the entity") String id,
                 @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference) {
        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = findEntityModel(reference, clazz);
        userDetailsService.validateReadAllowed(model);

        T entity = findSearchService(clazz).fetchById(convertId(clazz, id));
        if (entity == null) {
            throw new OcsNotFoundException("Entity of type %s with ID %s cannot be found"
                    .formatted(entityName, id));
        }
        return entityCopier.copyResult(entity, model);
    }

    /**
     * Instantiates a new entity
     *
     * @param entityName the name of the entity
     * @param reference  the entity model reference
     * @return the entity
     */
    @GetMapping(value = "/{entityName}/init", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.ALL_VALUE)
    @Operation(summary = "Instantiates a new entity")
    public T init(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                  @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference) {
        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = findEntityModel(reference, clazz);
        userDetailsService.validateWriteAllowed(model);

        T entity = findService(clazz).initialize();
        return entityCopier.copyResult(entity, model);
    }

    /**
     * Retrieves a simple list of entities (without any sorting or filtering)
     *
     * @param entityName the name of the entity
     * @param reference  an optional reference to the exact entity model
     * @return a list of all the entities
     */
    @GetMapping(value = "/{entityName}", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.ALL_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieves a simple list of entities (without any sorting or filtering)")
    public List<T> list(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                        @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference) {
        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = findEntityModel(reference, clazz);
        validateMethodAllowed(model, EntityModel::isListAllowed);
        userDetailsService.validateReadAllowed(model);

        SortOrders orders = constructSortOrders(model);

        List<T> results = findSearchService(clazz).fetch(null, orders);

        return entityCopier.copyResults(results, model);
    }

    /**
     * Executes a search request
     *
     * @param entityName  the name of the entity
     * @param searchModel the search request
     * @param reference   an optional reference to the exact entity model
     * @return a list of all the entities
     */
    @PostMapping(value = "/{entityName}/search", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Executes a search request")
    public SearchResultsModel<T> search(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                                        @RequestBody @Valid @Parameter(description = "The search request") SearchModel searchModel,
                                        @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference) {

        Class<T> clazz = findClass(entityName);

        EntityModel<T> model = findEntityModel(reference, clazz);
        validateMethodAllowed(model, EntityModel::isSearchAllowed);
        userDetailsService.validateReadAllowed(model);

        return searchService.search(searchModel, model);
    }

    @DeleteMapping("/{entityName}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an entity")
    public void delete(@PathVariable("entityName") @Parameter(description = "The name of the entity") String entityName,
                       @PathVariable("id") @Parameter(description = "The ID of the entity") String id) {
        Class<T> clazz = findClass(entityName);
        EntityModel<T> model = getEntityModelFactory().getModel(clazz);
        validateMethodAllowed(model, EntityModel::isDeleteAllowed);
        userDetailsService.validateDeleteAllowed(model);

        T entity = findService(clazz).fetchById(convertId(clazz, id));
        if (entity == null) {
            throw new OcsNotFoundException("Entity of type %s with ID %s cannot be found"
                    .formatted(entityName, id));
        }
        findService(clazz).delete(entity);
    }

    private void validateMethodAllowed(EntityModel<?> model, Predicate<EntityModel<?>> predicate) {
        if (!predicate.test(model)) {
            throw new OCSValidationException("This method is not allowed");
        }
    }

    /**
     * Merges simple attribute values from the source (received from request) to the target
     *
     * @param source   the source entity
     * @param target   the target entity
     * @param model    the entity model
     * @param updating whether we are dealing with an update of an existing entity
     */
    private <U> void mergeSimpleValues(U source, U target, EntityModel<U> model, boolean updating) {
        List<String> readOnlyFields = new ArrayList<>(model.getAttributeModels(am -> !canUpdateAttribute(am, updating)).map(AttributeModel::getName).toList());
        // exclude nested IDs
        readOnlyFields.addAll(model.getAttributeModels(am -> am.getPath().endsWith(".id"))
                .map(AttributeModel::getName).toList());
        EntityModelUtils.copySimpleAttributes(source, target, model, false, readOnlyFields.toArray(new String[0]));
    }

    /**
     * Merges complex attribute values from the source (received from request) to the target
     *
     * @param source   the source entity
     * @param target   the target entity
     * @param model    the entity model
     * @param updating whether we are dealing with an update of an existing entity
     */
    private <U> void mergeComplexValues(U source, U target, EntityModel<U> model, boolean updating,
                                        boolean nested) {
        List<AttributeModel> attributeModels = model.getAttributeModels(
                this::isComplexAttribute).toList();

        for (AttributeModel am : attributeModels) {
            if (!am.isVisibleInForm() && am.getEditableType() != EditableType.HIDDEN) {
                log.info("Skipping update of complex attribute that is not visible in form: {}",
                        am.getName());
                continue;
            }

            if (!nested && !canUpdateAttribute(am, updating)) {
                log.info("Skipping update of complex attribute that is read-only: {}",
                        am.getName());
                continue;
            }

            if (am.getAttributeType() == AttributeType.MASTER) {
                mergeNestedEntity(source, target, am);
            } else if (am.getAttributeType() == AttributeType.DETAIL) {
                if (!am.isNestedDetails()) {
                    mergeNestedEntities(source, target, am);
                } else {
                    copyNestedEntities(source, target, am, updating);
                }
            } else {
                // element collection
                Object value = ClassUtils.getFieldValue(source, am.getName());
                ClassUtils.setFieldValue(target, am.getName(), value);
            }
        }
    }

    /**
     * Merges a nested entity from the source entity to the target entity
     *
     * @param source the source entity
     * @param target the target entity
     * @param am     the attribute model
     * @param <ID2>  type parameter, type of the ID
     * @param <U>    type parameter, type of the entity
     */
    @SuppressWarnings("unchecked")
    private <T, ID2, U extends AbstractEntity<ID2>> void mergeNestedEntity(T source,
                                                                           T target, AttributeModel am) {
        U nestedEntity = (U) ClassUtils.getFieldValue(source, am.getName());
        if (nestedEntity == null) {
            ClassUtils.setFieldValue(target, am.getName(), null);
            return;
        }

        EntityModel<?> nestedEntityModel = am.getNestedEntityModel();
        BaseService<ID2, U> nestedService = (BaseService<ID2, U>) serviceLocator.getServiceForEntity(nestedEntityModel.getEntityClass());
        if (nestedService != null) {
            U found = nestedService.fetchById(nestedEntity.getId());
            if (found != null) {
                ClassUtils.setFieldValue(target, am.getName(), found);
            } else {
                throw new OcsNotFoundException("Entity with name %s and ID %s cannot be found"
                        .formatted(am.getType().toString(), nestedEntity.getId()));
            }
        }
    }

    /**
     * Merges a collection of nested entities from the source to the target entity
     *
     * @param source the source entity
     * @param target the target entity
     * @param am     the attribute model
     * @param <ID2>  type parameter, type of the ID
     * @param <U>    type parameter, type of the entity
     */
    @SuppressWarnings("unchecked")
    private <T, ID2, U extends AbstractEntity<ID2>> void mergeNestedEntities(T source, T target, AttributeModel am) {
        Collection<U> nestedEntities = (Collection<U>) ClassUtils.getFieldValue(source, am.getName());
        if (nestedEntities == null) {
            return;
        }

        Set<U> copySet = new HashSet<>();
        nestedEntities.forEach(nestedEntity -> {
            BaseService<ID2, U> nestedService = (BaseService<ID2, U>) serviceLocator.getServiceForEntity(am.getNestedEntityModel().getEntityClass());
            U found = nestedService.fetchById(nestedEntity.getId());
            if (found != null) {
                copySet.add(found);
            } else {
                throw new OcsNotFoundException("Entity with name %s and ID %s cannot be found"
                        .formatted(am.getNestedEntityModel().getEntityClass(), nestedEntity.getId().toString()));
            }
        });

        ClassUtils.setFieldValue(target, am.getName(), copySet);
    }

    @SuppressWarnings("unchecked")
    private ID convertId(Class<T> clazz, String id) {
        if (StringUtils.isEmpty(id)) {
            return null;
        }

        EntityModel<T> model = getEntityModelFactory().getModel(clazz);
        AttributeModel idModel = model.getAttributeModel(DynamoConstants.ID);

        if (idModel.getType() == Integer.class) {
            return (ID) Integer.valueOf(id);
        } else if (idModel.getType() == Long.class) {
            return (ID) Long.valueOf(id);
        } else if (idModel.getType() == String.class) {
            return (ID) id;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BaseService<ID, T> findService(Class<T> clazz) {
        return (BaseService<ID, T>) serviceLocator.getServiceForEntity(clazz);
    }

    @SuppressWarnings("unchecked")
    private BaseSearchService<ID, T> findSearchService(Class<T> clazz) {
        return (BaseSearchService<ID, T>) serviceLocator.getSearchServiceForEntity(clazz);
    }

    private boolean isComplexAttribute(AttributeModel am) {
        return (am.getAttributeType() == AttributeType.MASTER || am.getAttributeType() ==
                AttributeType.DETAIL || am.getAttributeType() == AttributeType.ELEMENT_COLLECTION);
    }

    private boolean canUpdateAttribute(AttributeModel am, boolean updating) {
        // hidden attributes live in the background
        if (am.getEditableType() == EditableType.HIDDEN) {
            return true;
        }

        return am.isVisibleInForm() && (am.getEditableType() == EditableType.EDITABLE ||
                (am.getEditableType() == EditableType.CREATE_ONLY && !updating));
    }

    private SortOrders constructSortOrders(EntityModel<T> model) {
        SortOrders orders = new SortOrders();
        model.getSortOrder().forEach((k, v) -> orders.addSortOrder(new SortOrder(k.getName(), v ? SortOrder.Direction.ASC : SortOrder.Direction.DESC)));
        return orders;
    }

    /**
     * Copies nested entity entities from source to target entity
     *
     * @param source the source
     * @param target the target
     * @param am     the attribute model
     */
    @SuppressWarnings("unchecked")
    private <T, ID2, U extends AbstractEntity<ID2>> void copyNestedEntities(T source, T target, AttributeModel am,
                                                                            boolean updating) {
        Collection<U> sourceEntities = (Collection<U>) ClassUtils.getFieldValue(source, am.getName());
        Collection<U> targetEntities = (Collection<U>) ClassUtils.getFieldValue(target, am.getName());

        targetEntities = mergeNestedEntities(am, sourceEntities, targetEntities, updating);

        // set references to parent object
//        targetEntities.forEach(nc -> am.getNestedEntityModel().getAttributeModels().forEach(nam -> {
//            if (nam.getType().equals(source.getClass())) {
//                ClassUtils.setFieldValue(nc, nam.getName(), target);
//            }
//        }));

        ClassUtils.setFieldValue(target, am.getName(), targetEntities);
    }

    /**
     * Merge a collection of nested entities. When the incoming
     * entity has an ID, look for a corresponding entity in the current data.
     * Otherwise, create a new entity
     *
     * @param am             the attribute model
     * @param sourceEntities the source entities (from the message)
     * @param targetEntities the target entities (from the DB0
     * @param updating       whether an update is taking place
     * @param <ID2>          type of the primary key
     * @param <U>            type of the
     * @return the result of the merge operation
     */
    @SuppressWarnings("unchecked")
    private <ID2, U extends AbstractEntity<ID2>> Collection<U> mergeNestedEntities(
            AttributeModel am, Collection<U> sourceEntities, Collection<U> targetEntities,
            boolean updating) {

        if (sourceEntities == null) {
            sourceEntities = Collections.emptyList();
        }

        Stream<U> stream = sourceEntities.stream().map(se -> {

            U target;

            if (se.getId() == null) {
                target = (U) ClassUtils.instantiateClass(se.getClass());
            } else {
                target = targetEntities.stream().filter(te -> te.getId().equals(se.getId()))
                        .findFirst().orElse((U) ClassUtils.instantiateClass(se.getClass()));
            }

            mergeSimpleValues(se, target, (EntityModel<U>) am.getNestedEntityModel(), true);
            mergeComplexValues(se, target, (EntityModel<U>) am.getNestedEntityModel(), updating, true);
            return target;
        });
        return am.getType().isAssignableFrom(Set.class) ? stream.collect(Collectors.toSet()) :
                stream.toList();
    }
}
