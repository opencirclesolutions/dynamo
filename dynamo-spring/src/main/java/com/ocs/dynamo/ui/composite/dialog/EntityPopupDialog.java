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

import java.io.Serializable;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.ServiceLocator;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * A popup dialog for adding a new entity or viewing the details of an existing entrty
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 */
public abstract class EntityPopupDialog<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseModalDialog {

    private static final long serialVersionUID = -2012972894321597214L;

    private MessageService messageService = ServiceLocator.getMessageService();

    private EntityModel<T> entityModel;

    private SimpleEditLayout<ID, T> layout;

    private BaseService<ID, T> service;

    private FormOptions formOptions;

    private T entity;

    /**
     * Constructor
     * 
     * @param service
     * @param entityModel
     */
    public EntityPopupDialog(BaseService<ID, T> service, T entity, EntityModel<T> entityModel,
            FormOptions formOptions) {
        this.service = service;
        this.entityModel = entityModel;
        this.formOptions = formOptions;
        this.entity = entity;
    }

    /**
     * Callback method that is called after the user is done editing the entry
     * 
     * @param cancel
     *            whether the edit action was cancelled
     * @param newEntity
     *            whether the user was adding a new entity
     * @param entity
     *            the entity that was being edited
     */
    public abstract void afterEditDone(boolean cancel, boolean newEntity, T entity);

    @Override
    protected void doBuild(Layout parent) {

        formOptions.setHideCancelButton(false);

        layout = new SimpleEditLayout<ID, T>(entity, service, entityModel, formOptions) {

            private static final long serialVersionUID = -2965981316297118264L;

            @Override
            protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
                super.afterEditDone(cancel, newEntity, entity);
                EntityPopupDialog.this.close();
                EntityPopupDialog.this.afterEditDone(cancel, newEntity, entity);
            }

            @Override
            protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<ID, T> editForm) {
                super.afterModeChanged(viewMode, editForm);
            }

        };
        parent.addComponent(layout);
    }

    @Override
    protected void doBuildButtonBar(HorizontalLayout buttonBar) {
        // in read-only mode, display only an "OK" button that closes the dialog
        buttonBar.setVisible(formOptions.isReadOnly());
        if (formOptions.isReadOnly()) {
            Button okButton = new Button(messageService.getMessage("ocs.ok"));
            okButton.addClickListener(new Button.ClickListener() {

                private static final long serialVersionUID = 1889018073135108348L;

                @Override
                public void buttonClick(ClickEvent event) {
                    close();
                }
            });

            buttonBar.addComponent(okButton);
        }
    }

    public T getEntity() {
        return layout.getEntity();
    }

    @Override
    protected String getTitle() {
        // not needed
        return null;
    }
}
