package com.ocs.dynamo.ui.composite.dialog;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;

/**
 * A simple modal dialog with a single "OK" button. The content of this dialog
 * can be customized
 * 
 * @author bas.rutten
 */
@SuppressWarnings("serial")
public abstract class SimpleModalDialog extends BaseModalDialog {

	private static final long serialVersionUID = -2265149201475495504L;

	private Button cancelButton;

	private Button okButton;

	private boolean showCancelButton;

	/**
	 * Constructor
	 */
	public SimpleModalDialog(boolean showCancelButton) {
		this.showCancelButton = showCancelButton;
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		okButton = new Button(
		        com.ocs.dynamo.ui.ServiceLocator.getMessageService().getMessage("ocs.ok"));
		okButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				boolean result = doClose();
				if (result) {
					SimpleModalDialog.this.close();
				}
			}
		});
		buttonBar.addComponent(okButton);

		cancelButton = new Button(
		        com.ocs.dynamo.ui.ServiceLocator.getMessageService().getMessage("ocs.cancel"));
		cancelButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				SimpleModalDialog.this.close();
			}
		});

		cancelButton.setVisible(showCancelButton);
		buttonBar.addComponent(cancelButton);
	}

	/**
	 * The method that is called just before closing the dialog
	 */
	protected boolean doClose() {
		// overwrite in subclass
		return true;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

	public Button getOkButton() {
		return okButton;
	}

}
