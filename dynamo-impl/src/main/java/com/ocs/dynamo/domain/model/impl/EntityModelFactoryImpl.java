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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CascadeMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.VisibilityType;
import com.ocs.dynamo.domain.model.annotation.Attribute;
import com.ocs.dynamo.domain.model.annotation.AttributeGroup;
import com.ocs.dynamo.domain.model.annotation.AttributeGroups;
import com.ocs.dynamo.domain.model.annotation.AttributeOrder;
import com.ocs.dynamo.domain.model.annotation.Cascade;
import com.ocs.dynamo.domain.model.annotation.CustomSetting;
import com.ocs.dynamo.domain.model.annotation.CustomType;
import com.ocs.dynamo.domain.model.annotation.Model;
import com.ocs.dynamo.domain.model.annotation.SearchMode;
import com.ocs.dynamo.domain.model.validator.Email;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.DateUtils;

/**
 * Implementation of the entity model factory - creates models that hold
 * metadata about an entity
 *
 * @author bas.rutten
 */

public class EntityModelFactoryImpl implements EntityModelFactory, EntityModelConstruct {

	private static final String PLURAL_POSTFIX = "s";

	private static final String CLASS = "class";

	private static final String VERSION = "version";

	private static final int RECURSIVE_MODEL_DEPTH = 3;

	private static final Logger LOG = LoggerFactory.getLogger(EntityModelFactoryImpl.class);

	@Autowired(required = false)
	private MessageService messageService;

	private ConcurrentMap<String, EntityModel<?>> cache = new ConcurrentHashMap<>();

	private ConcurrentMap<String, Class<?>> alreadyProcessed = new ConcurrentHashMap<>();

	private EntityModelFactory[] delegatedModelFactories;

	/**
	 * Default constructor without the use of delegated model factories
	 */
	public EntityModelFactoryImpl() {
	}

	/**
	 * Use this constructor when one needs to delegate creation of models to other
	 * model factories
	 *
	 * @param delegatedModelFactories
	 */
	public EntityModelFactoryImpl(EntityModelFactory... delegatedModelFactories) {
		super();
		this.delegatedModelFactories = delegatedModelFactories;
	}

	/**
	 * Indicates whether this factory can provide the model for the specified
	 * combination of reference and entity class
	 * 
	 * @param reference the reference
	 * @pram entityClass the entity class
	 */
	@Override
	public <T> boolean canProvideModel(String reference, Class<T> entityClass) {
		return true;
	}

	/**
	 * Check that the"week" setting is only allowed for java.time.LocalDate
	 *
	 * @param model the attribute model
	 */
	protected void checkWeekSettingAllowed(AttributeModel model) {
		if (!LocalDate.class.equals(model.getType())) {
			throw new OCSRuntimeException("'Week' setting only allowed for attributes of type LocalDate");
		}
	}

