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
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;

/**
 * A button that acts an an internal link that can be used to navigate to the
 * detail screen of an entity
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T> the type of the entity
 */
public class InternalLinkButton<ID extends Serializable, T extends AbstractEntity<ID>> extends Button {

    private static final long serialVersionUID = -7930361861398979317L;

    private T value;

    /**
     * Constructor
     * 
     * @param entity      the entity
     * @param entityModel the entity model
     * @param am          the attribute model
     */
    public InternalLinkButton(T value, EntityModel<T> entityModel, AttributeModel am) {
        String caption = FormatUtils.formatEntity(entityModel != null ? entityModel : am.getNestedEntityModel(), value);
        setText(caption);
        addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        this.value = value;
        addClickListener(event -> {
            UIHelper ui = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
            ui.navigateToEntityScreen(this.value);
        });
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        this.value = (T) value;
    }

}
