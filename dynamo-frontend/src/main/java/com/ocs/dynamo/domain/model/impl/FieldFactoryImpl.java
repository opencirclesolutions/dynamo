package com.ocs.dynamo.domain.model.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EditableType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.FieldFactory;
import com.ocs.dynamo.domain.model.NumberSelectMode;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocator;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.converter.BigDecimalToDoubleConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.IntToDoubleConverter;
import com.ocs.dynamo.ui.converter.LongToDoubleConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.Binder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextField;

public class FieldFactoryImpl<T> implements FieldFactory {

	private static final long serialVersionUID = -5684112523268959448L;

	private static final ConcurrentMap<String, FieldFactoryImpl<?>> nonValidatingInstances = new ConcurrentHashMap<>();

	private static final ConcurrentMap<String, FieldFactoryImpl<?>> searchInstances = new ConcurrentHashMap<>();

	private static final ConcurrentMap<String, FieldFactoryImpl<?>> validatingInstances = new ConcurrentHashMap<>();

	private final MessageService messageService;

	private final EntityModel<T> model;

	private final ServiceLocator serviceLocator = ServiceLocatorFactory.getServiceLocator();

	private final Collection<FieldFactory> fieldFactories;

	// indicates whether the system is in search mode. In search mode,
	// components for
	// some attributes are constructed differently (e.g. we render two search
	// fields to be able to
	// search for a range of integers)
	private final boolean search;

	// indicates whether extra validators must be added. This is the case when
	// using the field factory in an
	// editable table
	private final boolean validate;

	/**
	 * Constructor
	 *
	 * @param model          the entity model
	 * @param messageService the message service
	 * @param validate       whether to add extra validators (this is the case when
	 *                       the field is displayed inside a table)
	 * @param search         whether the fields are displayed inside a search form
	 *                       (this has an effect on the construction of some fields)
	 */
	public FieldFactoryImpl(final EntityModel<T> model, final MessageService messageService, final boolean validate,
			final boolean search) {
		this.model = model;
		this.messageService = messageService;
		this.validate = validate;
		this.search = search;
		this.fieldFactories = serviceLocator.getServices(FieldFactory.class);
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 *
	 * @param model          the entity model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> FieldFactoryImpl<T> getInstance(final EntityModel<T> model, final MessageService messageService) {
		if (!nonValidatingInstances.containsKey(model.getReference())) {
			nonValidatingInstances.put(model.getReference(),
					new FieldFactoryImpl<>(model, messageService, false, false));
		}
		return (FieldFactoryImpl<T>) nonValidatingInstances.get(model.getReference());
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 *
	 * @param model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> FieldFactoryImpl<T> getSearchInstance(final EntityModel<T> model,
			final MessageService messageService) {
		if (!searchInstances.containsKey(model.getReference())) {
			searchInstances.put(model.getReference(), new FieldFactoryImpl<>(model, messageService, false, true));
		}
		return (FieldFactoryImpl<T>) searchInstances.get(model.getReference());
	}

	/**
	 * Returns an appropriate instance from the pool, or creates a new one
	 *
	 * @param model
	 * @param messageService
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> FieldFactoryImpl<T> getValidatingInstance(final EntityModel<T> model,
			final MessageService messageService) {
		if (!validatingInstances.containsKey(model.getReference())) {
			validatingInstances.put(model.getReference(), new FieldFactoryImpl<>(model, messageService, true, false));
		}
		return (FieldFactoryImpl<T>) validatingInstances.get(model.getReference());
	}

	@Override
	public AbstractField<?> constructField(Context<?> context) {
		// TODO Auto-generated method stub
		return null;
	}

	public <S> AbstractField<?> constructField(AttributeModel am, Map<String, SerializablePredicate<?>> fieldFilters,
			EntityModel<?> entityModel) {
		AbstractField<?> field = null;

		// in certain cases, never render a field
		if (EditableType.READ_ONLY.equals(am.getEditableType())
				&& (!am.isUrl() && !AttributeType.DETAIL.equals(am.getAttributeType())) && !search) {
			return null;
		}

		if ((NumberUtils.isLong(am.getType()) || NumberUtils.isInteger(am.getType())
				|| BigDecimal.class.equals(am.getType())) && NumberSelectMode.SLIDER.equals(am.getNumberSelectMode())) {
			final Slider slider = new Slider(am.getDisplayName());

			if (NumberUtils.isInteger(am.getType())) {
				// binder.forField(slider).withConverter(new IntToDoubleConverter());
			} else if (NumberUtils.isLong(am.getType())) {
				// binder.forField(slider).withConverter(new LongToDoubleConverter());
			} else {
				// binder.forField(slider).withConverter(new BigDecimalToDoubleConverter());
				slider.setResolution(am.getPrecision());
			}

			if (am.getMinValue() != null) {
				slider.setMin(am.getMinValue());
			}
			if (am.getMaxValue() != null) {
				slider.setMax(am.getMaxValue());
			}
			field = slider;
		} else if (LocalDate.class.equals(am.getType()) || LocalDateTime.class.equals(am.getType())) {
			field = new DateField();
		} else if (String.class.equals(am.getType()) || NumberUtils.isNumeric(am.getType())) {
			field = new TextField();
		}

		if (field != null) {
			postProcessField(field, am);
		}

		return field;
	}

	private void postProcessField(final AbstractField<?> field, final AttributeModel am) {
		field.setCaption(am.getDisplayName());
		field.setDescription(am.getDescription());

		if (field instanceof AbstractTextField) {
			final AbstractTextField textField = (AbstractTextField) field;
			textField.setDescription(am.getDescription());
//			if (!StringUtils.isEmpty(am.getPrompt())) {
//				textField.setInputPrompt(am.getPrompt());
//			}

			// set converters
			setConverters(textField, am);

			// add email validator
//			if (am.isEmail()) {
//				field.addValidator(
//						new EmailValidator(messageService.getMessage("ocs.no.valid.email", VaadinUtils.getLocale())));
//			}
		} else if (field instanceof DateField) {
			// set a separate format for a date field
			final DateField dateField = (DateField) field;
			if (am.getDisplayFormat() != null) {
				dateField.setDateFormat(am.getDisplayFormat());
			}

			if (AttributeDateType.TIMESTAMP.equals(am.getDateType())) {
				// dateField.setResolution(Resolution.SECOND);
			}
		}

	}

	/**
	 * Sets the appropriate converter on a text field
	 * 
	 * @param textField the text field
	 * @param am        the attribute model
	 */
	protected void setConverters(final AbstractTextField textField, final AttributeModel am) {
		Binder<T> binder = new Binder<>();
		if (am.getType().equals(BigDecimal.class)) {
			binder.forField(textField)
					.withConverter(ConverterFactory.createBigDecimalConverter(am.isCurrency(), am.isPercentage(),
							SystemPropertyUtils.useThousandsGroupingInEditMode(), am.getPrecision(),
							VaadinUtils.getCurrencySymbol()));
		} else if (NumberUtils.isInteger(am.getType())) {
			binder.forField(textField).withConverter(ConverterFactory
					.createIntegerConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), am.isPercentage()));
		} else if (NumberUtils.isLong(am.getType())) {
			binder.forField(textField).withConverter(ConverterFactory
					.createLongConverter(SystemPropertyUtils.useThousandsGroupingInEditMode(), am.isPercentage()));
		}
	}

}
