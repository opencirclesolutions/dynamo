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

import java.util.function.Supplier;

import com.ocs.dynamo.constants.DynamoConstants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;

import lombok.Getter;
import lombok.Setter;

/**
 * A simple modal dialog
 * 
 * @author Bas Rutten
 *
 */
@Getter
@Setter
public class SimpleModalDialog extends BaseModalDialog {

	private static final long serialVersionUID = -2265149201475495504L;

	private Button cancelButton;

	private Button okButton;

	private boolean showCancelButton;

	private Runnable onCancel;

	private Supplier<Boolean> onClose = () -> true;

	public SimpleModalDialog(boolean showCancelButton) {
		this(showCancelButton, DynamoConstants.CSS_DIALOG);
	}

	/**
	 * Constructor
	 * 
	 * @param showCancelButton whether do display the "Cancel" button
	 */
	public SimpleModalDialog(boolean showCancelButton, String className) {
		super(className);
		this.showCancelButton = showCancelButton;

		setBuildButtonBar(buttonBar -> {
			cancelButton = new Button(message("ocs.cancel"));
			cancelButton.setIcon(VaadinIcon.BAN.create());
			cancelButton.addClickListener(event -> {
				if (onCancel != null) {
					onCancel.run();
				}
				SimpleModalDialog.this.close();
			});
			cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			cancelButton.setVisible(showCancelButton);
			buttonBar.add(cancelButton);

			okButton = new Button(message("ocs.ok"));
			okButton.setIcon(VaadinIcon.CHECK.create());
			okButton.addClickListener(event -> {
				if (onClose.get()) {
					SimpleModalDialog.this.close();
				}
			});
			buttonBar.add(okButton);
		});
	}

}
