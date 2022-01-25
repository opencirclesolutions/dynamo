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
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeSelectMode;
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.FieldFactoryContext;
import com.ocs.dynamo.domain.model.PagingType;
import com.ocs.dynamo.domain.model.SelectMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DateTimePicker;
import com.ocs.dynamo.ui.component.ElementCollectionGrid;
import com.ocs.dynamo.ui.component.EntityLookupField;
import com.ocs.dynamo.ui.component.InternalLinkField;
import com.ocs.dynamo.ui.component.QuickAddEntityComboBox;
import com.ocs.dynamo.ui.component.QuickAddListSingleSelect;
import com.ocs.dynamo.ui.component.QuickAddTokenSelect;
import com.ocs.dynamo.ui.component.SimpleTokenFieldSelect;
import com.ocs.dynamo.ui.component.URLField;
import com.ocs.dynamo.ui.component.ZonedDateTimePicker;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.LocalDateWeekCodeConverter;
import com.ocs.dynamo.ui.converter.TrimSpacesConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.ui.validator.EmailValidator;
import com.ocs.dynamo.ui.validator.URLValidator;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.NoArgsConstructor;

/**
 * A factory class for generating various input fields based on the entity and
 * attribute models
 * 
 * @author Bas Rutten
 *
 */
@NoArgsConstructor
public class FieldFactoryImpl implements FieldFactory {

	private FieldFactory delegate;

	@Autowired
	private MessageService messageService;

	private ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	public FieldFactoryImpl(FieldFactory delegate) {
		this.delegate = delegate;
	}