	/**
	 * Constructs an attribute model for a property
	 *
	 * @param descriptor  the property descriptor
	 * @param entityModel the entity model
	 * @param parentClass the type of the direct parent of the attribute (relevant
	 *                    in case of embedded attributes)
	 * @param nested      whether this is a nested attribute
	 * @param prefix      the prefix to apply to the attribute name
	 * @return
	 */
	protected <T> List<AttributeModel> constructAttributeModel(PropertyDescriptor descriptor,
			EntityModelImpl<T> entityModel, Class<?> parentClass, boolean nested, String prefix) {
		List<AttributeModel> result = new ArrayList<>();

		// validation methods annotated with @AssertTrue or @AssertFalse have to
		// be ignored
		String fieldName = descriptor.getName();
		Class<?> pClass = parentClass != null ? parentClass : entityModel.getEntityClass();
		AssertTrue assertTrue = ClassUtils.getAnnotation(pClass, fieldName, AssertTrue.class);
		AssertFalse assertFalse = ClassUtils.getAnnotation(pClass, fieldName, AssertFalse.class);

		if (assertTrue == null && assertFalse == null) {

			AttributeModelImpl model = new AttributeModelImpl();
			model.setEntityModel(entityModel);

			String displayName = com.ocs.dynamo.utils.StringUtils.propertyIdToHumanFriendly(fieldName,
					SystemPropertyUtils.isCapitalizeWords());

			// first, set the defaults
			model.setDefaultDisplayName(displayName);
			model.setDefaultDescription(displayName);
			model.setDefaultPrompt(displayName);

			model.setMainAttribute(descriptor.isPreferred());
			model.setSearchMode(SearchMode.NONE);
			model.setName((prefix == null ? "" : (prefix + ".")) + fieldName);
			model.setImage(false);

			model.setEditableType(descriptor.isHidden() ? EditableType.READ_ONLY : EditableType.EDITABLE);
			model.setSortable(true);
			model.setComplexEditable(false);
			model.setPrecision(SystemPropertyUtils.getDefaultDecimalPrecision());
			model.setSearchCaseSensitive(SystemPropertyUtils.getDefaultSearchCaseSensitive());
			model.setSearchPrefixOnly(SystemPropertyUtils.getDefaultSearchPrefixOnly());
			model.setUrl(false);
			model.setThousandsGrouping(true);

			Id idAttr = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Id.class);
			if (idAttr != null) {
				entityModel.setIdAttributeModel(model);
				// the ID column is hidden. details collections are also hidden
				// by default
				model.setVisible(false);
			} else {
				model.setVisible(true);
			}
			model.setType(descriptor.getPropertyType());

			// determine the possible date type
			model.setDateType(determineDateType(model.getType(), entityModel.getEntityClass(), fieldName));

			// determine default display format
			model.setDisplayFormat(determineDefaultDisplayFormat(model.getType(), model.getDateType()));

			// determine if the attribute is required based on the @NotNull
			// annotation
			NotNull notNull = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, NotNull.class);
			model.setRequired(notNull != null);

			model.setAttributeType(determineAttributeType(parentClass, model));

