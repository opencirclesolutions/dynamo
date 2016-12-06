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
package com.ocs.dynamo.ui.composite.layout;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.OptimisticLockException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.exception.OCSValidationException;
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
import com.ocs.dynamo.utils.StringUtil;
import com.vaadin.data.Property;
import com.vaadin.data.util.NestedMethodProperty;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.converter.StringToBooleanConverter;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Base class for custom components - contains convenience methods for getting various often-used
 * services
 * 
 * @author bas.rutten
 */
public abstract class BaseCustomComponent extends CustomComponent implements Buildable {

	private static final Logger LOG = Logger.getLogger(BaseCustomComponent.class);

	private static final long serialVersionUID = -8982555842423738005L;

	private MessageService messageService = ServiceLocator.getMessageService();

	/**
	 * Constructs a (formatted) label based on the attribute model
	 * 
	 * @param entity
	 *            the entity that is being displayed
	 * @param attributeModel
	 *            the attribute model
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
				// week code
				property = new ObjectProperty<Date>((Date) value);
				fieldLabel.setConverter(new WeekCodeConverter());
				fieldLabel.setPropertyDataSource(property);
			} else if (String.class.equals(type)) {
				// String
				String str = (String) value;
				str = str.replace("\n", StringUtil.getHtmlLineBreak());
				property = new ObjectProperty<String>(str);
				fieldLabel.setPropertyDataSource(property);
				fieldLabel.setContentMode(ContentMode.HTML);
			} else if (Date.class.equals(type)) {
				property = new ObjectProperty<Date>((Date) value);
				if (AttributeDateType.TIME.equals(attributeModel.getDateType())) {
					// for a time, do not include a time zone (we have no way of
					// knowing it!)
					fieldLabel
					        .setConverter(new FormattedStringToDateConverter(null, attributeModel.getDisplayFormat()));
				} else if (AttributeDateType.TIMESTAMP.equals(attributeModel.getDateType())) {
					fieldLabel.setConverter(new FormattedStringToDateConverter(
					        VaadinUtils.getTimeZone(UI.getCurrent()), attributeModel.getDisplayFormat()));
				} else {
					// just a date
					fieldLabel
					        .setConverter(new FormattedStringToDateConverter(null, attributeModel.getDisplayFormat()));
				}
			} else if (attributeModel.getType().isEnum()) {
				String msg = getMessageService().getEnumMessage((Class<Enum<?>>) attributeModel.getType(),
				        (Enum<?>) value);
				if (msg != null) {
					fieldLabel.setValue(msg);
				}
			} else if (BigDecimal.class.equals(type)) {
				property = new ObjectProperty<BigDecimal>((BigDecimal) value);
				fieldLabel.setConverter(ConverterFactory.createBigDecimalConverter(attributeModel.isCurrency(),
				        attributeModel.isPercentage(), attributeModel.isUseThousandsGrouping(),
				        attributeModel.getPrecision(), VaadinUtils.getCurrencySymbol()));
			} else if (Number.class.isAssignableFrom(type)) {
				// other number types
				property = new ObjectProperty<Number>((Number) value);
				fieldLabel.setConverter(ConverterFactory.createConverterFor(type, attributeModel,
				        attributeModel.isUseThousandsGrouping()));
			} else if (AbstractEntity.class.isAssignableFrom(type)) {
				// another entity - use the value of the "displayProperty"
				EntityModel<?> model = getEntityModelFactory().getModel(type);
				String displayProperty = model.getDisplayProperty();
				property = new NestedMethodProperty<String>(value, displayProperty);
			} else if (attributeModel.isImage()) {
				// create image preview
				final byte[] bytes = ClassUtils.getBytes(entity, attributeModel.getName());
				Embedded image = new DefaultEmbedded(attributeModel.getDisplayName(), bytes);
				image.setStyleName(DynamoConstants.CSS_CLASS_UPLOAD);
				return image;
			} else if (Boolean.class.equals(attributeModel.getType()) || boolean.class.equals(attributeModel.getType())) {
				if (!StringUtils.isEmpty(attributeModel.getTrueRepresentation()) && Boolean.TRUE.equals(value)) {
					property = new ObjectProperty<String>(attributeModel.getTrueRepresentation());
				} else if (!StringUtils.isEmpty(attributeModel.getFalseRepresentation()) && Boolean.FALSE.equals(value)) {
					property = new ObjectProperty<String>(attributeModel.getFalseRepresentation());
				} else {
					property = new ObjectProperty<Boolean>((Boolean) value);
					fieldLabel.setConverter(new StringToBooleanConverter());
				}
			} else if (Iterable.class.isAssignableFrom(attributeModel.getType())) {
				// collection of entities
				String str = TableUtils.formatEntityCollection(getEntityModelFactory(), attributeModel,
				        (Iterable<?>) value);
				property = new ObjectProperty<String>(str);
				fieldLabel.setPropertyDataSource(property);
			}

			if (attributeModel.isNumerical()) {
				fieldLabel.setStyleName(DynamoConstants.CSS_NUMERICAL);
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

	/**
	 * Generic handling of error messages after a save operation
	 * 
	 * @param ex
	 *            the exception that occurred
	 */
	protected void handleSaveException(RuntimeException ex) {
		if (ex instanceof OCSValidationException) {
			// validation exception
			LOG.error(ex.getMessage(), ex);
			showNotifification(((OCSValidationException) ex).getErrors().get(0), Notification.Type.ERROR_MESSAGE);
		} else if (ex instanceof OCSRuntimeException) {
			// any other OCS runtime exception
			LOG.error(ex.getMessage(), ex);
			showNotifification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} else if (ex instanceof OptimisticLockException) {
			// optimistic lock
			LOG.error(ex.getMessage(), ex);
			showNotifification(message("ocs.optimistic.lock"), Notification.Type.ERROR_MESSAGE);
		} else {
			// any other save exception
			LOG.error(ex.getMessage(), ex);
			showNotifification(message("ocs.error.occurred"), Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Retrieves a message from the message bundle
	 * 
	 * @param key
	 *            the key of the message
	 * @return
	 */
	protected String message(String key) {
		return getMessageService().getMessage(key);
	}

	/**
	 * Retrieves a message from the message bundle
	 * 
	 * @param key
	 *            the key of the message
	 * @param args
	 *            any arguments that are used in the message
	 * @return
	 */
	protected String message(String key, Object... args) {
		return getMessageService().getMessage(key, args);
	}

	/**
	 * Shows a notification message - this method will check for the availability of a Vaadin Page
	 * object and if this is not present, write the notification to the log instead
	 * 
	 * @param message
	 *            the message
	 * @param type
	 *            the type of the message (error, warning, tray etc.)
	 */
	protected void showNotifification(String message, Notification.Type type) {
		if (Page.getCurrent() != null) {
			Notification.show(message, type);
		} else {
			LOG.info(message);
		}
	}
}
