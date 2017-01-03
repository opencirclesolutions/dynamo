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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
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
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.model.LazyEntityModelWrapper;
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
import com.ocs.dynamo.utils.SystemPropertyUtils;
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

	private static final Logger LOG = Logger.getLogger(EntityModelFactoryImpl.class);

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
		String fieldName = descriptor.getName();
		AssertTrue assertTrue = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, AssertTrue.class);
		AssertFalse assertFalse = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, AssertFalse.class);

		if (assertTrue == null && assertFalse == null) {

			AttributeModelImpl model = new AttributeModelImpl();
			model.setEntityModel(entityModel);

			String displayName = DefaultFieldFactory.createCaptionByPropertyId(fieldName);

			// first, set the defaults
			model.setDisplayName(displayName);
			model.setDescription(displayName);
			model.setPrompt(displayName);
			model.setMainAttribute(descriptor.isPreferred());
			model.setSearchable(descriptor.isPreferred());
			model.setName((prefix == null ? "" : (prefix + ".")) + fieldName);
			model.setImage(false);

			model.setReadOnly(descriptor.isHidden());
			model.setSortable(true);
			model.setComplexEditable(false);
			model.setPrecision(SystemPropertyUtils.getDefaultDecimalPrecision());
			model.setSearchCaseSensitive(false);
			model.setSearchPrefixOnly(false);
			model.setUrl(false);
			model.setUseThousandsGrouping(true);

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
			model.setVisibleInTable(!nested && model.isVisible()
			        && (AttributeType.BASIC.equals(model.getAttributeType())));

			if (getMessageService() != null) {
				model.setTrueRepresentation(getMessageService().getMessage("ocs.true"));
				model.setFalseRepresentation(getMessageService().getMessage("ocs.false"));
			}

			// by default, use a combo box to look up
			model.setSelectMode(AttributeSelectMode.COMBO);
			model.setTextFieldMode(AttributeTextFieldMode.TEXTFIELD);
			model.setSearchSelectMode(AttributeSelectMode.COMBO);

			// is the field an email field?
			Email email = ClassUtils.getAnnotation(entityModel.getEntityClass(), fieldName, Email.class);
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

			// exception if using a multiple select field type for a single select field
			if ((AttributeSelectMode.TOKEN.equals(model.getSelectMode()) || AttributeSelectMode.FANCY_LIST.equals(model
			        .getSelectMode()))
			        && !AttributeType.DETAIL.equals(model.getAttributeType())
			        && !AttributeType.BASIC.equals(model.getAttributeType())) {
				throw new OCSRuntimeException("Token or Fancy List field not allowed for field " + model.getName());
			}

			// searching on a LOB is pointless
			if (AttributeType.LOB.equals(model.getAttributeType()) && model.isSearchable()) {
				throw new OCSRuntimeException("Searching on a LOB is not allowed for field " + model.getName());
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
	@SuppressWarnings("unchecked")
	private synchronized <T> EntityModel<T> constructModel(String reference, Class<T> entityClass) {
		EntityModel<T> result = (EntityModel<T>) cache.get(reference);
		if (result == null) {
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

				model.addAttributeModel(group, attributeModel);
			}

			Set<String> already = new HashSet<>();
			// check if there aren't any illegal "group together" settings
			for (AttributeModel m : model.getAttributeModels()) {
				already.add(m.getName());
				if (!m.getGroupTogetherWith().isEmpty()) {
					for (String together : m.getGroupTogetherWith()) {
						if (already.contains(together)) {
							AttributeModel other = model.getAttributeModel(together);
							if (together != null) {
								((AttributeModelImpl) other).setAlreadyGrouped(true);
								LOG.warn("Incorrect groupTogetherWith found: " + m.getName() + " refers to " + together);
							}
						}
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
			cache.put(reference, model);
			result = model;
		}
		return result;
	}

	/**
	 * @param entityClass
	 * @param reference
	 * @return
	 */
	private <T> EntityModelImpl<T> constructModelInner(Class<T> entityClass, String reference) {

		String displayName = DefaultFieldFactory.createCaptionByPropertyId(entityClass.getSimpleName());
		String displayNamePlural = displayName + PLURAL_POSTFIX;
		String description = displayName;
		String selectDisplayProperty = null;

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

		return new EntityModelImpl<>(entityClass, reference, displayName, displayNamePlural, description,
		        selectDisplayProperty);
	}

	/**
	 * Construct a mapping from attribute to its corresponding attribute group
	 * 
	 * @param entityClass
	 * @return
	 */
	private <T> Map<String, String> determineAttributeGroupMapping(EntityModelImpl<T> model, Class<T> entityClass) {
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
			String groupName = messageService.getEntityMessage(model.getReference(), EntityModel.ATTRIBUTE_GROUP + "."
			        + i + "." + EntityModel.DISPLAY_NAME);

			if (groupName != null) {
				result.clear();
			}

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
				groupName = messageService.getEntityMessage(model.getReference(), EntityModel.ATTRIBUTE_GROUP + "." + i
				        + "." + EntityModel.DISPLAY_NAME);
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
	 * @param reference
	 * @param attributeModels
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

				if (attribute != null && attribute.memberType() != null && !attribute.memberType().equals(Object.class)) {
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

	private <T> AttributeDateType determineDateType(Class<?> modelType, Class<T> entityClass, String fieldName) {
		// set the date type
		if (Date.class.equals(modelType)) {
			Temporal temporal = ClassUtils.getAnnotation(entityClass, fieldName, Temporal.class);

			Attribute attribute = ClassUtils.getAnnotation(entityClass, fieldName, Attribute.class);

			final boolean customAttributeDateTypeSet = attribute != null
			        && attribute.dateType() != AttributeDateType.INHERIT;
			if (temporal != null && !customAttributeDateTypeSet) {
				// use the @Temporal annotation when available and not overridden by Attribute
				return translateDateType(temporal.value());
			} else {
				if (customAttributeDateTypeSet) {
					return attribute.dateType();
				}

				// by default use date
				return AttributeDateType.DATE;
			}
		}
		return null;
	}

	/**
	 * Determines the display format on a property
	 * 
	 * @param type
	 *            the java type
	 * @param entityClass
	 *            the class on which the property is defined
	 * @param fieldName
	 * @param dateType
	 * @return
	 */
	private String determineDefaultDisplayFormat(Class<?> type, AttributeDateType dateType) {
		String format = null;
		if (Date.class.isAssignableFrom(type)) {
			switch (dateType) {
			case TIME:
				format = SystemPropertyUtils.getDefaultTimeFormat();
				break;
			case TIMESTAMP:
				format = SystemPropertyUtils.getDefaultDateTimeFormat();
				break;
			default:
				// by default use a date
				format = SystemPropertyUtils.getDefaultDateFormat();
			}
		}
		return format;
	}

	/**
	 * Retrieves a message relating to an attribute from the message bundle
	 * 
	 * @param model
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @param propertyName
	 *            the name of the property
	 * @return
	 */
	private <T> String getAttributeMessage(EntityModel<T> model, AttributeModel attributeModel, String propertyName) {
		if (messageService != null) {
			return messageService.getAttributeMessage(model.getReference(), attributeModel, propertyName);
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
	 * @param type
	 *            the type of the entity
	 * @param reference
	 *            the reference to the entity
	 * @return
	 */
	private boolean hasEntityModel(Class<?> type, String reference) {
		for (Entry<String, Class<?>> e : alreadyProcessed.entrySet()) {
			if (reference.equals(e.getKey()) && e.getValue().equals(type)) {
				// only check for starting reference in order to prevent recursive looping between
				// two-sided relations
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
	 * @param parentClass
	 * @param model
	 * @param descriptor
	 */
	private void setAnnotationOverrides(Class<?> parentClass, AttributeModelImpl model, PropertyDescriptor descriptor,
	        boolean nested) {
		Attribute attribute = ClassUtils.getAnnotation(parentClass, descriptor.getName(), Attribute.class);

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
			if (attribute.visible() != null && !VisibilityType.INHERIT.equals(attribute.visible()) && !nested) {
				model.setVisible(VisibilityType.SHOW.equals(attribute.visible()));
				model.setVisibleInTable(model.isVisible());
			}

			if (attribute.searchable() && !nested) {
				model.setSearchable(true);
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

			if (attribute.showInTable() != null && !VisibilityType.INHERIT.equals(attribute.showInTable()) && !nested) {
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

			if (attribute.allowedExtensions() != null && attribute.allowedExtensions().length > 0) {
				Set<String> hashSet = Sets.newHashSet(attribute.allowedExtensions());
				model.setAllowedExtensions(hashSet);
			}

			if (attribute.groupTogetherWith() != null && attribute.groupTogetherWith().length > 0) {
				for (String s : attribute.groupTogetherWith()) {
					model.addGroupTogetherWith(s);
				}
			}

			if (!StringUtils.isEmpty(attribute.trueRepresentation())) {
				model.setTrueRepresentation(attribute.trueRepresentation());
			}

			if (!StringUtils.isEmpty(attribute.falseRepresentation())) {
				model.setFalseRepresentation(attribute.falseRepresentation());
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

			// overrule select mode - by default, this overrules the search select mode as well
			if (attribute.selectMode() != null && !AttributeSelectMode.INHERIT.equals(attribute.selectMode())) {
				model.setSelectMode(attribute.selectMode());
				model.setSearchSelectMode(attribute.selectMode());
			}

			// set multiple search
			if (attribute.multipleSearch()) {
				model.setMultipleSearch(true);
				// by default, use a token for multiple select
				model.setSearchSelectMode(AttributeSelectMode.TOKEN);
			}

			if (!AttributeSelectMode.INHERIT.equals(attribute.searchSelectMode())) {
				model.setSearchSelectMode(attribute.searchSelectMode());
			}

			model.setSearchCaseSensitive(attribute.searchCaseSensitive());
			model.setSearchPrefixOnly(attribute.searchPrefixOnly());

			if (attribute.textFieldMode() != null && !AttributeTextFieldMode.INHERIT.equals(attribute.textFieldMode())) {
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

			if (attribute.maxValue() < Long.MAX_VALUE) {
				model.setMaxValue(attribute.maxValue());
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

			if (!attribute.useThousandsGrouping()) {
				model.setUseThousandsGrouping(false);
			}

			if (attribute.searchForExactValue()) {
				model.setSearchForExactValue(true);
			}

			if (!StringUtils.isEmpty(attribute.fileNameProperty())) {
				model.setFileNameProperty(attribute.fileNameProperty());
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
			model.setDefaultValue(Enum.valueOf(model.getType().asSubclass(Enum.class), defaultValue));
		} else if (model.getType().equals(Date.class)) {
			SimpleDateFormat fmt = new SimpleDateFormat(model.getDisplayFormat());
			try {
				model.setDefaultValue(fmt.parseObject(defaultValue));
			} catch (ParseException e) {
				throw new OCSRuntimeException("Cannot parse default date value: " + defaultValue + " with format: "
				        + model.getDisplayFormat());
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

		msg = getAttributeMessage(entityModel, model, EntityModel.GROUP_TOGETHER_WITH);
		if (!StringUtils.isEmpty(msg)) {
			String[] extensions = msg.split(",");
			for (String s : extensions) {
				model.addGroupTogetherWith(s);
			}
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.TRUE_REPRESENTATION);
		if (!StringUtils.isEmpty(msg)) {
			model.setTrueRepresentation(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.FALSE_REPRESENTATION);
		if (!StringUtils.isEmpty(msg)) {
			model.setFalseRepresentation(msg);
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

		// set multiple search (also overwrites the search select mode and sets it to fancy list)
		msg = getAttributeMessage(entityModel, model, EntityModel.MULTIPLE_SEARCH);
		if (!StringUtils.isEmpty(msg)) {
			model.setMultipleSearch(Boolean.valueOf(msg));
			model.setSearchSelectMode(AttributeSelectMode.TOKEN);
		}

		// set the select mode (also sets the search select mode to the same value)
		msg = getAttributeMessage(entityModel, model, EntityModel.SELECT_MODE);
		if (!StringUtils.isEmpty(msg) && AttributeSelectMode.valueOf(msg) != null) {
			model.setSelectMode(AttributeSelectMode.valueOf(msg));
			model.setSearchSelectMode(AttributeSelectMode.valueOf(msg));
		}

		// explicitly set the search select mode
		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_SELECT_MODE);
		if (!StringUtils.isEmpty(msg)) {
			model.setSearchSelectMode(AttributeSelectMode.valueOf(msg));
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

		msg = getAttributeMessage(entityModel, model, EntityModel.MIN_VALUE);
		if (!StringUtils.isEmpty(msg)) {
			model.setMinValue(Long.parseLong(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.MAX_LENGTH);
		if (!StringUtils.isEmpty(msg)) {
			model.setMaxLength(Integer.parseInt(msg));
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

		msg = getAttributeMessage(entityModel, model, EntityModel.QUICK_ADD_PROPERTY);
		if (!StringUtils.isEmpty(msg)) {
			model.setQuickAddPropertyName(msg);
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.THOUSANDS_GROUPING);
		if (!StringUtils.isEmpty(msg)) {
			model.setUseThousandsGrouping(Boolean.valueOf(msg));
		}

		msg = getAttributeMessage(entityModel, model, EntityModel.SEARCH_EXACT_VALUE);
		if (!StringUtils.isEmpty(msg)) {
			model.setSearchForExactValue(Boolean.valueOf(msg));
		}

	}

	/**
	 * Calculates the entity model for a nested property, recursively up till a certain depth
	 * 
	 * @param model
	 *            the attribute model
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
					EntityModel<?> nem = new LazyEntityModelWrapper<>(this, ref, type);
					model.setNestedEntityModel(nem);
				}
			}
		}
	}

	/**
	 * Sets the sort order on an entity model
	 * 
	 * @param model
	 *            the entity model
	 * @param sortOrderMsg
	 *            the sort order from the message bundle
	 */
	private <T> void setSortOrder(EntityModelImpl<T> model, String sortOrderMsg) {
		if (!StringUtils.isEmpty(sortOrderMsg)) {
			String[] tokens = sortOrderMsg.split(",");
			for (String token : tokens) {
				String[] sd = token.trim().split(" ");
				if (sd.length > 0 && !StringUtils.isEmpty(sd[0]) && model.getAttributeModel(sd[0]) != null) {
					model.getSortOrder()
					        .put(model.getAttributeModel(sd[0]),
					                (sd.length > 1 && ("DESC".equalsIgnoreCase(sd[1]) || "DSC".equalsIgnoreCase(sd[1]))) ? false
					                        : true);
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
