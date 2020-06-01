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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * A simple confirm dialog with a Yes and No button
 * 
 * @author Bas Rutten
 *
 */
public class ConfirmDialog extends BaseModalDialog {

	private static final long serialVersionUID = -2004730447215123112L;

	private Button yesButton;

	private Button noButton;

	private Runnable whenConfirmed;

	private Runnable whenCancelled;

	private String question;

	public ConfirmDialog(String question, Runnable whenConfirmed, Runnable whenCancelled) {
		this.question = question;
		this.whenConfirmed = whenConfirmed;
		this.whenCancelled = whenCancelled;
	}

	@Override
	protected void doBuild(VerticalLayout parent) {
		Span questionSpan = new Span(question);
		parent.add(questionSpan);
	}

	@Override
	protected void doBuildButtonBar(HorizontalLayout buttonBar) {
		yesButton = new Button(message("ocs.yes"));
		yesButton.setIcon(VaadinIcon.CHECK.create());
		yesButton.addClickListener(event -> {
			whenConfirmed.run();
			this.close();
		});
		buttonBar.add(yesButton);

		noButton = new Button(message("ocs.no"));
		noButton.setIcon(VaadinIcon.BAN.create());
		noButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		noButton.addClickListener(event -> {
			if (whenCancelled != null) {
				whenCancelled.run();
			}
			this.close();
		});
		buttonBar.add(noButton);

	}

	@Override
	protected String getTitle() {
		return message("ocs.confirm");
	}

}
