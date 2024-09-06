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

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.constants.DynamoConstants;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.JoinType;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.*;
import org.dynamoframework.domain.model.annotation.*;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.service.ServiceLocatorFactory;
import org.dynamoframework.utils.ClassUtils;
import org.dynamoframework.utils.DateUtils;
import org.dynamoframework.utils.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Implementation of the entity model factory - creates models that hold
 * metadata about an entity
 *
 * @author bas.rutten
 */
@Slf4j
@NoArgsConstructor
public class EntityModelFactoryImpl implements EntityModelFactory {

    private static final String CLASS = "class";

    private static final String PLURAL_POSTFIX = "s";

    private static final String VERSION = "version";

    private final ConcurrentMap<String, Class<?>> alreadyProcessed = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, EntityModel<?>> cache = new ConcurrentHashMap<>();

    private EntityModelFactory[] delegatedModelFactories;

    @Autowired(required = false)
    private MessageService messageService;

    @Autowired
    private DynamoProperties dynamoProperties;

    private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

    /**
     * Use this constructor when one needs to delegate creation of models to other
     * model factories
     *
     * @param delegatedModelFactories the delegates
     */
    public EntityModelFactoryImpl(EntityModelFactory... delegatedModelFactories) {
        this.delegatedModelFactories = delegatedModelFactories;
    }

    /**
     * Actually adds the attribute models to the entity model, in the correct group
     *
     * @param <T>             type parameter
     * @param entityClass     the entity class
     * @param entityModel     the entity model
     * @param attributeModels the list of attribute models to add
     */
    private <T> void addAttributeModels(Class<T> entityClass, EntityModelImpl<T> entityModel,
                                        List<AttributeModel> attributeModels) {
        Map<String, String> attributeGroupMap = determineAttributeGroupMapping(entityModel, entityClass);
        entityModel.addAttributeGroup(EntityModel.DEFAULT_GROUP);

        attributeModels.sort(Comparator.comparing(AttributeModel::getOrder));
        for (AttributeModel attributeModel : attributeModels) {
            // determine the attribute group name
            String group = attributeGroupMap.get(attributeModel.getName());
            if (StringUtils.isEmpty(group)) {
                group = EntityModel.DEFAULT_GROUP;
            }
            entityModel.addAttributeModel(group, attributeModel);
        }
    }

    /**
     * Adds overrides from annotation to entity model
     *
     * @param <T>         type parameter
     * @param entityClass the entity class
     * @param builder     the entity model builder
     */
    private <T> void addEntityModelAnnotationOverrides(Class<?> entityClass,
                                                       EntityModelImpl.EntityModelImplBuilder<T> builder) {
        Model modelAnnotation = entityClass.getAnnotation(Model.class);
        if (modelAnnotation != null) {
            if (!StringUtils.isEmpty(modelAnnotation.displayName())) {
                builder.defaultDisplayName(modelAnnotation.displayName());
                builder.defaultDescription(modelAnnotation.description());
            }
            if (!StringUtils.isEmpty(modelAnnotation.displayNamePlural())) {
                builder.defaultDisplayNamePlural(modelAnnotation.displayNamePlural());
            }
            if (!StringUtils.isEmpty(modelAnnotation.description())) {
                builder.defaultDescription(modelAnnotation.description());
            }
            if (!StringUtils.isEmpty(modelAnnotation.displayProperty())) {
                builder.displayProperty(modelAnnotation.displayProperty());
            }

            if (modelAnnotation.nestingDepth() > -1) {
                builder.nestingDepth(modelAnnotation.nestingDepth());
            }

            if (!modelAnnotation.listAllowed()) {
                builder.listAllowed(false);
            }
            if (!modelAnnotation.searchAllowed()) {
                builder.searchAllowed(false);
            }
            if (!modelAnnotation.createAllowed()) {
                builder.createAllowed(false);
            }
            if (!modelAnnotation.updateAllowed()) {
                builder.updateAllowed(false);
            }

            if (modelAnnotation.deleteAllowed()) {
                builder.deleteAllowed(true);
            }

            if (!modelAnnotation.exportAllowed()) {
                builder.exportAllowed(false);
            }

            builder.maxSearchResults(modelAnnotation.maxSearchResults());
            builder.autofillInstructions(modelAnnotation.autofillInstructions());
        }

        Roles rolesAnnotation = entityClass.getAnnotation(Roles.class);
        if (rolesAnnotation != null) {
            builder.readRoles(Arrays.asList(rolesAnnotation.readRoles()));
            builder.writeRoles(Arrays.asList(rolesAnnotation.writeRoles()));
            builder.deleteRoles(Arrays.asList(rolesAnnotation.deleteRoles()));
        }
    }

    /**
     * Adds overrides from message bundle to entity model
     *
     * @param <T>       type parameter
     * @param reference the entity model reference
     * @param builder   the entity model builder
     */
    private <T> void addEntityModelMessageBundleOverrides(String reference,
                                                          EntityModelImpl.EntityModelImplBuilder<T> builder) {
        setStringSetting(getEntityMessage(reference, EntityModel.DISPLAY_NAME),
                builder::defaultDisplayName);
        setStringSetting(getEntityMessage(reference, EntityModel.DISPLAY_NAME_PLURAL),
                builder::defaultDisplayNamePlural);
        setStringSetting(getEntityMessage(reference, EntityModel.DESCRIPTION),
                builder::defaultDescription);
        setStringSetting(getEntityMessage(reference, EntityModel.DISPLAY_PROPERTY),
                builder::displayProperty);
        setStringSetting(getEntityMessage(reference, EntityModel.AUTO_FILL_INSTRUCTIONS),
                builder::autofillInstructions);

        setIntSettingIfAbove(getEntityMessage(reference, EntityModel.NESTING_DEPTH), -1, builder::nestingDepth);

        setIntSettingIfBelow(getEntityMessage(reference, EntityModel.MAX_SEARCH_RESULTS),
                Integer.MAX_VALUE, builder::maxSearchResults);

        setBooleanSetting(getEntityMessage(reference, EntityModel.LIST_ALLOWED),
                builder::listAllowed);
        setBooleanSetting(getEntityMessage(reference, EntityModel.SEARCH_ALLOWED),
                builder::searchAllowed);

        setBooleanSetting(getEntityMessage(reference, EntityModel.CREATE_ALLOWED),
                builder::createAllowed);
        setBooleanSetting(getEntityMessage(reference, EntityModel.DELETE_ALLOWED),
                builder::deleteAllowed);
        setBooleanSetting(getEntityMessage(reference, EntityModel.UPDATE_ALLOWED),
                builder::updateAllowed);
        setBooleanSetting(getEntityMessage(reference, EntityModel.EXPORT_ALLOWED),
                builder::exportAllowed);

        setMessageBundleRoleOverrides(reference, EntityModel.READ_ROLES,
                builder::readRoles);
        setMessageBundleRoleOverrides(reference, EntityModel.WRITE_ROLES,
                builder::writeRoles);
        setMessageBundleRoleOverrides(reference, EntityModel.DELETE_ROLES,
                builder::deleteRoles);
    }

    /**
     * Sets roles based on message bundle overrides
     *
     * @param reference reference of the entity model
     * @param name      name of the setting to look up
     * @param consumer  the consumer that is used to set the values
     */
    private void setMessageBundleRoleOverrides(String reference, String name,
                                               Consumer<List<String>> consumer) {
        String roleMessage = getEntityMessage(reference, name);
        if (roleMessage != null) {
            String[] roles = roleMessage.split(",");
            setStringListSetting(Arrays.asList(roles), consumer);
        }
    }

    private void addMissingAttributeNames(List<String> explicitAttributeNames, List<AttributeModel> attributeModels,
                                          List<String> additionalNames) {
        for (AttributeModel am : attributeModels) {
            String name = am.getName();
            if (!skipAttribute(name) && !explicitAttributeNames.contains(name)) {
                additionalNames.add(name);
            }
        }
    }

    /**
     * Indicates whether this factory can provide the model for the specified
     * combination of reference and entity class
     *
     * @param reference   the reference
     * @param entityClass the entity class
     */
    @Override
    public <T> boolean canProvideModel(String reference, Class<T> entityClass) {
        return true;
    }

    /**
     * Collect attribute group data by checking the @AttributeGroup(s) annotations
     *
     * @param <T>         the type parameter
     * @param model       the entity model
     * @param entityClass the entity class
     * @return a mapping from attribute name to the associated group
     */
    private <T> Map<String, String> collectAttributeGroups(EntityModel<T> model, Class<T> entityClass) {

        Map<String, String> result = new HashMap<>();
        AttributeGroups groups = entityClass.getAnnotation(AttributeGroups.class);

        AttributeGroup[] groupArray = new AttributeGroup[0];
        if (groups != null) {
            groupArray = groups.value();
        } else {
            // just a single group
            AttributeGroup group = entityClass.getAnnotation(AttributeGroup.class);
            if (group != null) {
                groupArray = new AttributeGroup[]{group};
            }
        }

        for (AttributeGroup attributeGroup : groupArray) {
            model.addAttributeGroup(attributeGroup.messageKey());
            for (String attributeName : attributeGroup.attributeNames()) {
                result.put(attributeName, attributeGroup.messageKey());
            }
        }
        return result;

    }

