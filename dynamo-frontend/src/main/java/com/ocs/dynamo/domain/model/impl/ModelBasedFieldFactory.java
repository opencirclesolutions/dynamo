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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.StringUtils;
import org.vaadin.teemu.switchui.Switch;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.CheckboxMode;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.EntityComboBox.SelectMode;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.FancyListSelect;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSelect;
import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
import com.ocs.dynamo.ui.component.TimeField;
import com.ocs.dynamo.ui.component.TokenFieldSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.composite.form.CollectionTable;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.converter.BigDecimalToDoubleConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.IntToDoubleConverter;
import com.ocs.dynamo.ui.converter.LocalDateWeekCodeConverter;
import com.ocs.dynamo.ui.converter.LongToDoubleConverter;
import com.ocs.dynamo.ui.converter.WeekCodeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.ui.validator.URLValidator;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.data.validator.EmailValidator;
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
import com.vaadin.ui.Slider;
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

	private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	// indicates whether the system is in search mode. In search mode,
	// components for
	// some attributes are constructed differently (e.g. we render two search
	// fields to be able to
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
	 *            whether to add extra validators (this is the case when the field
	 *            is displayed inside a table)
	 * @param search
	 *            whether the fields are displayed inside a search form (this has an
	 *            effect on the construction of some fields)
	 */
	public ModelBasedFieldFactory(EntityModel<T> model, MessageService messageService, boolean validate,
			boolean search) {
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
			nonValidatingInstances.put(model.getReference(),
					new ModelBasedFieldFactory<>(model, messageService, false, false));
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
			validatingInstances.put(model.getReference(),
					new ModelBasedFieldFactory<>(model, messageService, true, false));
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
	 *            optional field filter - only items that match the filter will be
	 *            included
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> AbstractField<?> constructComboBox(
			EntityModel<?> entityModel, AttributeModel attributeModel, Filter filter, boolean search) {
		entityModel = resolveEntityModel(entityModel, attributeModel);
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());
		SortOrder[] sos = constructSortOrder(entityModel);
		return new QuickAddEntityComboBox<>((EntityModel<S>) entityModel, attributeModel, service, SelectMode.FILTERED,
				filter, search, null, sos);
	}

	/**
	 * Constructs an internal link field
	 * 
	 * @param entityModel
	 * @param attributeModel
	 * @param filter
	 * @param search
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> AbstractField<?> constructInternalLinkField(
			EntityModel<?> entityModel, AttributeModel attributeModel) {
		entityModel = resolveEntityModel(entityModel, attributeModel);
		return new InternalLinkField<>((EntityModel<S>) entityModel, attributeModel);
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
						attributeModel.getPath().substring(attributeModel.getPath().lastIndexOf('.') + 1), false,
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
	 *            the attribute model of the property that is displayed in the
	 *            ListSelect
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

		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator.getServiceForEntity(em.getEntityClass());
		SortOrder[] sos = constructSortOrder(em);

		// mode depends on whether we are searching
		AttributeSelectMode mode = search ? attributeModel.getSearchSelectMode() : attributeModel.getSelectMode();

		if (AttributeSelectMode.LOOKUP.equals(mode)) {
			// lookup field - take care to NOT use the nested model here!
			return constructLookupField(fieldEntityModel, attributeModel, fieldFilter, search, true);
		} else if (AttributeSelectMode.FANCY_LIST.equals(mode)) {
			// fancy list select
			FancyListSelect<ID, S> listSelect = new FancyListSelect<>(service, (EntityModel<S>) em, attributeModel,
					fieldFilter, search, sos);
			listSelect.setRows(SystemPropertyUtils.getDefaultListSelectRows());
			return listSelect;
		} else if (AttributeSelectMode.LIST.equals(mode)) {
			// simple list select if everything else fails or is not applicable
			return new QuickAddListSelect<>((EntityModel<S>) em, attributeModel, service, fieldFilter, multipleSelect,
					SystemPropertyUtils.getDefaultListSelectRows(), sos);
		} else {
			// by default, use a token field
			return new TokenFieldSelect<>((EntityModel<S>) em, attributeModel, service, fieldFilter, search, sos);
		}
	}

	/**
	 * Constructs a lookup field (field that brings up a popup search dialog)
	 * 
	 * @param overruled
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

		// for a lookup field, don't use the nested model but the base model -
		// this is
		// because the search in the popup screen is conducted on a "clean",
		// unnested entity list so
		// using a path from the parent entity makes no sense here
		EntityModel<?> entityModel = overruled != null ? overruled
				: serviceLocator.getEntityModelFactory().getModel(attributeModel.getNormalizedType());

		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator.getServiceForEntity(
				attributeModel.getMemberType() != null ? attributeModel.getMemberType() : entityModel.getEntityClass());
		SortOrder[] sos = constructSortOrder(entityModel);
		return new EntityLookupField<>(service, (EntityModel<S>) entityModel, attributeModel, fieldFilter, search,
				multiSelect, sos.length == 0 ? null : Lists.newArrayList(sos));
	}

	/**
	 * Create a combo box for searching on a boolean. This combo box contains three
	 * values (yes, no, and null)
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
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());

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
			sos[i++] = new SortOrder(am.getName(),
					entityModel.getSortOrder().get(am) ? SortDirection.ASCENDING : SortDirection.DESCENDING);
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
				ComboBox cb = createEnumCombo(type, ComboBox.class);
				cb.setFilteringMode(FilteringMode.CONTAINS);
				return (F) cb;
			}
		} else if (AbstractEntity.class.isAssignableFrom(type)) {
			// inside a table, always use a combo box
			EntityModel<?> entityModel = serviceLocator.getEntityModelFactory().getModel(type);
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
		if (EditableType.READ_ONLY.equals(attributeModel.getEditableType()) && (!attributeModel.isUrl()
				&& !attributeModel.isNavigable() && !AttributeType.DETAIL.equals(attributeModel.getAttributeType()))
				&& !search) {
			return null;
		}

		Field<?> field = null;

		if (AttributeTextFieldMode.TEXTAREA.equals(attributeModel.getTextFieldMode()) && !search) {
			// text area field
			field = new TextArea();
		} else if ((NumberUtils.isLong(attributeModel.getType()) || NumberUtils.isInteger(attributeModel.getType())
				|| BigDecimal.class.equals(attributeModel.getType()))
				&& NumberSelectMode.SLIDER.equals(attributeModel.getNumberSelectMode())) {
			Slider slider = new Slider(attributeModel.getDisplayName());

			if (NumberUtils.isInteger(attributeModel.getType())) {
				slider.setConverter(new IntToDoubleConverter());
			} else if (NumberUtils.isLong(attributeModel.getType())) {
				slider.setConverter(new LongToDoubleConverter());
			} else {
				slider.setConverter(new BigDecimalToDoubleConverter());
				slider.setResolution(attributeModel.getPrecision());
			}

			if (attributeModel.getMinValue() != null) {
				slider.setMin(attributeModel.getMinValue());
			}
			if (attributeModel.getMaxValue() != null) {
				slider.setMax(attributeModel.getMaxValue());
			}
			field = slider;
		} else if (attributeModel.isNavigable()) {
			field = constructInternalLinkField(fieldEntityModel, attributeModel);
		} else if (attributeModel.isWeek()) {
			// special case - week field in a table
			TextField tf = new TextField();
			tf.setConverter(Date.class.equals(attributeModel.getType()) ? new WeekCodeConverter()
					: new LocalDateWeekCodeConverter());
			field = tf;
		} else if (search && AttributeSelectMode.TOKEN.equals(attributeModel.getSearchSelectMode())
				&& AttributeType.BASIC.equals(attributeModel.getAttributeType())) {
			// simple token field (for distinct string values)
			field = constructSimpleTokenField(
					fieldEntityModel != null ? fieldEntityModel : attributeModel.getEntityModel(), attributeModel,
					propertyId.substring(propertyId.lastIndexOf('.') + 1), false, null);

		} else if (search
				&& (attributeModel.getType().equals(Boolean.class) || attributeModel.getType().equals(boolean.class))) {
			// in a search screen, we need to offer the true, false, and
			// undefined options
			field = constructSearchBooleanComboBox(attributeModel);
		} else if (AbstractEntity.class.isAssignableFrom(attributeModel.getType())) {
			// lookup or combo field for an entity
			field = constructSelectField(attributeModel, fieldEntityModel, null);
		} else if (AttributeType.ELEMENT_COLLECTION.equals(attributeModel.getAttributeType())) {
			if (!search) {
				// use a "collection table" for an element collection
				FormOptions fo = new FormOptions().setShowRemoveButton(true);
				if (String.class.equals(attributeModel.getMemberType())
						|| Integer.class.equals(attributeModel.getMemberType())
						|| Long.class.equals(attributeModel.getMemberType())
						|| BigDecimal.class.equals(attributeModel.getMemberType())) {
					field = new CollectionTable<>(attributeModel, true, fo);
				} else {
					// other types not supported for now
					throw new OCSRuntimeException("Element collections of this type are currently not supported");
				}
			} else {
				field = constructSimpleTokenField(
						fieldEntityModel != null ? fieldEntityModel : attributeModel.getEntityModel(), attributeModel,
						propertyId.substring(propertyId.lastIndexOf('.') + 1), true, null);
			}
		} else if (Collection.class.isAssignableFrom(attributeModel.getType())) {
			// render a multiple select component for a collection
			field = constructCollectionSelect(fieldEntityModel, attributeModel, null, true, search);
		} else if (LocalDate.class.equals(attributeModel.getType())) {
			DateField df = new DateField();
			df.setResolution(Resolution.DAY);
			df.setConverter(ConverterFactory.createLocalDateConverter());
			df.setTimeZone(VaadinUtils.getTimeZone(UI.getCurrent()));
			field = df;
		} else if (LocalDateTime.class.equals(attributeModel.getType())) {
			DateField df = new DateField();
			df.setResolution(Resolution.SECOND);
			df.setConverter(ConverterFactory.createLocalDateTimeConverter());
			df.setTimeZone(VaadinUtils.getTimeZone(UI.getCurrent()));
			field = df;
		} else if (ZonedDateTime.class.equals(attributeModel.getType())) {
			DateField df = new DateField();
			df.setResolution(Resolution.SECOND);
			df.setConverter(ConverterFactory.createZonedDateTimeConverter());
			df.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
			field = df;
		} else if (AttributeDateType.TIME.equals(attributeModel.getDateType())) {
			// use custom time field, potentially with Java 8 date converter
			TimeField tf = new TimeField();
			tf.setResolution(Resolution.MINUTE);
			tf.setLocale(VaadinUtils.getDateLocale());
			if (DateUtils.isJava8DateType(attributeModel.getType())) {
				tf.setConverter(ConverterFactory.createLocalTimeConverter());
			}
			field = tf;
		} else if (attributeModel.isUrl()) {
			// URL field (offers clickable link in readonly mode)
			TextField tf = (TextField) createField(attributeModel.getType(), Field.class);
			tf.addValidator(new URLValidator(messageService.getMessage("ocs.no.valid.url", VaadinUtils.getLocale())));
			tf.setNullRepresentation(null);
			tf.setSizeFull();

			// wrap text field in URL field
			field = new URLField(tf, attributeModel, false);
			field.setSizeFull();
		} else if (Boolean.class.equals(attributeModel.getType())
				&& CheckboxMode.SWITCH.equals(attributeModel.getCheckboxMode())) {
			field = new Switch();
			((Switch) field).addStyleName("compact");
		} else {
			// just a regular field
			field = createField(attributeModel.getType(), Field.class);
		}

		if (field instanceof DateField) {
			DateField df = (DateField) field;
			Locale dateLocale = VaadinUtils.getDateLocale();
			df.setLocale(dateLocale);
			if (UI.getCurrent() != null) {
				df.setTimeZone(VaadinUtils.getTimeZone(UI.getCurrent()));
			}
		}

		field.setCaption(attributeModel.getDisplayName());

		postProcessField(field, attributeModel);

		// add a field validator based on JSR-303 bean validation
		if (validate) {
			field.addValidator(new BeanValidator(model.getEntityClass(), propertyId));
			// disable the field if it cannot be edited
			if (!attributeModel.isUrl()) {
				field.setEnabled(!EditableType.READ_ONLY.equals(attributeModel.getEditableType()));
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
	 * @param field the field
	 * @param attributeModel the attribute model
	 */
	private void postProcessField(Field<?> field, AttributeModel attributeModel) {
		if (field instanceof AbstractTextField) {
			AbstractTextField textField = (AbstractTextField) field;
			textField.setNullSettingAllowed(true);
			textField.setNullRepresentation("");
			if (!StringUtils.isEmpty(attributeModel.getPrompt())) {
				textField.setInputPrompt(attributeModel.getPrompt());
			}

			// set converters
			setConverters(textField, attributeModel);

			// add email validator
			if (attributeModel.isEmail()) {
				field.addValidator(
						new EmailValidator(messageService.getMessage("ocs.no.valid.email", VaadinUtils.getLocale())));
			}
		} else if (field instanceof DateField) {
			// set a separate format for a date field
			DateField dateField = (DateField) field;
			if (attributeModel.getDisplayFormat() != null) {
				dateField.setDateFormat(attributeModel.getDisplayFormat());
			}

			if (AttributeDateType.TIMESTAMP.equals(attributeModel.getDateType())) {
				dateField.setResolution(Resolution.SECOND);
			}
		} 
		
		// set description for all fields
		if (field instanceof AbstractField) {
			((AbstractField<?>) field).setDescription(attributeModel.getDescription());
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
			// in case of multiple search, defer to the
			// "constructCollectionSelect" method
			field = this.constructCollectionSelect(fieldEntityModel, attributeModel, fieldFilter, true, search);
		} else if (AttributeSelectMode.COMBO.equals(selectMode)) {
			// combo box
			field = constructComboBox(fieldEntityModel, attributeModel, fieldFilter, search);
		} else if (AttributeSelectMode.LOOKUP.equals(selectMode)) {
			// single select lookup field
			field = constructLookupField(fieldEntityModel, attributeModel, fieldFilter, search, false);
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

		// sort on the description
		List<E> list = Arrays.asList(enumClass.getEnumConstants());
		list.sort((a, b) -> {
			String msg1 = messageService.getEnumMessage(enumClass, a, VaadinUtils.getLocale());
			String msg2 = messageService.getEnumMessage(enumClass, b, VaadinUtils.getLocale());
			return msg1.compareToIgnoreCase(msg2);
		});

		for (E e : list) {
			Item newItem = select.addItem(e);

			String msg = messageService.getEnumMessage(enumClass, e, VaadinUtils.getLocale());
			if (msg != null) {
				newItem.getItemProperty(CAPTION_PROPERTY_ID).setValue(msg);
			} else {
				newItem.getItemProperty(CAPTION_PROPERTY_ID)
						.setValue(DefaultFieldFactory.createCaptionByPropertyId(e.name()));
			}
		}

	}

	public EntityModel<T> getModel() {
		return model;
	}

	/**
	 * Resolves an entity model by falling back first to the nested attribute model
	 * and then to the default model for the normalized type of the property
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
				entityModel = serviceLocator.getEntityModelFactory().getModel(type.asSubclass(AbstractEntity.class));
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
	protected void setConverters(AbstractTextField textField, AttributeModel am) {
		if (am.getType().equals(BigDecimal.class)) {
			textField.setConverter(ConverterFactory.createBigDecimalConverter(am.isCurrency(), am.isPercentage(),
					SystemPropertyUtils.useThousandsGroupingInEditMode(), am.getPrecision(),
					VaadinUtils.getCurrencySymbol()));
		} else if (NumberUtils.isInteger(am.getType())) {
			textField.setConverter(ConverterFactory
					.createIntegerConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), am.isPercentage()));
		} else if (NumberUtils.isLong(am.getType())) {
			textField.setConverter(ConverterFactory
					.createLongConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), am.isPercentage()));
		}
	}

}
