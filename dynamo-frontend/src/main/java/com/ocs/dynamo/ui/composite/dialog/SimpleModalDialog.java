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

import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

/**
 * A simple modal dialog with a single "OK" button. The content of this dialog
 * can be customized
 * 
 * @author bas.rutten
 */
public abstract class SimpleModalDialog extends BaseModalDialog {

	private static final long serialVersionUID = -2265149201475495504L;

	private Button cancelButton;

	private Button okButton;

	private boolean showCancelButton;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * Constructor
	 */
	public SimpleModalDialog(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		cancelButton = new Button(message("ocs.cancel"));
		cancelButton.setIcon(FontAwesome.BAN);
		cancelButton.addClickListener((Button.ClickListener) event -> {
			doCancel();
			SimpleModalDialog.this.close();
		});
		cancelButton.setVisible(showCancelButton);
		buttonBar.addComponent(cancelButton);

		okButton = new Button(message("ocs.ok"));
		okButton.setIcon(FontAwesome.CHECK);
		okButton.addClickListener((Button.ClickListener) event -> {
			if (doClose()) {
				SimpleModalDialog.this.close();
			}
		});
		buttonBar.addComponent(okButton);

	}

	/**
	 * The method that is called just before closing the dialog
	 */
	protected boolean doClose() {
		// overwrite in subclass
		return true;
	}

	/**
	 *
	 */
	protected void doCancel() {
		// overwrite in subclass
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public Button getOkButton() {
		return okButton;
	}

	protected String message(String key) {
		return messageService.getMessage(key, VaadinUtils.getLocale());
	}

	/**
	 * Retrieves a message based on its key
	 *
	 * @param key
	 *            the key of the message
	 * @param args
	 *            any arguments to pass to the message
	 * @return
	 */
	protected String message(String key, Object... args) {
		return messageService.getMessage(key, VaadinUtils.getLocale(), args);
	}

	public MessageService getMessageService() {
		return messageService;
	}
}
