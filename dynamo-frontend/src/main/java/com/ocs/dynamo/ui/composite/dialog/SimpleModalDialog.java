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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * A simple modal dialog
 * 
 * @author Bas Rutten
 *
 */
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
        cancelButton = new Button(message("ocs.cancel"));
        cancelButton.setIcon(VaadinIcon.BAN.create());
        cancelButton.addClickListener(event -> {
            doCancel();
            SimpleModalDialog.this.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelButton.setVisible(showCancelButton);
        buttonBar.add(cancelButton);

        okButton = new Button(message("ocs.ok"));
        okButton.setIcon(VaadinIcon.CHECK.create());
        okButton.addClickListener(event -> {
            if (doClose()) {
                SimpleModalDialog.this.close();
            }
        });
        buttonBar.add(okButton);

    }

    /**
     * Callback method that is fired when the user presses the OK button.
     * 
     * @return true if the dialog can be closed, false otherwise
     */
    protected boolean doClose() {
        // overwrite in subclass
        return true;
    }

    /**
     * Callback method that is fired when the user presses the cancel button
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

}
