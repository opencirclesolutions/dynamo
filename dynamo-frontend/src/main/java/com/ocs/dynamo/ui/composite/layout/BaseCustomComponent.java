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

import javax.persistence.OptimisticLockException;

import org.apache.log4j.Logger;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.BaseUI;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * Base class for custom components - contains convenience methods for getting
 * various often-used services
 * 
 * @author bas.rutten
 */
public abstract class BaseCustomComponent extends CustomComponent implements Buildable {

	private static final Logger LOG = Logger.getLogger(BaseCustomComponent.class);

	private static final long serialVersionUID = -8982555842423738005L;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * Constructs a (formatted) label based on the attribute model
	 *
	 * @param entity
	 *            the entity that is being displayed
	 * @param attributeModel
	 *            the attribute model
	 * @return
	 */
	protected Component constructLabel(Object entity, AttributeModel attributeModel) {
		Label fieldLabel = new Label("", ContentMode.HTML);
		fieldLabel.setCaption(attributeModel.getDisplayName());
		Object value = ClassUtils.getFieldValue(entity, attributeModel.getName());
		String formatted = FormatUtils.formatPropertyValue(getEntityModelFactory(), attributeModel, value,
				"<br/>");
		fieldLabel.setValue(formatted);
		return fieldLabel;
	}

	protected EntityModelFactory getEntityModelFactory() {
		return ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();
	}

	protected MessageService getMessageService() {
		return messageService;
	}

	protected <T> T getService(Class<T> clazz) {
		return ServiceLocatorFactory.getServiceLocator().getService(clazz);
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
			LOG.warn(ex.getMessage(), ex);
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
		return getMessageService().getMessage(key, VaadinUtils.getLocale());
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
		return getMessageService().getMessage(key, VaadinUtils.getLocale(), args);
	}

	/**
	 * Navigates to the specified view
	 *
	 * @param view
	 *            the ID of the view
	 */
	protected void navigate(String viewName) {
		BaseUI ui = (BaseUI) UI.getCurrent();
		ui.navigate(viewName);
	}

	/**
	 * Shows a notification message - this method will check for the
	 * availability of
	 * a Vaadin Page object and if this is not present, write
	 * the notification to
	 * the log instead
	 *
	 * @param message
	 *            the message
	 * @param type
	 *            the type of the message (error, warning, tray etc.)
	 */
	protected void showNotifification(String message, Notification.Type type) {
		if (Page.getCurrent() != null) {
			Notification not = new Notification(message, type);
			if (Notification.Type.TRAY_NOTIFICATION.equals(type)) {
				// custom settings for tray notification
				not.setStyleName("dynamoTray");
				not.setDelayMsec(1000);
				not.setPosition(Position.MIDDLE_CENTER);
			}
			not.show(Page.getCurrent());

		} else {
			LOG.info(message);
		}
	}
}
