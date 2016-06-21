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
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;

/**
 * A popup dialog for adding a new entity
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 */
public abstract class AddDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseModalDialog {

    private static final long serialVersionUID = -2012972894321597214L;

    private EntityModel<T> entityModel;

    private SimpleEditLayout<ID, T> layout;

    private BaseService<ID, T> service;

    /**
     * Constructor
     * 
     * @param service
     * @param entityModel
     */
    public AddDialog(BaseService<ID, T> service, EntityModel<T> entityModel) {
        this.service = service;
        this.entityModel = entityModel;
    }

    /**
     * Callback method that is called after the a new entity is added
     * 
     * @param entity
     *            the entity
     */
    public abstract void afterAddition(T entity);

    @Override
    protected void doBuild(Layout parent) {
        FormOptions fo = new FormOptions();
        fo.setHideCancelButton(false);

        layout = new SimpleEditLayout<ID, T>(null, service, entityModel, fo) {

            private static final long serialVersionUID = -2965981316297118264L;

            @Override
            protected void afterEditDone(boolean cancel, boolean newEntity, T entity) {
                super.afterEditDone(cancel, newEntity, entity);
                AddDialog.this.close();
                AddDialog.this.afterAddition(entity);
            }
        };
        parent.addComponent(layout);
    }

    @Override
    protected void doBuildButtonBar(HorizontalLayout buttonBar) {
        // no button bar needed (we use the button bar from the edit form)
        buttonBar.setVisible(false);
    }

    public T getEntity() {
        return layout.getEntity();
    }

    @Override
    protected String getTitle() {
        // TODO Auto-generated method stub
        return null;
    }
}
