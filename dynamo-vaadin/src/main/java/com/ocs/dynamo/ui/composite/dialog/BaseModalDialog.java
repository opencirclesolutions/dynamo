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

import org.apache.log4j.Logger;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.vaadin.server.Page;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Base class for modal dialogs. This class has an empty button bar and no content. Subclasses
 * should implement the "doBuildButtonBar" method to add the appropriate buttons and "doBuild" to
 * add the appropriate content
 * 
 * @author bas.rutten
 */
public abstract class BaseModalDialog extends Window implements Buildable {

	private static final Logger LOG = Logger.getLogger(BaseModalDialog.class);

	private static final long serialVersionUID = -2265149201475495504L;

	/**
	 * Constructor
	 */
	public BaseModalDialog() {
	}

	@Override
	public void build() {
		constructLayout();
	}

	/**
	 * Constructs the layout
	 */
	private void constructLayout() {
		this.setModal(true);
		this.setResizable(false);

		Panel panel = new Panel();
		panel.setCaptionAsHtml(true);
		panel.setCaption(getTitle());

		this.setContent(panel);

		VerticalLayout main = new DefaultVerticalLayout();
		main.setStyleName(DynamoConstants.CSS_OCS_DIALOG);
		panel.setContent(main);

		doBuild(main);

		DefaultHorizontalLayout buttonBar = new DefaultHorizontalLayout();
		main.addComponent(buttonBar);

		doBuildButtonBar(buttonBar);
	}

	/**
	 * Constructs the actual contents of the window
	 * 
	 * @param parent
	 *            the parent layout to which to add the specific components
	 */
	protected abstract void doBuild(Layout parent);

	/**
	 * Constructs the button bar
	 * 
	 * @param buttonBar
	 *            the button bar
	 */
	protected abstract void doBuildButtonBar(HorizontalLayout buttonBar);

	/**
	 * Returns the title of the dialog
	 * 
	 * @return
	 */
	protected abstract String getTitle();

	/**
	 * Shows a notification message - this method will check for the availability of a Vaadin Page
	 * object and if this is not present, write the notification to the log instead
	 * 
	 * @param message
	 *            the message
	 * @param type
	 *            the type of the message
	 */
	protected void showNotifification(String message, Notification.Type type) {
		if (Page.getCurrent() != null) {
			Notification.show(message, type);
		} else {
			LOG.info(message);
		}
	}
}