    /**
     * Constructs an attribute model for a property
     *
     * @param descriptor  the property descriptor
     * @param entityModel the entity model
     * @param parentClass the type of the direct parent of the attribute (relevant
     *                    in case of embedded attributes)
     * @param nested      whether this is a nested attribute
     * @param prefix      the prefix to apply to the attribute name (in case of
     *                    nested attributes)
     * @return the constructed attribute model
     */
    protected <T> List<AttributeModel> constructAttributeModel(PropertyDescriptor descriptor,
                                                               EntityModel<T> entityModel, Class<?> parentClass, boolean nested,
                                                               String prefix) {
        List<AttributeModel> result = new ArrayList<>();

        // ignore methods annotated with @AssertTrue or @AssertFalse
        String fieldName = descriptor.getName();
        Class<?> pClass = parentClass != null ? parentClass : entityModel.getEntityClass();
        AssertTrue assertTrue = ClassUtils.getAnnotation(pClass, fieldName, AssertTrue.class);
        AssertFalse assertFalse = ClassUtils.getAnnotation(pClass, fieldName, AssertFalse.class);
        if (assertTrue != null || assertFalse != null) {
            return result;
        }

        AttributeModelImpl model = new AttributeModelImpl(dynamoProperties);
        model.setEntityModel(entityModel);

        setAttributeModelDefaults(descriptor, entityModel, parentClass, prefix, fieldName, model);

        setNestedEntityModel(entityModel, model);

        // only basic attributes are shown in the grid by default. nested attributes are hidden
        // unless they are IDs
        boolean isId = model.getName().equals(DynamoConstants.ID);
        boolean displayProperty = model.getName().equals(entityModel.getDisplayProperty());

        model.setVisibleInGrid((isId || displayProperty || !nested) && (AttributeType.BASIC.equals(model.getAttributeType())));
        model.setVisibleInForm(!isId && (AttributeType.BASIC.equals(model.getAttributeType()) ||
                AttributeType.LOB.equals(model.getAttributeType())));

        boolean isIdOrNestedId = model.getName().equals(DynamoConstants.ID) ||
                model.getName().endsWith(DynamoConstants.ID);
        model.setEditableType(isIdOrNestedId ? EditableType.READ_ONLY : EditableType.EDITABLE);

        if (getMessageService() != null) {
            model.setDefaultTrueRepresentation(dynamoProperties.getDefaults().getTrueRepresentation());
            model.setDefaultFalseRepresentation(dynamoProperties.getDefaults().getFalseRepresentation());
        }

        AttributeSelectMode defaultMode = AttributeType.DETAIL.equals(model.getAttributeType())
                ? AttributeSelectMode.MULTI_SELECT
                : AttributeSelectMode.COMBO;

        model.setSelectMode(defaultMode);
        model.setTextFieldMode(AttributeTextFieldMode.TEXTFIELD);
        model.setSearchSelectMode(defaultMode);
        model.setBooleanFieldMode(dynamoProperties.getDefaults().getBooleanFieldMode());
        model.setElementCollectionMode(dynamoProperties.getDefaults().getElementCollectionMode());
        model.setEnumFieldMode(dynamoProperties.getDefaults().getEnumFieldMode());

        Email email = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName,
                Email.class);
        if (email != null) {
            model.setEmail(true);
        }

        setAttributeModelAnnotationOverrides(parentClass, model, descriptor, nested);
        setAttributeModelMessageBundleOverrides(entityModel, model);

        if (!model.isEmbedded()) {
            result.add(model);
        } else {
            processEmbeddedAttributeModel(result, model, entityModel, nested);
        }
        validateAttributeModel(model);

