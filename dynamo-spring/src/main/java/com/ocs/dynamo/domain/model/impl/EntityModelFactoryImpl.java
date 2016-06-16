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

import java.beans.PropertyDescriptor;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeGroup;
import com.ocs.dynamo.domain.model.annotation.AttributeGroups;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.validator.Email;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.DefaultFieldFactory;

/**
 * Implementation of the entity model factory - creates models that hold metadata about an entity
 * 
 * @author bas.rutten
 */
public class EntityModelFactoryImpl implements EntityModelFactory {

    private static final String PLURAL_POSTFIX = "s";

    private static final String CLASS = "class";

    private static final String VERSION = "version";

    private static final int RECURSIVE_MODEL_DEPTH = 3;

    @Value("#{systemProperties['ocs.default.date.format']?:'dd-MM-yyyy'}")
    private String defaultDateFormat;

    @Value("#{systemProperties['ocs.default.time.format']?:'HH:mm:ss'}")
    private String defaultTimeFormat;

    @Value("#{systemProperties['ocs.default.datetime.format']?:'dd-MM-yyyy HH:mm:ss'}")
    private String defaultDateTimeFormat;

    @Value("#{systemProperties['ocs.default.decimal.precision']?:'2'}")
    private Integer defaultPrecision;

    @Autowired(required = false)
    private MessageService messageService;

    private ConcurrentMap<String, EntityModel<?>> cache = new ConcurrentHashMap<String, EntityModel<?>>();

    private ConcurrentMap<String, Class<?>> alreadyProcessed = new ConcurrentHashMap<String, Class<?>>();

