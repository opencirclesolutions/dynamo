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
package com.ocs.dynamo.ui.composite.layout;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;

/**
 * Base class for UI components that need/have access to a Service
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public abstract class BaseServiceCustomComponent<ID, T extends AbstractEntity<ID>>
        extends BaseCustomComponent {

    /**
     * A remove button with a built in confirmation message
     * 
     * @author bas.rutten
     */
    protected abstract class RemoveButton extends Button {

        private static final long serialVersionUID = -942298948585447203L;

        @SuppressWarnings("serial")
        public RemoveButton() {
            super(message("ocs.remove"));
            this.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {

                    Runnable r = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                doDelete();
                            } catch (OCSValidationException ex) {
                                Notification.show(ex.getErrors().get(0),
                                        Notification.Type.ERROR_MESSAGE);
                            }
                        }

                    };
                    VaadinUtils.showConfirmDialog(getMessageService(),
                            message("ocs.delete.confirm"), r);

                }
            });
        }

        /**
         * Performs the actual deletion
         */
        protected abstract void doDelete();
    }

    private static final long serialVersionUID = 6015180039863418544L;

    /**
     * The entity model of the entity or entities to display
     */
    private EntityModel<T> entityModel;

    private FormOptions formOptions;

    private BaseService<ID, T> service;

    /**
     * Constructor
     * 
     * @param service
     *            the service used to query the database
     * @param entityModel
     *            the entity model
     * @param formOptions
     *            the form options
     */
    public BaseServiceCustomComponent(BaseService<ID, T> service, EntityModel<T> entityModel,
            FormOptions formOptions) {
        this.service = service;
        this.entityModel = entityModel;
        this.formOptions = formOptions;
    }

    /**
     * Creates a custom field - override in subclass
     * 
     * @param entityModel
     *            the entity model of the entity to display
     * @param attributeModel
     *            the attribute model of the entity to display
     * @param viewMode
     *            indicates whether the screen is in read only mode
     * @param searchMode
     *            indicates whether the screen is in search mode
     * @return
     */
    protected Field<?> constructCustomField(EntityModel<T> entityModel,
            AttributeModel attributeModel, boolean viewMode, boolean searchMode) {
        return null;
    }

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public FormOptions getFormOptions() {
        return formOptions;
    }

    public BaseService<ID, T> getService() {
        return service;
    }

    public void setService(BaseService<ID, T> service) {
        this.service = service;
    }
}