	@SuppressWarnings("unchecked")
	public <U, V> void addConvertersAndValidators(BindingBuilder<U, V> builder, AttributeModel am,
			Converter<V, U> customConverter, Validator<V> customValidator, Validator<V> customRequiredValidator) {

		if (customValidator != null) {
			builder.withValidator(customValidator);
		}
		if (customConverter != null) {
			if (am.getType().equals(String.class) || NumberUtils.isNumeric(am.getType())) {
				builder.withNullRepresentation((V) "");
			}
			builder.withConverter(customConverter);
		}

		if (customRequiredValidator != null) {
			builder.asRequired(customRequiredValidator);
		}

		if (am.isEmail() && customConverter == null) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			sBuilder.withNullRepresentation("").withValidator(
					new EmailValidator(messageService.getMessage("ocs.no.valid.email", VaadinUtils.getLocale())));
		} else if (am.isWeek()) {
			if (customConverter == null) {
				BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
				sBuilder.withConverter(new LocalDateWeekCodeConverter());
			}
		} else if (builder.getField() instanceof TextField) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			addTextFieldConvertersAndValidators(am, customConverter, sBuilder);
		} else if (builder.getField() instanceof URLField) {
			BindingBuilder<U, String> sBuilder = (BindingBuilder<U, String>) builder;
			sBuilder.withNullRepresentation("").withValidator(
					new URLValidator(messageService.getMessage("ocs.no.valid.url", VaadinUtils.getLocale())));
		}
	}

	/**
	 * Adds converters and validators for a text field
	 * 
	 * @param <U>
	 * @param <V>
	 * @param am              the attribute model for the field
	 * @param customConverter custom converter for the field (if any)
	 * @param sBuilder        the binding builder
	 */
	private <U, V> void addTextFieldConvertersAndValidators(AttributeModel am, Converter<V, U> customConverter,
			BindingBuilder<U, String> sBuilder) {
		if (customConverter == null) {
			sBuilder.withNullRepresentation("");
			if (am.getType().equals(BigDecimal.class)) {
				sBuilder.withConverter(ConverterFactory.createBigDecimalConverter(am.isCurrency(), am.isPercentage(),
						am.useThousandsGroupingInEditMode(), am.getPrecision(), VaadinUtils.getCurrencySymbol()));
			} else if (NumberUtils.isInteger(am.getType())) {
				sBuilder.withConverter(ConverterFactory.createIntegerConverter(am.useThousandsGroupingInEditMode(),
						am.isPercentage()));
			} else if (NumberUtils.isLong(am.getType())) {
				sBuilder.withConverter(
						ConverterFactory.createLongConverter(am.useThousandsGroupingInEditMode(), am.isPercentage()));
			} else if (NumberUtils.isDouble(am.getType())) {
				sBuilder.withConverter(
						ConverterFactory.createDoubleConverter(am.isCurrency(), am.useThousandsGroupingInEditMode(),
								am.isPercentage(), am.getPrecision(), VaadinUtils.getCurrencySymbol()));
			} else if (String.class.equals(am.getType()) && am.isTrimSpaces()) {
				sBuilder.withConverter(new TrimSpacesConverter());
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <S> DataProvider<S, SerializablePredicate<S>> castSharedProvider(
			DataProvider<?, SerializablePredicate<?>> provider) {
		return (DataProvider<S, SerializablePredicate<S>>) (DataProvider) provider;
	}

	/**
	 * Constructs a field for selecting multiple values from a collection. This can
	 * only be a lookup field or a token field
	 * 
	 * @param am               the attribute model
	 * @param fieldEntityModel the entity model
	 * @param fieldFilter      the field filter to apply
	 * @param sharedProvider   shared provider to use when multiple identical
	 *                         components are used e.g. in a grid
	 * @param search           whether the field is in search mode
	 * @param grid             whether the component appears inside a grid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> Component constructCollectionSelect(
			AttributeModel am, EntityModel<?> fieldEntityModel, SerializablePredicate<?> fieldFilter,
			DataProvider<?, SerializablePredicate<?>> sharedProvider, boolean search, boolean grid) {
		EntityModel<?> em = resolveEntityModel(fieldEntityModel, am, search);

		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator.getServiceForEntity(em.getEntityClass());
		SortOrder<?>[] sos = constructSortOrder(em);

		// mode depends on whether we are searching or inside a grid
		AttributeSelectMode mode = search ? am.getSearchSelectMode()
				: (grid ? am.getGridSelectMode() : am.getSelectMode());

		if (AttributeSelectMode.LOOKUP.equals(mode)) {
			// lookup field
			return constructLookupField(am, fieldEntityModel, fieldFilter, search, true, grid);
		} else {
			// by default, use a token field
			return new QuickAddTokenSelect<ID, S>((EntityModel<S>) em, am, service, mapPagingType(am.getPagingType()),
					(SerializablePredicate<S>) fieldFilter, castSharedProvider(sharedProvider), search, sos);
		}
	}

	/**
	 * Constructs a combo box for selectin a value from a collection
	 * 
	 * @param <ID>
	 * @param <S>
	 * @param am             the attribute model
	 * @param entityModel    the entity model
	 * @param fieldFilter    the field filter
	 * @param sharedProvider the shared data provider to use (if any)
	 * @param search         whether the component is part of a search form
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> QuickAddEntityComboBox<ID, S> constructComboBox(
			AttributeModel am, EntityModel<?> entityModel, SerializablePredicate<?> fieldFilter,
			DataProvider<?, SerializablePredicate<?>> sharedProvider, boolean search) {
		entityModel = resolveEntityModel(entityModel, am, search);
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());
		SortOrder<?>[] sortOrder = constructSortOrder(entityModel);

		return new QuickAddEntityComboBox<>((EntityModel<S>) entityModel, am, service,
				mapPagingType(am.getPagingType()), (SerializablePredicate<S>) fieldFilter, search,
				castSharedProvider(sharedProvider), null, sortOrder);
	}

	/**
	 * Constructs a combo box for displaying values from an enumeration
	 * 
	 * @param <E>       the type of the enumeration
	 * @param enumClass the class of the enumeration
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <E extends Enum> ComboBox constructEnumComboBox(Class<E> enumClass) {
		ComboBox cb = new ComboBox<>();

		// sort on the description
		List<E> list = Lists.newArrayList(enumClass.getEnumConstants());
		list.sort((a, b) -> {
			String msg1 = messageService.getEnumMessage(enumClass, a, VaadinUtils.getLocale());
			String msg2 = messageService.getEnumMessage(enumClass, b, VaadinUtils.getLocale());
			return msg1.compareToIgnoreCase(msg2);
		});

		cb.setDataProvider(new ListDataProvider<E>(list));
		cb.setItemLabelGenerator(e -> messageService.getEnumMessage(enumClass, (E) e, VaadinUtils.getLocale()));
		return cb;
	}

	/**
	 * Constructs a field - shortcut method that used the default context
	 * 
	 * @param am the attribute model to base the field on
	 * @return
	 */
	@Override
	public Component constructField(AttributeModel am) {
		return constructField(FieldFactoryContext.createDefault(am));
	}

	/**
	 * Constructs a field - this is the main method
	 * 
	 * @param context the context that governs how the field must be created
	 */
	public Component constructField(FieldFactoryContext context) {
		Component field = null;

		// delegate to managed field factory
		if (delegate != null) {
			field = delegate.constructField(context);
			if (field != null) {
				return field;
			}
		}

		AttributeModel am = context.getAttributeModel();
		Map<String, SerializablePredicate<?>> fieldFilters = context.getFieldFilters();
		EntityModel<?> fieldEntityModel = context.getFieldEntityModel();
		boolean search = context.isSearch();
		boolean grid = context.isEditableGrid();

		DataProvider<?, SerializablePredicate<?>> sharedProvider = context.getSharedProvider(am.getPath());

		// for read-only attributes, do not render a field unless it's a link field or
		// unless the component is inside a search form
		if (EditableType.READ_ONLY.equals(am.getEditableType()) && !AttributeType.DETAIL.equals(am.getAttributeType())
				&& !context.isSearch()) {
			return null;
		}

		SerializablePredicate<?> fieldFilter = fieldFilters == null ? null : fieldFilters.get(am.getPath());
		Locale dateLoc = VaadinUtils.getDateLocale();

		if (AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())) {
			field = constructForElementCollection(context, am, fieldEntityModel);
		} else if (AbstractEntity.class.isAssignableFrom(am.getType())) {
			// lookup or combo field for an entity
			field = constructSelect(am, fieldEntityModel, fieldFilter, sharedProvider, search, grid);
		} else if (Collection.class.isAssignableFrom(am.getType())) {
			// render a multiple select component for a collection
			field = constructCollectionSelect(am, fieldEntityModel, fieldFilter,
					(DataProvider<?, SerializablePredicate<?>>) sharedProvider, search, grid);
		} else if (AttributeTextFieldMode.TEXTAREA.equals(am.getTextFieldMode()) && !search && !grid) {
			// construct text area (but only when not inside search form or grid)
			field = new TextArea();
			((TextArea) field).setHeight(SystemPropertyUtils.getDefaultTextAreaHeight());
		} else if (Enum.class.isAssignableFrom(am.getType())) {
			field = constructEnumComboBox(am.getType().asSubclass(Enum.class));
		} else if (am.isBoolean()) {
			field = search ? constructSearchBooleanComboBox(am, search) : new Checkbox();
		} else if (am.isWeek()) {
			field = new TextField();
		} else if (search && AttributeSelectMode.TOKEN.equals(am.getSearchSelectMode())
				&& AttributeType.BASIC.equals(am.getAttributeType())) {
			// token field for searching distinct values
			field = constructSimpleTokenField(fieldEntityModel != null ? fieldEntityModel : am.getEntityModel(), am,
					am.getPath().substring(am.getPath().lastIndexOf('.') + 1), false, fieldFilter);
		} else if (LocalDate.class.equals(am.getType()) || (search && am.isSearchDateOnly())) {
			DatePicker df = new DatePicker();
			df.setLocale(dateLoc);
			df.setI18n(getDatePickerLocalization(dateLoc));
			field = df;
		} else if (LocalDateTime.class.equals(am.getType())) {
			DateTimePicker df = new DateTimePicker(dateLoc);
			df.setI18n(getDatePickerLocalization(dateLoc));
			field = df;
		} else if (ZonedDateTime.class.equals(am.getType())) {
			ZonedDateTimePicker df = new ZonedDateTimePicker(dateLoc);
			df.setI18n(getDatePickerLocalization(dateLoc));
			field = df;
		} else if (LocalTime.class.equals(am.getType())) {
			TimePicker tf = new TimePicker();
			tf.setLocale(dateLoc);
			field = tf;
		} else if (String.class.equals(am.getType()) || NumberUtils.isNumeric(am.getType())) {
			if (am.isUrl()) {
				TextField textField = new TextField();
				textField.setSizeFull();
				field = new URLField(textField, am, false);
			} else {
				TextField textField = new TextField();
				field = textField;
			}
		}
		if (field != null) {
			postProcessField(field, am, search, grid);
		}
		return field;
	}

	/**
	 * Constructs a component for managing a (simple) element collection
	 * 
	 * @param context          the context for creating the component
	 * @param am               the attribute model
	 * @param fieldEntityModel the field entity model
	 * @return
	 */
	private Component constructForElementCollection(FieldFactoryContext context, AttributeModel am,
			EntityModel<?> fieldEntityModel) {
		Component field = null;
		if (!context.isSearch()) {
			// use a "collection grid" for an element collection
			FormOptions fo = new FormOptions().setShowRemoveButton(true);

			boolean allowed = String.class.equals(am.getMemberType()) || NumberUtils.isLong(am.getMemberType())
					|| NumberUtils.isInteger(am.getMemberType()) || BigDecimal.class.equals(am.getMemberType());
			if (allowed) {
				field = new ElementCollectionGrid<>(am, fo);
			} else {
				// other types not supported for now
				throw new OCSRuntimeException(
						"Element collections of type " + am.getMemberType() + " are currently not supported");
			}
		} else {
			// token search field
			field = constructSimpleTokenField(fieldEntityModel != null ? fieldEntityModel : am.getEntityModel(), am,
					am.getPath().substring(am.getPath().lastIndexOf('.') + 1), true, null);
		}
		return field;
	}

	/**
	 * Constructs an internal link field
	 * 
	 * @param am          the attribute model
	 * @param entityModel the entity model of the entity to display in the field
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <ID extends Serializable, S extends AbstractEntity<ID>> Component constructInternalLinkField(
			AttributeModel am, EntityModel<?> entityModel) {
		EntityModel<?> em = resolveEntityModel(entityModel, am, true);
		return new InternalLinkField<>(am, (EntityModel<S>) em);
	}

	/**
	 * Constructs a list field for selecting a value from a collection
	 * 
	 * @param am               the attribute model
	 * @param fieldEntityModel the entity model
	 * @param fieldFilter      the field filter to apply
	 * @param sharedProvider   shared data provider
	 * @param search           whether the component is part of a search form
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> Component constructListSelect(AttributeModel am,
			EntityModel<?> fieldEntityModel, SerializablePredicate<?> fieldFilter,
			DataProvider<?, SerializablePredicate<?>> sharedProvider, boolean search) {
		EntityModel<?> em = resolveEntityModel(fieldEntityModel, am, search);
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator.getServiceForEntity(em.getEntityClass());
		SortOrder<?>[] sos = constructSortOrder(em);
		return new QuickAddListSingleSelect<>((EntityModel<S>) em, am, service, mapPagingType(am.getPagingType()),
				(SerializablePredicate<S>) fieldFilter, null, search, sos);
	}

	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>> EntityLookupField<ID, S> constructLookupField(
			AttributeModel am, EntityModel<?> overruled, SerializablePredicate<?> fieldFilter, boolean search,
			boolean multiSelect, boolean grid) {

		// for a lookup field, don't use the nested model but the base model -
		// this is
		// because the search in the pop-up screen is conducted on a "clean",
		// non-nested entity list so
		// using a path from the parent entity makes no sense here
		EntityModel<?> entityModel = overruled != null ? overruled
				: serviceLocator.getEntityModelFactory().getModel(am.getNormalizedType());
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(am.getMemberType() != null ? am.getMemberType() : entityModel.getEntityClass());
		SortOrder<?>[] sos = constructSortOrder(entityModel);
		return new EntityLookupField<>(service, (EntityModel<S>) entityModel, am,
				(SerializablePredicate<S>) fieldFilter, search, multiSelect,
				sos.length == 0 ? null : Lists.newArrayList(sos));
	}

	/**
	 * Constructs a combo box for filtering on a boolean property - this includes
	 * options for "true" and "false" but also allows searches on "no value"
	 * 
	 * @param am        the attribute model
	 * @param searching indicates whether the component appears in a search form
	 * @return
	 */
	private ComboBox<Boolean> constructSearchBooleanComboBox(AttributeModel am, boolean searching) {
		ComboBox<Boolean> cb = new ComboBox<>();
		ListDataProvider<Boolean> provider = new ListDataProvider<>(Lists.newArrayList(Boolean.TRUE, Boolean.FALSE));
		cb.setDataProvider(provider);
		cb.setItemLabelGenerator(b -> Boolean.TRUE.equals(b) ? am.getTrueRepresentation(VaadinUtils.getLocale())
				: am.getFalseRepresentation(VaadinUtils.getLocale()));
		cb.setRequiredIndicatorVisible(searching ? am.isRequiredForSearching() : am.isRequired());
		return cb;
	}

	/**
	 * Constructs a component for selecting a single value from a collection
	 * 
	 * @param am               the attribute model
	 * @param fieldEntityModel the field entity model
	 * @param fieldFilter      the field filter to apply
	 * @param sharedProvider   shared data provider to be used when component is
	 *                         inside a grid
	 * @param search
	 * @return
	 */
	private Component constructSelect(AttributeModel am, EntityModel<?> fieldEntityModel,
			SerializablePredicate<?> fieldFilter, DataProvider<?, SerializablePredicate<?>> sharedProvider,
			boolean search, boolean grid) {
		Component field = null;
		AttributeSelectMode selectMode = search ? am.getSearchSelectMode() : am.getSelectMode();
		if (grid) {
			selectMode = am.getGridSelectMode();
		}

		if (search && am.isMultipleSearch()) {
			// in case of multiple search, defer to the
			// "constructCollectionSelect" method
			field = this.constructCollectionSelect(am, fieldEntityModel, fieldFilter, sharedProvider, search, grid);
		} else if (AttributeSelectMode.COMBO.equals(selectMode)) {
			// combo box
			field = constructComboBox(am, fieldEntityModel, fieldFilter, sharedProvider, search);
		} else if (AttributeSelectMode.LOOKUP.equals(selectMode)) {
			// single select lookup field
			field = constructLookupField(am, fieldEntityModel, fieldFilter, search, false, grid);
		} else {
			// list select (single select)
			field = this.constructListSelect(am, fieldEntityModel, fieldFilter, sharedProvider, search);
		}
		return field;
	}

	/**
	 * Constructs a token field for looking up simple values (Strings, ints) from a
	 * property or element collection
	 * 
	 * @param entityModel       the entity model
	 * @param am                the attribute model
	 * @param distinctField     the database field from which to collect the
	 *                          distinct values
	 * @param elementCollection whether the lookup is from an element collection
	 * @param fieldFilter       field filter used to limit the matching entities
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <ID extends Serializable, S extends AbstractEntity<ID>, O extends Comparable<O>> SimpleTokenFieldSelect<ID, S, O> constructSimpleTokenField(
			EntityModel<?> entityModel, AttributeModel am, String distinctField, boolean elementCollection,
			SerializablePredicate<?> fieldFilter) {
		BaseService<ID, S> service = (BaseService<ID, S>) serviceLocator
				.getServiceForEntity(entityModel.getEntityClass());
		return new SimpleTokenFieldSelect<>(service, (EntityModel<S>) entityModel, am,
				(SerializablePredicate<S>) fieldFilter, distinctField, (Class<O>) am.getNormalizedType(),
				elementCollection);
	}

	@SuppressWarnings("unchecked")
	private SortOrder<String>[] constructSortOrder(final EntityModel<?> entityModel) {
		final SortOrder<String>[] sos = new SortOrder[entityModel.getSortOrder().size()];
		int i = 0;
		for (final AttributeModel am : entityModel.getSortOrder().keySet()) {
			sos[i++] = new SortOrder<>(am.getName(),
					entityModel.getSortOrder().get(am) ? SortDirection.ASCENDING : SortDirection.DESCENDING);
		}
		return sos;
	}

	private DatePickerI18n getDatePickerLocalization(Locale dateLoc) {
		DatePickerI18n dpi = new DatePickerI18n();
		dpi.setCalendar(messageService.getMessage("ocs.calendar.calendar", dateLoc));

		String weekdays = messageService.getMessage("ocs.calendar.days", dateLoc);
		if (weekdays != null) {
			dpi.setWeekdays(Lists.newArrayList(weekdays.split(",")));
		}

		String weekdaysShort = messageService.getMessage("ocs.calendar.days.short", dateLoc);
		if (weekdaysShort != null) {
			dpi.setWeekdaysShort(Lists.newArrayList(weekdaysShort.split(",")));
		}

		String months = messageService.getMessage("ocs.calendar.months", dateLoc);
		if (months != null) {
			dpi.setMonthNames(Lists.newArrayList(months.split(",")));
		}

		dpi.setCancel(messageService.getMessage("ocs.calendar.cancel", dateLoc));
		dpi.setClear(messageService.getMessage("ocs.calendar.clear", dateLoc));
		dpi.setFirstDayOfWeek(Integer.parseInt(messageService.getMessage("ocs.calendar.first", dateLoc)));
		dpi.setClear(messageService.getMessage("ocs.calendar.today", dateLoc));
		dpi.setWeek(messageService.getMessage("ocs.calendar.week", dateLoc));

		return dpi;
	}

	/**
	 * Maps the filter type from the attribute model to a select mode for the
	 * component
	 * 
	 * @param pagingType the pagingType to map
	 * @return the associated SelectMode
	 */
	private SelectMode mapPagingType(PagingType filterType) {
		switch (filterType) {
		case NON_PAGED:
			return SelectMode.FILTERED_ALL;
		case PAGED:
			return SelectMode.FILTERED_PAGED;
		}
		return null;
	}

	private void postProcessField(Component field, AttributeModel am, boolean search, boolean editableGrid) {
		String displayName = am.getDisplayName(VaadinUtils.getLocale());

		VaadinUtils.setLabel(field, editableGrid ? "" : displayName);
		VaadinUtils.setTooltip(field, am.getDescription(VaadinUtils.getLocale()));
		VaadinUtils.setPlaceHolder(field, am.getPrompt(VaadinUtils.getLocale()));

		if (field instanceof AbstractField) {
			AbstractField<?, ?> af = (AbstractField<?, ?>) field;
			af.setRequiredIndicatorVisible(search ? am.isRequiredForSearching() : am.isRequired());
		}

		// right alignment for text field
		if (NumberUtils.isNumeric(am.getType()) && editableGrid) {
			TextField atf = (TextField) field;
			atf.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
		}

		// add percentage sign
		if (am.isPercentage() && field instanceof TextField) {
			TextField atf = (TextField) field;
			atf.addBlurListener(event -> {
				String value = atf.getValue();
				if (!StringUtils.isEmpty(value) && value.indexOf('%') < 0) {
					value = value.trim() + "%";
					atf.setValue(value);
				}
			});
		}

		// add currency sign
		if (am.isCurrency() && field instanceof TextField) {
			TextField atf = (TextField) field;
			atf.addBlurListener(event -> {
				String value = atf.getValue();
				if (value != null && value.indexOf(VaadinUtils.getCurrencySymbol()) < 0) {
					value = VaadinUtils.getCurrencySymbol() + " " + value.trim();
					atf.setValue(value);
				}
			});
		}

	}

	/**
	 * Looks up the entity model to use for a certain field
	 * 
	 * @param entityModel the base entity model
	 * @param am          the attribute model to use for the file
	 * @param search      whether the screen is in search mode
	 * @return
	 */
	private EntityModel<?> resolveEntityModel(EntityModel<?> entityModel, final AttributeModel am, boolean search) {
		if (entityModel == null) {
			if (!Boolean.TRUE.equals(search) && am.getNestedEntityModel() != null) {
				entityModel = am.getNestedEntityModel();
			} else {
				final Class<?> type = am.getNormalizedType();
				entityModel = serviceLocator.getEntityModelFactory().getModel(type.asSubclass(AbstractEntity.class));
			}
		}
		return entityModel;
	}

}
