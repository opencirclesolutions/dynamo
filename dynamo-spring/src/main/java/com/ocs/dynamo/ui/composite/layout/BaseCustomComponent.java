package com.ocs.dynamo.ui.composite.layout;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.component.DefaultEmbedded;
import com.ocs.dynamo.ui.composite.table.TableUtils;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.FormattedStringToDateConverter;
import com.ocs.dynamo.ui.converter.WeekCodeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Property;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToBooleanConverter;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Base class for custom components - contains convenience methods for getting
 * various often-used services
 * 
 * @author bas.rutten
 * 
 */
public abstract class BaseCustomComponent extends CustomComponent implements Buildable {

	private static final long serialVersionUID = -8982555842423738005L;

	private MessageService messageService = ServiceLocator.getMessageService();

	/**
	 * Constructs a (formatted) label based on the attribute model
	 * 
	 * @param entity
	 *            the entity that is being displayed
	 * @param attributeModel
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected Component constructLabel(Object entity, AttributeModel attributeModel) {
		Label fieldLabel = new Label();
		fieldLabel.setCaption(attributeModel.getDisplayName());

		Object value = ClassUtils.getFieldValue(entity, attributeModel.getName());
		if (value != null) {
			Class<?> type = attributeModel.getType();
			Property<?> property = null;
			if (attributeModel.isWeek()) {
				property = new ObjectProperty<Date>((Date) value);
				fieldLabel.setConverter(new WeekCodeConverter());
				fieldLabel.setPropertyDataSource(property);
			} else if (String.class.equals(type)) {
				// string
				property = new ObjectProperty<String>((String) value);
				fieldLabel.setPropertyDataSource(property);
			} else if (Date.class.equals(type)) {
				property = new ObjectProperty<Date>((Date) value);
				if (AttributeDateType.TIME.equals(attributeModel.getDateType())) {
					// for a time, do not include a time zone (we have no way of
					// knowing it!)
					fieldLabel.setConverter(new FormattedStringToDateConverter(null, attributeModel
							.getDisplayFormat()));
				} else {
					fieldLabel.setConverter(new FormattedStringToDateConverter(VaadinUtils
							.getTimeZone(UI.getCurrent()), attributeModel.getDisplayFormat()));
				}
			} else if (attributeModel.getType().isEnum()) {
				String msg = getMessageService().getEnumMessage(
						(Class<Enum<?>>) attributeModel.getType(), (Enum<?>) value);
				if (msg != null) {
					fieldLabel.setValue(msg);
				}
			} else if (BigDecimal.class.equals(type)) {
				property = new ObjectProperty<BigDecimal>((BigDecimal) value);
				fieldLabel.setConverter(ConverterFactory.createBigDecimalConverter(
						attributeModel.isCurrency(), attributeModel.isPercentage(), true,
						attributeModel.getPrecision(), VaadinUtils.getCurrencySymbol()));
			} else if (Integer.class.equals(type)) {
				property = new ObjectProperty<Integer>((Integer) value);
				fieldLabel.setConverter(ConverterFactory.createIntegerConverter(true));
			} else if (Long.class.equals(type)) {
				property = new ObjectProperty<Long>((Long) value);
				fieldLabel.setConverter(ConverterFactory.createLongConverter(true));
			} else if (AbstractEntity.class.isAssignableFrom(type)) {
				// another entity - use the value of the "displayProperty"
				EntityModel<?> model = getEntityModelFactory().getModel(type);
				String displayProperty = model.getDisplayProperty();
				property = new NestedMethodProperty<String>(value, displayProperty);
			} else if (attributeModel.isImage()) {
				// create image preview
				final byte[] bytes = ClassUtils.getBytes(entity, attributeModel.getName());
				Embedded image = new DefaultEmbedded(attributeModel.getDisplayName(), bytes);
				image.setStyleName(OCSConstants.CSS_CLASS_UPLOAD);
				return image;
			} else if (Boolean.class.equals(attributeModel.getType())
					|| boolean.class.equals(attributeModel.getType())) {
				if (!StringUtils.isEmpty(attributeModel.getTrueRepresentation())
						&& Boolean.TRUE.equals(value)) {
					property = new ObjectProperty<String>(attributeModel.getTrueRepresentation());
				} else if (!StringUtils.isEmpty(attributeModel.getFalseRepresentation())
						&& Boolean.FALSE.equals(value)) {
					property = new ObjectProperty<String>(attributeModel.getFalseRepresentation());
				} else {
					property = new ObjectProperty<Boolean>((Boolean) value);
					fieldLabel.setConverter(new StringToBooleanConverter());
				}
			} else if (Iterable.class.isAssignableFrom(attributeModel.getType())) {
				// collection of entities
				String str = TableUtils.formatEntityCollection(getEntityModelFactory(),
						(Iterable<?>) value);
				property = new ObjectProperty<String>(str);
				fieldLabel.setPropertyDataSource(property);
			}

			if (attributeModel.isNumerical()) {
				fieldLabel.setStyleName(OCSConstants.CSS_NUMERICAL);
			}

			if (property != null) {
				fieldLabel.setPropertyDataSource(property);
			}
		}

		return fieldLabel;
	}

	protected EntityModelFactory getEntityModelFactory() {
		return ServiceLocator.getEntityModelFactory();
	}

	protected MessageService getMessageService() {
		return messageService;
	}

	protected <T> T getService(Class<T> clazz) {
		return ServiceLocator.getService(clazz);
	}

	protected String message(String key) {
		return getMessageService().getMessage(key);
	}

	protected String message(String key, Object... args) {
		return getMessageService().getMessage(key, args);
	}

}
