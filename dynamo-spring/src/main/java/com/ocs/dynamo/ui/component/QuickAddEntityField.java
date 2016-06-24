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

import org.springframework.util.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSNonUniqueException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.dialog.SimpleModalDialog;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/**
 * Base class for components that display an Entity or collection of entities and that allow the
 * user to easily add new values on the fly
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 * @param <U>
 *            the type of the value of the component (can be an entity, or a collection of entities)
 */
public abstract class QuickAddEntityField<ID extends Serializable, T extends AbstractEntity<ID>, U>
        extends CustomEntityField<ID, T, U> {

    /**
     * Simple dialog for adding a new value
     * 
     * @author bas.rutten
     *
     */
    private class NewValueDialog extends SimpleModalDialog {

        private static final long serialVersionUID = 6208738706327329145L;

        private TextField valueField;

        NewValueDialog() {
            super(true);
        }

        @Override
        protected void doBuild(Layout parent) {
            valueField = new TextField(getMessageService().getMessage("ocs.enter.new.value"));
            valueField.setSizeFull();
            valueField.focus();
            parent.addComponent(valueField);
        }

        @Override
        protected boolean doClose() {
            String value = valueField.getValue();
            if (!StringUtils.isEmpty(value)) {
                T t = getService().createNewEntity();

                // disallow values that are too long
                String propName = getAttributeModel().getQuickAddPropertyName();
                Integer maxLength = getEntityModel().getAttributeModel(propName).getMaxLength();

                if (maxLength != null && value.length() > maxLength) {
                    Notification.show(getMessageService().getMessage("ocs.value.too.long"),
                            Notification.Type.ERROR_MESSAGE);
                    return false;
                }
                ClassUtils.setFieldValue(t, propName, value);

                try {
                    t = getService().save(t);
                    afterNewEntityAdded(t);
                    return true;
                } catch (OCSNonUniqueException ex) {
                    // not unique - produce warning
                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            } else {
                Notification.show(getMessageService().getMessage("ocs.value.required"),
                        Notification.Type.ERROR_MESSAGE);
            }
            return false;
        }

        @Override
        protected String getTitle() {
            return getMessageService().getMessage("ocs.enter.new.value");
        }
    }

    private static final long serialVersionUID = 7118578276952170818L;

    /**
     * Constructor
     * 
     * @param service
     * @param entityModel
     * @param attributeModel
     */
    public QuickAddEntityField(BaseService<ID, T> service, EntityModel<T> entityModel,
            AttributeModel attributeModel) {
        super(service, entityModel, attributeModel);
    }

    /**
     * Callback method that is called after a new entity has been succesfully created. Use this to
     * add the new entity to the component and select it
     * 
     * @param entity
     */
    protected abstract void afterNewEntityAdded(T entity);

    /**
     * Constructs the button that brings up the dialog that allows the user to add a new item
     * 
     * @return
     */
    protected Button constructAddButton() {
        Button addButton = new Button(getMessageService().getMessage("ocs.add"));
        addButton.addClickListener(new Button.ClickListener() {

            private static final long serialVersionUID = 4074804834729142520L;

            @Override
            public void buttonClick(ClickEvent event) {
                NewValueDialog dialog = new NewValueDialog();
                dialog.build();
                UI.getCurrent().addWindow(dialog);
            }
        });
        return addButton;
    }

}
