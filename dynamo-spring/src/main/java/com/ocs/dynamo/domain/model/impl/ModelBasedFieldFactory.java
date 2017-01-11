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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.component.EntityComboBox;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.ocs.dynamo.ui.component.EntityListSelect;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.FancyListSelect;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
import com.ocs.dynamo.ui.component.TimeField;
import com.ocs.dynamo.ui.component.TokenFieldSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.form.CollectionTable;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.WeekCodeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.ui.validator.URLValidator;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * Extension of the standard Vaadin field factory for creating custom fields
 * 
 * @author bas.rutten
 * @param <T>
 *            the type of the entity for which to create a field
 */
public class ModelBasedFieldFactory<T> extends DefaultFieldGroupFieldFactory implements TableFieldFactory {

	private static ConcurrentMap<String, ModelBasedFieldFactory<?>> nonValidatingInstances = new ConcurrentHashMap<>();

	private static ConcurrentMap<String, ModelBasedFieldFactory<?>> searchInstances = new ConcurrentHashMap<>();

	private static final long serialVersionUID = -5684112523268959448L;

	private static ConcurrentMap<String, ModelBasedFieldFactory<?>> validatingInstances = new ConcurrentHashMap<>();

	private MessageService messageService;

	private EntityModel<T> model;

	// indicates whether the system is in search mode. In search mode, components for
	// some attributes are constructed differently (e.g. we render two search fields to be able to
	// search for a range of integers)
	private boolean search;

	// indicates whether extra validators must be added. This is the case when
	// using the field factory in an
	// editable table
	private boolean validate;