        return result;
    }

    /**
     * Processes an embedded attribute model. This will make sure that the
     * properties of the embedded attribute are added to the parent entity model
     *
     * @param <T>         the type of the entity model
     * @param result      list of attributes so far
     * @param model       the nested attribute model
     * @param entityModel the entity model
     * @param nested      whether the attribute is nested
     */
    private <T> void processEmbeddedAttributeModel(List<AttributeModel> result, AttributeModel model,
                                                   EntityModel<T> entityModel, boolean nested) {
        // an embedded entity does not get its own entity model, but its properties are
        // added
        // to the parent entity
        if (model.getType().equals(entityModel.getEntityClass())) {
            throw new IllegalStateException("Embedding a class in itself is not allowed");
        }
        PropertyDescriptor[] embeddedDescriptors = BeanUtils.getPropertyDescriptors(model.getType());
        for (PropertyDescriptor embeddedDescriptor : embeddedDescriptors) {
            String name = embeddedDescriptor.getName();
            if (!skipAttribute(name)) {
                List<AttributeModel> embeddedModels = constructAttributeModel(embeddedDescriptor, entityModel,
                        model.getType(), nested, model.getName());
                result.addAll(embeddedModels);
            }
        }
    }

    /**
     * Iterates over the properties of an entity and created attribute models for
     * each of them
     *
     * @param <T>         the type parameter
     * @param reference   reference of the entity model
     * @param entityClass the entity class
     * @param entityModel the entity model
     * @return the constructed attribute models
     */
    private <T> List<AttributeModel> constructAttributeModels(String reference, Class<T> entityClass,
                                                              EntityModelImpl<T> entityModel) {
        boolean nested = reference.indexOf('.') >= 0;

        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(entityClass);
        List<AttributeModel> result = new ArrayList<>();
        for (PropertyDescriptor descriptor : descriptors) {
            if (!skipAttribute(descriptor.getName())) {
                List<AttributeModel> attributeModels = constructAttributeModel(descriptor, entityModel,
                        entityModel.getEntityClass(), nested, null);
                result.addAll(attributeModels);
            }
        }
        return result;
    }

    /**
     * Constructs the model for an entity
     *
     * @param reference   unique reference to the entity model
     * @param entityClass the class of the entity
     * @return the constructed model
     */
    protected synchronized <T> EntityModel<T> constructModel(String reference, Class<T> entityClass) {

        // Delegate to other factories first
        EntityModelImpl<T> entityModel = null;
        if (delegatedModelFactories != null) {
            for (EntityModelFactory delegate : delegatedModelFactories) {
                if (delegate.canProvideModel(reference, entityClass)) {
                    entityModel = (EntityModelImpl<T>) delegate.getModel(reference, entityClass);
                    if (entityModel != null) {
                        break;
                    }
                }
            }
        }

        if (entityModel != null) {
            return entityModel;
        }
        return constructModelInner(entityClass, reference);
    }

    /**
     * Constructs the entity model for a class
     *
     * @param entityClass the entity class
     * @param reference   the unique reference for the entity model
     * @return the constructed entity model
     */
    private <T> EntityModelImpl<T> constructModelInner(Class<T> entityClass, String reference) {

        String displayName = org.dynamoframework.utils.StringUtils.propertyIdToHumanFriendly(entityClass.getSimpleName(), dynamoProperties.isCapitalizePropertyNames());

        EntityModelImpl.EntityModelImplBuilder<T> builder = EntityModelImpl.builder();
        builder.reference(reference).nestingDepth(dynamoProperties.getDefaults().getNestingDepth())
                .defaultDescription(displayName).defaultDisplayName(displayName)
                .defaultDisplayNamePlural(displayName + PLURAL_POSTFIX).entityClass(entityClass);

        builder.listAllowed(true);
        builder.createAllowed(true);
        builder.searchAllowed(true);
        builder.updateAllowed(true);
        builder.exportAllowed(true);
        builder.maxSearchResults(Integer.MAX_VALUE);

        addEntityModelAnnotationOverrides(entityClass, builder);
        addEntityModelMessageBundleOverrides(reference, builder);

        EntityModelImpl<T> entityModel = builder.build();
        alreadyProcessed.put(reference, entityClass);

        addEntityModelActions(entityModel);

        List<AttributeModel> attributeModels = constructAttributeModels(reference, entityClass, entityModel);

        // calculate the various attribute orders
        attributeModels.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        determineAttributeOrder(entityClass, reference, attributeModels);
        boolean gridOrder = determineGridAttributeOrder(entityClass, reference, attributeModels);
        boolean searchOrder = determineSearchAttributeOrder(entityClass, reference, attributeModels);
        entityModel.setGridOrderSet(gridOrder);
        entityModel.setSearchOrderSet(searchOrder);

        addAttributeModels(entityClass, entityModel, attributeModels);

        validateGroupTogetherSettings(entityModel);

        String sortOrder = null;
        Model modelAnnotation = entityClass.getAnnotation(Model.class);
        if (modelAnnotation != null && !StringUtils.isEmpty(modelAnnotation.sortOrder())) {
            sortOrder = modelAnnotation.sortOrder();
        }

        String sortOrderMsg = getEntityMessage(reference, EntityModel.SORT_ORDER);
        if (!StringUtils.isEmpty(sortOrderMsg)) {
            sortOrder = sortOrderMsg;
        }
        setSortOrder(entityModel, sortOrder);

        entityModel.setFetchJoins(new ArrayList<>());
        entityModel.setDetailJoins(new ArrayList<>());

        processJoinAnnotations(entityClass, entityModel);
        processMessageBundleJoinOverrides(entityModel);

        cache.put(reference, entityModel);

        return entityModel;
    }


    private <T> void processJoinAnnotations(Class<T> entityClass, EntityModelImpl<T> entityModel) {
        FetchJoins joins = entityClass.getAnnotation(FetchJoins.class);
        if (joins != null) {
            List<FetchJoinInformation> mapped = Arrays.stream(joins.joins())
                    .map(join -> FetchJoinInformation.of(join.attribute(), join.type()))
                    .toList();
            entityModel.setFetchJoins(mapped);
            entityModel.setDetailJoins(mapped);

            if (joins.detailJoins() != null && joins.detailJoins().length > 0) {
                mapped = Arrays.stream(joins.detailJoins())
                        .map(join -> FetchJoinInformation.of(join.attribute(), join.type()))
                        .toList();
                entityModel.setDetailJoins(mapped);
            }
        }
    }

    private <T> void addEntityModelActions(EntityModelImpl<T> entityModel) {

        List<EntityModelAction> modelActions = new ArrayList<>();
        BaseService<?, ?> service = serviceLocator.getServiceForEntity(entityModel.getEntityClass());
        if (service != null) {
            Class<?> clazz = org.springframework.util.ClassUtils.getUserClass(service);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                addEntityModelAction(method, modelActions);
            }
        }
        entityModel.setEntityModelActions(modelActions);
    }

    /**
     * Potentially adds an entity model action for the provided method
     *
     * @param method       the method
     * @param modelActions the model actions
     */
    private void addEntityModelAction(Method method, List<EntityModelAction> modelActions) {
        ModelAction action = ClassUtils.getAnnotationOnMethod(method, ModelAction.class);
        if (action != null) {

            if (method.getParameters().length == 0) {
                throw new OCSRuntimeException("@ModelAction annotation found on method %s without parameters"
                        .formatted(method.getName()));
            }

            if (StringUtils.isEmpty(action.id())) {
                throw new OCSRuntimeException("@ModelAction annotation found on method %s without an action ID"
                        .formatted(method.getName()));
            }

            Class<?> actionClass = method.getParameters()[0].getType();
            EntityModelActionImpl actionImpl = new EntityModelActionImpl();
            actionImpl.setEntityClass(actionClass);
            actionImpl.setReference(action.id());
            actionImpl.setId(action.id());
            actionImpl.setDefaultDisplayName(action.displayName());
            actionImpl.setMethodName(method.getName());
            actionImpl.setType(action.type());
            actionImpl.setIcon(action.icon());
            actionImpl.setRoles(Arrays.asList(action.roles()));

            EntityModel<?> actionModel = constructModel(action.id(), actionClass);
            actionImpl.setEntityModel(actionModel);
            modelActions.add(actionImpl);

            processEntityModelActionMessageBundleOverrides(actionImpl);
        }
    }

    private void processEntityModelActionMessageBundleOverrides(EntityModelActionImpl action) {

        setMessageBundleRoleOverrides(action.getReference(), EntityModel.ACTION_ROLES,
                action::setRoles);
        setStringSetting(getEntityMessage(action.getReference(), EntityModel.ICON),
                action::setIcon);
    }

    /**
     * Process message bundle join overrides for an entity
     *
     * @param entityModel the entity model
     * @param <T>         type parameter, the type of the entity
     */
    private <T> void processMessageBundleJoinOverrides(EntityModelImpl<T> entityModel) {
        List<FetchJoinInformation> overrideJoins = processMessageBundleFetchJoinOverrides(
                entityModel, EntityModel.JOIN);
        if (!overrideJoins.isEmpty()) {
            entityModel.setFetchJoins(overrideJoins);
        }

        List<FetchJoinInformation> overrideDetailJoins = processMessageBundleFetchJoinOverrides(
                entityModel, EntityModel.DETAIL_JOIN);
        if (!overrideDetailJoins.isEmpty()) {
            entityModel.setDetailJoins(overrideDetailJoins);
        }
    }

    /**
     * Processes message bundle fetch joins overrides for a specific type
     *
     * @param model    the entity model
     * @param joinName the name of the join
     * @param <T>      type parameter
     * @return the list of fetch joins (possibly empty)
     */
    protected <T> List<FetchJoinInformation> processMessageBundleFetchJoinOverrides(EntityModel<T> model,
                                                                                    String joinName) {
        List<FetchJoinInformation> result = new ArrayList<>();

        // look for message bundle overwrites
        int i = 1;
        if (messageService != null) {
            String key = joinName + "." + i + "." + EntityModel.ATTRIBUTE;
            String joinAttribute = messageService.getEntityMessage(model.getReference(),
                    key, getLocale());
            while (joinAttribute != null) {
                String joinType = messageService.getEntityMessage(model.getReference(),
                        joinName + "." + i + "." + EntityModel.JOIN_TYPE, getLocale());

                if (joinType != null) {
                    result.add(new FetchJoinInformation(joinAttribute,
                            JoinType.valueOf(joinType)));
                } else {
                    result.add(new FetchJoinInformation(joinAttribute));
                }
                i++;
                joinAttribute = messageService.getEntityMessage(model.getReference(),
                        joinName + "." + i + "." + EntityModel.ATTRIBUTE, getLocale());
            }

        }
        return result;
    }

    /**
     * Determines the attribute group mapping - from attribute name to the group it
     * belongs to
     *
     * @param model       the entity model
     * @param entityClass the entity class
     * @return the mapping from attribute name to group
     */
    protected <T> Map<String, String> determineAttributeGroupMapping(EntityModel<T> model, Class<T> entityClass) {
        Map<String, String> result = collectAttributeGroups(model, entityClass);

        int i = 1;
        if (messageService != null) {
            String groupName = messageService.getEntityMessage(model.getReference(),
                    EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.MESSAGE_KEY, getLocale());

            if (groupName != null) {
                result.clear();
                model.getAttributeGroups().clear();
            }

            while (groupName != null) {
                String attributeNames = messageService.getEntityMessage(model.getReference(),
                        EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.ATTRIBUTE_NAMES, getLocale());

                if (attributeNames != null) {
                    model.addAttributeGroup(groupName);
                    for (String s : attributeNames.split(",")) {
                        result.put(s, groupName);
                    }
                }
                i++;
                groupName = messageService.getEntityMessage(model.getReference(),
                        EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.MESSAGE_KEY, getLocale());
            }
        }
        return result;
    }

    /**
     * Determines the (default) attribute ordering for an entity based on
     * the @AttributeOrder annotation
     *
     * @param <T>             the type of the entity class
     * @param entityClass     the entity class
     * @param reference       the unique reference of the entity model
     * @param attributeModels the list of attribute models to process
     */
    protected <T> void determineAttributeOrder(Class<T> entityClass, String reference,
                                               List<AttributeModel> attributeModels) {
        List<String> explicitAttributeNames = new ArrayList<>();
        AttributeOrder orderAnnotation = entityClass.getAnnotation(AttributeOrder.class);
        if (orderAnnotation != null) {
            explicitAttributeNames = List.of(orderAnnotation.attributeNames());
        }

        // set all orders
        determineAttributeOrderInner(reference, EntityModel.ATTRIBUTE_ORDER, explicitAttributeNames, attributeModels,
                AttributeModelImpl::setOrder);
    }

    /**
     * Determines the order of the attributes - this will first pick up any
     * attributes that are mentioned in one of the @AttributeOrder annotations (in
     * the order in which they occur) and then add any attributes that are not
     * explicitly mentioned
     *
     * @param reference              the unique reference to the entity model
     * @param messageBundleKey       the key under which to look up the attribute
     *                               overrides in the message bundle
     * @param explicitAttributeNames the attribute names explicitly mentioned in the
     *                               annotation
     * @param attributeModels        the full set of attribute model
     * @param consumer               the consumer that is called to actually set the
     *                               proper order on the attribute model
     * @return whether an explicit ordering is defined
     */
    protected boolean determineAttributeOrderInner(String reference, String messageBundleKey,
                                                   List<String> explicitAttributeNames, List<AttributeModel> attributeModels,
                                                   BiConsumer<AttributeModelImpl, Integer> consumer) {
        List<String> additionalNames = new ArrayList<>();

        // overwrite by message bundle (if present)
        String msg = messageService == null ? null
                : messageService.getEntityMessage(reference, messageBundleKey, getLocale());
        if (msg != null) {
            explicitAttributeNames = List.of(msg.replaceAll("\\s+", "").split(","));
        }

        boolean explicit = !explicitAttributeNames.isEmpty();
        List<String> result = new ArrayList<>(explicitAttributeNames);

        addMissingAttributeNames(explicitAttributeNames, attributeModels, additionalNames);
        result.addAll(additionalNames);

        // loop over the attributes and set the orders
        int i = 0;
        for (String attributeName : result) {
            AttributeModel am = attributeModels.stream().filter(m -> m.getName().equals(attributeName)).findFirst()
                    .orElse(null);
            if (am != null) {
                consumer.accept((AttributeModelImpl) am, i);
                i++;
            } else {
                throw new OCSRuntimeException("Attribute %s is not known".formatted(attributeName));
            }
        }

        return explicit;
    }

    /**
     * Determines the attribute type
     *
     * @param parentClass the parent class on which the attribute is defined
     * @param model       the model representation of the attribute
     * @return the attribute type
     */
    protected AttributeType determineAttributeType(Class<?> parentClass, AttributeModelImpl model) {
        AttributeType result = null;
        String name = model.getName();
        int p = name.lastIndexOf('.');
        if (p > 0) {
            name = name.substring(p + 1);
        }

        if (!BeanUtils.isSimpleValueType(model.getType()) && !DateUtils.isJava8DateType(model.getType())) {
            // No relation type set in view model definition, hence derive
            // defaults
            Embedded embedded = ClassUtils.getAnnotation(parentClass, name, Embedded.class);
            Attribute attribute = ClassUtils.getAnnotation(parentClass, name, Attribute.class);

            if (embedded != null) {
                result = AttributeType.EMBEDDED;
            } else if (Collection.class.isAssignableFrom(model.getType())) {
                if (attribute != null && attribute.memberType() != null
                        && !attribute.memberType().equals(Object.class)) {
                    // if a member type is explicitly set, use that type
                    result = AttributeType.DETAIL;
                    model.setMemberType(attribute.memberType());
                } else if (ClassUtils.getAnnotation(parentClass, name, ManyToMany.class) != null
                        || ClassUtils.getAnnotation(parentClass, name, OneToMany.class) != null) {
                    result = AttributeType.DETAIL;
                    model.setMemberType(ClassUtils.getResolvedType(parentClass, name, 0));
                } else if (ClassUtils.getAnnotation(parentClass, name, ElementCollection.class) != null) {
                    result = AttributeType.ELEMENT_COLLECTION;
                    handleElementCollectionSettings(parentClass, model, name);
                } else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
                    // not a collection but a reference to another object
                    result = AttributeType.MASTER;
                }
            } else if (model.getType().isArray()) {
                Lob lob = ClassUtils.getAnnotation(parentClass, name, Lob.class);
                Class<?> componentType = model.getType().getComponentType();

                if (lob != null || componentType.equals(byte.class)) {
                    result = AttributeType.LOB;
                }
            } else {
                // not a collection but a reference to another object
                result = AttributeType.MASTER;
            }
        } else {
            // simple attribute type
            result = AttributeType.BASIC;
        }
        return result;
    }

    /**
     * Determines the "dateType" for an attribute
     *
     * @param modelType the type of the attribute. Can be a java 8 LocalX type
     * @return the data type
     */
    protected AttributeDateType determineDateType(Class<?> modelType) {
        // set the date type
        if (LocalDate.class.equals(modelType)) {
            return AttributeDateType.DATE;
        } else if (LocalDateTime.class.equals(modelType)) {
            return AttributeDateType.LOCAL_DATE_TIME;
        } else if (LocalTime.class.equals(modelType)) {
            return AttributeDateType.TIME;
        } else if (Instant.class.equals(modelType)) {
            return AttributeDateType.INSTANT;
        }
        return null;
    }

    /**
     * Determines the default format to use for a date or time property
     *
     * @param type the type of the property
     * @return the default display format
     */
    protected String determineDefaultDisplayFormat(Class<?> type) {
        String format = null;
        if (LocalDate.class.isAssignableFrom(type)) {
            format = dynamoProperties.getDefaults().getDateFormat();
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            format = dynamoProperties.getDefaults().getDateTimeFormat();
        } else if (LocalTime.class.isAssignableFrom(type)) {
            format = dynamoProperties.getDefaults().getTimeFormat();
        } else if (Instant.class.isAssignableFrom(type)) {
            format = dynamoProperties.getDefaults().getDateTimeFormat();
        }
        return format;
    }

    /**
     * Determines the order in which attributes must appear in a search results grid
     * for an entity
     *
     * @param <T>             type parameter
     * @param entityClass     the class of the entity model
     * @param reference       unique reference of the entity model
     * @param attributeModels the list of attribute models to order
     * @return the attribute order
     */
    protected <T> boolean determineGridAttributeOrder(Class<T> entityClass, String reference,
                                                      List<AttributeModel> attributeModels) {
        List<String> explicitAttributeNames = new ArrayList<>();
        GridAttributeOrder orderAnnotation = entityClass.getAnnotation(GridAttributeOrder.class);
        if (orderAnnotation != null) {
            explicitAttributeNames = List.of(orderAnnotation.attributeNames());
        }
        return determineAttributeOrderInner(reference, EntityModel.GRID_ATTRIBUTE_ORDER, explicitAttributeNames,
                attributeModels, AttributeModelImpl::setGridOrder);
    }

    /**
     * Determines the order in which attributes must appear in a search form for an
     * entity
     *
     * @param <T>             the type of the class
     * @param entityClass     the class of the entity model
     * @param reference       unique reference of the entity model
     * @param attributeModels the list of attribute models to order
     * @return the attribute order
     */
    protected <T> boolean determineSearchAttributeOrder(Class<T> entityClass, String reference,
                                                        List<AttributeModel> attributeModels) {
        List<String> explicitAttributeNames = new ArrayList<>();
        SearchAttributeOrder orderAnnotation = entityClass.getAnnotation(SearchAttributeOrder.class);
        if (orderAnnotation != null) {
            explicitAttributeNames = List.of(orderAnnotation.attributeNames());
        }
        return determineAttributeOrderInner(reference, EntityModel.SEARCH_ATTRIBUTE_ORDER, explicitAttributeNames,
                attributeModels, AttributeModelImpl::setSearchOrder);
    }

    /**
     * Looks up a possible delegated model factory for an entity model
     *
     * @param reference   the reference of the entity model
     * @param entityClass the entity class
     * @return the model factory
     */
    protected <T> EntityModelFactory findModelFactory(String reference, Class<T> entityClass) {
        EntityModelFactory entityModelFactory = this;
        if (delegatedModelFactories != null) {
            for (EntityModelFactory delegate : delegatedModelFactories) {
                if (delegate.canProvideModel(reference, entityClass)) {
                    entityModelFactory = delegate;
                    break;
                }
            }
        }
        return entityModelFactory;
    }

    /**
     * Retrieves a message relating to an attribute from the message bundle
     *
     * @param model          the entity model
     * @param attributeModel the attribute model
     * @param propertyName   the name of the property
     * @return the message
     */
    protected <T> String getAttributeMessage(EntityModel<T> model, AttributeModel attributeModel, String propertyName) {
        if (messageService != null) {
            return messageService.getAttributeMessage(model.getReference(), attributeModel, propertyName, getLocale());
        }
        return null;
    }

    /**
     * Retrieves a message relating to an entity from the message bundle
     *
     * @param reference    the reference of the entity model
     * @param propertyName the name of the property to retrieve the message for
     * @return the message
     */
    protected String getEntityMessage(String reference, String propertyName) {
        if (messageService != null) {
            return messageService.getEntityMessage(reference, propertyName, getLocale());
        }
        return null;
    }

    protected Locale getLocale() {
        return dynamoProperties.getDefaults().getLocale();
    }

    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public synchronized <T> EntityModel<T> getModel(Class<T> entityClass) {
        return getModel(entityClass.getSimpleName(), entityClass);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public synchronized <T> EntityModel<T> getModel(String reference, Class<T> entityClass) {
        EntityModel<T> model = null;
        if (!StringUtils.isEmpty(reference) && entityClass != null) {
            model = (EntityModel<T>) cache.get(reference);
            if (model == null) {
                log.debug("Creating entity model for {}, ({})", reference, entityClass);
                model = constructModel(reference, entityClass);
            }
        }
        return model;
    }

    /**
     * Handles element collection settings for an attribute model
     *
     * @param parentClass the parent class on which the attribute is defined
     * @param model       the attribute model
     * @param name        the name of the attribute model
     */
    private void handleElementCollectionSettings(Class<?> parentClass, AttributeModelImpl model, String name) {
        model.setMemberType(ClassUtils.getResolvedType(parentClass, name, 0));
        model.setCollectionTableName(model.getName());
        model.setCollectionTableFieldName(model.getName());

        // override table name
        CollectionTable table = ClassUtils.getAnnotation(parentClass, name, CollectionTable.class);
        if (table != null && table.name() != null) {
            model.setCollectionTableName(table.name());
        }
        // override field name
        Column col = ClassUtils.getAnnotation(parentClass, name, Column.class);
        if (col != null && col.name() != null) {
            model.setCollectionTableFieldName(col.name());
        }
    }

    /**
     * Check if a certain entity model has already been processed
     *
     * @param type      the type of the entity
     * @param reference the reference to the entity
     * @return true if this is the case, false otherwise
     */
    protected boolean hasEntityModel(Class<?> type, String reference) {
        for (Entry<String, Class<?>> entry : alreadyProcessed.entrySet()) {
            if (reference.equals(entry.getKey()) && entry.getValue().equals(type)) {
                // only check for starting reference in order to prevent
                // recursive looping between
                // two-sided relations
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the model factory already contains a model for the specified
     * reference
     *
     * @param reference the unique reference of the entity model
     * @return true if this is the case, false otherwise
     */
    public boolean hasModel(String reference) {
        return cache.containsKey(reference);
    }

    /**
     * Check whether a message contains a value that marks the attribute as
     * "visible". "true" and SHOW are interpreted as positive values, "false" and
     * HIDE are negative values
     *
     * @param msg the message
     * @return true if this is the case, false otherwise
     */
    private boolean isVisible(String msg) {
        try {
            VisibilityType other = VisibilityType.valueOf(msg);
            return VisibilityType.SHOW.equals(other);
        } catch (IllegalArgumentException ex) {
            // do nothing, threat as false
        }
        return Boolean.parseBoolean(msg);
    }

    /**
     * Sets the custom settings for an attribute model based on the annotation
     *
     * @param attribute the attribute annotation
     * @param model     the attribute model
     */
    private void setAnnotationCustomOverwrites(Attribute attribute, AttributeModel model) {
        if (attribute.custom() != null) {
            for (CustomSetting s : attribute.custom()) {
                if (!StringUtils.isEmpty(s.name())) {
                    String value = s.value();
                    if (CustomType.BOOLEAN.equals(s.type())) {
                        model.setCustomSetting(s.name(), Boolean.valueOf(value));
                    } else if (CustomType.INT.equals(s.type())) {
                        model.setCustomSetting(s.name(), Integer.parseInt(value));
                    } else {
                        model.setCustomSetting(s.name(), value);
                    }
                }
            }
        }
    }

    /**
     * Sets visibility settings for an attribute model based on annotation overrides
     *
     * @param attribute the attribute annotation
     * @param model     the attribute model
     * @param nested    whether we are dealing with a nested attribute
     */
    private void setAnnotationVisibilityOverrides(Attribute attribute, AttributeModelImpl model, boolean nested) {
        // set visibility (hide nested attribute by default; they must be shown using
        // the message bundle)
        if (attribute.visibleInForm() != null && !VisibilityType.INHERIT.equals(attribute.visibleInForm()) && !nested) {
            model.setVisibleInForm(VisibilityType.SHOW.equals(attribute.visibleInForm()));
        }

        // set grid visibility
        if (attribute.visibleInGrid() != null && !VisibilityType.INHERIT.equals(attribute.visibleInGrid()) && !nested) {
            model.setVisibleInGrid(VisibilityType.SHOW.equals(attribute.visibleInGrid()));
        }
    }

    /**
     * Overwrite the default settings for an attribute model with the
     *
     * @param parentClass the entity class in which the attribute is declared
     * @param model       the attribute model
     * @param descriptor  the property descriptor for the attribute
     * @param nested      whether the attribute is nested
     */
    private void setAttributeModelAnnotationOverrides(Class<?> parentClass, AttributeModelImpl model,
                                                      PropertyDescriptor descriptor, boolean nested) {
        Attribute attribute = ClassUtils.getAnnotation(parentClass, descriptor.getName(), Attribute.class);

        if (attribute != null) {
            if (!StringUtils.isEmpty(attribute.displayName())) {
                model.setDefaultDisplayName(attribute.displayName());
                model.setDefaultDescription(attribute.displayName());
                model.setDefaultPrompt(attribute.displayName());
            }

            setStringSetting(attribute.description(), model::setDefaultDescription);
            setStringSetting(attribute.prompt(), model::setDefaultPrompt);
            setStringSetting(attribute.displayFormat(), model::setDefaultDisplayFormat);
            setStringSetting(attribute.trueRepresentation(), model::setDefaultTrueRepresentation);
            setStringSetting(attribute.falseRepresentation(), model::setDefaultFalseRepresentation);
            setStringSetting(attribute.currencyCode(), model::setCurrencyCode);
            setStringSetting(attribute.lookupEntityReference(), model::setLookupEntityReference);
            setStringSetting(attribute.navigationLink(), model::setNavigationLink);
            setStringSetting(attribute.autoFillInstructions(), model::setAutofillInstructions);

            boolean isId = model.getName().equals(DynamoConstants.ID) ||
                    model.getName().endsWith(DynamoConstants.ID);
            model.setEditableType(isId ? EditableType.READ_ONLY : attribute.editable());

            setAnnotationVisibilityOverrides(attribute, model, nested);

            if ((SearchMode.ADVANCED.equals(attribute.searchable()) || SearchMode.ALWAYS.equals(attribute.searchable()))
                    && !nested) {
                model.setSearchMode(attribute.searchable());
            }

            if (attribute.requiredForSearching() && !nested) {
                model.setRequiredForSearching(true);
            }

            setBooleanTrueSetting(attribute.image(), model::setImage);
            setBooleanTrueSetting(attribute.downloadAllowed(), model::setDownloadAllowed);
            setBooleanTrueSetting(attribute.percentage(), model::setPercentage);
            setBooleanTrueSetting(attribute.url(), model::setUrl);
            setBooleanTrueSetting(attribute.showPassword(), model::setShowPassword);
            setBooleanTrueSetting(attribute.quickAddAllowed(), model::setQuickAddAllowed);
            setBooleanTrueSetting(attribute.neededInData(), model::setNeededInData);
            setBooleanTrueSetting(attribute.nestedDetails(), model::setNestedDetails);

            setBooleanFalseSetting(attribute.sortable(), model::setSortable);

            if (attribute.allowedExtensions() != null && attribute.allowedExtensions().length > 0) {
                Set<String> set = Arrays.stream(attribute.allowedExtensions()).map(String::toLowerCase)
                        .collect(Collectors.toSet());
                model.setAllowedExtensions(set);
            }

            if (attribute.cascade() != null) {
                for (Cascade cascade : attribute.cascade()) {
                    model.addCascade(cascade.cascadeTo(), cascade.filterPath(), cascade.mode());
                }
            }

            setAnnotationCustomOverwrites(attribute, model);

            if (attribute.groupTogetherWith() != null) {
                for (String attributeName : attribute.groupTogetherWith()) {
                    model.addGroupTogetherWith(attributeName);
                }
            }

            if (attribute.dateType() != null && !AttributeDateType.INHERIT.equals(attribute.dateType())) {
                model.setDateType(attribute.dateType());
            }

            if (attribute.selectMode() != null && !AttributeSelectMode.INHERIT.equals(attribute.selectMode())) {
                // setting the select mode also sets the search and grid modes
                model.setSelectMode(attribute.selectMode());
                model.setSearchSelectMode(attribute.selectMode());
            }

            // multiple search for master object (default to token)
            if (attribute.multipleSearch()) {
                model.setMultipleSearch(true);
                model.setSearchSelectMode(AttributeSelectMode.MULTI_SELECT);
            }

            if (!AttributeSelectMode.INHERIT.equals(attribute.searchSelectMode())) {
                model.setSearchSelectMode(attribute.searchSelectMode());
                // for a basic attribute, automatically set multiple search when a token field
                // is selected
                if (AttributeType.BASIC.equals(model.getAttributeType())
                        && AttributeSelectMode.MULTI_SELECT.equals(model.getSearchSelectMode())) {
                    model.setMultipleSearch(true);
                }
            }

            setEnumValueUnless(attribute.searchCaseSensitive(), BooleanType.INHERIT,
                    value -> model.setSearchCaseSensitive(value.toBoolean()));
            setEnumValueUnless(attribute.searchPrefixOnly(), BooleanType.INHERIT,
                    value -> model.setSearchPrefixOnly(value.toBoolean()));
            setEnumValueUnless(attribute.enumFieldMode(), AttributeEnumFieldMode.INHERIT,
                    model::setEnumFieldMode);
            setEnumValueUnless(attribute.lookupQueryType(), QueryType.INHERIT,
                    model::setLookupQueryType);

            if (attribute.textFieldMode() != null
                    && !AttributeTextFieldMode.INHERIT.equals(attribute.textFieldMode())) {
                model.setTextFieldMode(attribute.textFieldMode());
            }

            setIntSetting(attribute.precision(), -1, model::setPrecision);
            setIntSetting(attribute.minLength(), -1, model::setMinLength);
            setIntSetting(attribute.maxLength(), -1, model::setMaxLength);
            setIntSetting(attribute.maxLengthInGrid(), -1, model::setMaxLengthInGrid);

            setBigDecimalSetting(attribute.minValue(), Double.MIN_VALUE, true, model::setMinValue);
            setBigDecimalSetting(attribute.maxValue(), Double.MAX_VALUE, false, model::setMaxValue);

            setStringSetting(attribute.replacementSearchPath(), model::setReplacementSearchPath);
            setStringSetting(attribute.replacementSortPath(), model::setReplacementSortPath);

            setBooleanTrueSetting(attribute.searchForExactValue(), model::setSearchForExactValue);
            setStringSetting(attribute.fileNameProperty(), model::setFileNameProperty);

            model.setSearchDateOnly(attribute.searchDateOnly());

            setDefaultValue(model, attribute);
            setDefaultSearchValue(model, attribute);
            setDefaultSearchValueFrom(model, attribute);
            setDefaultSearchValueTo(model, attribute);

            model.setNavigable(attribute.navigable());
            model.setIgnoreInSearchFilter(attribute.ignoreInSearchFilter());

            setEnumValueUnless(attribute.trimSpaces(), TrimType.INHERIT,
                    ts -> model.setTrimSpaces(TrimType.TRIM.equals(ts)));
            setEnumValueUnless(attribute.numberFieldMode(), NumberFieldMode.INHERIT, model::setNumberFieldMode);
            setEnumValueUnless(attribute.booleanFieldMode(), AttributeBooleanFieldMode.INHERIT,
                    model::setBooleanFieldMode);
            setEnumValueUnless(attribute.elementCollectionMode(), ElementCollectionMode.INHERIT,
                    model::setElementCollectionMode);

            setIntSetting(attribute.numberFieldStep(), 0, model::setNumberFieldStep);
        }
    }

    /**
     * Sets the default values for an attribute model
     *
     * @param <T>         the class of the entity model
     * @param descriptor  the property descriptor to base the attribute model on
     * @param entityModel the entity model
     * @param parentClass the parent class
     * @param prefix      the prefix of the attribute path (for nested attributes)
     * @param fieldName   the name of the field
     * @param model       the attribute model
     */
    private <T> void setAttributeModelDefaults(PropertyDescriptor descriptor, EntityModel<T> entityModel,
                                               Class<?> parentClass, String prefix, String fieldName, AttributeModelImpl model) {
        String displayName = org.dynamoframework.utils.StringUtils.propertyIdToHumanFriendly(fieldName, dynamoProperties.isCapitalizePropertyNames());
        model.setDefaultDisplayName(displayName);
        model.setHasSetterMethod(descriptor.getWriteMethod() != null);
        model.setDefaultDescription(displayName);
        model.setDefaultPrompt(displayName);
        model.setSearchMode(SearchMode.NONE);
        model.setName((prefix == null ? "" : (prefix + ".")) + fieldName);
        model.setImage(false);
        model.setEditableType(descriptor.isHidden() ? EditableType.READ_ONLY : EditableType.EDITABLE);
        model.setSortable(true);
        model.setPrecision(dynamoProperties.getDefaults().getDecimalPrecision());
        model.setSearchCaseSensitive(dynamoProperties.getDefaults().isSearchCaseSensitive());
        model.setSearchPrefixOnly(dynamoProperties.getDefaults().isSearchPrefixOnly());
        model.setUrl(false);
        model.setTrimSpaces(dynamoProperties.getDefaults().isTrimSpaces());
        model.setType(descriptor.getPropertyType());
        model.setDateType(determineDateType(model.getType()));
        model.setDefaultDisplayFormat(determineDefaultDisplayFormat(model.getType()));
        model.setNumberFieldMode(dynamoProperties.getDefaults().getNumberFieldMode());
        model.setNumberFieldStep(1);
        model.setLookupQueryType(QueryType.ID_BASED);

        setRequiredAndMinMaxSetting(entityModel, model, parentClass, fieldName);
    }

    /**
     * Overwrite the values of an attribute model with values from a message bundle
     *
     * @param entityModel    the entity model
     * @param attributeModel the attribute model implementation
     */
    private <T> void setAttributeModelMessageBundleOverrides(EntityModel<T> entityModel,
                                                             AttributeModelImpl attributeModel) {

        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DISPLAY_NAME),
                attributeModel::setDefaultDisplayName);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DESCRIPTION),
                attributeModel::setDefaultDescription);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DEFAULT_VALUE),
                attributeModel::setDefaultValue);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DEFAULT_SEARCH_VALUE),
                attributeModel::setDefaultSearchValue);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DEFAULT_SEARCH_VALUE_FROM),
                attributeModel::setDefaultSearchValueFrom);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DEFAULT_SEARCH_VALUE_TO),
                attributeModel::setDefaultSearchValueTo);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DISPLAY_FORMAT),
                attributeModel::setDefaultDisplayFormat);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TRUE_REPRESENTATION),
                attributeModel::setDefaultTrueRepresentation);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.FALSE_REPRESENTATION),
                attributeModel::setDefaultFalseRepresentation);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.CURRENCY_CODE),
                attributeModel::setCurrencyCode);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.LOOKUP_ENTITY_REFERENCE),
                attributeModel::setLookupEntityReference);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NAVIGATION_LINK),
                attributeModel::setNavigationLink);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.AUTO_FILL_INSTRUCTIONS),
                attributeModel::setAutofillInstructions);

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.REQUIRED_FOR_SEARCHING),
                attributeModel::setRequiredForSearching);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SORTABLE),
                attributeModel::setSortable);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.IMAGE),
                attributeModel::setImage);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DOWNLOAD_ALLOWED),
                attributeModel::setDownloadAllowed);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NEEDED_IN_DATA),
                attributeModel::setNeededInData);

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SHOW_PASSWORD),
                attributeModel::setShowPassword);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NESTED_DETAILS),
                attributeModel::setNestedDetails);

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_CASE_SENSITIVE),
                attributeModel::setSearchCaseSensitive);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_PREFIX_ONLY),
                attributeModel::setSearchPrefixOnly);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TRIM_SPACES),
                attributeModel::setTrimSpaces);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.PERCENTAGE),
                attributeModel::setPercentage);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.URL), attributeModel::setUrl);

        // check for read only (convenience only, overwritten by "editable")
        String msg = getAttributeMessage(entityModel, attributeModel, EntityModel.READ_ONLY);
        if (!StringUtils.isEmpty(msg)) {
            boolean editable = Boolean.parseBoolean(msg);
            if (editable) {
                attributeModel.setEditableType(EditableType.READ_ONLY);
            } else {
                attributeModel.setEditableType(EditableType.EDITABLE);
            }
        }
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.EDITABLE), EditableType.class,
                attributeModel::setEditableType);

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.VISIBLE_IN_FORM);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setVisibleInForm(isVisible(msg));
        }
        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.VISIBLE_IN_GRID);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setVisibleInGrid(isVisible(msg));
        }

        // "searchable" also supports true/false for legacy reasons
        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCHABLE);
        if (!StringUtils.isEmpty(msg)) {
            if ("true".equals(msg)) {
                attributeModel.setSearchMode(SearchMode.ALWAYS);
            } else if ("false".equals(msg)) {
                attributeModel.setSearchMode(SearchMode.NONE);
            } else {
                attributeModel.setSearchMode(SearchMode.valueOf(msg));
            }
        }

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.ALLOWED_EXTENSIONS);
        if (msg != null && !StringUtils.isEmpty(msg)) {
            String[] extensions = msg.split(",");
            Set<String> hashSet = Set.of(extensions);
            attributeModel.setAllowedExtensions(hashSet);
        }

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.GROUP_TOGETHER_WITH);
        if (msg != null && !StringUtils.isEmpty(msg)) {
            String[] extensions = msg.split(",");
            for (String s : extensions) {
                attributeModel.addGroupTogetherWith(s);
            }
        }

        setIntSettingIfAbove(getAttributeMessage(entityModel, attributeModel, EntityModel.PRECISION), -1,
                attributeModel::setPrecision);

        // multiple search setting - setting this to true also sets the search select
        // mode to TOKEN
        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.MULTIPLE_SEARCH);
        if (msg != null && !StringUtils.isEmpty(msg)) {
            attributeModel.setMultipleSearch(Boolean.parseBoolean(msg));
            attributeModel.setSearchSelectMode(AttributeSelectMode.MULTI_SELECT);
        }

        // set the select mode (also sets the search select mode and grid select mode)
        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.SELECT_MODE);
        if (!StringUtils.isEmpty(msg)) {
            AttributeSelectMode mode = AttributeSelectMode.valueOf(msg);
            attributeModel.setSelectMode(mode);
            attributeModel.setSearchSelectMode(mode);
        }

        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_SELECT_MODE),
                AttributeSelectMode.class, attributeModel::setSearchSelectMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DATE_TYPE), AttributeDateType.class,
                attributeModel::setDateType);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TEXT_FIELD_MODE),
                AttributeTextFieldMode.class, attributeModel::setTextFieldMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.BOOLEAN_FIELD_MODE),
                AttributeBooleanFieldMode.class, attributeModel::setBooleanFieldMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.ELEMENT_COLLECTION_MODE),
                ElementCollectionMode.class, attributeModel::setElementCollectionMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.LOOKUP_QUERY_TYPE),
                QueryType.class, attributeModel::setLookupQueryType);

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.MIN_VALUE);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setMinValue(new BigDecimal(msg));
        }

        setIntSettingIfAbove(getAttributeMessage(entityModel, attributeModel, EntityModel.MIN_LENGTH), -1,
                attributeModel::setMinLength);
        setIntSettingIfAbove(getAttributeMessage(entityModel, attributeModel, EntityModel.MAX_LENGTH), -1,
                attributeModel::setMaxLength);
        setIntSettingIfAbove(getAttributeMessage(entityModel, attributeModel, EntityModel.MAX_LENGTH_IN_GRID), -1,
                attributeModel::setMaxLengthInGrid);

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.MAX_VALUE);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setMaxValue(new BigDecimal(msg));
        }

        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.REPLACEMENT_SEARCH_PATH),
                attributeModel::setReplacementSearchPath);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.REPLACEMENT_SORT_PATH),
                attributeModel::setReplacementSortPath);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.QUICK_ADD_ALLOWED),
                attributeModel::setQuickAddAllowed);

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_EXACT_VALUE),
                attributeModel::setSearchForExactValue);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NAVIGABLE),
                attributeModel::setNavigable);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_DATE_ONLY),
                attributeModel::setSearchDateOnly);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.IGNORE_IN_SEARCH_FILTER),
                attributeModel::setIgnoreInSearchFilter);

        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NUMBER_FIELD_MODE),
                NumberFieldMode.class, attributeModel::setNumberFieldMode);

        setIntSettingIfAbove(getAttributeMessage(entityModel, attributeModel, EntityModel.NUMBER_FIELD_STEP), -1,
                attributeModel::setNumberFieldStep);

        setMessageBundleCascadeOverrides(entityModel, attributeModel);
        setMessageBundleCustomOverrides(entityModel, attributeModel);
    }

    /**
     * Sets a value on an attribute model if the provided boolean value is false
     *
     * @param value    the boolean value
     * @param receiver the code that is executed to set the value
     */
    private void setBooleanFalseSetting(Boolean value, Consumer<Boolean> receiver) {
        if (!value) {
            receiver.accept(false);
        }
    }

    /**
     * Sets a boolean setting if it is non-null
     *
     * @param value    the value
     * @param receiver the receiver function
     */
    private void setBooleanSetting(String value, Consumer<Boolean> receiver) {
        if (value != null) {
            receiver.accept(Boolean.valueOf(value));
        }
    }

    /**
     * Sets a value on the attribute model if the provided boolean value is true
     *
     * @param value    the boolean value
     * @param receiver the code that is executed to set the value
     */
    private void setBooleanTrueSetting(Boolean value, Consumer<Boolean> receiver) {
        if (value) {
            receiver.accept(true);
        }
    }

    /**
     * Sets the default value on the attribute model (translates a String to the
     * appropriate type)
     *
     * @param model        the attribute model
     * @param defaultValue the default value to set
     * @param search       whether we are dealing with search mode
     * @param
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setDefaultValue(AttributeModelImpl model, String defaultValue, boolean search, Consumer<Object> consumer) {
        if (model.getType().isEnum()) {
            Class<? extends Enum> enumType = model.getType().asSubclass(Enum.class);
            consumer.accept(Enum.valueOf(enumType, defaultValue));
        } else if (DateUtils.isJava8DateType(model.getType())) {

            Object object;
            if (search && model.isSearchDateOnly()) {
                object = DateUtils.createJava8Date(LocalDate.class, defaultValue,
                        dynamoProperties.getDefaults().getDateFormat());
            } else {
                object = DateUtils.createJava8Date(model.getType(), defaultValue,
                        DateUtils.getDefaultDisplayFormat(model.getType()));
            }
            consumer.accept(object);

        } else if (Boolean.class.equals(model.getType()) || boolean.class.equals(model.getType())) {
            consumer.accept(Boolean.valueOf(defaultValue));
        } else {
            consumer.accept(ClassUtils.instantiateClass(model.getType(), defaultValue));
        }
    }

    /**
     * Sets the default value of an attribute based on the annotation
     *
     * @param model     the attribute model
     * @param attribute the annotation
     */
    private void setDefaultValue(AttributeModelImpl model, Attribute attribute) {
        if (!StringUtils.isEmpty(attribute.defaultValue())) {
            if (!AttributeType.BASIC.equals(model.getAttributeType())) {
                throw new OCSRuntimeException("%s: setting a default value is only allowed for BASIC attributes"
                        .formatted(model.getName()));
            }

            String defaultValue = attribute.defaultValue();
            setDefaultValue(model, defaultValue, false, model::setDefaultValue);
        }
    }

    /**
     * Sets the default search value of an attribute based on the annotation
     *
     * @param model     the attribute model
     * @param attribute the annotation
     */
    private void setDefaultSearchValue(AttributeModelImpl model, Attribute attribute) {
        if (!StringUtils.isEmpty(attribute.defaultSearchValue())) {
            if (!AttributeType.BASIC.equals(model.getAttributeType())) {
                throw new OCSRuntimeException("%s: setting a default search value is only allowed for BASIC attributes"
                        .formatted(model.getName()));
            }

            String defaultValue = attribute.defaultSearchValue();
            setDefaultValue(model, defaultValue, true, model::setDefaultSearchValue);
        }
    }

    /**
     * Sets the default search value of an attribute based on the annotation
     *
     * @param model     the attribute model
     * @param attribute the annotation
     */
    private void setDefaultSearchValueFrom(AttributeModelImpl model, Attribute attribute) {
        if (!StringUtils.isEmpty(attribute.defaultSearchValueFrom())) {
            if (!AttributeType.BASIC.equals(model.getAttributeType())) {
                throw new OCSRuntimeException("%s: setting a default search from value is only allowed for BASIC attributes"
                        .formatted(model.getName()));
            }

            String defaultValue = attribute.defaultSearchValueFrom();
            setDefaultValue(model, defaultValue, true, model::setDefaultSearchValueFrom);
        }
    }

    /**
     * Sets the default search value of an attribute based on the annotation
     *
     * @param model     the attribute model
     * @param attribute the annotation
     */
    private void setDefaultSearchValueTo(AttributeModelImpl model, Attribute attribute) {
        if (!StringUtils.isEmpty(attribute.defaultSearchValueTo())) {
            if (!AttributeType.BASIC.equals(model.getAttributeType())) {
                throw new OCSRuntimeException("%s: setting a default search to value is only allowed for BASIC attributes"
                        .formatted(model.getName()));
            }

            String defaultValue = attribute.defaultSearchValueTo();
            setDefaultValue(model, defaultValue, true, model::setDefaultSearchValueTo);
        }
    }

    /**
     * Sets an enum field based on a string value from a message bundle
     *
     * @param <E>       the type of the class
     * @param value     the string value
     * @param enumClass the type of the enum
     * @param receiver  receiver function
     */
    @SneakyThrows
    @SuppressWarnings({"rawtypes", "unchecked"})
    private <E extends Enum> void setEnumSetting(String value, Class<E> enumClass, Consumer<E> receiver) {
        if (!StringUtils.isEmpty(value)) {
            E enumValue = (E) Enum.valueOf(enumClass, value);
            receiver.accept(enumValue);
        }
    }

    /**
     * Sets an enum value on the attribute model, unless the value is the specified
     * excluded value
     *
     * @param <E>      enum type parameter
     * @param value    the value
     * @param exclude  the value to exclude
     * @param consumer consumer to call when the value is not equal to the excluded
     *                 value
     */
    private <E extends Enum<?>> void setEnumValueUnless(E value, E exclude, Consumer<E> consumer) {
        if (!exclude.equals(value)) {
            consumer.accept(value);
        }
    }

    /**
     * Sets an integer value on the attribute model if the value is above the
     * specified limit
     *
     * @param value    the integer value
     * @param limit    the lower limit
     * @param receiver the receiver function
     */
    private void setIntSetting(Integer value, int limit, Consumer<Integer> receiver) {
        if (value != null && value > limit) {
            receiver.accept(value);
        }
    }

    /**
     * Sets an integer value on the attribute model if the value is above the
     * specified limit
     *
     * @param value    the integer value
     * @param limit    the lower limit
     * @param receiver the receiver function
     */
    private void setIntSettingIfAbove(String value, int limit, Consumer<Integer> receiver) {
        if (value == null) {
            return;
        }

        int intValue = Integer.parseInt(value);
        if (intValue > limit) {
            receiver.accept(intValue);
        }
    }

    /**
     * Sets an integer value on the attribute model if the value is above the
     * specified limit
     *
     * @param value    the integer value
     * @param limit    the lower limit
     * @param receiver the receiver function
     */
    private void setIntSettingIfBelow(String value, int limit, Consumer<Integer> receiver) {
        if (value == null) {
            return;
        }

        int intValue = Integer.parseInt(value);
        if (intValue < limit) {
            receiver.accept(intValue);
        }
    }

    /**
     * Sets a long value on the attribute model if it is either above or below the
     * specified limit
     *
     * @param value    the value
     * @param limit    the limit
     * @param above    whether to check if the value is above the limit
     * @param receiver the function to call if the condition is met
     */
    private void setBigDecimalSetting(Double value, Double limit, boolean above, Consumer<BigDecimal> receiver) {
        if (value != null && (above && value.compareTo(limit) > 0 || value.compareTo(limit) < 0)) {
            receiver.accept(BigDecimal.valueOf(value));
        }
    }

    /**
     * Reads cascade settings for an attribute from the message bundle
     *
     * @param entityModel the entity model
     * @param model       the attribute model
     */
    private void setMessageBundleCascadeOverrides(EntityModel<?> entityModel, AttributeModel model) {
        String msg = getAttributeMessage(entityModel, model, EntityModel.CASCADE_OFF);
        if (msg != null) {
            // completely cancel all cascades for this attribute
            model.removeCascades();
        } else {
            int cascadeIndex = 1;
            msg = getAttributeMessage(entityModel, model, EntityModel.CASCADE + "." + cascadeIndex);
            while (msg != null) {
                String filter = getAttributeMessage(entityModel, model,
                        EntityModel.CASCADE_FILTER_PATH + "." + cascadeIndex);
                // optional mode (defaults to BOTH when omitted)
                String mode = getAttributeMessage(entityModel, model, EntityModel.CASCADE_MODE + "." + cascadeIndex);

                if (filter != null && mode != null) {
                    model.addCascade(msg, filter, CascadeMode.valueOf(mode));
                } else {
                    throw new OCSRuntimeException("Incomplete cascade definition for " + model.getPath());
                }

                cascadeIndex++;
                msg = getAttributeMessage(entityModel, model, EntityModel.CASCADE + "." + cascadeIndex);
            }
        }
    }

    /**
     * Adds custom setting overrides. These take the form of "custom.1",
     * "customValue.1" and "customType.1"
     *
     * @param entityModel the entity model that is being processed
     * @param model       the attribute model
     */
    private void setMessageBundleCustomOverrides(EntityModel<?> entityModel, AttributeModel model) {

        int customIndex = 1;
        String name = getAttributeMessage(entityModel, model, EntityModel.CUSTOM + "." + customIndex);
        while (name != null) {
            String value = getAttributeMessage(entityModel, model, EntityModel.CUSTOM_VALUE + "." + customIndex);
            String type = getAttributeMessage(entityModel, model, EntityModel.CUSTOM_TYPE + "." + customIndex);
            CustomType t = CustomType.STRING;
            if (type != null) {
                t = CustomType.valueOf(type);
            }

            if (value != null) {
                if (CustomType.BOOLEAN.equals(t)) {
                    model.setCustomSetting(name, Boolean.valueOf(value));
                } else if (CustomType.INT.equals(t)) {
                    model.setCustomSetting(name, Integer.parseInt(value));
                } else {
                    model.setCustomSetting(name, value);
                }
                customIndex++;
                name = getAttributeMessage(entityModel, model, EntityModel.CUSTOM + "." + customIndex);
            }
        }
    }

    /**
     * Calculates the entity model for a nested property, recursively up until a
     * certain depth
     *
     * @param parentEntityModel the parent entity model
     * @param attributeModel    the attribute model
     */
    protected void setNestedEntityModel(EntityModel<?> parentEntityModel, AttributeModelImpl attributeModel) {
        EntityModel<?> em = attributeModel.getEntityModel();
        if (StringUtils.countMatches(em.getReference(), ".") < parentEntityModel.getNestingDepth()) {
            Class<?> type = null;

            // only needed for master and detail attributes
            if (AttributeType.MASTER.equals(attributeModel.getAttributeType()) || AttributeType.EMBEDDED.equals(
                    attributeModel.getAttributeType())) {
                type = attributeModel.getType();
            } else if (AttributeType.DETAIL.equals(attributeModel.getAttributeType())) {
                type = attributeModel.getMemberType();
            }

            if (type != null) {
                String ref;
                if (StringUtils.isEmpty(em.getReference())) {
                    ref = em.getEntityClass() + "." + attributeModel.getName();
                } else {
                    ref = em.getReference() + "." + attributeModel.getName();
                }

//                if (type.equals(em.getEntityClass()) || !hasEntityModel(type, ref)) {
                EntityModel<?> nestedModel = findModelFactory(ref, type).getModel(ref, type);
                attributeModel.setNestedEntityModel(nestedModel);
//                } else {
//                    //EntityModel<?> nestedModel = findModelFactory(ref, type).getModel(ref, type);
//                    //attributeModel.setNestedEntityModel(nestedModel);
//                }
            }
        }
    }

    /**
     * Sets the "required" setting on an attribute based on JPA validation
     * annotations
     *
     * @param <T>         the type parameter
     * @param entityModel the entity model that the attribute model is part of
     * @param model       the attribute model
     * @param parentClass the parent class
     * @param fieldName   the name of the field
     */
    private <T> void setRequiredAndMinMaxSetting(EntityModel<T> entityModel, AttributeModelImpl model,
                                                 Class<?> parentClass, String fieldName) {
        // determine if the attribute is required based on the @NotNull
        // annotation
        NotNull notNull = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, NotNull.class);
        model.setRequired(notNull != null);

        // also set to required when it is a collection with a size greater than 0
        model.setAttributeType(determineAttributeType(parentClass, model));
        Size size = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Size.class);
        if (size != null && size.min() > 0 && AttributeType.DETAIL.equals(model.getAttributeType())) {
            model.setRequired(true);
        }

        // minimum and maximum size for collections
        if (size != null && AttributeType.DETAIL.equals(model.getAttributeType())) {
            model.setMinCollectionSize(size.min());
            model.setMaxCollectionSize(size.max());
        }

        // minimum and maximum length based on the @Size annotation
        if (model.getType() == String.class && size != null) {
            model.setMinLength(size.min());
            model.setMaxLength(size.max());
        }

        if (model.getAttributeType() == AttributeType.ELEMENT_COLLECTION && size != null) {
            model.setMinCollectionSize(size.min());
            model.setMaxCollectionSize(size.max());
        }

        Min min = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Min.class);
        if ((model.isNumerical() || model.getAttributeType() == AttributeType.ELEMENT_COLLECTION) && min != null) {
            model.setMinValue(BigDecimal.valueOf(min.value()));
        }

        Max max = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Max.class);
        if ((model.isNumerical() || model.getAttributeType() == AttributeType.ELEMENT_COLLECTION) && max != null) {
            model.setMaxValue(BigDecimal.valueOf(max.value()));
        }
    }

    /**
     * Sets the sort order on an entity model
     *
     * @param model        the entity model
     * @param sortOrderMsg the sort order from the message bundle
     */
    protected <T> void setSortOrder(EntityModel<T> model, String sortOrderMsg) {
        if (!StringUtils.isEmpty(sortOrderMsg)) {
            String[] tokens = sortOrderMsg.split(",");
            for (String token : tokens) {
                String[] sd = token.trim().split(" ");
                if (sd.length > 0 && !StringUtils.isEmpty(sd[0]) && model.getAttributeModel(sd[0]) != null) {
                    model.getSortOrder().put(model.getAttributeModel(sd[0]),
                            sd.length == 1 || (!"DESC".equalsIgnoreCase(sd[1]) && !"DSC".equalsIgnoreCase(sd[1])));
                }
            }
        }
    }

    /**
     * Sets a String field value if the provided argument is not empty
     *
     * @param value    the string value
     * @param receiver the code that is executed to set the value
     */
    private void setStringSetting(String value, Consumer<String> receiver) {
        if (!StringUtils.isEmpty(value)) {
            receiver.accept(value);
        }
    }

    /**
     * Sets a String field value if the provided argument is not empty
     *
     * @param value    the string value
     * @param receiver the code that is executed to set the value
     */
    private void setStringListSetting(List<String> value, Consumer<List<String>> receiver) {
        if (value != null && !value.isEmpty()) {
            receiver.accept(value);
        }
    }

    /**
     * Indicates whether to skip an attribute since it does not constitute an actual
     * property but rather a generic or technical field that all entities have
     *
     * @param name the name of the attribute
     * @return true if this is case, false otherwise
     */
    private boolean skipAttribute(String name) {
        return CLASS.equals(name) || VERSION.equals(name);
    }

    /**
     * Validates an attribute model, by checking for illegal combinations of
     * settings
     *
     * @param model the attribute model to check
     */
    private void validateAttributeModel(AttributeModel model) {
        // multiple select fields not allowed for some attribute types
        if (AttributeSelectMode.MULTI_SELECT.equals(model.getSelectMode())
                && (!AttributeType.DETAIL.equals(model.getAttributeType()))) {
            throw new OCSRuntimeException("Multi-select field not allowed for attribute %s".formatted(model.getName()));
        }

        if (AttributeSelectMode.MULTI_SELECT.equals(model.getSearchSelectMode())
                && !(AttributeType.DETAIL.equals(model.getAttributeType()) || isMultiSelectMaster(model))) {
            throw new OCSRuntimeException("Multi-select field not allowed for attribute %s".formatted(model.getName()));
        }

        if (AttributeSelectMode.AUTO_COMPLETE.equals(model.getSelectMode())
                && AttributeType.DETAIL.equals(model.getAttributeType())) {
            throw new OCSRuntimeException("Auto-complete field not allowed for attribute %s".formatted(model.getName()));
        }

        if (AttributeSelectMode.COMBO.equals(model.getSelectMode())
                && AttributeType.DETAIL.equals(model.getAttributeType())) {
            throw new OCSRuntimeException("Combo box not allowed for attribute %s".formatted(model.getName()));
        }

        if (AttributeSelectMode.COMBO.equals(model.getSearchSelectMode())
                && model.isMultipleSearch()) {
            throw new OCSRuntimeException("Combo box not allowed for multiple search for attribute %s".formatted(model.getName()));
        }

        if (AttributeSelectMode.AUTO_COMPLETE.equals(model.getSearchSelectMode())
                && model.isMultipleSearch()) {
            throw new OCSRuntimeException("Auto-complete field not allowed for multiple search for attribute %s".formatted(model.getName()));
        }

        // navigating only allowed in case of a many-to-one relation
        if (!AttributeType.MASTER.equals(model.getAttributeType()) && model.isNavigable()) {
            throw new OCSRuntimeException("Navigation is not possible for attribute %s".formatted(model.getName()));
        }

        // searching on a LOB is pointless
        if (AttributeType.LOB.equals(model.getAttributeType()) && model.isSearchable()) {
            throw new OCSRuntimeException("Searching on a LOB is not allowed for attribute %s".formatted(model.getName()));
        }

        // "search date only" is only supported for date/time fields
        if (model.isSearchDateOnly() && !LocalDateTime.class.equals(model.getType())
                && !Instant.class.equals(model.getType())) {
            throw new OCSRuntimeException("SearchDateOnly is not allowed for attribute %s".formatted(model.getName()));
        }

        // field cannot be percentage and currency at the same time
        if (model.isPercentage() && !StringUtils.isEmpty(model.getCurrencyCode())) {
            throw new OCSRuntimeException("%s is not allowed to be both a percentage and a currency".formatted(model.getName()));
        }

        // element collection only supported for strings or integral numbers
        if (model.getAttributeType() == AttributeType.ELEMENT_COLLECTION && (!model.getMemberType().equals(String.class)
                && !NumberUtils.isLong(model.getMemberType()) && !NumberUtils.isInteger(model.getMemberType()))) {
            throw new OCSRuntimeException("Element collection for %s is not allowed (not a String or an integral number)"
                    .formatted(model.getName()));
        }
    }

    /**
     * Validates the "group together with" settings for all attributes in the
     * specified entity model
     *
     * @param <T>         type parameter, type of the class managed by the entity model
     * @param entityModel the entity model
     */
    private <T> void validateGroupTogetherSettings(EntityModel<T> entityModel) {
        Set<String> alreadyUsed = new HashSet<>();
        // check if there aren't any illegal "group together" settings
        for (AttributeModel am : entityModel.getAttributeModels()) {
            alreadyUsed.add(am.getName());
            if (!am.getGroupTogetherWith().isEmpty()) {
                for (String together : am.getGroupTogetherWith()) {
                    if (alreadyUsed.contains(together)) {
                        AttributeModel other = entityModel.getAttributeModel(together);
                        if (together != null) {
                            ((AttributeModelImpl) other).setAlreadyGrouped(true);
                            throw new OCSRuntimeException("Incorrect groupTogetherWith found: %s refers to %s".formatted(am.getName(), together));
                        }
                    }
                }
            }
        }
    }

    private boolean isMultiSelectMaster(AttributeModel attributeModel) {
        return attributeModel.getAttributeType() == AttributeType.MASTER
                && attributeModel.isMultipleSearch();
    }

}
