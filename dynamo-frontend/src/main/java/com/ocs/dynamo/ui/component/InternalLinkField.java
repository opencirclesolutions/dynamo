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

import java.io.Serializable;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;

/**
 * A field for displaying an internal link
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID of the entity to link to
 * @param <T> the type of the entity to link to
 */
public class InternalLinkField<ID extends Serializable, T extends AbstractEntity<ID>> extends CustomField<T> {

    private static final long serialVersionUID = -4586184051577153289L;

    private InternalLinkButton<ID, T> linkButton;

    private T value;

    private AttributeModel attributeModel;

    private EntityModel<T> entityModel;

    /**
     * Constructor
     * 
     * @param attributeModel
     * @param entityModel
     */
    public InternalLinkField(AttributeModel attributeModel, EntityModel<T> entityModel) {
        this(attributeModel, entityModel, null);
    }

    /**
     * Constructor
     * 
     * @param attributeModel
     * @param entityModel
     * @param value
     */
    public InternalLinkField(AttributeModel attributeModel, EntityModel<T> entityModel, T value) {
        this.value = value;
        this.attributeModel = attributeModel;
        this.entityModel = entityModel;
        initContent();
    }

    protected void initContent() {
        linkButton = new InternalLinkButton<>(value, entityModel, attributeModel);
        add(linkButton);
    }

    @Override
    public void setEnabled(boolean enabled) {
        // field is always enabled
        super.setEnabled(true);
    }

    @Override
    public T getValue() {
        return value;
    }

    public Button getLinkButton() {
        return linkButton;
    }

    @Override
    protected T generateModelValue() {
        return value;
    }

    @Override
    protected void setPresentationValue(T newPresentationValue) {
        this.value = newPresentationValue;
        if (linkButton != null) {
            String str = FormatUtils.formatEntity(attributeModel.getNestedEntityModel(), value);
            linkButton.setText(str);
            linkButton.setValue(value);
        }
    }

}
