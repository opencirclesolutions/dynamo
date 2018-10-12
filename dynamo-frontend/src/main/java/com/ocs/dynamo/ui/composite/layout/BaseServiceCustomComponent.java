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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;

/**
 * Base class for UI components that need/have access to a Service that can read
 * from the database
 *
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 * @author bas.rutten
 */
public abstract class BaseServiceCustomComponent<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCustomComponent {

    /**
     * A remove button with a built in confirmation message
     *
     * @author bas.rutten
     */
    protected abstract class RemoveButton extends Button {

        private static final long serialVersionUID = -942298948585447203L;

        public RemoveButton(String message, Resource icon) {
            super(message);
            setIcon(icon);
            this.addClickListener(event -> {
                Runnable r = () -> {
                    try {
                        doDelete();
                    } catch (OCSRuntimeException ex) {
                        showNotifification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                };
                VaadinUtils.showConfirmDialog(getMessageService(), message("ocs.delete.confirm", getItemToDelete()), r);
            });
        }

        /**
         * Performs the actual deletion
         */
        protected abstract void doDelete();

        /**
         * @return
         */
        protected abstract String getItemToDelete();
    }

    private static final long serialVersionUID = 6015180039863418544L;

    /**
     * The entity model of the entity or entities to display
     */
    private EntityModel<T> entityModel;

    /**
     * The entity models used for rendering the individual fields (mostly useful
     * for lookup components)
     */
    private Map<String, String> fieldEntityModels = new HashMap<>();

    /**
     * The form options that determine what options are available in the screen
     */
    private FormOptions formOptions;

    /**
     * The width of the title caption above the form (in pixels)
     */
    private Integer formTitleWidth;

    /**
     * The service used for retrieving data
     */
    private BaseService<ID, T> service;

    /**
     * The list of buttons to update after an entity is selected
     */
    private List<Button> toUpdate = new ArrayList<>();

    /**
     * Constructor
     *
     * @param service     the service used to query the database
     * @param entityModel the entity model
     * @param formOptions the form options that govern how the component behaves
     */
    public BaseServiceCustomComponent(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions) {
        this.service = service;
        this.entityModel = entityModel;
        this.formOptions = formOptions;
    }

    /**
     * Adds a field entity model - this can be used to overwrite the default
     * entity
     * model that is used for rendering complex selection components
     * (lookup
     * dialogs)
     *
     * @param path      the path to the field
     * @param reference the unique ID of the entity model
     */
    public final void addFieldEntityModel(String path, String reference) {
        fieldEntityModels.put(path, reference);
    }

    /**
     * Method that is called after the user selects an entity to view in Details
     * mode
     *
     * @param editForm the edit form which displays the entity
     * @param entity   the selected entity
     */
    protected void afterEntitySelected(ModelBasedEditForm<ID, T> editForm, T entity) {
        // override in subclass
    }

    /**
     * Method that is called after the mode is changed (from editable to read
     * only
     * or vice versa)
     *
     * @param viewMode whether the component is now in view mode (after the change)
     * @param editForm the edit form
     */
    protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<ID, T> editForm) {
        // override in subclasses
    }

    /**
     * Method that is called before saving an entity but after the validation
     *
     * @return
     */
    protected boolean beforeSave() {
        return true;
    }

    /**
     * Checks which buttons in the button bar must be enabled after an item has
     * been
     * selected
     *
     * @param selectedItem the selected item
     */
    protected void checkButtonState(T selectedItem) {
        for (Button b : toUpdate) {
            boolean enabled = selectedItem != null && mustEnableButton(b, selectedItem);
            b.setEnabled(enabled);
        }
    }

    /**
     * Creates a custom field - override in subclass
     *
     * @param entityModel    the entity model of the entity to display
     * @param attributeModel the attribute model of the entity to display
     * @param viewMode       indicates whether the screen is in read only mode
     * @param searchMode     indicates whether the screen is in search mode
     * @return
     */
    protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode,
                                            boolean searchMode) {
        // overwrite in subclass
        return null;
    }

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public Map<String, String> getFieldEntityModels() {
        return fieldEntityModels;
    }

    public FormOptions getFormOptions() {
        return formOptions;
    }

    public Integer getFormTitleWidth() {
        return formTitleWidth;
    }

    public BaseService<ID, T> getService() {
        return service;
    }

    /**
     * Method that is called in order to enable/disable a button after selecting
     * an
     * item table
     *
     * @param button       the button
     * @param selectedItem the currently selected item
     * @return
     */
    protected boolean mustEnableButton(Button button, T selectedItem) {
        // overwrite in subclasses if needed
        return true;
    }

    /**
     * Registers a button that must be enabled/disabled after an item is
     * selected.
     * use the "mustEnableButton" callback method to impose additional
     * constraints
     * on when the button must be enabled
     *
     * @param button the button to register
     */
    public final void registerButton(Button button) {
        if (button != null) {
            button.setEnabled(false);
            toUpdate.add(button);
        }
    }

	/**
	 * Removes the custom field entity model for a certain attribute
	 *
	 * @param path
	 *            the path to the attribute
	 */
	public final void removeFieldEntityModel(String path) {
		fieldEntityModels.remove(path);
	}

    public void setFormTitleWidth(Integer formTitleWidth) {
        this.formTitleWidth = formTitleWidth;
    }

    public void setService(BaseService<ID, T> service) {
        this.service = service;
    }

}
