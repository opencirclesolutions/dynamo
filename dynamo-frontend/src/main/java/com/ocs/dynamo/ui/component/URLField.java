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

import com.ocs.dynamo.domain.model.AttributeModel;
import com.vaadin.componentfactory.EnhancedFormLayout;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;

/**
 * A custom field for displaying a clickable URL
 * 
 * @author bas.rutten
 *
 */
public class URLField extends CustomField<String> {

    private static final long serialVersionUID = -1899451186343723434L;

    @Getter
    private final AttributeModel attributeModel;

    private EnhancedFormLayout bar;

    @Getter
    private boolean editable;

    @Getter
    private Anchor link;

    private HorizontalLayout main;

    @Getter
    private final TextField textField;

    public URLField(TextField textField, AttributeModel attributeModel, boolean editable) {
        setSizeFull();
        this.attributeModel = attributeModel;
        this.textField = textField;
        this.textField.setSizeFull();
        this.editable = editable;
        textField.addValueChangeListener(event -> setValue(event.getValue()));
        initContent();
    }

    @Override
    protected String generateModelValue() {
        return textField.getValue();
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    protected void initContent() {
        main = new DefaultHorizontalLayout(false, false);
        main.setSizeFull();

        bar = new EnhancedFormLayout();
        bar.setSizeFull();
        updateLink(getValue());
        setMode();
        add(main);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        setMode();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        if (textField != null) {
            textField.setErrorMessage(errorMessage);
        }
    }

    @Override
	public void setInvalid(boolean invalid) {
		super.setInvalid(invalid);
		if (textField != null) {
			textField.setInvalid(invalid);
		}
	}

    /**
     * Sets the correct mode (read only or editable)
     */
    private void setMode() {
        if (main != null) {
            // display different component depending on mode
            if (editable) {
                main.replace(bar, textField);
            } else {
                main.replace(textField, bar);
            }
        }
    }

    public void setPlaceholder(String placeHolder) {
        if (textField != null) {
            textField.setPlaceholder(placeHolder);
        }
    }

    @Override
    protected void setPresentationValue(String value) {
        updateLink(value);
        if (value == null) {
            textField.clear();
        } else {
            textField.setValue(value);
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
     * @param value the new value
     */
    private void updateLink(String value) {
        if (bar != null) {
            bar.removeAll();
            if (!StringUtils.isEmpty(value)) {
                String temp = com.ocs.dynamo.utils.StringUtils.prependProtocol(value);
                link = new Anchor(temp, temp);
                link.setTarget("_blank");
                bar.add(link);
            } else {
                link = null;
            }
        }
    }

}