	/**
	 * Constructor
	 * 
	 * @param model
	 *            the entity model
	 * @param messageService
	 *            the message service
	 * @param validate
	 *            whether to add extra validators (this is the case when the field is displayed
	 *            inside a table)
	 * @param search
	 *            whether the fields are displayed inside a search form (this has an effect on the
	 *            construction of some fields)
	 */
	public ModelBasedFieldFactory(EntityModel<T> model, MessageService messageService, boolean validate, boolean search) {
		this.model = model;
		this.messageService = messageService;
		this.validate = validate;
		this.search = search;
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 * 
	 * @param model
	 *            the entity model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> ModelBasedFieldFactory<T> getInstance(EntityModel<T> model, MessageService messageService) {
		if (!nonValidatingInstances.containsKey(model.getReference())) {
			nonValidatingInstances.put(model.getReference(), new ModelBasedFieldFactory<>(model, messageService, false,
			        false));
		}
		return (ModelBasedFieldFactory<T>) nonValidatingInstances.get(model.getReference());
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 * 
	 * @param model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> ModelBasedFieldFactory<T> getSearchInstance(EntityModel<T> model, MessageService messageService) {
		if (!searchInstances.containsKey(model.getReference())) {
			searchInstances.put(model.getReference(), new ModelBasedFieldFactory<>(model, messageService, false, true));
		}
		return (ModelBasedFieldFactory<T>) searchInstances.get(model.getReference());
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 * 
	 * @param model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> ModelBasedFieldFactory<T> getValidatingInstance(EntityModel<T> model,
	        MessageService messageService) {
		if (!validatingInstances.containsKey(model.getReference())) {
			validatingInstances.put(model.getReference(), new ModelBasedFieldFactory<>(model, messageService, true,
			        false));
		}
		return (ModelBasedFieldFactory<T>) validatingInstances.get(model.getReference());
	}

	/**
	 * Constructs a combo box- the sort order will be taken from the entity model
	 * 
	 * @param entityModel
	 *            the entity model to base the combo box on
	 * @param attributeModel
	 *            the attribute model
	 * @param filter
	 *            optional field filter - only items that match the filter will be included
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> AbstractField<?> constructComboBox(
	        EntityModel<?> entityModel, AttributeModel attributeModel, Filter filter, boolean search) {
		entityModel = resolveEntityModel(entityModel, attributeModel);
		BaseService<ID, S> service = (BaseService<ID, S>) ServiceLocator.getServiceForEntity(entityModel
		        .getEntityClass());
		SortOrder[] sos = constructSortOrder(entityModel);
		if (attributeModel != null && attributeModel.isQuickAddAllowed() && !search) {
			return new QuickAddEntityComboBox<ID, S>((EntityModel<S>) entityModel, attributeModel, service,
			        SelectMode.FILTERED, filter, null, sos);
		} else {
			return new EntityComboBox<ID, S>((EntityModel<S>) entityModel, attributeModel, service, filter, sos);
		}
	}

	/**
	 * Constructs a field based on an attribute model and possibly a field filter
	 * 
	 * @param attributeModel
	 *            the attribute model
	 * @param fieldFilters
	 *            the list of field filters
	 * @param fieldEntityModel
	 *            the custom entity model for the field
	 * @return
	 */
	public Field<?> constructField(AttributeModel attributeModel, Map<String, Filter> fieldFilters,
	        EntityModel<?> fieldEntityModel) {

		Filter fieldFilter = fieldFilters == null ? null : fieldFilters.get(attributeModel.getPath());

		Field<?> field = null;
		if (fieldFilter != null) {
			if (AttributeType.MASTER.equals(attributeModel.getAttributeType())) {
				// create a combo box or lookup field
				field = constructSelectField(attributeModel, fieldEntityModel, fieldFilter);
			} else if (search && AttributeSelectMode.TOKEN.equals(attributeModel.getSearchSelectMode())
			        && AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
				// simple token field (for distinct string values)
				field = constructSimpleTokenField(
				        fieldEntityModel != null ? fieldEntityModel : attributeModel.getEntityModel(), attributeModel,
				        attributeModel.getPath().substring(attributeModel.getPath().lastIndexOf(".") + 1), false,
				        fieldFilter);
			} else {
				// detail relationship, render a multiple select
				field = this.constructCollectionSelect(fieldEntityModel, attributeModel, fieldFilter, true, search);
			}
		} else {
			// no field filter present - delegate to default construction
			field = this.createField(attributeModel.getPath(), fieldEntityModel);
		}

		// Is it a required field?
		field.setRequired(search ? attributeModel.isRequiredForSearching() : attributeModel.isRequired());

		//
		if (field instanceof AbstractComponent) {
			((AbstractComponent) field).setImmediate(true);
			field.setCaption(attributeModel.getDisplayName());
		}
		return field;
	}

	/**
	 * Constructs a select component for selecting multiple values
	 * 
	 * @param fieldEntityModel
	 *            the entity model of the entity to display in the field
	 * @param attributeModel
	 *            the attribute model of the property that is displayed in the ListSelect
	 * @param fieldFilter
	 *            optional field filter
	 * @param multipleSelect
	 *            is multiple select supported?
	 * @param search
	 *            indicates whether the component is being used in a search screen
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> Field<?> constructCollectionSelect(
	        EntityModel<?> fieldEntityModel, AttributeModel attributeModel, Filter fieldFilter, boolean multipleSelect,
	        boolean search) {
		EntityModel<?> em = resolveEntityModel(fieldEntityModel, attributeModel);

		BaseService<ID, S> service = (BaseService<ID, S>) ServiceLocator.getServiceForEntity(em.getEntityClass());
		SortOrder[] sos = constructSortOrder(em);

		// mode depends on whether we are searching
		AttributeSelectMode mode = search ? attributeModel.getSearchSelectMode() : attributeModel.getSelectMode();

		if (AttributeSelectMode.LOOKUP.equals(mode)) {
			// lookup field - take care to NOT use the nested model here!
			return constructLookupField((EntityModel<S>) fieldEntityModel, attributeModel, fieldFilter, search, true);
		} else if (AttributeSelectMode.FANCY_LIST.equals(mode)) {
			// fancy list select
			FancyListSelect<ID, S> listSelect = new FancyListSelect<ID, S>(service, (EntityModel<S>) em,
			        attributeModel, fieldFilter, search, sos);
			listSelect.setRows(SystemPropertyUtils.getDefaultListSelectRows());
			return listSelect;
		} else if (AttributeSelectMode.LIST.equals(mode)) {
			// simple list select if everything else fails or is not applicable
			EntityListSelect<ID, S> listSelect = new EntityListSelect<ID, S>((EntityModel<S>) em, attributeModel,
			        service, fieldFilter, sos);
			listSelect.setMultiSelect(multipleSelect);
			listSelect.setRows(SystemPropertyUtils.getDefaultListSelectRows());
			return listSelect;
		} else {
			// by default, use a token field
			return new TokenFieldSelect<ID, S>((EntityModel<S>) em, attributeModel, service, fieldFilter, search, sos);
		}
	}

	/**
	 * Constructs a lookup field (field that brings up a popup search dialog)
	 * 
	 * @param entityModel
	 *            the entity model of the entity to display in the field
	 * @param attributeModel
	 *            the attribute model of the property that is bound to the field
	 * @param fieldFilter
	 *            optional field filter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> EntityLookupField<ID, S> constructLookupField(
	        EntityModel<?> overruled, AttributeModel attributeModel, Filter fieldFilter, boolean search,
	        boolean multiSelect) {

		// for a lookup field, don't use the nested model but the base model - this is
		// because the search in the popup screen is conducted on a "clean", unnested entity list so
		// using a path from the parent entity makes no sense here
		EntityModel<?> entityModel = overruled != null ? overruled : ServiceLocator.getEntityModelFactory().getModel(
		        attributeModel.getNormalizedType());

		BaseService<ID, S> service = (BaseService<ID, S>) ServiceLocator.getServiceForEntity(attributeModel
		        .getMemberType() != null ? attributeModel.getMemberType() : entityModel.getEntityClass());
		SortOrder[] sos = constructSortOrder(entityModel);
		return new EntityLookupField<ID, S>(service, (EntityModel<S>) entityModel, attributeModel,
		        fieldFilter == null ? null : Lists.newArrayList(fieldFilter), search, multiSelect,
		        sos.length == 0 ? null : Lists.newArrayList(sos));
	}

	/**
	 * Create a combo box for searching on a boolean. This combo box contains three values (yes, no,
	 * and null)
	 * 
	 * @return
	 */
	public ComboBox constructSearchBooleanComboBox(AttributeModel am) {
		ComboBox cb = new ComboBox();
		cb.addItem(Boolean.TRUE);
		cb.setItemCaption(Boolean.TRUE, am.getTrueRepresentation());
		cb.addItem(Boolean.FALSE);
		cb.setItemCaption(Boolean.FALSE, am.getFalseRepresentation());
		return cb;
	}

	/**
	 * Construct a combo box that contains a list of String values
	 * 
	 * @param values
	 *            the list of values
	 * @param am
	 *            the attribute model
	 * @return
	 */
	public static ComboBox constructStringListCombo(List<String> values, AttributeModel am) {
		ComboBox cb = new ComboBox();
		cb.setCaption(am.getDisplayName());
		cb.addItems(values);
		cb.setFilteringMode(FilteringMode.CONTAINS);
		return cb;
	}

	/**
	 * Constructs a token field for basic attributes (Strings)
	 * 
	 * @param entityModel
	 *            the entity model to base the field on
	 * @param attributeModel
	 *            the attribute model
	 * @param distinctField
	 *            the field for which to return the distinct values
	 * @param fieldFilter
	 *            field filter to apply in order to limit the search results
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>, O extends Comparable<O>> SimpleTokenFieldSelect<ID, S, O> constructSimpleTokenField(
	        EntityModel<?> entityModel, AttributeModel attributeModel, String distinctField, boolean elementCollection,
	        Filter fieldFilter) {
		BaseService<ID, S> service = (BaseService<ID, S>) ServiceLocator.getServiceForEntity(entityModel
		        .getEntityClass());

		SortOrder[] sos;
		if (distinctField == null) {
			sos = constructSortOrder(entityModel);
		} else {
			sos = new SortOrder[] { new SortOrder(distinctField, SortDirection.ASCENDING) };
		}

		return new SimpleTokenFieldSelect<>(service, (EntityModel<S>) entityModel, attributeModel, fieldFilter,
		        distinctField, (Class<O>) attributeModel.getNormalizedType(), elementCollection, sos);
	}

	/**
	 * Constructs the default sort order of a component based on an Entity Model
	 * 
	 * @param entityModel
	 *            the entity model
	 * @return
	 */
	private SortOrder[] constructSortOrder(EntityModel<?> entityModel) {
		SortOrder[] sos = new SortOrder[entityModel.getSortOrder().size()];
		int i = 0;
		for (AttributeModel am : entityModel.getSortOrder().keySet()) {
			sos[i++] = new SortOrder(am.getName(), entityModel.getSortOrder().get(am) ? SortDirection.ASCENDING
			        : SortDirection.DESCENDING);
		}
		return sos;
	}

	/**
	 * Creates a field for displaying an enumeration
	 * 
	 * @param type
	 *            the type of enum the values to display
	 * @param fieldType
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends Field<?>> E createEnumCombo(Class<?> type, Class<E> fieldType) {
		AbstractSelect s = createCompatibleSelect((Class<? extends AbstractSelect>) fieldType);
		s.setNullSelectionAllowed(true);
		fillEnumField(s, (Class<? extends Enum>) type);
		return (E) s;
	}

	/**
	 * Creates a field - overridden from the default field factory
	 * 
	 * @param type
	 *            the type of the property that is bound to the field
	 * @param fieldType
	 *            the type of the field
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <F extends Field> F createField(Class<?> type, Class<F> fieldType) {
		if (Enum.class.isAssignableFrom(type)) {
			if (AbstractSelect.class.isAssignableFrom(fieldType)) {
				return createEnumCombo(type, fieldType);
			} else {
				ComboBox cb = (ComboBox) createEnumCombo(type, ComboBox.class);
				cb.setFilteringMode(FilteringMode.CONTAINS);
				return (F) cb;
			}
		} else if (AbstractEntity.class.isAssignableFrom(type)) {
			// inside a table, always use a combo box
			EntityModel<?> entityModel = ServiceLocator.getEntityModelFactory().getModel(type);
			return (F) constructComboBox(entityModel, null, null, search);
		}

		return super.createField(type, fieldType);
	}

	/**
	 * Creates a field (called when creating the field inside a table)
	 * 
	 * @param container
	 * @param itemId
	 * @param propertyId
	 */
	@Override
	public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		return createField(propertyId.toString(), null);
	}

	/**
	 * 
	 * Creates a field for a certain property ID
	 * 
	 * @param propertyId
	 *            the property
	 * @return
	 */
	public Field<?> createField(String propertyId) {
		return createField(propertyId, null);
	}

	/**
	 * Creates a field
	 * 
	 * @param propertyId
	 *            the name of the property that can be edited by this field
	 * @param fieldEntityModel
	 *            the custom entity model for the field
	 * @return
	 */
	public Field<?> createField(String propertyId, EntityModel<?> fieldEntityModel) {

		// in case of a read-only field, return <code>null</code> so Vaadin will
		// render a label instead
		AttributeModel attributeModel = model.getAttributeModel(propertyId);
		if (attributeModel.isReadOnly()
		        && (!attributeModel.isUrl() && !AttributeType.DETAIL.equals(attributeModel.getAttributeType()))
		        && !search) {
			return null;
		}

		Field<?> field = null;

		if (AttributeTextFieldMode.TEXTAREA.equals(attributeModel.getTextFieldMode()) && !search) {
			// text area field
			field = new TextArea();
		} else if (attributeModel.isWeek()) {
			// special case - week field in a table
			TextField tf = new TextField();
			tf.setConverter(new WeekCodeConverter());
			field = tf;
		} else if (search && AttributeSelectMode.TOKEN.equals(attributeModel.getSearchSelectMode())
		        && AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
			// simple token field (for distinct string values)
			field = constructSimpleTokenField(
			        fieldEntityModel != null ? fieldEntityModel : attributeModel.getEntityModel(), attributeModel,
			        propertyId.substring(propertyId.lastIndexOf(".") + 1), false, null);

		} else if (search && (attributeModel.getType().equals(Boolean.class))) {
			// in a search screen, we need to offer the true, false, and
			// undefined options
			field = constructSearchBooleanComboBox(attributeModel);
		} else if (AbstractEntity.class.isAssignableFrom(attributeModel.getType())) {
			// lookup or combo field for an entity
			field = constructSelectField(attributeModel, fieldEntityModel, null);
		} else if (AttributeType.ELEMENT_COLLECTION.equals(attributeModel.getAttributeType())) {
			if (!search) {
				// use a "collection table" for an element collection
				FormOptions fo = new FormOptions();
				fo.setShowRemoveButton(true);
				if (String.class.equals(attributeModel.getMemberType())) {
					field = new CollectionTable<>(attributeModel, true, fo);
				} else if (Integer.class.equals(attributeModel.getMemberType())) {
					field = new CollectionTable<>(attributeModel, true, fo);
				} else if (Long.class.equals(attributeModel.getMemberType())) {
					field = new CollectionTable<>(attributeModel, true, fo);
				} else if (BigDecimal.class.equals(attributeModel.getMemberType())) {
					field = new CollectionTable<>(attributeModel, true, fo);
				} else {
					// other types not supported for now
					throw new OCSRuntimeException("Element collections of this type are currently not supported");
				}
			} else {
				field = constructSimpleTokenField(
				        fieldEntityModel != null ? fieldEntityModel : attributeModel.getEntityModel(), attributeModel,
				        propertyId.substring(propertyId.lastIndexOf(".") + 1), true, null);
			}
		} else if (Collection.class.isAssignableFrom(attributeModel.getType())) {
			// render a multiple select component for a collection
			field = constructCollectionSelect(fieldEntityModel, attributeModel, null, true, search);
		} else if (AttributeDateType.TIME.equals(attributeModel.getDateType())) {
			TimeField tf = new TimeField();
			tf.setResolution(Resolution.MINUTE);
			tf.setLocale(VaadinSession.getCurrent() == null ? DynamoConstants.DEFAULT_LOCALE : VaadinSession
			        .getCurrent().getLocale());
			field = tf;
		} else if (attributeModel.isUrl()) {
			// URL field (offers clickable link in readonly mode)
			TextField tf = (TextField) createField(attributeModel.getType(), Field.class);
			tf.addValidator(new URLValidator(messageService.getMessage("ocs.no.valid.url")));
			tf.setNullRepresentation(null);
			tf.setSizeFull();

			// wrap text field in URL field
			field = new URLField(tf, attributeModel, false);
			field.setSizeFull();
		} else {
			// just a regular field
			field = createField(attributeModel.getType(), Field.class);
		}

		if (field instanceof DateField) {
			DateField df = (DateField) field;
			df.setLocale(new Locale(SystemPropertyUtils.getDateLocale()));
			if (UI.getCurrent() != null) {
				df.setTimeZone(VaadinUtils.getTimeZone(UI.getCurrent()));
			}
		}

		field.setCaption(attributeModel.getDisplayName());

		postProcessField(field, attributeModel);

		// add a field validator based on JSR-303 bean validation
		if (validate) {
			field.addValidator(new BeanValidator(model.getEntityClass(), (String) propertyId));
			// disable the field if it cannot be edited
			if (!attributeModel.isUrl()) {
				field.setEnabled(!attributeModel.isReadOnly());
			}

			if (attributeModel.isNumerical()) {
				field.addStyleName(DynamoConstants.CSS_NUMERICAL);
			}
		}
		return field;
	}

	/**
	 * Add additional field settings to a field
	 * 
	 * @param field
	 * @param attributeModel
	 */
	private void postProcessField(Field<?> field, AttributeModel attributeModel) {
		if (field instanceof AbstractTextField) {
			AbstractTextField textField = (AbstractTextField) field;
			textField.setDescription(attributeModel.getDescription());
			textField.setNullSettingAllowed(true);
			textField.setNullRepresentation("");
			if (!StringUtils.isEmpty(attributeModel.getPrompt())) {
				textField.setInputPrompt(attributeModel.getPrompt());
			}

			// set converters
			setConverters(textField, attributeModel);

			// add email validator
			if (attributeModel.isEmail()) {
				field.addValidator(new EmailValidator(messageService.getMessage("ocs.no.valid.email")));
			}

		} else if (field instanceof DateField) {
			// set a separate format for a date field
			DateField dateField = (DateField) field;
			if (attributeModel.getDisplayFormat() != null) {
				dateField.setDateFormat(attributeModel.getDisplayFormat());
			}

			// display minutes only when dealing with time stamps
			if (AttributeDateType.TIMESTAMP.equals(attributeModel.getDateType())) {
				dateField.setResolution(Resolution.MINUTE);
			}
		}
	}

	/**
	 * Creates a select field for a single-valued attribute
	 * 
	 * @param attributeModel
	 *            the attribute
	 * @param fieldEntityModel
	 *            the (overruled) entity model
	 * @param fieldFilter
	 *            the field filter
	 * @return
	 */
	protected Field<?> constructSelectField(AttributeModel attributeModel, EntityModel<?> fieldEntityModel,
	        Filter fieldFilter) {
		Field<?> field = null;

		AttributeSelectMode selectMode = search ? attributeModel.getSearchSelectMode() : attributeModel.getSelectMode();

		if (search && attributeModel.isMultipleSearch()) {
			// in case of multiple search, defer to the "constructCollectionSelect" method
			field = this.constructCollectionSelect(fieldEntityModel, attributeModel, fieldFilter, true, search);
		} else if (AttributeSelectMode.COMBO.equals(selectMode)) {
			// combo box
			field = (Field<?>) constructComboBox(fieldEntityModel, attributeModel, fieldFilter, search);
		} else if (AttributeSelectMode.LOOKUP.equals(selectMode)) {
			// single select lookup field
			field = (Field<?>) constructLookupField(fieldEntityModel, attributeModel, fieldFilter, search, false);
		} else {
			// list select (single select)
			field = this.constructCollectionSelect(fieldEntityModel, attributeModel, fieldFilter, false, search);
		}
		return field;
	}

	/**
	 * Fills an enumeration field with messages from the message bundle
	 * 
	 * @param select
	 * @param enumClass
	 */
	@SuppressWarnings("unchecked")
	private <E extends Enum<E>> void fillEnumField(AbstractSelect select, Class<E> enumClass) {
		select.removeAllItems();
		for (Object p : select.getContainerPropertyIds()) {
			select.removeContainerProperty(p);
		}
		select.addContainerProperty(CAPTION_PROPERTY_ID, String.class, "");
		select.setItemCaptionPropertyId(CAPTION_PROPERTY_ID);
		for (E e : enumClass.getEnumConstants()) {
			Item newItem = select.addItem(e);

			String msg = messageService.getEnumMessage(enumClass, e);
			if (msg != null) {
				newItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(msg);
			} else {
				newItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(
				        DefaultFieldFactory.createCaptionByPropertyId(e.name()));
			}
		}
	}

	public EntityModel<T> getModel() {
		return model;
	}

	/**
	 * Resolves an entity model by falling back first to the nested attribute model and then to the
	 * default model for the normalized type of the property
	 * 
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @return
	 */
	private EntityModel<?> resolveEntityModel(EntityModel<?> entityModel, AttributeModel attributeModel) {
		if (entityModel == null) {
			if (attributeModel.getNestedEntityModel() != null) {
				entityModel = attributeModel.getNestedEntityModel();
			} else {
				Class<?> type = attributeModel.getNormalizedType();
				entityModel = ServiceLocator.getEntityModelFactory().getModel(type.asSubclass(AbstractEntity.class));
			}
		}
		return entityModel;
	}

	/**
	 * Set the appropriate converter on a text field
	 * 
	 * @param textField
	 *            the field
	 * @param attributeModel
	 *            the attribute model of the attribute to bind to the field
	 */
	protected void setConverters(AbstractTextField textField, AttributeModel attributeModel) {
		if (attributeModel.getType().equals(BigDecimal.class)) {
			textField.setConverter(ConverterFactory.createBigDecimalConverter(attributeModel.isCurrency(),
			        attributeModel.isPercentage(), false, attributeModel.getPrecision(),
			        VaadinUtils.getCurrencySymbol()));
		} else if (attributeModel.getType().equals(Integer.class)) {
			textField.setConverter(ConverterFactory.createIntegerConverter(SystemPropertyUtils
			        .useThousandsGroupingInEditMode()));
		} else if (attributeModel.getType().equals(Long.class)) {
			textField.setConverter(ConverterFactory.createLongConverter(SystemPropertyUtils
			        .useThousandsGroupingInEditMode()));
		}
	}

}
