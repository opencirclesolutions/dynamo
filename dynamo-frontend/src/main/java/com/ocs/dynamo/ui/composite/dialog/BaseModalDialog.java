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
package com.ocs.dynamo.ui.composite.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Base class for modal dialogs. This class has an empty button bar and no
 * content. Subclasses should implement the "doBuildButtonBar" method to add the
 * appropriate buttons and "doBuild" to add the appropriate content
 * 
 * @author bas.rutten
 */
public abstract class BaseModalDialog extends Dialog implements Buildable {

	private static final Logger LOG = LoggerFactory.getLogger(BaseModalDialog.class);

	private static final long serialVersionUID = -2265149201475495504L;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	@Override
	public void build() {
		setCloseOnOutsideClick(false);
		constructLayout();
	}

	public void buildAndOpen() {
		build();
		open();
	}

	/**
	 * Constructs the layout
	 */
	private void constructLayout() {
		VerticalLayout main = new DefaultVerticalLayout(false, false);
		main.addClassName(getStyleName());
		add(main);

		// differently colored title layout
		VerticalLayout titleLayout = new DefaultVerticalLayout(false, false);
		titleLayout.setPadding(true);
		titleLayout.add(new Text(getTitle()));
		titleLayout.addClassName(DynamoConstants.CSS_DIALOG_TITLE);
		main.add(titleLayout);

		VerticalLayout parent = new DefaultVerticalLayout(true, true);
		main.add(parent);
		
		doBuild(parent);

		DefaultHorizontalLayout buttonBar = new DefaultHorizontalLayout(true, false);
		main.add(buttonBar);

		doBuildButtonBar(buttonBar);
	}

	/**
	 * Constructs the actual contents of the window
	 * 
	 * @param parent the parent layout to which to add the specific components
	 */
	protected abstract void doBuild(VerticalLayout parent);

	/**
	 * Constructs the button bar
	 * 
	 * @param buttonBar the button bar
	 */
	protected abstract void doBuildButtonBar(HorizontalLayout buttonBar);

	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Returns the CSS style name. Can be overridden in subclass to modify styling
	 * 
	 * @return
	 */
	protected String getStyleName() {
		return DynamoConstants.CSS_DIALOG;
	}

	/**
	 * Returns the title of the dialog
	 * 
	 * @return
	 */
	protected abstract String getTitle();

	/**
	 * Retrieves a localized message
	 * 
	 * @param key the message key
	 * @return
	 */
	protected String message(String key) {
		return messageService.getMessage(key, VaadinUtils.getLocale());
	}

	/**
	 * Retrieves a message based on its key
	 * 
	 * @param key  the key of the message
	 * @param args any arguments to pass to the message
	 * @return
	 */
	protected String message(String key, Object... args) {
		return messageService.getMessage(key, VaadinUtils.getLocale(), args);
	}

	/**
	 * Shows a notification message - this method will check for the availability of
	 * a Vaadin Page object and if this is not present, write the notification to
	 * the log instead
	 * 
	 * @param message the message
	 * @param type    the type of the message
	 */
	protected void showNotification(String message) {
		if (UI.getCurrent() != null && UI.getCurrent().getPage() != null) {
			Notification.show(message, SystemPropertyUtils.getDefaultMessageDisplayTime(), Position.MIDDLE)
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
		} else {
			LOG.info(message);
		}
	}
}
