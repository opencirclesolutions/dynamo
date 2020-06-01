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
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;

/**
 * Base class for UI components that need/have access to a Service that can read
 * from the database
 *
 * @param <ID> type of the primary key of the entity
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

		public RemoveButton(String message, Component icon) {
			super(message);
			setIcon(icon);
			this.addClickListener(event -> {
				Runnable r = () -> {
					try {
						doDelete();
					} catch (OCSRuntimeException ex) {
						showErrorNotification(ex.getMessage());
					}
				};
				VaadinUtils.showConfirmDialog(message("ocs.delete.confirm", getItemToDelete()), r);
			});
		}

		/**
		 * Performs the actual deletion
		 */
		protected abstract void doDelete();

		/**
		 * @return the description of the item to delete (for use in the confirmation
		 *         dialog)
		 */
		protected abstract String getItemToDelete();
	}

	private static final long serialVersionUID = 6015180039863418544L;

	/**
	 * The list of components to update after an entity is selected
	 */
	private List<Component> componentsToUpdate = new ArrayList<>();

	/**
	 * Custom button mapping
	 */
	private Map<String, List<Component>> customButtonMap = new HashMap<>();

	/**
	 * The entity model of the entity or entities to display
	 */
	private EntityModel<T> entityModel;

	/**
	 * The entity models used for rendering the individual fields (mostly useful for
	 * lookup components)
	 */
	private Map<String, String> fieldEntityModels = new HashMap<>();

	/**
	 * The form options that determine what options are available in the screen
	 */
	private FormOptions formOptions;

	/**
	 * The service used for retrieving data
	 */
	private BaseService<ID, T> service;

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
	 * Sets a custom entity model to use for a certain field/property
	 *
	 * @param path      the path of the property
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
	 * Method that is called after the mode is changed (from editable to read only
	 * or vice versa)
	 *
	 * @param viewMode whether the component is now in view mode (after the change)
	 * @param editForm the edit form
	 */
	protected void afterModeChanged(boolean viewMode, ModelBasedEditForm<ID, T> editForm) {
		// override in subclasses
	}

	/**
	 * Checks which buttons in the button bar must be enabled after an item has been
	 * selected
	 *
	 * @param selectedItem the selected item
	 */
	protected void checkComponentState(T selectedItem) {
		for (Component b : componentsToUpdate) {
			if (b instanceof DownloadButton) {
				((DownloadButton) b).update();
			}
			boolean enabled = selectedItem != null && mustEnableComponent(b, selectedItem);
			if (b instanceof HasEnabled) {
				((HasEnabled) b).setEnabled(enabled);
			}
		}
	}

	/**
	 * Callback method for constructing a custom converter - currently only
	 * supported for text fields
	 * 
	 * @param am the attribute model to base the converter on
	 * @return
	 */
	protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
		return null;
	}

	/**
	 * 
	 * @param <V>
	 * @param am
	 * @return
	 */
	protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
		return null;
	}

	/**
	 * 
	 * @param <V>
	 * @param am
	 * @return
	 */
	protected <V> Validator<V> constructCustomRequiredValidator(AttributeModel am) {
		return null;
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
	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, boolean searchMode) {
		// overwrite in subclass
		return null;
	}

	public List<Component> getCustomComponents(String key) {
		return customButtonMap.get(key);
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

	public BaseService<ID, T> getService() {
		return service;
	}

	/**
	 * Callback method that is called during the save process. Allows the developer
	 * to respond to a specific type of exception thrown in the service layer in a
	 * custom way
	 * 
	 * @param ex
	 * @return <code>true</code> if the exception was handled by this method, false
	 *         otherwise
	 */
	protected boolean handleCustomException(RuntimeException ex) {
		return false;
	}

	/**
	 * Check whether the provided component is a custom component stored under the
	 * provided key
	 * 
	 * @param key     the key
	 * @param toCheck the component to check
	 * @return
	 */
	public boolean isCustomComponent(String key, Component toCheck) {
		return customButtonMap.get(key) != null && customButtonMap.get(key).contains(toCheck);
	}

	/**
	 * Method that is called in order to enable/disable a component (i.e. a button)
	 * in a button bar after selecting an item in the grid
	 *
	 * @param component    the component
	 * @param selectedItem the currently selected item in the grid
	 * @return
	 */
	protected boolean mustEnableComponent(Component component, T selectedItem) {
		// overwrite in subclasses if needed
		return true;
	}

	/**
	 * Registers a component that must be enabled/disabled after an item is
	 * selected. use the "mustEnableButton" callback method to impose additional
	 * constraints on when the button must be enabled
	 *
	 * @param comp the component to register
	 */
	public final void registerComponent(Component comp) {
		if (comp != null) {
			// disable the component because by default no row has been selected
			if (comp instanceof HasEnabled) {
				((HasEnabled) comp).setEnabled(false);
			}
			componentsToUpdate.add(comp);
		}
	}

	/**
	 * Removes the custom field entity model for a certain attribute
	 *
	 * @param path the path to the attribute
	 */
	public final void removeFieldEntityModel(String path) {
		fieldEntityModels.remove(path);
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	/**
	 * Stores a custom component. This can e.g. be used for checking whether extra
	 * components you added to the button bar must be enabled
	 * 
	 * @param key       the key under which to store the custom component
	 * @param component the component to store
	 */
	public void storeCustomComponent(String key, Component component) {
		customButtonMap.putIfAbsent(key, new ArrayList<>());
		customButtonMap.get(key).add(component);
	}

	/**
	 * Stores and registers a custom comonent
	 * 
	 * @param key       the key under which to store the component
	 * @param component the component
	 */
	public void storeAndRegisterCustomComponent(String key, Component component) {
		registerComponent(component);
		storeCustomComponent(key, component);
	}
}
