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
package com.ocs.dynamo.ui.component;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.BorderStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;

/**
 * A custom field for displaying a clickable URL
 * 
 * @author bas.rutten
 *
 */
public class URLField extends CustomField<String> {

	private static final long serialVersionUID = -1899451186343723434L;

	private AttributeModel attributeModel;

	private HorizontalLayout bar;

	private boolean editable;

	private Link link;

	private CssLayout main;

	private TextField textField;

	/**
	 * Constructor
	 * 
	 * @param textField      the text field that this component wraps around
	 * @param attributeModel the attribute model used to construct the compoent
	 * @param editable       whether to display the field in editable mode
	 */
	public URLField(TextField textField, AttributeModel attributeModel, boolean editable) {
		this.attributeModel = attributeModel;
		this.textField = textField;
		this.editable = editable;
		textField.addValueChangeListener(event -> setValue(event.getValue()));
	}

	protected Link getLink() {
		return link;
	}

	public TextField getTextField() {
		return textField;
	}

	@Override
	protected Component initContent() {
		main = new CssLayout();
		setCaption(attributeModel.getDisplayName());

		bar = new DefaultHorizontalLayout(false, true, true);
		updateLink(getValue());
		setMode();
		return main;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		setMode();
	}

	@Override
	protected void doSetValue(String value) {
		updateLink(value);
		if (value == null) {
			textField.clear();
		} else {
			textField.setValue(value);
		}
	}

	/**
	 * Sets the correct mode (read only or editable)
	 */
	private void setMode() {
		if (main != null) {
			// display different component depending on mode
			if (editable) {
				main.replaceComponent(bar, textField);
			} else {
				main.replaceComponent(textField, bar);
			}
		}
	}

	@Override
	public void setValue(String newValue) {
		super.setValue(newValue);
		updateLink(newValue);
		textField.setValue(newValue);
	}

	/**
	 * Updates the field value - renders a clickable URL if the field value is not
	 * empty
	 * 
	 * @param value
	 */
	private void updateLink(String value) {
		if (bar != null) {
			bar.removeAllComponents();
			if (!StringUtils.isEmpty(value)) {
				String temp = com.ocs.dynamo.utils.StringUtils.prependProtocol(value);
				link = new Link(temp, new ExternalResource(temp), "_blank", 0, 0, BorderStyle.DEFAULT);
				bar.addComponent(link);
			} else {
				link = null;
			}
		}
	}

	@Override
	public String getValue() {
		return textField.getValue();
	}

	@Override
	public Registration addValueChangeListener(final ValueChangeListener<String> listener) {
		return textField.addValueChangeListener(listener);
	}

	@Override
	public void setComponentError(ErrorMessage componentError) {
		if (textField != null) {
			textField.setComponentError(componentError);
		}
	}

}
