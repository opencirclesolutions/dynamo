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

import java.util.function.Consumer;

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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Base class for modal dialogs. This class has an empty button bar and no
 * content. Subclasses should use the "setBuildMainLayout" and "setBuildButtonBar"
 * methods to add actual content
 * 
 * @author bas.rutten
 */
@Slf4j
@Getter
@Setter
public abstract class BaseModalDialog extends Dialog implements Buildable {

	private static final long serialVersionUID = -2265149201475495504L;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	private Consumer<VerticalLayout> buildMainLayout;

	private Consumer<HorizontalLayout> buildButtonBar;

	private String className;

	private Consumer<HorizontalLayout> postProcessButtonBar;

	private String title;

	protected BaseModalDialog() {
		this(DynamoConstants.CSS_DIALOG);
	}

	protected BaseModalDialog(String className) {
		this.className = className;
	}

	@Override
	public void build() {
		setCloseOnOutsideClick(false);
		constructLayout();
	}

	public void buildAndOpen() {
		build();
		open();
	}
	
	@Override
	public void open() {
		super.open();
		UI.getCurrent().beforeClientResponse(this, executionContext -> UI.getCurrent().setChildComponentModal(this, false));
	}

	/**
	 * Constructs the layout
	 */
	private void constructLayout() {
		VerticalLayout main = new DefaultVerticalLayout(false, false);
		main.addClassName(className);
		add(main);

		VerticalLayout titleLayout = new DefaultVerticalLayout(false, false);
		titleLayout.setPadding(true);
		titleLayout.add(new Text(title));
		titleLayout.addClassName(DynamoConstants.CSS_DIALOG_TITLE);
		main.add(titleLayout);

		VerticalLayout parent = new DefaultVerticalLayout(true, false);
		main.add(parent);

		if (buildMainLayout != null) {
			buildMainLayout.accept(parent);
		}

		DefaultHorizontalLayout buttonBar = new DefaultHorizontalLayout(true, false);
		main.add(buttonBar);
		if (buildButtonBar != null) {
			buildButtonBar.accept(buttonBar);
		}

		if (postProcessButtonBar != null) {
			postProcessButtonBar.accept(buttonBar);
		}

	}

	public MessageService getMessageService() {
		return messageService;
	}

	/**
	 * Retrieves a localized message
	 * 
	 * @param key the message key
	 * @return the message
	 */
	protected String message(String key) {
		return messageService.getMessage(key, VaadinUtils.getLocale());
	}

	/**
	 * Retrieves a message based on its key
	 * 
	 * @param key  the key of the message
	 * @param args any arguments to pass to the message
	 * @return the message
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
	 */
	protected void showNotification(String message) {
		if (UI.getCurrent() != null && UI.getCurrent().getPage() != null) {
			Notification.show(message, SystemPropertyUtils.getDefaultMessageDisplayTime(), Position.MIDDLE)
					.addThemeVariants(NotificationVariant.LUMO_ERROR);
		} else {
			log.info(message);
		}
	}
}
