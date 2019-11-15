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
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * A custom field for displaying a clickable URL
 * 
 * @author bas.rutten
 *
 */
public class URLField extends CustomField<String> {

    private static final long serialVersionUID = -1899451186343723434L;

    /**
     * The underlying attribute model
     */
    private AttributeModel attributeModel;

    /**
     * Horizontal wrapper layout
     */
    private FormLayout bar;

    /**
     * Whether the field is in editable mode
     */
    private boolean editable;

    /**
     * The clickable link
     */
    private Anchor link;

    /**
     * The main layout
     */
    private HorizontalLayout main;

    /**
     * The text field for editing
     */
    private TextField textField;

    /**
     * Constructor
     * 
     * @param textField      the text field that this component wraps around
     * @param attributeModel the attribute model used to construct the compoent
     * @param editable       whether to display the field in editable mode
     */
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

    protected Anchor getLink() {
        return link;
    }

    public TextField getTextField() {
        return textField;
    }

    @Override
    public String getValue() {
        return textField.getValue();
    }

    protected void initContent() {
        main = new DefaultHorizontalLayout(false, false);
        main.setSizeFull();

        bar = new FormLayout();
        bar.setSizeFull();
        updateLink(getValue());
        setMode();
        add(main);
    }

    public boolean isEditable() {
        return editable;
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
     * @param value
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

    public AttributeModel getAttributeModel() {
        return attributeModel;
    }

}