			// minimum and maximum length based on the @Size annotation
			Size size = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Size.class);
			if (AttributeType.BASIC.equals(model.getAttributeType()) && size != null) {
				model.setMaxLength(size.max());
				model.setMinLength(size.min());
			}

			setNestedEntityModel(model);

			// only basic attributes are shown in the table by default
			model.setVisibleInGrid(
					!nested && model.isVisible() && (AttributeType.BASIC.equals(model.getAttributeType())));

			if (getMessageService() != null) {
				model.setDefaultTrueRepresentation(SystemPropertyUtils.getDefaultTrueRepresentation());
				model.setDefaultFalseRepresentation(SystemPropertyUtils.getDefaultFalseRepresentation());
			}

			// by default, use a combo box to look up
			model.setSelectMode(AttributeSelectMode.COMBO);
			model.setTextFieldMode(AttributeTextFieldMode.TEXTFIELD);
			model.setSearchSelectMode(AttributeSelectMode.COMBO);
			model.setGridSelectMode(AttributeSelectMode.COMBO);

			// is the field an email field?
			Email email = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Email.class);
			if (email != null) {
				model.setEmail(true);
			}

			// override the defaults with annotation values
			setAnnotationOverrides(parentClass, model, descriptor, nested);

			// override any earlier version with message bundle contents
			setMessageBundleOverrides(entityModel, model);

			if (!model.isEmbedded()) {
				result.add(model);
			} else {
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

			// multiple select fields not allowed for all attribute types
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

			// navigating only allowed in case of a many to one relation
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
		}
		return result;
	}

	/**
	 * Constructs a model
	 *
	 * @param entityClass the entity class
	 * @param reference   the unique reference
	 * @param entityModel the base (?) entity model
	 * @return
	 */
	protected <T> EntityModelImpl<T> constructModel(Class<T> entityClass, String reference,
			EntityModelImpl<T> entityModel) {

		String displayName = com.ocs.dynamo.utils.StringUtils.propertyIdToHumanFriendly(entityClass.getSimpleName(),
				SystemPropertyUtils.isCapitalizeWords());
		String displayNamePlural = displayName + PLURAL_POSTFIX;
		String description = displayName;
		String displayProperty = null;

		Model annot = entityClass.getAnnotation(Model.class);
		if (annot != null) {

			if (!StringUtils.isEmpty(annot.displayName())) {
				displayName = annot.displayName();
				// set description to display name by default
				description = annot.displayName();
			}
			if (!StringUtils.isEmpty(annot.displayNamePlural())) {
				displayNamePlural = annot.displayNamePlural();
			}
			if (!StringUtils.isEmpty(annot.description())) {
				description = annot.description();
			}
			if (!StringUtils.isEmpty(annot.displayProperty())) {
				displayProperty = annot.displayProperty();
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

		String displayPropertyMsg = getEntityMessage(reference, EntityModel.DISPLAY_PROPERTY);
		if (!StringUtils.isEmpty(displayPropertyMsg)) {
			displayProperty = displayPropertyMsg;
		}

		entityModel.setEntityClass(entityClass);
		entityModel.setReference(reference);

		entityModel.setDefaultDisplayName(displayName);
		entityModel.setDefaultDisplayNamePlural(displayNamePlural);
		entityModel.setDefaultDescription(description);
		entityModel.setDisplayProperty(displayProperty);

		Map<String, String> attributeGroupMap = determineAttributeGroupMapping(entityModel, entityClass);
		entityModel.addAttributeGroup(EntityModel.DEFAULT_GROUP);

		alreadyProcessed.put(reference, entityClass);

		// keep track of main attributes - if no main attribute is defined, mark either
		// the first string attribute or
		// the first searchable attribute as the main
		boolean mainAttributeFound = false;
		AttributeModel firstStringAttribute = null;
		AttributeModel firstSearchableAttribute = null;
		boolean nested = reference.indexOf('.') >= 0;

		PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(entityClass);
		// create attribute models for all attributes
		List<AttributeModel> tempModelList = new ArrayList<>();
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
			entityModel.addAttributeModel(group, attributeModel);
		}

		Set<String> alreadyUsed = new HashSet<>();
		// check if there aren't any illegal "group together" settings
		for (AttributeModel m : entityModel.getAttributeModels()) {
			alreadyUsed.add(m.getName());
			if (!m.getGroupTogetherWith().isEmpty()) {
				for (String together : m.getGroupTogetherWith()) {
					if (alreadyUsed.contains(together)) {
						AttributeModel other = entityModel.getAttributeModel(together);
						if (together != null) {
							((AttributeModelImpl) other).setAlreadyGrouped(true);
							LOG.warn("Incorrect groupTogetherWith found: {} refers to {} ", m.getName(), together);
						}
					}
				}
			}
		}

		// set main attribute
		if (!mainAttributeFound && !nested) {
			if (firstStringAttribute != null) {
				firstStringAttribute.setMainAttribute(true);
			} else if (firstSearchableAttribute != null) {
				firstSearchableAttribute.setMainAttribute(true);
			}
		}

		String sortOrder = null;
		annot = entityClass.getAnnotation(Model.class);
		if (annot != null && !StringUtils.isEmpty(annot.sortOrder())) {
			sortOrder = annot.sortOrder();
		}

		String sortOrderMsg = getEntityMessage(reference, EntityModel.SORT_ORDER);
		if (!StringUtils.isEmpty(sortOrderMsg)) {
			sortOrder = sortOrderMsg;
		}
		setSortOrder(entityModel, sortOrder);
		cache.put(reference, entityModel);

		return entityModel;
	}

	/**
	 * Constructs the model for an entity
	 *
	 * @param entityClass the class of the entity
	 * @return
	 */
	protected synchronized <T> EntityModel<T> constructModel(String reference, Class<T> entityClass) {

		// Delegate to other factories first
		EntityModelImpl<T> entityModel = null;
		if (delegatedModelFactories != null) {
			for (EntityModelFactory demf : delegatedModelFactories) {
				if (demf.canProvideModel(reference, entityClass)) {
					entityModel = (EntityModelImpl<T>) demf.getModel(reference, entityClass);
					if (entityModel != null) {
						break;
					}
				}
			}
		}
		// construct the basic model
		if (entityModel == null) {
			entityModel = new EntityModelImpl<>();
		}
		entityModel = constructModel(entityClass, reference, entityModel);

		return entityModel;
	}

	@Override
	public EntityModel<?> constructNestedEntityModel(EntityModelFactory master, Class<?> type, String reference) {
		return new LazyEntityModelWrapper<>(master, reference, type);
	}

	/**
	 * Constructs the attribute group mapping - from attribute name to the group it
	 * belongs to
	 *
	 * @param model       the entity model
	 * @param entityClass the entity class
	 * @return
	 */
	protected <T> Map<String, String> determineAttributeGroupMapping(EntityModel<T> model, Class<T> entityClass) {
		Map<String, String> result = new HashMap<>();
		AttributeGroups groups = entityClass.getAnnotation(AttributeGroups.class);

		if (groups != null) {
			// multiple attribute groups
			for (AttributeGroup g : groups.value()) {
				model.addAttributeGroup(g.messageKey());
				for (String s : g.attributeNames()) {
					result.put(s, g.messageKey());
				}
			}
		} else {
			// just a single group
			AttributeGroup group = entityClass.getAnnotation(AttributeGroup.class);
			if (group != null) {
				model.addAttributeGroup(group.messageKey());
				for (String s : group.attributeNames()) {
					result.put(s, group.messageKey());
				}
			}
		}

		// look for message bundle overwrites
		int i = 1;
		if (messageService != null) {
			String groupName = messageService.getEntityMessage(model.getReference(),
					EntityModel.ATTRIBUTE_GROUP + "." + i + "." + EntityModel.MESSAGE_KEY, getLocale());

			if (groupName != null) {
				result.clear();
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
	 * Determines the order of the attributes - this will first pick up any
	 * attributes that are mentioned in the @AttributeOrder annotation (in the order
	 * in which they occur) and then add any attributes that are not explicitly
	 * mentioned
	 *
	 * @param entityClass
	 * @param reference
	 * @param attributeModels
	 * @return
	 */
	protected <T> void determineAttributeOrder(Class<T> entityClass, String reference,
			List<AttributeModel> attributeModels) {

		List<String> explicitAttributeNames = new ArrayList<>();
		List<String> additionalNames = new ArrayList<>();

		// read ordering from the annotation (if present)
		AttributeOrder orderAnnot = entityClass.getAnnotation(AttributeOrder.class);
		if (orderAnnot != null) {
			explicitAttributeNames = Arrays.asList(orderAnnot.attributeNames());
		}

		// overwrite by message bundle (if present)
		String msg = messageService == null ? null
				: messageService.getEntityMessage(reference, EntityModel.ATTRIBUTE_ORDER, getLocale());
		if (msg != null) {
			explicitAttributeNames = Arrays.asList(msg.replaceAll("\\s+", "").split(","));
		}

		for (AttributeModel am : attributeModels) {
			String name = am.getName();
			if (!skipAttribute(name) && !explicitAttributeNames.contains(name)) {
				additionalNames.add(name);
			}
		}

		// add the explicitly named attributes
		List<String> result = new ArrayList<>(explicitAttributeNames);
		// add the additional unmentioned attributes
		result.addAll(additionalNames);

		// loop over the attributes and set the orders
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
	 * @param model the model representation of the attribute
	 * @return
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
	 * Determines the "dateType" for an attribute
	 *
	 * @param modelType   the type of the attribute. Can be a java 8 LocalX type
	 * @param entityClass the class of the entity
	 * @param fieldName   the name of the attribute
	 * @return
	 */
	protected <T> AttributeDateType determineDateType(Class<?> modelType, Class<T> entityClass, String fieldName) {
		// set the date type
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
	 * Determines the default format to use for a date or time property
	 *
	 * @param type     the type of the property
	 * @param dateType the date type
	 * @return
	 */
	protected String determineDefaultDisplayFormat(Class<?> type, AttributeDateType dateType) {
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
	 * 
	 * @param reference
	 * @param entityClass
	 * @return
	 */
	protected <T> EntityModelFactory findModelFactory(String reference, Class<T> entityClass) {
		EntityModelFactory emf = this;
		if (delegatedModelFactories != null) {
			for (EntityModelFactory demf : delegatedModelFactories) {
				if (demf.canProvideModel(reference, entityClass)) {
					emf = demf;
					break;
				}
			}
		}
		return emf;
	}

	/**
	 * Retrieves a message relating to an attribute from the message bundle
	 *
	 * @param model          the entity model
	 * @param attributeModel the attribute model
	 * @param propertyName   the name of the property
	 * @return
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
	 * @param reference
	 * @param propertyName
	 * @return
	 */
	protected String getEntityMessage(String reference, String propertyName) {
		if (messageService != null) {
			return messageService.getEntityMessage(reference, propertyName, getLocale());
		}
		return null;
	}

	protected Locale getLocale() {
		return new Locale(SystemPropertyUtils.getDefaultLocale());
	}

	public MessageService getMessageService() {
		return messageService;
	}

	@Override
	public <T> EntityModel<T> getModel(Class<T> entityClass) {
		return getModel(entityClass.getSimpleName(), entityClass);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> EntityModel<T> getModel(String reference, Class<T> entityClass) {
		EntityModel<T> model = null;
		if (!StringUtils.isEmpty(reference) && entityClass != null) {
			model = (EntityModel<T>) cache.get(reference);
			if (model == null) {
				model = constructModel(reference, entityClass);
			}
		}
		return model;
	}

	/**
	 * Check if a certain entity model has already been processed
	 *
	 * @param type      the type of the entity
	 * @param reference the reference to the entity
	 * @return
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

	public boolean hasModel(String reference) {
		return cache.containsKey(reference);
	}

	/**
	 * Check whether a message contains a value that marks the attribute as
	 * "visible"
	 *
	 * @param msg the message
	 * @return
	 */
	protected boolean isVisible(String msg) {
		try {
			VisibilityType other = VisibilityType.valueOf(msg);
			return VisibilityType.SHOW.equals(other);
		} catch (IllegalArgumentException ex) {
			// do nothing
		}
		return Boolean.valueOf(msg);
	}

	/**
	 * Overwrite the default values with annotation values
	 *
	 * @param parentClass
	 * @param model
	 * @param descriptor
	 */
	protected void setAnnotationOverrides(Class<?> parentClass, AttributeModelImpl model, PropertyDescriptor descriptor,
			boolean nested) {
		Attribute attribute = ClassUtils.getAnnotation(parentClass, descriptor.getName(), Attribute.class);

		// overwrite with annotation values
		if (attribute != null) {
			if (!StringUtils.isEmpty(attribute.displayName())) {
				model.setDefaultDisplayName(attribute.displayName());
				model.setDefaultDescription(attribute.displayName());
				model.setDefaultPrompt(attribute.displayName());
			}

			if (!StringUtils.isEmpty(attribute.description())) {
				model.setDefaultDescription(attribute.description());
			}

			if (!StringUtils.isEmpty(attribute.prompt())) {
				model.setDefaultPrompt(attribute.prompt());
			}

			if (!StringUtils.isEmpty(attribute.displayFormat())) {
				model.setDisplayFormat(attribute.displayFormat());
			}

			model.setEditableType(attribute.editable());

			// set visibility (but not for nested attributes - these are hidden
			// by default)
			if (attribute.visible() != null && !VisibilityType.INHERIT.equals(attribute.visible()) && !nested) {
				model.setVisible(VisibilityType.SHOW.equals(attribute.visible()));
				model.setVisibleInGrid(model.isVisible());
			}

			if ((SearchMode.ADVANCED.equals(attribute.searchable()) || SearchMode.ALWAYS.equals(attribute.searchable()))
					&& !nested) {
				model.setSearchMode(attribute.searchable());
			}

			if (attribute.requiredForSearching() && !nested) {
				model.setRequiredForSearching(true);
			}

			if (!attribute.sortable()) {
				model.setSortable(false);
			}

			if (attribute.main() && !nested) {
				model.setMainAttribute(true);
			}

			if (attribute.visibleInGrid() != null && !VisibilityType.INHERIT.equals(attribute.visibleInGrid())
					&& !nested) {
				model.setVisibleInGrid(VisibilityType.SHOW.equals(attribute.visibleInGrid()));
			}

			if (attribute.complexEditable()) {
				model.setComplexEditable(true);
			}

			if (attribute.image()) {
				model.setImage(true);
			}

			if (attribute.week()) {
				checkWeekSettingAllowed(model);
				model.setWeek(true);
			}

			if (attribute.allowedExtensions() != null && attribute.allowedExtensions().length > 0) {
				Set<String> set = Arrays.stream(attribute.allowedExtensions()).map(x -> x.toLowerCase())
						.collect(Collectors.toSet());
				model.setAllowedExtensions(set);
			}

			if (attribute.cascade() != null && attribute.cascade().length > 0) {
				for (Cascade cc : attribute.cascade()) {
					model.addCascade(cc.cascadeTo(), cc.filterPath(), cc.mode());
				}
			}

			// custom settings
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

			if (attribute.groupTogetherWith() != null && attribute.groupTogetherWith().length > 0) {
				for (String s : attribute.groupTogetherWith()) {
					model.addGroupTogetherWith(s);
				}
			}

			if (!StringUtils.isEmpty(attribute.trueRepresentation())) {
				model.setDefaultTrueRepresentation(attribute.trueRepresentation());
			}

			if (!StringUtils.isEmpty(attribute.falseRepresentation())) {
				model.setDefaultFalseRepresentation(attribute.falseRepresentation());
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

			if (attribute.dateType() != null && !AttributeDateType.INHERIT.equals(attribute.dateType())) {
				model.setDateType(attribute.dateType());
			}

			// overrule select mode - by default, this overrules the search
			// select mode as well
			if (attribute.selectMode() != null && !AttributeSelectMode.INHERIT.equals(attribute.selectMode())) {
				model.setSelectMode(attribute.selectMode());
				model.setSearchSelectMode(attribute.selectMode());
				model.setGridSelectMode(attribute.selectMode());
			}

			// set multiple search
			if (attribute.multipleSearch()) {
				model.setMultipleSearch(true);
				// by default, use a token for multiple select
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

			if (!AttributeSelectMode.INHERIT.equals(attribute.gridSelectMode())) {
				model.setGridSelectMode(attribute.gridSelectMode());
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

			if (attribute.minValue() > Long.MIN_VALUE) {
				model.setMinValue(attribute.minValue());
			}

			if (attribute.maxLength() > -1) {
				model.setMaxLength(attribute.maxLength());
			}

			if (attribute.maxLengthInGrid() > -1) {
				model.setMaxLengthInGrid(attribute.maxLengthInGrid());
			}

			if (attribute.maxValue() < Long.MAX_VALUE) {
				model.setMaxValue(attribute.maxValue());
			}

			if (attribute.url()) {
				model.setUrl(true);
			}

			if (!StringUtils.isEmpty(attribute.replacementSearchPath())) {
				model.setReplacementSearchPath(attribute.replacementSearchPath());
			}

			if (!StringUtils.isEmpty(attribute.replacementSortPath())) {
				model.setReplacementSortPath(attribute.replacementSortPath());
			}

			if (!StringUtils.isEmpty(attribute.quickAddPropertyName())) {
				model.setQuickAddPropertyName(attribute.quickAddPropertyName());
				model.setQuickAddAllowed(true);
			}

			if (attribute.required()) {
				model.setRequired(true);
			}

			if (!attribute.thousandsGrouping()) {
				model.setThousandsGrouping(false);
			}

			if (attribute.searchForExactValue()) {
				model.setSearchForExactValue(true);
			}

			if (!StringUtils.isEmpty(attribute.fileNameProperty())) {
				model.setFileNameProperty(attribute.fileNameProperty());
			}

			if (!StringUtils.isEmpty(attribute.defaultValue())) {
				if (!AttributeType.BASIC.equals(model.getAttributeType())) {
					throw new OCSRuntimeException(
							model.getName() + ": setting a default value is only allowed for BASIC attributes");
				}

				String defaultValue = attribute.defaultValue();
				setDefaultValue(model, defaultValue);
			}

			if (!StringUtils.isEmpty(attribute.styles())) {
				model.setStyles(attribute.styles());
			}

			model.setNavigable(attribute.navigable());

			if (!StringUtils.isEmpty(attribute.styles())) {
				model.setStyles(attribute.styles());
			}

			model.setIgnoreInSearchFilter(attribute.ignoreInSearchFilter());
			model.setSearchDateOnly(attribute.searchDateOnly());
		}
	}

	/**
	 * Sets the default value on the attribute model (translates a String to the
	 * appropriate type)
	 *
	 * @param model        the attribute model
	 * @param defaultValue the default value to set
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setDefaultValue(AttributeModelImpl model, String defaultValue) {
		if (model.getType().isEnum()) {
			Class<? extends Enum> enumType = model.getType().asSubclass(Enum.class);
			model.setDefaultValue(Enum.valueOf(enumType, defaultValue));
		}
		if (DateUtils.isJava8DateType(model.getType())) {
			Object o = DateUtils.createJava8Date(model.getType(), defaultValue, model.getDisplayFormat());
			model.setDefaultValue(o);
		} else {
			model.setDefaultValue(ClassUtils.instantiateClass(model.getType(), defaultValue));
		}
	}

	/**
	 * Reads cascade data for an attribute from the message bundle
	 *
	 * @param entityModel the entity model
	 * @param model       the attribute model
	 */
	private void setMessageBundleCascadeOverrides(EntityModel<?> entityModel, AttributeModel model) {
		String msg = getAttributeMessage(entityModel, model, EntityModel.CASCADE_OFF);
		if (msg != null) {
			// complete cancel all cascades for this attribute
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
	 * Adds custom setting overrides. These take the form of "custom.1", "customValue.1" and "customType.1"
	 * 
	 * @param entityModel
	 * @param model
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
	 * Overwrite the values of the model with values read from the messageBundle
	 *
	 * @param entityModel the entity model
	 * @param model       the attribute model implementation
	 */
	private <T> void setMessageBundleOverrides(EntityModel<T> entityModel, AttributeModelImpl model) {

		String msg = getAttributeMessage(entityModel, model, EntityModel.DISPLAY_NAME);
		if (!StringUtils.isEmpty(msg)) {
			model.setDefaultDisplayName(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.DESCRIPTION);
		if (!StringUtils.isEmpty(msg)) {
			model.setDefaultDescription(msg);
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

		// check for read only (convenience only, overwritten by "editable")
		msg = getAttributeMessage(entityModel, model, EntityModel.READ_ONLY);
		if (!StringUtils.isEmpty(msg)) {
			boolean edt = Boolean.parseBoolean(msg);
			if (edt) {
				model.setEditableType(EditableType.READ_ONLY);
			} else {
				model.setEditableType(EditableType.EDITABLE);
			}
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.EDITABLE);
		if (!StringUtils.isEmpty(msg)) {
			model.setEditableType(EditableType.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCHABLE);
		if (!StringUtils.isEmpty(msg)) {
			if ("true".equals(msg)) {
				model.setSearchMode(SearchMode.ALWAYS);
			} else if ("false".equals(msg)) {
				model.setSearchMode(SearchMode.NONE);
			} else {
				model.setSearchMode(SearchMode.valueOf(msg));
			}
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.REQUIRED_FOR_SEARCHING);
		if (!StringUtils.isEmpty(msg)) {
			model.setRequiredForSearching(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SORTABLE);
		if (!StringUtils.isEmpty(msg)) {
			model.setSortable(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.VISIBLE);
		if (!StringUtils.isEmpty(msg)) {
			model.setVisible(isVisible(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.VISIBLE_IN_GRID);
		if (!StringUtils.isEmpty(msg)) {
			model.setVisibleInGrid(isVisible(msg));
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
			checkWeekSettingAllowed(model);
			model.setWeek(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.ALLOWED_EXTENSIONS);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			String[] extensions = msg.split(",");
			Set<String> hashSet = Sets.newHashSet(extensions);
			model.setAllowedExtensions(hashSet);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.GROUP_TOGETHER_WITH);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			String[] extensions = msg.split(",");
			for (String s : extensions) {
				model.addGroupTogetherWith(s);
			}
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.TRUE_REPRESENTATION);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setDefaultTrueRepresentation(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.FALSE_REPRESENTATION);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setDefaultFalseRepresentation(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.PERCENTAGE);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setPercentage(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.PRECISION);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setPercentage(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.EMBEDDED);
		if (msg != null && !StringUtils.isEmpty(msg) && Boolean.valueOf(msg)) {
			model.setAttributeType(AttributeType.EMBEDDED);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.CURRENCY);
		if (msg != null && !StringUtils.isEmpty(msg) && Boolean.valueOf(msg)) {
			model.setCurrency(Boolean.valueOf(msg));
		}

		// set multiple search (also overwrites the search select mode and sets
		// it to fancy list)
		msg = getAttributeMessage(entityModel, model, EntityModel.MULTIPLE_SEARCH);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setMultipleSearch(Boolean.valueOf(msg));
			model.setSearchSelectMode(AttributeSelectMode.TOKEN);
		}

		// set the select mode (also sets the search select mode and grid select mode)
		msg = getAttributeMessage(entityModel, model, EntityModel.SELECT_MODE);
		if (msg != null && !StringUtils.isEmpty(msg) && AttributeSelectMode.valueOf(msg) != null) {
			AttributeSelectMode mode = AttributeSelectMode.valueOf(msg);
			model.setSelectMode(mode);
			model.setSearchSelectMode(mode);
			model.setGridSelectMode(mode);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_SELECT_MODE);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setSearchSelectMode(AttributeSelectMode.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.GRID_SELECT_MODE);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setGridSelectMode(AttributeSelectMode.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.DATE_TYPE);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setDateType(AttributeDateType.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_CASE_SENSITIVE);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setSearchCaseSensitive(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_PREFIX_ONLY);
		if (msg != null && !StringUtils.isEmpty(msg)) {
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

		msg = getAttributeMessage(entityModel, model, EntityModel.MIN_VALUE);
		if (!StringUtils.isEmpty(msg)) {
			model.setMinValue(Long.parseLong(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.MAX_LENGTH);
		if (!StringUtils.isEmpty(msg)) {
			model.setMaxLength(Integer.parseInt(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.MAX_LENGTH_IN_GRID);
		if (!StringUtils.isEmpty(msg)) {
			model.setMaxLengthInGrid(Integer.parseInt(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.MAX_VALUE);
		if (!StringUtils.isEmpty(msg)) {
			model.setMaxValue(Long.parseLong(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.URL);
		if (!StringUtils.isEmpty(msg)) {
			model.setUrl(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.REPLACEMENT_SEARCH_PATH);
		if (!StringUtils.isEmpty(msg)) {
			model.setReplacementSearchPath(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.REPLACEMENT_SORT_PATH);
		if (!StringUtils.isEmpty(msg)) {
			model.setReplacementSortPath(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.QUICK_ADD_PROPERTY);
		if (!StringUtils.isEmpty(msg)) {
			model.setQuickAddPropertyName(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.THOUSANDS_GROUPING);
		if (!StringUtils.isEmpty(msg)) {
			model.setThousandsGrouping(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_EXACT_VALUE);
		if (!StringUtils.isEmpty(msg)) {
			model.setSearchForExactValue(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.STYLES);
		if (msg != null && !StringUtils.isEmpty(msg)) {
			model.setStyles(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.NAVIGABLE);
		if (!StringUtils.isEmpty(msg)) {
			model.setNavigable(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.STYLES);
		if (!StringUtils.isEmpty(msg)) {
			model.setStyles(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_DATE_ONLY);
		if (!StringUtils.isEmpty(msg)) {
			model.setSearchDateOnly(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.IGNORE_IN_SEARCH_FILTER);
		if (!StringUtils.isEmpty(msg)) {
			model.setIgnoreInSearchFilter(Boolean.valueOf(msg));
		}

		setMessageBundleCascadeOverrides(entityModel, model);
		setMessageBundleCustomOverrides(entityModel, model);
	}

	/**
	 * Calculates the entity model for a nested property, recursively up until a
	 * certain depth
	 *
	 * @param model the attribute model
	 */
	protected void setNestedEntityModel(AttributeModelImpl model) {
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
					model.setNestedEntityModel(((EntityModelConstruct) findModelFactory(ref, type))
							.constructNestedEntityModel(this, type, ref));
				}
			}
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
							(sd.length > 1 && ("DESC".equalsIgnoreCase(sd[1]) || "DSC".equalsIgnoreCase(sd[1]))) ? false
									: true);
				}
			}
		}
	}

	/**
	 * Indicates whether to skip an attribute since it does not constitute an actual
	 * property but rather a generic or technical field that all entities have
	 *
	 * @param name the name of the attribute
	 * @return
	 */
	protected boolean skipAttribute(String name) {
		return CLASS.equals(name) || VERSION.equals(name);
	}
}
