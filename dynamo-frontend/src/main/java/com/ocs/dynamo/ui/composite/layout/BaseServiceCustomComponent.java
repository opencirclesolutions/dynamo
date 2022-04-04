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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.GroupTogetherMode;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.CustomFieldContext;
import com.ocs.dynamo.ui.component.DownloadButton;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.utils.VaadinUtils;
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
				Function<T, String> itemDescriptionCreator) {
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
				String description = itemDescriptionCreator.apply(selectedItem);
				VaadinUtils.showConfirmDialog(message("ocs.delete.confirm", description), r);
			});
		}

	}

	private static final long serialVersionUID = 6015180039863418544L;

	@Getter
	@Setter
	private ComponentContext<ID, T> componentContext = ComponentContext.<ID, T>builder().build();

	@Getter
	private BaseService<ID, T> service;

	/**
	 * The list of components to update after an entity is selected
	 */
	private List<Component> componentsToUpdate = new ArrayList<>();

	/**
	 * The code that is carried out to create a new entity
	 */
	@Getter
	@Setter
	private Supplier<T> createEntity = () -> service.createNewEntity();

	/**
	 * Mapping from custom component label to custom component
	 */
	private Map<String, List<Component>> customComponentMap = new HashMap<>();

	/**
	 * Code that is carried out to determine whether the current user is allowed to
	 * edit
	 */
	@Getter
	@Setter
	private BooleanSupplier editAllowed = () -> true;

	/**
	 * The entity model of the entity or entities to display
	 */
	@Getter
	private final EntityModel<T> entityModel;

	/**
	 * The function used to determine the parent group of an attribute group
	 */
	@Getter
	@Setter
	private Function<String, String> findParentGroup;

	/**
	 * The form options that determine what options are available in the screen
	 */
	@Getter
	private FormOptions formOptions;

	/**
	 * Code that is executed to determine when a component in a button bar must be
	 * enabled
	 */
	@Getter
	@Setter
	private BiPredicate<Component, T> mustEnableComponent;

	/**
	 * Code that is executed when the user clicks the back button in an edit form
	 */
	@Getter
	@Setter
	private Runnable onBackButtonClicked = () -> {
	};

	/**
	 * The headers for the additional layer of attribute grouping that can be added
	 * to an edit form
	 */
	@Getter
	@Setter
	private String[] parentGroupHeaders;

	/**
	 * Constructor
	 *
	 * @param service     the service used to query the database
	 * @param entityModel the entity model
	 * @param formOptions the form options that govern how the component behaves
	 */
	protected BaseServiceCustomComponent(BaseService<ID, T> service, EntityModel<T> entityModel,
			FormOptions formOptions) {
		this.service = service;
		this.entityModel = entityModel;
		this.formOptions = formOptions;
	}

	public void addCustomConverter(String path, Supplier<Converter<?, ?>> converter) {
		componentContext.addCustomConverter(path, converter);
	}

	public void addCustomField(String path, Function<CustomFieldContext, Component> function) {
		componentContext.addCustomField(path, function);
	}

	public void addCustomRequiredValidator(String path, Supplier<Validator<?>> validator) {
		componentContext.addCustomRequiredValidator(path, validator);
	}

	public void addCustomValidator(String path, Supplier<Validator<?>> validator) {
		componentContext.addCustomValidator(path, validator);
	}

	public void addFieldEntityModel(String path, String reference) {
		componentContext.addFieldEntityModel(path, reference);
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

	public boolean checkEditAllowed() {
		return getEditAllowed() == null ? true : getEditAllowed().getAsBoolean();
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

	/**
	 * Copies component settings to the edit form
	 * 
	 * @param editForm the edit form
	 */
	protected void initEditForm(ModelBasedEditForm<ID, T> editForm) {
		editForm.setComponentContext(componentContext);
		editForm.setOnBackButtonClicked(getOnBackButtonClicked());
		editForm.setParentGroupHeaders(getParentGroupHeaders());
		editForm.setFindParentGroup(getFindParentGroup());
		editForm.setEditAllowed(getEditAllowed());
	}

	/**
	 * Check whether the provided component is a custom component stored under the
	 * provided key
	 * 
	 * @param key     the key
	 * @param toCheck the component to check
	 * @return
	 */
	public boolean isRegisteredComponent(String key, Component toCheck) {
		return customComponentMap.get(key) != null && customComponentMap.get(key).contains(toCheck);
	}

	/**
	 * Registers a component that must be enabled/disabled after an item is
	 * selected. use the "mustEnableComponent" callback method to impose additional
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

	public void setAfterEntitySelected(BiConsumer<ModelBasedEditForm<ID, T>, T> afterEntitySelected) {
		componentContext.setAfterEntitySelected(afterEntitySelected);
	}

	public void setAfterEntitySet(Consumer<T> afterEntitySet) {
		componentContext.setAfterEntitySet(afterEntitySet);
	}

	/**
	 * Specifies the code that will be carried out after the edit form has been
	 * constructed
	 */
	public void setAfterEditFormBuilt(BiConsumer<HasComponents, Boolean> aFterEditFormBuilt) {
		componentContext.setAfterEditFormBuilt(aFterEditFormBuilt);
	}

	public void setAfterModeChanged(BiConsumer<ModelBasedEditForm<ID, T>, Boolean> afterModeChanged) {
		componentContext.setAfterModeChanged(afterModeChanged);
	}

	public void setAfterTabSelected(Consumer<Integer> afterTabSelected) {
		componentContext.setAfterTabSelected(afterTabSelected);
	}

	public void setAfterUploadCompleted(BiConsumer<String, byte[]> afterUploadCompleted) {
		componentContext.setAfterUploadCompleted(afterUploadCompleted);
	}

	public void setCustomSaveAction(BiConsumer<ModelBasedEditForm<ID, T>, T> customSaveAction) {
		componentContext.setCustomSaveAction(customSaveAction);
	}

	public void setEditColumnThresholds(List<String> columnThresholds) {
		componentContext.setEditColumnThresholds(columnThresholds);
	}

	public void setGroupTogetherMode(GroupTogetherMode groupTogetherMode) {
		componentContext.setGroupTogetherMode(groupTogetherMode);
	}

	public void setGroupTogetherWidth(Integer groupTogetherWidth) {
		componentContext.setGroupTogetherWidth(groupTogetherWidth);
	}

	public void setMaxEditFormWidth(String maxEditFormWidth) {
		componentContext.setMaxEditFormWidth(maxEditFormWidth);
	}

	public void setPostProcessEditFields(Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields) {
		componentContext.setPostProcessEditFields(postProcessEditFields);
	}

	/**
	 * Registers a custom component under the specified key
	 * 
	 * @param key       the key under which to store the component
	 * @param component the component
	 */
	public void registerComponent(String key, Component component) {
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
	private void storeCustomComponent(String key, Component component) {
		customComponentMap.putIfAbsent(key, new ArrayList<>());
		customComponentMap.get(key).add(component);
	}

}