    /**
     * Constructs an attribute model for a property
     * 
     * @param descriptor
     *            the property descriptor
     * @param entityModel
     *            the entity model
     * @param parentClass
     *            the type of the direct parent of the attribute (relevant in case of embedded
     *            attributes)
     * @param nested
     *            whether this is a nested attribute
     * @param prefix
     *            the prefix to apply to the attribute name
     * @return
     */
    private <T> List<AttributeModel> constructAttributeModel(PropertyDescriptor descriptor,
            EntityModelImpl<T> entityModel, Class<?> parentClass, boolean nested, String prefix) {
        List<AttributeModel> result = new ArrayList<AttributeModel>();

        // validation methods annotated with @AssertTrue or @AssertFalse have to
        // be ignored
        AssertTrue assertTrue = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                descriptor.getName(), AssertTrue.class);
        AssertFalse assertFalse = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                descriptor.getName(), AssertFalse.class);

        if (assertTrue == null && assertFalse == null) {

            AttributeModelImpl model = new AttributeModelImpl();
            model.setEntityModel(entityModel);

            String displayName = DefaultFieldFactory
                    .createCaptionByPropertyId(descriptor.getName());

            // first, set the defaults
            model.setDisplayName(displayName);
            model.setDescription(displayName);
            model.setPrompt(displayName);
            model.setMainAttribute(descriptor.isPreferred());
            model.setSearchable(descriptor.isPreferred());
            model.setName((prefix == null ? "" : (prefix + ".")) + descriptor.getName());
            model.setImage(false);

            model.setReadOnly(descriptor.isHidden());
            model.setSortable(true);
            model.setComplexEditable(false);
            model.setPrecision(defaultPrecision);
            model.setSearchCaseSensitive(false);
            model.setSearchPrefixOnly(false);
            model.setUrl(false);

            Id idAttr = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                    descriptor.getName(), Id.class);
            if (idAttr != null) {
                entityModel.setIdAttributeModel(model);
                // the ID column is hidden. details collections are also hidden
                // by default
                model.setVisible(false);
            } else {
                model.setVisible(true);
            }
            model.setType(descriptor.getPropertyType());

            // determine default display format
            model.setDisplayFormat(determineDefaultDisplayFormat(model.getType(),
                    entityModel.getEntityClass(), descriptor.getName()));

            // determine if the attribute is required based on the @NotNull
            // annotation
            NotNull notNull = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                    descriptor.getName(), NotNull.class);
            model.setRequired(notNull != null);

            model.setAttributeType(determineAttributeType(parentClass, model));

            // minimum and maximum length based on the @Size annotation
            Size size = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                    descriptor.getName(), Size.class);
            if (AttributeType.BASIC.equals(model.getAttributeType()) && size != null) {
                model.setMaxLength(size.max());
                model.setMinLength(size.min());
            }

            setNestedEntityModel(model);

            // only basic attributes are shown in the table by default
            model.setVisibleInTable(!nested && model.isVisible()
                    && (AttributeType.BASIC.equals(model.getAttributeType())));

            if (getMessageService() != null) {
                model.setTrueRepresentation(getMessageService().getMessage("ocs.true"));
                model.setFalseRepresentation(getMessageService().getMessage("ocs.false"));
            }

            // set the date type
            if (Date.class.equals(model.getType())) {
                Temporal temporal = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                        descriptor.getName(), Temporal.class);
                if (temporal != null) {
                    // use the @Temporal annotation when available
                    model.setDateType(translateDateType(temporal.value() == null ? TemporalType.DATE
                            : temporal.value()));
                } else {
                    // by default use date
                    model.setDateType(AttributeDateType.DATE);
                }
            }

            // by default, use a combo box to look up
            model.setSelectMode(AttributeSelectMode.COMBO);
            model.setTextFieldMode(AttributeTextFieldMode.TEXTFIELD);

            // is the field an email field?
            Email email = ClassUtils.getAnnotation(entityModel.getEntityClass(),
                    descriptor.getName(), Email.class);
            model.setEmail(email != null);

            // override the defaults with annotation values
            setAnnotationOverrides(parentClass, model, descriptor, nested);

            // override any earlier version with message bundle contents
            setMessageBundleOverrides(entityModel, model);

            if (!model.isEmbedded()) {
                result.add(model);
            } else {
                // an embedded object is not directly added. Instead, its child
                // properties are added as attributes
                if (model.getType().equals(entityModel.getEntityClass())) {
                    throw new IllegalStateException("Embedding a class in itself is not allowed");
                }
                PropertyDescriptor[] embeddedDescriptors = BeanUtils.getPropertyDescriptors(model
                        .getType());
                for (PropertyDescriptor embeddedDescriptor : embeddedDescriptors) {
                    String name = embeddedDescriptor.getName();
                    if (!skipAttribute(name)) {
                        List<AttributeModel> embeddedModels = constructAttributeModel(
                                embeddedDescriptor, entityModel, model.getType(), nested,
                                model.getName());
                        result.addAll(embeddedModels);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Constructs the model for an entity
     * 
     * @param entityClass
     *            the class of the entity
     * @return
     */
    private <T> EntityModel<T> constructModel(String reference, Class<T> entityClass) {

        boolean nested = reference.indexOf('.') > 0;

        // construct the basic model
        EntityModelImpl<T> model = constructModelInner(entityClass, reference);

        Map<String, String> attributeGroupMap = determineAttributeGroupMapping(model, entityClass);
        model.addAttributeGroup(EntityModel.DEFAULT_GROUP);

        alreadyProcessed.put(reference, entityClass);

        // keep track of main attributes - if no main attribute is defined, mark
        // either the
        // first string attribute or the first searchable attribute as the main
        boolean mainAttributeFound = false;
        AttributeModel firstStringAttribute = null;
        AttributeModel firstSearchableAttribute = null;

        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(entityClass);
        // create attribute models for all attributes
        List<AttributeModel> tempModelList = new ArrayList<>();
        for (PropertyDescriptor descriptor : descriptors) {
            if (!skipAttribute(descriptor.getName())) {
                List<AttributeModel> attributeModels = constructAttributeModel(descriptor, model,
                        model.getEntityClass(), nested, null);

                for (AttributeModel attributeModel : attributeModels) {

                    // check if the main attribute has been found
                    mainAttributeFound |= attributeModel.isMainAttribute();

                    // also keep track of the first string property...
                    if (firstStringAttribute == null
                            && String.class.equals(attributeModel.getType())) {
                        firstStringAttribute = attributeModel;
                    }
                    // ... and the first searchable property
                    if (firstSearchableAttribute == null && attributeModel.isSearchable()) {
                        firstSearchableAttribute = attributeModel;
                    }
                    tempModelList.add(attributeModel);
                }
            }
        }

        // assign ordering and sort
        determineAttributeOrder(entityClass, reference, tempModelList);
        Collections.sort(tempModelList);

        // add the attributes to the model
        for (AttributeModel attributeModel : tempModelList) {
            // determine the attribute group name
            String group = attributeGroupMap.get(attributeModel.getName());
            if (StringUtils.isEmpty(group)) {
                group = EntityModel.DEFAULT_GROUP;
            }

            model.addAttributeModel(group, attributeModel);
        }

        if (!mainAttributeFound && !nested) {
            if (firstStringAttribute != null) {
                firstStringAttribute.setMainAttribute(true);
            } else if (firstSearchableAttribute != null) {
                firstSearchableAttribute.setMainAttribute(true);
            }
        }

        String sortOrder = null;
        Model annot = entityClass.getAnnotation(Model.class);
        if (annot != null && !StringUtils.isEmpty(annot.sortOrder())) {
            sortOrder = annot.sortOrder();
        }

        String sortOrderMsg = getEntityMessage(reference, EntityModel.SORT_ORDER);
        if (!StringUtils.isEmpty(sortOrderMsg)) {
            sortOrder = sortOrderMsg;
        }
        setSortOrder(model, sortOrder);

        return model;
    }

    /**
     * @param entityClass
     * @param reference
     * @return
     */
    private <T> EntityModelImpl<T> constructModelInner(Class<T> entityClass, String reference) {

        String displayName = DefaultFieldFactory.createCaptionByPropertyId(entityClass
                .getSimpleName());
        String displayNamePlural = displayName + PLURAL_POSTFIX;
        String description = displayName;
        String selectDisplayProperty = null;

        Model annot = entityClass.getAnnotation(Model.class);
        if (annot != null) {

            if (!StringUtils.isEmpty(annot.displayName())) {
                displayName = annot.displayName();
            }
            if (!StringUtils.isEmpty(annot.displayNamePlural())) {
                displayNamePlural = annot.displayNamePlural();
            }
            if (!StringUtils.isEmpty(annot.description())) {
                description = annot.description();
            }
            if (!StringUtils.isEmpty(annot.displayProperty())) {
                selectDisplayProperty = annot.displayProperty();
            }

        }

        // override using message bundle
        String displayMsg = getEntityMessage(reference, EntityModel.DISPLAY_NAME);
        if (!StringUtils.isEmpty(displayMsg)) {
            displayName = displayMsg;
        }

        String pluralMsg = getEntityMessage(reference, EntityModel.DISPLAY_NAME_PLURAL);
        if (!StringUtils.isEmpty(pluralMsg)) {
            displayNamePlural = pluralMsg;
        }

        String descriptionMsg = getEntityMessage(reference, EntityModel.DESCRIPTION);
        if (!StringUtils.isEmpty(descriptionMsg)) {
            description = descriptionMsg;
        }

        String selectDisplayPropertyMsg = getEntityMessage(reference, EntityModel.DISPLAY_PROPERTY);
        if (!StringUtils.isEmpty(selectDisplayPropertyMsg)) {
            selectDisplayProperty = selectDisplayPropertyMsg;
        }

        return new EntityModelImpl<>(entityClass, reference, displayName, displayNamePlural,
                description, selectDisplayProperty);
    }

    /**
     * Construct a mapping from attribute to its corresponding attribute group
     * 
     * @param entityClass
     * @return
     */
    private <T> Map<String, String> determineAttributeGroupMapping(EntityModelImpl<T> model,
            Class<T> entityClass) {
        Map<String, String> result = new HashMap<>();
        AttributeGroups groups = entityClass.getAnnotation(AttributeGroups.class);
        if (groups != null) {
            for (AttributeGroup g : groups.attributeGroups()) {
                model.addAttributeGroup(g.displayName());
                for (String s : g.attributeNames()) {
                    result.put(s, g.displayName());
                }
            }
        } else {
            AttributeGroup group = entityClass.getAnnotation(AttributeGroup.class);
            if (group != null) {
                model.addAttributeGroup(group.displayName());
                for (String s : group.attributeNames()) {
                    result.put(s, group.displayName());
                }
            }
        }

        // look for message bundle overwrites
        int i = 1;
        if (messageService != null) {
            String groupName = messageService.getEntityMessage(model.getReference(),
                    EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.DISPLAY_NAME);
            while (groupName != null) {

                String attributeNames = messageService.getEntityMessage(model.getReference(),
                        EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.ATTRIBUTE_NAMES);

                if (attributeNames != null) {
                    model.addAttributeGroup(groupName);
                    for (String s : attributeNames.split(",")) {
                        result.put(s, groupName);
                    }
                }

                i++;
                groupName = messageService.getEntityMessage(model.getReference(),
                        EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.DISPLAY_NAME);
            }
        }
        return result;
    }

    /**
     * Determines the order of the attributes - this will first pick up any attributes that are
     * mentioned in the @AttributeOrder annotation (in the order in which they occur) and then add
     * any attributes that are not explicitly mentioned
     * 
     * @param entityClass
     * @param descriptors
     * @return
     */
    private <T> void determineAttributeOrder(Class<T> entityClass, String reference,
            List<AttributeModel> attributeModels) {

        List<String> explicitAttributeNames = new ArrayList<>();
        List<String> additionalNames = new ArrayList<>();

        // read ordering from the annotation (if present)
        AttributeOrder orderAnnot = entityClass.getAnnotation(AttributeOrder.class);
        if (orderAnnot != null) {
            explicitAttributeNames = Arrays.asList(orderAnnot.attributeNames());
        }

        // overwrite by message bundle (if present)
        String msg = messageService == null ? null : messageService.getEntityMessage(reference,
                EntityModel.ATTRIBUTE_ORDER);
        if (msg != null) {
            explicitAttributeNames = Arrays.asList(msg.replaceAll("\\s+", "").split(","));
        }

        for (AttributeModel attributeModel : attributeModels) {
            String name = attributeModel.getName();
            if (!skipAttribute(name) && !explicitAttributeNames.contains(name)) {
                additionalNames.add(name);
            }
        }

        // add the explicitly named attributes
        List<String> result = new ArrayList<>(explicitAttributeNames);
        // add the additional unmentioned attributes
        result.addAll(additionalNames);

        int i = 0;
        for (String attributeName : result) {
            AttributeModel am = null;
            for (AttributeModel m : attributeModels) {
                if (m.getName().equals(attributeName)) {
                    am = m;
                    break;
                }
            }
            if (am != null) {
                ((AttributeModelImpl) am).setOrder(i);
                i++;
            } else {
                throw new OCSRuntimeException("Attribute " + attributeName + " is not known");
            }
        }

    }

    /**
     * Determines the attribute type of an attribute
     * 
     * @param model
     *            the model representation of the attribute
     * @return
     */
    private AttributeType determineAttributeType(Class<?> parentClass, AttributeModelImpl model) {
        AttributeType result = null;
        String name = model.getName();
        int p = name.lastIndexOf(".");
        if (p > 0) {
            name = name.substring(p + 1);
        }

        if (!BeanUtils.isSimpleValueType(model.getType())) {
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
                    model.setMemberType(ClassUtils.getResolvedType(parentClass, model.getName(), 0));
                } else if (ClassUtils.getAnnotation(parentClass, name, ElementCollection.class) != null) {
                    result = AttributeType.ELEMENT_COLLECTION;
                    model.setMemberType(ClassUtils.getResolvedType(parentClass, model.getName(), 0));
                } else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
                    // not a collection but a reference to another object
                    result = AttributeType.MASTER;
                }
            } else if (model.getType().isArray()) {
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
     * Determines the display format on a property
     * 
     * @param type
     *            the java type
     * @param entityClass
     *            the class on which the property is defined
     * @param fieldName
     * @return
     */
    private String determineDefaultDisplayFormat(Class<?> type, Class<?> entityClass,
            String fieldName) {
        if (Date.class.isAssignableFrom(type)) {
            Temporal temporal = ClassUtils.getAnnotation(entityClass, fieldName, Temporal.class);
            TemporalType temporalType = temporal == null ? TemporalType.DATE : temporal.value();

            String format = null;
            if (TemporalType.TIMESTAMP.equals(temporalType)) {
                // the field is a date field
                format = defaultDateTimeFormat;
            } else if (TemporalType.TIME.equals(temporalType)) {
                // time
                format = defaultTimeFormat;
            } else {
                // by default use a date
                format = defaultDateFormat;
            }
            return format;
        }
        return null;
    }

    /**
     * Retrieves a message relating to an attribute from the message bundle
     * 
     * @param model
     * @param attributeModel
     * @param propertyName
     * @return
     */
    private <T> String getAttributeMessage(EntityModel<T> model, AttributeModel attributeModel,
            String propertyName) {
        if (messageService != null) {
            return messageService.getAttributeMessage(model.getReference(), attributeModel,
                    propertyName);
        }
        return null;
    }

    /**
     * Retrieves a message relating to an entity from the message bundle
     * 
     * @param reference
     * @param propertyName
     * @return
     */
    private String getEntityMessage(String reference, String propertyName) {
        if (messageService != null) {
            return messageService.getEntityMessage(reference, propertyName);
        }
        return null;
    }

    protected Locale getLocale() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            return session.getLocale();
        }
        return Locale.getDefault();
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }

    @Override
    public <T> EntityModel<T> getModel(Class<T> entityClass) {
        return getModel(entityClass.getSimpleName(), entityClass);
    }

    @Override
    @SuppressWarnings({ "unchecked", "unused" })
    public <T> EntityModel<T> getModel(String reference, Class<T> entityClass) {
        EntityModel<T> model = null;
        if (!StringUtils.isEmpty(reference) && entityClass != null) {
            model = (EntityModel<T>) cache.get(reference);
            if (model == null) {
                model = constructModel(reference, entityClass);
                // needed to make FindBugs happy
                EntityModel<?> skip = cache.putIfAbsent(reference, model);
            }
        }
        return model;
    }

    private boolean hasEntityModel(Class<?> type, String reference) {
        for (Entry<String, Class<?>> e : alreadyProcessed.entrySet()) {
            if (reference.startsWith(e.getKey()) && e.getValue().equals(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasModel(String reference) {
        return cache.containsKey(reference);
    }

    /**
     * Overwrite the default values with annotation values
     * 
     * @param entityModel
     * @param model
     * @param descriptor
     */
    private void setAnnotationOverrides(Class<?> parentClass, AttributeModelImpl model,
            PropertyDescriptor descriptor, boolean nested) {
        Attribute attribute = ClassUtils.getAnnotation(parentClass, descriptor.getName(),
                Attribute.class);

        // overwrite with annotation values
        if (attribute != null) {
            if (!StringUtils.isEmpty(attribute.displayName())) {
                model.setDisplayName(attribute.displayName());
                // by default, set prompt and description to the display
                // name as
                // well -
                // they are overwritten in the following code if they are
                // explicitly set
                model.setPrompt(attribute.displayName());
                model.setDescription(attribute.displayName());
            }

            if (!StringUtils.isEmpty(attribute.defaultValue())) {
                String defaultValue = attribute.defaultValue();
                setDefaultValue(model, defaultValue);
            }
            if (!StringUtils.isEmpty(attribute.description())) {
                model.setDescription(attribute.description());
            }
            if (!StringUtils.isEmpty(attribute.displayFormat())) {
                model.setDisplayFormat(attribute.displayFormat());
            }
            if (!StringUtils.isEmpty(attribute.prompt())) {
                model.setPrompt(attribute.prompt());
            }

            if (attribute.readOnly()) {
                model.setReadOnly(true);
            }

            // set visibility (but not for nested attributes - these are hidden by default)
            if (attribute.visible() != null && !VisibilityType.INHERIT.equals(attribute.visible())
                    && !nested) {
                model.setVisible(VisibilityType.SHOW.equals(attribute.visible()));
                model.setVisibleInTable(model.isVisible());
            }

            if (attribute.searchable() && !nested) {
                model.setSearchable(true);
            }

            if (!attribute.sortable()) {
                model.setSortable(false);
            }

            if (attribute.main() && !nested) {
                model.setMainAttribute(true);
            }

            if (attribute.showInTable() != null
                    && !VisibilityType.INHERIT.equals(attribute.showInTable()) && !nested) {
                model.setVisibleInTable(VisibilityType.SHOW.equals(attribute.showInTable()));
            }

            if (attribute.complexEditable()) {
                model.setComplexEditable(true);
            }

            if (attribute.image()) {
                model.setImage(true);
            }

            if (attribute.week()) {
                model.setWeek(true);
            }

            if (!StringUtils.isEmpty(attribute.allowedExtensions())) {
                String[] extensions = attribute.allowedExtensions().split(",");
                Set<String> hashSet = Sets.newHashSet(extensions);
                model.setAllowedExtensions(hashSet);
            }

            if (!StringUtils.isEmpty(attribute.trueRepresentation())) {
                model.setTrueRepresentation(attribute.trueRepresentation());
            }

            if (!StringUtils.isEmpty(attribute.falseRepresentation())) {
                model.setFalseRepresentation(attribute.falseRepresentation());
            }

            if (attribute.detailFocus()) {
                model.setDetailFocus(true);
            }

            if (attribute.percentage()) {
                model.setPercentage(true);
            }

            if (attribute.embedded()) {
                model.setAttributeType(AttributeType.EMBEDDED);
            }

            if (attribute.currency()) {
                model.setCurrency(true);
            }

            if (attribute.dateType() != null
                    && !AttributeDateType.INHERIT.equals(attribute.dateType())) {
                model.setDateType(attribute.dateType());
            }

            if (attribute.selectMode() != null
                    && !AttributeSelectMode.INHERIT.equals(attribute.selectMode())) {
                model.setSelectMode(attribute.selectMode());
            }

            model.setSearchCaseSensitive(attribute.searchCaseSensitive());
            model.setSearchPrefixOnly(attribute.searchPrefixOnly());

            if (attribute.textFieldMode() != null
                    && !AttributeTextFieldMode.INHERIT.equals(attribute.textFieldMode())) {
                model.setTextFieldMode(attribute.textFieldMode());
            }

            if (attribute.precision() > -1) {
                model.setPrecision(attribute.precision());
            }

            if (attribute.minLength() > -1) {
                model.setMinLength(attribute.minLength());
            }

            if (attribute.maxLength() > -1) {
                model.setMaxLength(attribute.maxLength());
            }

            if (attribute.url()) {
                model.setUrl(true);
            }

            if (!StringUtils.isEmpty(attribute.replacementSearchPath())) {
                model.setReplacementSearchPath(attribute.replacementSearchPath());
            }

            if (!StringUtils.isEmpty(attribute.quickAddPropertyName())) {
                model.setQuickAddPropertyName(attribute.quickAddPropertyName());
                model.setQuickAddAllowed(true);
            }

            if (attribute.required()) {
                model.setRequired(true);
            }

            if (attribute.multipleSearch()) {
                model.setMultipleSearch(true);
            }
        }
    }

    /**
     * Sets the default value on the attribute model (translates a String to the appropriate type)
     * 
     * @param model
     * @param defaultValue
     */
    @SuppressWarnings("unchecked")
    private void setDefaultValue(AttributeModelImpl model, String defaultValue) {
        if (model.getType().isEnum()) {
            model.setDefaultValue(Enum
                    .valueOf(model.getType().asSubclass(Enum.class), defaultValue));
        } else if (model.getType().equals(Date.class)) {
            String format = null;
            switch (model.getDateType()) {
            case TIME:
                format = defaultTimeFormat;
                break;
            case TIMESTAMP:
                format = defaultDateTimeFormat;
                break;
            default:
                format = defaultDateFormat;
            }

            SimpleDateFormat fmt = new SimpleDateFormat(format);
            try {
                model.setDefaultValue(fmt.parseObject(defaultValue));
            } catch (ParseException e) {
                throw new OCSRuntimeException("Cannot parse default date value: " + defaultValue);
            }
        } else {
            model.setDefaultValue(ClassUtils.instantiateClass(model.getType(), defaultValue));
        }
    }

    /**
     * Overwrite the values of the model with values read from the messageBundle
     * 
     * @param entityModel
     * @param model
     */
    private <T> void setMessageBundleOverrides(EntityModel<T> entityModel, AttributeModelImpl model) {

        String msg = getAttributeMessage(entityModel, model, EntityModel.DISPLAY_NAME);
        if (!StringUtils.isEmpty(msg)) {
            model.setDisplayName(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.DESCRIPTION);
        if (!StringUtils.isEmpty(msg)) {
            model.setDescription(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.DEFAULT_VALUE);
        if (!StringUtils.isEmpty(msg)) {
            setDefaultValue(model, msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.DISPLAY_FORMAT);
        if (!StringUtils.isEmpty(msg)) {
            model.setDisplayFormat(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.MAIN);
        if (!StringUtils.isEmpty(msg)) {
            model.setMainAttribute(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.PROMPT);
        if (!StringUtils.isEmpty(msg)) {
            model.setPrompt(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.READ_ONLY);
        if (!StringUtils.isEmpty(msg)) {
            model.setReadOnly(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.SEARCHABLE);
        if (!StringUtils.isEmpty(msg)) {
            model.setSearchable(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.SORTABLE);
        if (!StringUtils.isEmpty(msg)) {
            model.setSortable(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.VISIBLE);
        if (!StringUtils.isEmpty(msg)) {
            model.setVisible(isVisible(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.SHOW_IN_TABLE);
        if (!StringUtils.isEmpty(msg)) {
            model.setVisibleInTable(isVisible(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.COMPLEX_EDITABLE);
        if (!StringUtils.isEmpty(msg)) {
            model.setComplexEditable(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.IMAGE);
        if (!StringUtils.isEmpty(msg)) {
            model.setImage(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.WEEK);
        if (!StringUtils.isEmpty(msg)) {
            model.setWeek(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.ALLOWED_EXTENSIONS);
        if (!StringUtils.isEmpty(msg)) {
            String[] extensions = msg.split(",");
            Set<String> hashSet = Sets.newHashSet(extensions);
            model.setAllowedExtensions(hashSet);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.TRUE_REPRESENTATION);
        if (!StringUtils.isEmpty(msg)) {
            model.setTrueRepresentation(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.FALSE_REPRESENTATION);
        if (!StringUtils.isEmpty(msg)) {
            model.setFalseRepresentation(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.DETAIL_FOCUS);
        if (!StringUtils.isEmpty(msg)) {
            model.setDetailFocus(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.PERCENTAGE);
        if (!StringUtils.isEmpty(msg)) {
            model.setPercentage(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.PRECISION);
        if (!StringUtils.isEmpty(msg)) {
            model.setPercentage(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.EMBEDDED);
        if (!StringUtils.isEmpty(msg) && Boolean.valueOf(msg)) {
            model.setAttributeType(AttributeType.EMBEDDED);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.CURRENCY);
        if (!StringUtils.isEmpty(msg) && Boolean.valueOf(msg)) {
            model.setCurrency(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.SELECT_MODE);
        if (!StringUtils.isEmpty(msg) && AttributeSelectMode.valueOf(msg) != null) {
            model.setSelectMode(AttributeSelectMode.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.DATE_TYPE);
        if (!StringUtils.isEmpty(msg)) {
            model.setDateType(AttributeDateType.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_CASE_SENSITIVE);
        if (!StringUtils.isEmpty(msg)) {
            model.setSearchCaseSensitive(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_PREFIX_ONLY);
        if (!StringUtils.isEmpty(msg)) {
            model.setSearchPrefixOnly(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.TEXTFIELD_MODE);
        if (!StringUtils.isEmpty(msg)) {
            model.setTextFieldMode(AttributeTextFieldMode.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.MIN_LENGTH);
        if (!StringUtils.isEmpty(msg)) {
            model.setMinLength(Integer.parseInt(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.MAX_LENGTH);
        if (!StringUtils.isEmpty(msg)) {
            model.setMaxLength(Integer.parseInt(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.URL);
        if (!StringUtils.isEmpty(msg)) {
            model.setUrl(Boolean.valueOf(msg));
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.REPLACEMENT_SEARCH_PATH);
        if (!StringUtils.isEmpty(msg)) {
            model.setReplacementSearchPath(msg);
        }

        msg = getAttributeMessage(entityModel, model, EntityModel.QUICK_ADD_PROPERTY);
        if (!StringUtils.isEmpty(msg)) {
            model.setQuickAddPropertyName(msg);
        }
    }

    /**
     * Calculates the entity model for a nested property, recursively up till a certain depth
     * 
     * @param model
     *            the attribute model
     * @param entityClass
     *            the entity class
     */
    private void setNestedEntityModel(AttributeModelImpl model) {
        EntityModel<?> em = model.getEntityModel();
        if (StringUtils.countMatches(em.getReference(), ".") < RECURSIVE_MODEL_DEPTH) {
            Class<?> type = null;

            // only needed for master and detail attributes
            if (AttributeType.MASTER.equals(model.getAttributeType())) {
                type = model.getType();
            } else if (AttributeType.DETAIL.equals(model.getAttributeType())) {
                type = model.getMemberType();
            }

            if (type != null) {
                String ref = null;
                if (StringUtils.isEmpty(em.getReference())) {
                    ref = em.getEntityClass() + "." + model.getName();
                } else {
                    ref = em.getReference() + "." + model.getName();
                }

                if (type.equals(em.getEntityClass()) || !hasEntityModel(type, ref)) {
                    EntityModel<?> nem = getModel(ref, type);
                    model.setNestedEntityModel(nem);
                }
            }
        }
    }

    private <T> void setSortOrder(EntityModelImpl<T> model, String sortOrderMsg) {
        if (!StringUtils.isEmpty(sortOrderMsg)) {
            String[] tokens = sortOrderMsg.split(",");
            for (String token : tokens) {
                String[] sd = token.trim().split(" ");
                if (sd.length > 0 && !StringUtils.isEmpty(sd[0])
                        && model.getAttributeModel(sd[0]) != null) {
                    model.getSortOrder().put(
                            model.getAttributeModel(sd[0]),
                            (sd.length > 1 && ("DESC".equalsIgnoreCase(sd[1]) || "DSC"
                                    .equalsIgnoreCase(sd[1]))) ? false : true);
                }
            }
        }
    }

    /**
     * Indicates whether to skip an attribute since it does not constitute an actual property but
     * rather a generic or technical field that all entities have
     * 
     * @param name
     * @return
     */
    private boolean skipAttribute(String name) {
        return CLASS.equals(name) || VERSION.equals(name);
    }

    /**
     * Translates a TemporalType enum value to an AttributeDateType
     * 
     * @param type
     * @return
     */
    private AttributeDateType translateDateType(TemporalType type) {
        switch (type) {
        case DATE:
            return AttributeDateType.DATE;
        case TIME:
            return AttributeDateType.TIME;
        case TIMESTAMP:
            return AttributeDateType.TIMESTAMP;
        default:
            return null;
        }
    }

    private boolean isVisible(String msg) {
        try {
            VisibilityType other = VisibilityType.valueOf(msg);
            return VisibilityType.SHOW.equals(other);
        } catch (IllegalArgumentException ex) {
            // do nothing
        }
        return Boolean.valueOf(msg);
    }
}
