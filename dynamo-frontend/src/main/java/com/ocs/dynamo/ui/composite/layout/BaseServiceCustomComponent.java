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
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.GroupTogetherMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.grid.ComponentContext;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;

import lombok.Getter;
import lombok.Setter;

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
	protected class RemoveButton extends Button {

		private static final long serialVersionUID = -942298948585447203L;

		public RemoveButton(HasSelectedItem<T> hasSelectedItem, String message, Component icon, Runnable doDelete,
				Function<T, String> itemDescriptionSupplier) {
			super(message);
			setIcon(icon);
			this.addClickListener(event -> {
				Runnable r = () -> {
					try {
						doDelete.run();
					} catch (OCSRuntimeException ex) {
						showErrorNotification(ex.getMessage());
					}
				};
				T selectedItem = hasSelectedItem.getSelectedItem();
				String description = itemDescriptionSupplier.apply(selectedItem);
				VaadinUtils.showConfirmDialog(message("ocs.delete.confirm", description), r);
			});
		}

	}

	private static final long serialVersionUID = 6015180039863418544L;

	@Getter
	@Setter
	private BiConsumer<ModelBasedEditForm<ID, T>, T> afterEntitySelected;

	@Getter
	@Setter
	private Consumer<T> afterEntitySet;

	@Getter
	@Setter
	private BiConsumer<HasComponents, Boolean> afterLayoutBuilt;

	@Getter
	@Setter
	private BiConsumer<ModelBasedEditForm<ID, T>, Boolean> afterModeChanged;

	@Getter
	@Setter
	private Consumer<Integer> afterTabSelected;

	@Getter
	@Setter
	private BiConsumer<String, byte[]> afterUploadCompleted;

	@Getter
	@Setter
	private Function<String, String> findParentGroup;

	@Getter
	@Setter
	private List<String> columnThresholds = new ArrayList<>();

	@Getter
	@Setter
	private ComponentContext componentContext = ComponentContext.builder().build();

	/**
	 * The list of components to update after an entity is selected
	 */
	private List<Component> componentsToUpdate = new ArrayList<>();

	/**
	 * Mapping from custom component label to custom component
	 */
	private Map<String, List<Component>> customComponentMap = new HashMap<>();

	@Getter
	private Map<AttributeModel, Supplier<Converter<?, ?>>> customConverters = new HashMap<>();

	@Getter
	private Map<AttributeModel, Supplier<Validator<?>>> customRequiredValidators = new HashMap<>();

	@Getter
	@Setter
	private Consumer<T> customSaveConsumer;

	@Getter
	@Setter
	private Function<RuntimeException, Boolean> customSaveExceptionHandler;

	@Getter
	private Map<AttributeModel, Supplier<Validator<?>>> customValidators = new HashMap<>();

	/**
	 * The entity model of the entity or entities to display
	 */
	@Getter
	private final EntityModel<T> entityModel;

	/**
	 * The entity models used for rendering the individual fields (mostly useful for
	 * lookup components)
	 */
	@Getter
	private Map<String, String> fieldEntityModels = new HashMap<>();

	/**
	 * The form options that determine what options are available in the screen
	 */
	@Getter
	private FormOptions formOptions;

	@Getter
	@Setter
	private GroupTogetherMode groupTogetherMode;

	@Getter
	@Setter
	private Integer groupTogetherWidth;

	@Getter
	@Setter
	private String maxEditFormWidth = SystemPropertyUtils.getDefaultMaxEditFormWidth();

	@Getter
	@Setter
	private BiPredicate<Component, T> mustEnableComponent;

	@Getter
	@Setter
	private Runnable onBackButtonClicked = () -> {
	};

	@Getter
	@Setter
	private String[] parentGroupHeaders;

	@Getter
	@Setter
	private Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields;

	@Getter
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

	public void addCustomConverter(AttributeModel attributeModel, Supplier<Converter<?, ?>> converter) {
		customConverters.put(attributeModel, converter);
	}

	public void addCustomRequiredValidator(AttributeModel attributeModel, Supplier<Validator<?>> validator) {
		customRequiredValidators.put(attributeModel, validator);
	}

	public void addCustomValidator(AttributeModel attributeModel, Supplier<Validator<?>> validator) {
		customValidators.put(attributeModel, validator);
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
	 * Checks which buttons in the button bar must be enabled after an item has been
	 * selected
	 *
	 * @param selectedItem the selected item
	 */
	protected void checkComponentState(T selectedItem) {
		for (Component component : componentsToUpdate) {
			if (component instanceof DownloadButton) {
				((DownloadButton) component).update();
			}
			boolean enabled = selectedItem != null
					&& (mustEnableComponent == null || mustEnableComponent.test(component, selectedItem));
			if (component instanceof HasEnabled) {
				((HasEnabled) component).setEnabled(enabled);
			}
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
	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, boolean searchMode) {
		// overwrite in subclass
		return null;
	}

	/**
	 * Returns all custom components that have been registered with the specified
	 * key
	 * 
	 * @param key the key under which the custom components are stored
	 * @return
	 */
	public List<Component> getCustomComponents(String key) {
		return customComponentMap.get(key);
	}

//	/**
//	 * Callback method that is called during the save process. Allows the developer
//	 * to respond to a specific type of exception thrown in the service layer in a
//	 * custom way
//	 * 
//	 * @param ex
//	 * @return <code>true</code> if the exception was handled by this method, false
//	 *         otherwise
//	 */
//	protected boolean handleCustomException(RuntimeException ex) {
//		return false;
//	}

	/**
	 * @param path the path to the attribute
	 * @return the field entity model reference for the specified attribute model
	 */
	public String getFieldEntityModel(String path) {
		return fieldEntityModels.get(path);
	}

	/**
	 * Copies component settings to the edit form
	 * 
	 * @param editForm the edit form
	 */
	protected void initEditForm(ModelBasedEditForm<ID, T> editForm) {
		editForm.setCustomSaveConsumer(customSaveConsumer);
		editForm.setFieldEntityModels(getFieldEntityModels());
		editForm.setColumnThresholds(getColumnThresholds());
		editForm.setMaxFormWidth(getMaxEditFormWidth());
		editForm.setGroupTogetherMode(getGroupTogetherMode());
		editForm.setGroupTogetherWidth(getGroupTogetherWidth());
		editForm.setAfterEntitySet(getAfterEntitySet());
		editForm.setAfterEntitySelected(getAfterEntitySelected());
		editForm.setAfterLayoutBuilt(getAfterLayoutBuilt());
		editForm.setAfterModeChanged(getAfterModeChanged());
		editForm.setAfterTabSelected(getAfterTabSelected());
		editForm.setOnBackButtonClicked(getOnBackButtonClicked());

		editForm.setCustomConverters(getCustomConverters());
		editForm.setCustomValidators(getCustomValidators());
		editForm.setCustomRequiredValidators(getCustomRequiredValidators());
		editForm.setAfterUploadCompleted(getAfterUploadCompleted());
		editForm.setPostProcessEditFields(getPostProcessEditFields());
		editForm.setCustomSaveExceptionHandler(getCustomSaveExceptionHandler());

		editForm.setParentGroupHeaders(getParentGroupHeaders());
		editForm.setFindParentGroup(getFindParentGroup());
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
		return customComponentMap.get(key) != null && customComponentMap.get(key).contains(toCheck);
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

	/**
	 * Stores and registers a custom component
	 * 
	 * @param key       the key under which to store the component
	 * @param component the component
	 */
	public void storeAndRegisterCustomComponent(String key, Component component) {
		registerComponent(component);
		storeCustomComponent(key, component);
	}

	/**
	 * Stores a custom component. This can e.g. be used for checking whether extra
	 * components you added to the button bar must be enabled
	 * 
	 * @param key       the key under which to store the custom component
	 * @param component the component to store
	 */
	public void storeCustomComponent(String key, Component component) {
		customComponentMap.putIfAbsent(key, new ArrayList<>());
		customComponentMap.get(key).add(component);
	}
}
