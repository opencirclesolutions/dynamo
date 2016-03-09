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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Reloadable;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout for editing a single object
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public class SimpleEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseServiceCustomComponent<ID, T> implements Reloadable {

    private static final long serialVersionUID = -7935358582100755140L;

    private ModelBasedEditForm<ID, T> editForm;

    private T entity;

    private VerticalLayout main;

    private Map<String, Filter> fieldFilters = new HashMap<>();

    /**
     * Constructor
     * 
     * @param entity
     * @param service
     * @param entityModel
     * @param formOptions
     */
    public SimpleEditLayout(T entity, BaseService<ID, T> service, EntityModel<T> entityModel,
            FormOptions formOptions) {
        super(service, entityModel, formOptions);
        this.entity = entity;
    }

    @Override
    public void attach() {
        super.attach();
        build();
    }

    /**
     * Constructs the screen - this method is called just once
     */
    @Override
    public void build() {
        main = new DefaultVerticalLayout(true, true);

        // if opening in edit mode, the cancel button is useless since there is
        // nothing to cancel or go back to
        if (!getFormOptions().isOpenInViewMode()) {
            getFormOptions().setHideCancelButton(true);
        }
        // there is just one component here, so the screen mode is always
        // vertical
        getFormOptions().setScreenMode(ScreenMode.VERTICAL);

        editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(),
                getFormOptions(), fieldFilters) {
            @Override
            protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
                setEntity(entity);
                SimpleEditLayout.this.afterEditDone(cancel, newObject, entity);
            }

            @Override
            protected Field<?> constructCustomField(EntityModel<T> entityModel,
                    AttributeModel attributeModel, boolean viewMode) {
                return SimpleEditLayout.this.constructCustomField(entityModel, attributeModel,
                        viewMode, false);
            }

            @Override
            protected String[] getParentGroupHeaders() {
                return SimpleEditLayout.this.getParentGroupHeaders();
            }

            @Override
            protected String getParentGroup(String childGroup) {
                return SimpleEditLayout.this.getParentGroup(childGroup);
            }

            @Override
            protected boolean isEditAllowed() {
                return SimpleEditLayout.this.isEditAllowed();
            }

            @Override
            protected void postProcessEditFields() {
                SimpleEditLayout.this.postProcessEditFields(editForm);
            }

            @Override
            protected void afterModeChanged(boolean viewMode) {
                SimpleEditLayout.this.afterModeChanged(viewMode, editForm);
            }

        };
        editForm.build();

        main.addComponent(editForm);

        setCompositionRoot(main);
    }

    /**
     * Creates a new entity - override in subclass if needed
     * 
     * @return
     */
    protected T createEntity() {
        return getService().createNewEntity();
    }

    /**
     * @param editForm
     */
    protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
        // do nothing by default - override in subclasses
    }

    /**
     * Method that is called after the mode is changes
     * 
     * @param viewMode
     *            the new view mode
     * @param editForm
     */
    protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<ID, T> editForm) {
        // override in subclasses
    }

    /**
     * Returns a list of additional group headers that can be used to apply an extra layer to the
     * layout
     * 
     * @return
     */
    protected String[] getParentGroupHeaders() {
        // overwrite in subclasses if needed
        return null;
    }

    /**
     * Returns the parent group (which must be returned by the getParentGroupHeaders method) to
     * which a certain child group belongs
     * 
     * @param childGroup
     * @return
     */
    protected String getParentGroup(String childGroup) {
        // overwrite in subclasses if needed
        return null;
    }

    @Override
    public void reload() {

        // reset to view mode
        if (getFormOptions().isOpenInViewMode()) {
            editForm.setViewMode(true);
        }

        if (entity.getId() != null) {
            setEntity(getService().fetchById(entity.getId()));
        }
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
        editForm.setEntity(entity);
    }

    /**
     * Method that is called after the user has completed (or cancelled) an edit action
     * 
     * @param newObject
     * @param entity
     */
    protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
        // reset to view mode
        if (getFormOptions().isOpenInViewMode()) {
            editForm.setViewMode(true);
        }

        if (entity.getId() != null) {
            setEntity(getService().fetchById(entity.getId()));
        }
    }

    public ModelBasedEditForm<ID, T> getEditForm() {
        return editForm;
    }

    public Map<String, Filter> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(Map<String, Filter> fieldFilters) {
        this.fieldFilters = fieldFilters;
    }

    protected boolean isEditAllowed() {
        return true;
    }

}
