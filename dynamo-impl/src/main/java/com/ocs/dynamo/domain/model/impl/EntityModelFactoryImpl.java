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

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.JoinType;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.*;
import com.ocs.dynamo.domain.model.annotation.*;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.DateUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.PropertyDescriptor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
                builder.filterProperty(modelAnnotation.displayProperty());
            }
            if (modelAnnotation.nestingDepth() > -1) {
                builder.nestingDepth(modelAnnotation.nestingDepth());
            }

            if (!StringUtils.isEmpty(modelAnnotation.filterProperty())) {
                builder.filterProperty(modelAnnotation.filterProperty());
            }
            if (!StringUtils.isEmpty(modelAnnotation.autofillInstructions())) {
                builder.autofillInstructions(modelAnnotation.autofillInstructions());
            }
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
        setStringSetting(getEntityMessage(reference, EntityModel.FILTER_PROPERTY),
                builder::filterProperty);
        setStringSetting(getEntityMessage(reference, EntityModel.AUTO_FILL_INSTRUCTIONS),
                builder::autofillInstructions);
        setIntSetting(getEntityMessage(reference, EntityModel.NESTING_DEPTH), -1, builder::nestingDepth);
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
     * Check that the "week" setting is only allowed for java.time.LocalDate
     *
     * @param model the attribute model
     */
    private void checkWeekSettingAllowed(AttributeModel model) {
        if (!LocalDate.class.equals(model.getType())) {
            throw new OCSRuntimeException("'Week' setting only allowed for attributes of type LocalDate");
        }
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

        for (AttributeGroup g : groupArray) {
            model.addAttributeGroup(g.messageKey());
            for (String s : g.attributeNames()) {
                result.put(s, g.messageKey());
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
                                                               EntityModelImpl<T> entityModel, Class<?> parentClass, boolean nested, String prefix) {
        List<AttributeModel> result = new ArrayList<>();

        // ignore methods annotated with @AssertTrue or @AssertFalse
        String fieldName = descriptor.getName();
        Class<?> pClass = parentClass != null ? parentClass : entityModel.getEntityClass();
        AssertTrue assertTrue = ClassUtils.getAnnotation(pClass, fieldName, AssertTrue.class);
        AssertFalse assertFalse = ClassUtils.getAnnotation(pClass, fieldName, AssertFalse.class);
        if (assertTrue != null || assertFalse != null) {
            return result;
        }

        AttributeModelImpl model = new AttributeModelImpl();
        model.setEntityModel(entityModel);

        setAttributeModelDefaults(descriptor, entityModel, parentClass, prefix, fieldName, model);
        setNestedEntityModel(entityModel, model);

        if (getMessageService() != null) {
            model.setDefaultTrueRepresentation(SystemPropertyUtils.getDefaultTrueRepresentation());
            model.setDefaultFalseRepresentation(SystemPropertyUtils.getDefaultFalseRepresentation());
        }

        boolean isId = model.getName().equals(DynamoConstants.ID);
        model.setVisibleInGrid(!isId && !nested && (AttributeType.BASIC.equals(model.getAttributeType())));
        model.setVisibleInForm(!isId && AttributeType.BASIC.equals(model.getAttributeType()) ||
                AttributeType.LOB.equals(model.getAttributeType()));

        boolean isIdOrNestedId = model.getName().equals(DynamoConstants.ID) ||
                model.getName().endsWith(DynamoConstants.ID);
        model.setEditableType(isIdOrNestedId ? EditableType.READ_ONLY : EditableType.EDITABLE);


        AttributeSelectMode defaultMode = AttributeType.DETAIL.equals(model.getAttributeType())
                ? AttributeSelectMode.TOKEN
                : AttributeSelectMode.COMBO;

        model.setSelectMode(defaultMode);
        model.setTextFieldMode(AttributeTextFieldMode.TEXTFIELD);
        model.setSearchSelectMode(defaultMode);
        model.setGridSelectMode(defaultMode);

        Email email = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName,
                jakarta.validation.constraints.Email.class);
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
        boolean mainAttributeFound = false;
        AttributeModel firstStringAttribute = null;
        AttributeModel firstSearchableAttribute = null;
        boolean nested = reference.indexOf('.') >= 0;

        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(entityClass);
        List<AttributeModel> result = new ArrayList<>();
        for (PropertyDescriptor descriptor : descriptors) {
            if (!skipAttribute(descriptor.getName())) {
                List<AttributeModel> attributeModels = constructAttributeModel(descriptor, entityModel,
                        entityModel.getEntityClass(), nested, null);
                for (AttributeModel attributeModel : attributeModels) {

                    // check if the main attribute has been found
                    mainAttributeFound |= attributeModel.isMainAttribute();

                    // also keep track of the first string property...
                    if (firstStringAttribute == null && String.class.equals(attributeModel.getType())) {
                        firstStringAttribute = attributeModel;
                    }
                    // ... and the first searchable property
                    if (firstSearchableAttribute == null && attributeModel.isSearchable()) {
                        firstSearchableAttribute = attributeModel;
                    }
                    result.add(attributeModel);
                }
            }
        }

        if (!mainAttributeFound && !nested) {
            if (firstStringAttribute != null) {
                firstStringAttribute.setMainAttribute(true);
            } else if (firstSearchableAttribute != null) {
                firstSearchableAttribute.setMainAttribute(true);
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

        String displayName = com.ocs.dynamo.utils.StringUtils.propertyIdToHumanFriendly(entityClass.getSimpleName(),
                SystemPropertyUtils.isCapitalizeWords());

        EntityModelImpl.EntityModelImplBuilder<T> builder = EntityModelImpl.builder();
        builder.reference(reference).nestingDepth(SystemPropertyUtils.getDefaultNestingDepth())
                .defaultDescription(displayName).defaultDisplayName(displayName)
                .defaultDisplayNamePlural(displayName + PLURAL_POSTFIX).entityClass(entityClass);

        addEntityModelAnnotationOverrides(entityClass, builder);
        addEntityModelMessageBundleOverrides(reference, builder);

        EntityModelImpl<T> entityModel = builder.build();

        alreadyProcessed.put(reference, entityClass);

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

        processFetchJoinAnnotations(entityClass, entityModel);
        processMessageBundleJoinOverrides(entityModel);

        cache.put(reference, entityModel);
        return entityModel;
    }

    /**
     * Translate @FetchJoin annotations to settings on the entity model
     * @param entityClass the entity class
     * @param entityModel the entity model
     * @param <T> type parameter, the entity class
     */
    private <T> void processFetchJoinAnnotations(Class<T> entityClass, EntityModelImpl<T> entityModel) {
        entityModel.setFetchJoins(new ArrayList<>());
        entityModel.setDetailJoins(new ArrayList<>());

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
     * Constructs a nested entity model
     *
     * @param type      the class of the entity
     * @param reference the unique reference
     * @return the constructed model
     */
    public EntityModel<?> constructNestedEntityModel(Class<?> type, String reference) {
        return getModel(reference, type);
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

        // look for message bundle overwrites
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
     * Processes message bundle fetch joins
     * @param model the entity model
     * @param name the name to look for in the message bundle
     * @param <T> type parameter
     * @return the list of fetch joins (possibly empty)
     */
    protected <T> List<FetchJoinInformation> processMessageBundleFetchJoinOverrides(EntityModel<T> model,
                                                                                    String name) {
        List<FetchJoinInformation> result = new ArrayList<>();

        // look for message bundle overwrites
        int i = 1;
        if (messageService != null) {
            String key = name + "." + i + "." + EntityModel.ATTRIBUTE;
            String joinAttribute = messageService.getEntityMessage(model.getReference(),
                    key, getLocale());
            while (joinAttribute != null) {
                String joinType = messageService.getEntityMessage(model.getReference(),
                        name + "." + i + "." + EntityModel.JOIN_TYPE, getLocale());

                if (joinType != null) {
                    result.add(new FetchJoinInformation(joinAttribute,
                            JoinType.valueOf(joinType)));
                } else {
                    result.add(new FetchJoinInformation(joinAttribute));
                }
                i++;
                joinAttribute = messageService.getEntityMessage(model.getReference(),
                        name + "." + i + "." + EntityModel.ATTRIBUTE, getLocale());
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
                throw new OCSRuntimeException("Attribute " + attributeName + " is not known");
            }
        }

        return explicit;
    }

    /**
     * Determines the attribute type
     *
     * @param parentClass    the parent class on which the attribute is defined
     * @param attributeModel the model to determine the type of
     * @return the attribute type
     */
    protected AttributeType determineAttributeType(Class<?> parentClass, AttributeModelImpl attributeModel) {
        AttributeType result = null;
        String name = attributeModel.getName();
        int p = name.lastIndexOf('.');
        if (p > 0) {
            name = name.substring(p + 1);
        }

        if (!BeanUtils.isSimpleValueType(attributeModel.getType()) && !DateUtils.isJava8DateType(attributeModel.getType())) {
            // No relation type set in view model definition, hence derive
            // defaults
            Embedded embedded = ClassUtils.getAnnotation(parentClass, name, Embedded.class);
            Attribute attribute = ClassUtils.getAnnotation(parentClass, name, Attribute.class);

            if (embedded != null) {
                result = AttributeType.EMBEDDED;
            } else if (Collection.class.isAssignableFrom(attributeModel.getType())) {

                if (attribute != null && attribute.memberType() != null
                        && !attribute.memberType().equals(Object.class)) {
                    // if a member type is explicitly set, use that type
                    result = AttributeType.DETAIL;
                    attributeModel.setMemberType(attribute.memberType());
                } else if (ClassUtils.getAnnotation(parentClass, name, ManyToMany.class) != null
                        || ClassUtils.getAnnotation(parentClass, name, OneToMany.class) != null) {
                    result = AttributeType.DETAIL;
                    attributeModel.setMemberType(ClassUtils.getResolvedType(parentClass, name, 0));
                } else if (ClassUtils.getAnnotation(parentClass, name, ElementCollection.class) != null) {
                    result = AttributeType.ELEMENT_COLLECTION;
                    handleElementCollectionSettings(parentClass, attributeModel, name);
                } else if (AbstractEntity.class.isAssignableFrom(attributeModel.getType())) {
                    // not a collection but a reference to another object
                    result = AttributeType.MASTER;
                }
            } else if (attributeModel.getType().isArray()) {
                // a byte array with the @Lob annotation is transformed to a
                // @Lob field
                Lob lob = ClassUtils.getAnnotation(parentClass, name, Lob.class);
                if (lob != null) {
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
     * @return the date type
     */
    protected AttributeDateType determineDateType(Class<?> modelType) {
        if (LocalDate.class.equals(modelType)) {
            return AttributeDateType.DATE;
        } else if (LocalDateTime.class.equals(modelType)) {
            return AttributeDateType.TIMESTAMP;
        } else if (LocalTime.class.equals(modelType)) {
            return AttributeDateType.TIME;
        } else if (ZonedDateTime.class.equals(modelType)) {
            return AttributeDateType.TIMESTAMP;
        }
        return null;
    }

    /**
     * Determines the default format to use for formatting a date or time property
     *
     * @param type the type of the property
     * @return the display format
     */
    protected String determineDefaultDisplayFormat(Class<?> type) {
        String format = null;
        if (LocalDate.class.isAssignableFrom(type)) {
            format = SystemPropertyUtils.getDefaultDateFormat();
        } else if (LocalDateTime.class.isAssignableFrom(type)) {
            format = SystemPropertyUtils.getDefaultDateTimeFormat();
        } else if (LocalTime.class.isAssignableFrom(type)) {
            format = SystemPropertyUtils.getDefaultTimeFormat();
        } else if (ZonedDateTime.class.isAssignableFrom(type)) {
            format = SystemPropertyUtils.getDefaultDateTimeWithTimezoneFormat();
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
     * @param <T>             type parameter, the class
     * @param entityClass     the class of the entity model
     * @param reference       unique reference of the entity model
     * @param attributeModels the list of attribute models to order
     * @return true if an explicit order was defined, false otherwise
     */
    protected <T> boolean determineSearchAttributeOrder(Class<T> entityClass, String reference,
                                                        List<AttributeModel> attributeModels) {
        List<String> explicitAttributeNames = new ArrayList<>();
        SearchAttributeOrder orderAnnot = entityClass.getAnnotation(SearchAttributeOrder.class);
        if (orderAnnot != null) {
            explicitAttributeNames = List.of(orderAnnot.attributeNames());
        }
        return determineAttributeOrderInner(reference, EntityModel.SEARCH_ATTRIBUTE_ORDER, explicitAttributeNames,
                attributeModels, AttributeModelImpl::setSearchOrder);
    }

    /**
     * Looks up a possible delegated model factory for an entity model
     *
     * @param reference   the reference of the entity model
     * @param entityClass the entity class
     * @return the model factory to be used for constructing the entity model
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
        return SystemPropertyUtils.getDefaultLocale();
    }

    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public <T> EntityModel<T> getModel(Class<T> entityClass) {
        return getModel(entityClass.getSimpleName(), entityClass);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> EntityModel<T> getModel(String reference, Class<T> entityClass) {
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
     * @return true if the entity model has already been computed
     */
    protected boolean hasEntityModel(Class<?> type, String reference) {
        for (Entry<String, Class<?>> e : alreadyProcessed.entrySet()) {
            if (reference.equals(e.getKey()) && e.getValue().equals(type)) {
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
     * @return true if the entity model is known, false otherwise
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
     * @return true if the setting is visible, false otherwise
     */
    private boolean isVisible(String msg) {
        try {
            VisibilityType other = VisibilityType.valueOf(msg);
            return VisibilityType.SHOW.equals(other);
        } catch (IllegalArgumentException ex) {
            // do nothing
        }
        return Boolean.parseBoolean(msg);
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
                                                   EntityModelImpl<T> entityModel, boolean nested) {
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
     * Sets the custom settings for an attribute model based on the annotation
     *
     * @param attribute the attribute annotation
     * @param model     the attribute model
     */
    private void setAnnotationCustomOverwrites(Attribute attribute, AttributeModel model) {
        if (attribute.custom() != null && attribute.custom().length > 0) {
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
            setStringSetting(attribute.displayFormat(), model::setDisplayFormat);
            setStringSetting(attribute.trueRepresentation(), model::setDefaultTrueRepresentation);
            setStringSetting(attribute.falseRepresentation(), model::setDefaultFalseRepresentation);
            setStringSetting(attribute.textAreaHeight(), model::setTextAreaHeight);
            setStringSetting(attribute.currencySymbol(), model::setCurrencySymbol);

            model.setEditableType(attribute.editable());

            setAnnotationVisibilityOverrides(attribute, model, nested);

            if ((SearchMode.ADVANCED.equals(attribute.searchable()) || SearchMode.ALWAYS.equals(attribute.searchable()))
                    && !nested) {
                model.setSearchMode(attribute.searchable());
            }

            if (attribute.requiredForSearching() && !nested) {
                model.setRequiredForSearching(true);
            }

            if (attribute.main() && !nested) {
                model.setMainAttribute(true);
            }

            setBooleanTrueSetting(attribute.image(), model::setImage);
            setBooleanTrueSetting(attribute.percentage(), model::setPercentage);
            setBooleanTrueSetting(attribute.currency(), model::setCurrency);
            setBooleanTrueSetting(attribute.url(), model::setUrl);
            setBooleanFalseSetting(attribute.sortable(), model::setSortable);
            setBooleanTrueSetting(attribute.showPassword(), model::setShowPassword);
            setBooleanTrueSetting(attribute.quickAddAllowed(), model::setQuickAddAllowed);

            if (attribute.week()) {
                checkWeekSettingAllowed(model);
                model.setWeek(true);
            }

            if (attribute.allowedExtensions() != null && attribute.allowedExtensions().length > 0) {
                Set<String> set = Arrays.stream(attribute.allowedExtensions()).map(String::toLowerCase)
                        .collect(Collectors.toSet());
                model.setAllowedExtensions(set);
            }

            if (attribute.cascade() != null && attribute.cascade().length > 0) {
                for (Cascade cascade : attribute.cascade()) {
                    model.addCascade(cascade.cascadeTo(), cascade.filterPath(), cascade.mode());
                }
            }

            setAnnotationCustomOverwrites(attribute, model);

            if (attribute.groupTogetherWith() != null && attribute.groupTogetherWith().length > 0) {
                for (String s : attribute.groupTogetherWith()) {
                    model.addGroupTogetherWith(s);
                }
            }

            if (attribute.embedded()) {
                model.setAttributeType(AttributeType.EMBEDDED);
            }

            if (attribute.dateType() != null && !AttributeDateType.INHERIT.equals(attribute.dateType())) {
                model.setDateType(attribute.dateType());
            }

            if (attribute.selectMode() != null && !AttributeSelectMode.INHERIT.equals(attribute.selectMode())) {
                // setting the select mode also sets the search and grid modes
                model.setSelectMode(attribute.selectMode());
                model.setSearchSelectMode(attribute.selectMode());
                model.setGridSelectMode(attribute.selectMode());
            }

            if (attribute.multipleSearch()) {
                model.setMultipleSearch(true);
                model.setSearchSelectMode(AttributeSelectMode.LOOKUP);
            }

            if (!AttributeSelectMode.INHERIT.equals(attribute.searchSelectMode())) {
                model.setSearchSelectMode(attribute.searchSelectMode());
                // for a basic attribute, automatically set multiple search when a token field
                // is selected
                if (AttributeType.BASIC.equals(model.getAttributeType())
                        && AttributeSelectMode.TOKEN.equals(model.getSearchSelectMode())) {
                    model.setMultipleSearch(true);
                }
            }

            setEnumValueUnless(attribute.gridSelectMode(), AttributeSelectMode.INHERIT, model::setGridSelectMode);
            setEnumValueUnless(attribute.searchCaseSensitive(), BooleanType.INHERIT,
                    value -> model.setSearchCaseSensitive(value.toBoolean()));
            setEnumValueUnless(attribute.searchPrefixOnly(), BooleanType.INHERIT,
                    value -> model.setSearchPrefixOnly(value.toBoolean()));
            setEnumValueUnless(attribute.clearButtonVisible(), BooleanType.INHERIT,
                    value -> model.setClearButtonVisible(value.toBoolean()));
            setEnumValueUnless(attribute.thousandsGrouping(), ThousandsGroupingMode.INHERIT,
                    model::setThousandsGroupingMode);
            setEnumValueUnless(attribute.lookupFieldCaptions(), VisibilityType.INHERIT, model::setLookupFieldCaptions);

            if (attribute.textFieldMode() != null
                    && !AttributeTextFieldMode.INHERIT.equals(attribute.textFieldMode())) {
                model.setTextFieldMode(attribute.textFieldMode());
            }

            setIntSetting(attribute.precision(), -1, model::setPrecision);
            setIntSetting(attribute.minLength(), -1, model::setMinLength);
            setIntSetting(attribute.maxLength(), -1, model::setMaxLength);
            setIntSetting(attribute.maxLengthInGrid(), -1, model::setMaxLengthInGrid);

            setLongSetting(attribute.minValue(), Long.MIN_VALUE, true, model::setMinValue);
            setLongSetting(attribute.maxValue(), Long.MAX_VALUE, false, model::setMaxValue);

            setStringSetting(attribute.replacementSearchPath(), model::setReplacementSearchPath);
            setStringSetting(attribute.replacementSortPath(), model::setReplacementSortPath);

            setBooleanTrueSetting(attribute.searchForExactValue(), model::setSearchForExactValue);
            setStringSetting(attribute.fileNameProperty(), model::setFileNameProperty);

            setDefaulValue(model, attribute);

            model.setNavigable(attribute.navigable());
            model.setIgnoreInSearchFilter(attribute.ignoreInSearchFilter());
            model.setSearchDateOnly(attribute.searchDateOnly());

            setEnumValueUnless(attribute.pagingMode(), PagingMode.INHERIT, model::setPagingMode);
            setEnumValueUnless(attribute.multiSelectMode(), MultiSelectMode.INHERIT, model::setMultiSelectMode);
            setEnumValueUnless(attribute.trimSpaces(), TrimType.INHERIT,
                    ts -> model.setTrimSpaces(TrimType.TRIM.equals(ts)));
            setEnumValueUnless(attribute.numberFieldMode(), NumberFieldMode.INHERIT, model::setNumberFieldMode);

            setIntSetting(attribute.numberFieldStep(), 0, model::setNumberFieldStep);

            if (!StringUtils.isEmpty(attribute.autoFillInstructions())) {
                model.setAutoFillInstructions(attribute.autoFillInstructions());
            }
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
    private <T> void setAttributeModelDefaults(PropertyDescriptor descriptor, EntityModelImpl<T> entityModel,
                                               Class<?> parentClass, String prefix, String fieldName, AttributeModelImpl model) {
        String displayName = com.ocs.dynamo.utils.StringUtils.propertyIdToHumanFriendly(fieldName,
                SystemPropertyUtils.isCapitalizeWords());
        model.setDefaultDisplayName(displayName);
        model.setDefaultDescription(displayName);
        model.setDefaultPrompt(displayName);
        model.setMainAttribute(descriptor.isPreferred());
        model.setSearchMode(SearchMode.NONE);
        model.setName((prefix == null ? "" : (prefix + ".")) + fieldName);
        model.setImage(false);
        model.setEditableType(descriptor.isHidden() ? EditableType.READ_ONLY : EditableType.EDITABLE);
        model.setSortable(true);
        model.setPrecision(SystemPropertyUtils.getDefaultDecimalPrecision());
        model.setSearchCaseSensitive(SystemPropertyUtils.getDefaultSearchCaseSensitive());
        model.setSearchPrefixOnly(SystemPropertyUtils.getDefaultSearchPrefixOnly());
        model.setUrl(false);
        model.setThousandsGroupingMode(SystemPropertyUtils.getDefaultThousandsGroupingMode());
        model.setTrimSpaces(SystemPropertyUtils.isDefaultTrimSpaces());
        model.setMultiSelectMode(SystemPropertyUtils.useGridSelectionCheckBoxes() ? MultiSelectMode.CHECKBOX
                : MultiSelectMode.ROWSELECT);
        model.setPagingMode(SystemPropertyUtils.getDefaultPagingMode());

        model.setType(descriptor.getPropertyType());
        model.setDateType(determineDateType(model.getType()));
        model.setDisplayFormat(determineDefaultDisplayFormat(model.getType()));
        model.setNumberFieldMode(SystemPropertyUtils.getDefaultNumberFieldMode());
        model.setNumberFieldStep(1);

        model.setClearButtonVisible(SystemPropertyUtils.isDefaultClearButtonVisible());
        model.setTextAreaHeight(SystemPropertyUtils.getDefaultTextAreaHeight());
        model.setCurrencySymbol(SystemPropertyUtils.getDefaultCurrencySymbol());
        model.setLookupFieldCaptions(SystemPropertyUtils.getDefaultLookupFieldCaptions());

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
                value -> attributeModel.setDefaultDisplayName(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DESCRIPTION),
                value -> attributeModel.setDefaultDescription(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DEFAULT_VALUE),
                value -> attributeModel.setDefaultValue(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DISPLAY_FORMAT),
                value -> attributeModel.setDisplayFormat(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TRUE_REPRESENTATION),
                value -> attributeModel.setDefaultTrueRepresentation(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.FALSE_REPRESENTATION),
                value -> attributeModel.setDefaultFalseRepresentation(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TEXT_AREA_HEIGHT),
                value -> attributeModel.setTextAreaHeight(value));
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.CURRENCY_SYMBOL),
                value -> attributeModel.setCurrencySymbol(value));

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.MAIN),
                attributeModel::setMainAttribute);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.REQUIRED_FOR_SEARCHING),
                attributeModel::setRequiredForSearching);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SORTABLE),
                attributeModel::setSortable);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.IMAGE),
                attributeModel::setImage);

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_CASE_SENSITIVE),
                attributeModel::setSearchCaseSensitive);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_PREFIX_ONLY),
                attributeModel::setSearchPrefixOnly);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TRIM_SPACES),
                attributeModel::setTrimSpaces);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.PERCENTAGE),
                attributeModel::setPercentage);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.CURRENCY),
                attributeModel::setCurrency);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.URL), attributeModel::setUrl);

        // check for read only (convenience only, overwritten by "editable")
        String msg = getAttributeMessage(entityModel, attributeModel, EntityModel.READ_ONLY);
        if (!StringUtils.isEmpty(msg)) {
            boolean edt = Boolean.parseBoolean(msg);
            if (edt) {
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

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.WEEK);
        if (!StringUtils.isEmpty(msg)) {
            checkWeekSettingAllowed(attributeModel);
            attributeModel.setWeek(Boolean.parseBoolean(msg));
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

        setIntSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.PRECISION), -1,
                attributeModel::setPrecision);

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.EMBEDDED);
        if (msg != null && !StringUtils.isEmpty(msg) && Boolean.parseBoolean(msg)) {
            attributeModel.setAttributeType(AttributeType.EMBEDDED);
        }

        // multiple search setting - setting this to true also sets the search select
        // mode to TOKEN
        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.MULTIPLE_SEARCH);
        if (msg != null && !StringUtils.isEmpty(msg)) {
            attributeModel.setMultipleSearch(Boolean.parseBoolean(msg));
            attributeModel.setSearchSelectMode(AttributeSelectMode.TOKEN);
        }

        // set the select mode (also sets the search select mode and grid select mode)
        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.SELECT_MODE);
        if (msg != null && !StringUtils.isEmpty(msg)) {
            AttributeSelectMode mode = AttributeSelectMode.valueOf(msg);
            attributeModel.setSelectMode(mode);
            attributeModel.setSearchSelectMode(mode);
            attributeModel.setGridSelectMode(mode);
        }

        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_SELECT_MODE),
                AttributeSelectMode.class, attributeModel::setSearchSelectMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.GRID_SELECT_MODE),
                AttributeSelectMode.class, attributeModel::setGridSelectMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.MULTI_SELECT_MODE),
                MultiSelectMode.class, attributeModel::setMultiSelectMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.DATE_TYPE), AttributeDateType.class,
                attributeModel::setDateType);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.TEXTFIELD_MODE),
                AttributeTextFieldMode.class, attributeModel::setTextFieldMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.LOOKUP_FIELD_CAPTIONS),
                VisibilityType.class, attributeModel::setLookupFieldCaptions);

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.MIN_VALUE);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setMinValue(Long.parseLong(msg));
        }

        setIntSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.MIN_LENGTH), -1,
                attributeModel::setMinLength);
        setIntSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.MAX_LENGTH), -1,
                value -> attributeModel.setMaxLength(value));
        setIntSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.MAX_LENGTH_IN_GRID), -1,
                value -> attributeModel.setMaxLengthInGrid(value));

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.MAX_VALUE);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setMaxValue(Long.parseLong(msg));
        }

        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.REPLACEMENT_SEARCH_PATH),
                attributeModel::setReplacementSearchPath);
        setStringSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.REPLACEMENT_SORT_PATH),
                attributeModel::setReplacementSortPath);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.QUICK_ADD_ALLOWED),
                attributeModel::setQuickAddAllowed);

        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.THOUSANDS_GROUPING_MODE),
                ThousandsGroupingMode.class, attributeModel::setThousandsGroupingMode);

        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_EXACT_VALUE),
                attributeModel::setSearchForExactValue);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NAVIGABLE),
                attributeModel::setNavigable);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.SEARCH_DATE_ONLY),
                attributeModel::setSearchDateOnly);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.IGNORE_IN_SEARCH_FILTER),
                attributeModel::setIgnoreInSearchFilter);
        setBooleanSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.CLEAR_BUTTON_VISIBLE),
                attributeModel::setClearButtonVisible);

        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.PAGING_MODE), PagingMode.class,
                attributeModel::setPagingMode);
        setEnumSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NUMBER_FIELD_MODE),
                NumberFieldMode.class, attributeModel::setNumberFieldMode);

        setIntSetting(getAttributeMessage(entityModel, attributeModel, EntityModel.NUMBER_FIELD_STEP), -1,
                attributeModel::setNumberFieldStep);

        setMessageBundleCascadeOverrides(entityModel, attributeModel);
        setMessageBundleCustomOverrides(entityModel, attributeModel);

        msg = getAttributeMessage(entityModel, attributeModel, EntityModel.AUTO_FILL_INSTRUCTIONS);
        if (!StringUtils.isEmpty(msg)) {
            attributeModel.setAutoFillInstructions(msg);
        }
    }

    /**
     * Sets a value on an attribute model if the provided boolean value is false
     *
     * @param value    the boolean value
     * @param receiver the code that is executed to set the value
     */
    private void setBooleanFalseSetting(Boolean value, Consumer<Boolean> receiver) {
        if (!value) {
            receiver.accept(value);
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
            receiver.accept(value);
        }
    }

    /**
     * Sets the default value on the attribute model (translates a String to the
     * appropriate type)
     *
     * @param model        the attribute model
     * @param defaultValue the default value to set
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setDefaultValue(AttributeModelImpl model, String defaultValue) {
        if (model.getType().isEnum()) {
            Class<? extends Enum> enumType = model.getType().asSubclass(Enum.class);
            model.setDefaultValue(Enum.valueOf(enumType, defaultValue));
        } else if (DateUtils.isJava8DateType(model.getType())) {
            Object o = DateUtils.createJava8Date(model.getType(), defaultValue, model.getDisplayFormat());
            model.setDefaultValue(o);
        } else if (Boolean.class.equals(model.getType()) || boolean.class.equals(model.getType())) {
            model.setDefaultValue(Boolean.valueOf(defaultValue));
        } else {
            model.setDefaultValue(ClassUtils.instantiateClass(model.getType(), defaultValue));
        }
    }

    /**
     * Sets the default value of an attribute based on the annotation
     *
     * @param model     the attribute model
     * @param attribute the annotation
     */
    private void setDefaulValue(AttributeModelImpl model, Attribute attribute) {
        if (!StringUtils.isEmpty(attribute.defaultValue())) {
            if (!AttributeType.BASIC.equals(model.getAttributeType())) {
                throw new OCSRuntimeException(
                        model.getName() + ": setting a default value is only allowed for BASIC attributes");
            }

            String defaultValue = attribute.defaultValue();
            setDefaultValue(model, defaultValue);
        }
    }

    /**
     * Sets an enum field based on a string value from a message bundle
     *
     * @param <E>       the type of the enum
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
    private void setIntSetting(String value, int limit, Consumer<Integer> receiver) {
        if (value == null) {
            return;
        }

        int intValue = Integer.parseInt(value);
        if (intValue > limit) {
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
     * @param consumer the consumer to execute
     */
    private void setLongSetting(Long value, long limit, boolean above, Consumer<Long> consumer) {
        if (value != null && ((above && value > limit) || value < limit)) {
            consumer.accept(value);
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
     * @param model             the attribute model
     */
    protected void setNestedEntityModel(EntityModel<?> parentEntityModel, AttributeModelImpl model) {
        EntityModel<?> em = model.getEntityModel();
        if (StringUtils.countMatches(em.getReference(), ".") < parentEntityModel.getNestingDepth()) {
            Class<?> type = null;

            // only needed for master and detail attributes
            if (AttributeType.MASTER.equals(model.getAttributeType())) {
                type = model.getType();
            } else if (AttributeType.DETAIL.equals(model.getAttributeType())) {
                type = model.getMemberType();
            }

            if (type != null) {
                String ref;
                if (StringUtils.isEmpty(em.getReference())) {
                    ref = em.getEntityClass() + "." + model.getName();
                } else {
                    ref = em.getReference() + "." + model.getName();
                }

                if (type.equals(em.getEntityClass()) || !hasEntityModel(type, ref)) {
                    model.setNestedEntityModel(findModelFactory(ref, type).getModel(ref, type));
                }
            }
        }
    }

    /**
     * Sets the "required", "min" and "max" settings on an attribute based on JPA validation
     * annotations
     *
     * @param <T>           the type parameter
     * @param entityModel   the entity model that the attribute model is part of
     * @param model         the attribute model
     * @param parentClass   the parent class
     * @param attributeName the name of the attribute
     */
    private <T> void setRequiredAndMinMaxSetting(EntityModelImpl<T> entityModel, AttributeModelImpl model,
                                                 Class<?> parentClass, String attributeName) {
        // determine if the attribute is required based on the @NotNull
        // annotation
        NotNull notNull = ClassUtils.getAnnotation(entityModel.getEntityClass(), attributeName, NotNull.class);
        model.setRequired(notNull != null);

        // also set to required when it is a collection with a size greater than 0
        model.setAttributeType(determineAttributeType(parentClass, model));
        Size size = ClassUtils.getAnnotation(entityModel.getEntityClass(), attributeName, Size.class);
        if (size != null && size.min() > 0 && AttributeType.DETAIL.equals(model.getAttributeType())) {
            model.setRequired(true);
        }

        // minimum and maximum length based on the @Size annotation
        if (AttributeType.BASIC.equals(model.getAttributeType()) && size != null) {
            model.setMaxLength(size.max());
            model.setMinLength(size.min());
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
                            sd.length <= 1 || (!"DESC".equalsIgnoreCase(sd[1]) && !"DSC".equalsIgnoreCase(sd[1])));
                }
            }
        }
    }

    /**
     * Sets a String field based on a value from a message bundle
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
     * Indicates whether to skip an attribute since it does not constitute an actual
     * property but rather a generic or technical field that all entities have
     *
     * @param name the name of the attribute
     * @return true if the attribute must be skipped
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
        if (AttributeSelectMode.TOKEN.equals(model.getSelectMode())
                && !AttributeType.DETAIL.equals(model.getAttributeType())
                && !AttributeType.BASIC.equals(model.getAttributeType())) {
            throw new OCSRuntimeException("Token field not allowed for attribute " + model.getName());
        }

        // list select field only allowed for "master" attribute types
        if ((AttributeSelectMode.LIST.equals(model.getSelectMode())
                && !AttributeType.MASTER.equals(model.getAttributeType()))) {
            throw new OCSRuntimeException("List field is not allowed for attribute " + model.getName());
        }

        // navigating only allowed in case of a many-to-one relation
        if (!AttributeType.MASTER.equals(model.getAttributeType()) && model.isNavigable()) {
            throw new OCSRuntimeException("Navigation is not possible for attribute " + model.getName());
        }

        // searching on a LOB is pointless
        if (AttributeType.LOB.equals(model.getAttributeType()) && model.isSearchable()) {
            throw new OCSRuntimeException("Searching on a LOB is not allowed for attribute " + model.getName());
        }

        // "search date only" is only supported for date/time fields
        if (model.isSearchDateOnly() && !LocalDateTime.class.equals(model.getType())
                && !ZonedDateTime.class.equals(model.getType())) {
            throw new OCSRuntimeException("SearchDateOnly is not allowed for attribute " + model.getName());
        }

        // field cannot be percentage and currency at the same time
        if (model.isPercentage() && model.isCurrency()) {
            throw new OCSRuntimeException(model.getName() + " is not allowed to be both a percentage and a currency");
        }
    }

    /**
     * Validates the "group together with" settings for all attributes in the
     * specified entity model
     *
     * @param <T>         the type of the class of the entity model
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
                            log.warn("Incorrect groupTogetherWith found: {} refers to {} ", am.getName(), together);
                        }
                    }
                }
            }
        }
    }

}
