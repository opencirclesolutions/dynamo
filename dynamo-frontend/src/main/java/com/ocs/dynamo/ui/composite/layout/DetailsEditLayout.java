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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.GroupTogetherMode;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.component.CustomFieldContext;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.TriConsumer;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A layout for displaying various nested forms below each other
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID
 * @param <T>  the type of the entity that is managed in the form
 */
public class DetailsEditLayout<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, Q extends AbstractEntity<ID2>>
		extends CustomField<Collection<T>> implements Buildable, NestedComponent, UseInViewMode {

	/**
	 * A container that holds the edit form for a single entity along with a button
	 * bar
	 * 
	 * @author Bas Rutten
	 *
	 */
	class FormContainer extends DefaultVerticalLayout implements Buildable {

		private static final long serialVersionUID = 3507638736422806589L;

		private HorizontalLayout buttonBar;

		private ModelBasedEditForm<ID, T> form;

		private int index;

		@Getter
		@Setter
		private TriConsumer<Integer, HorizontalLayout, Boolean> postProcessDetailButtonBar;

		private Button removeButton;

		/**
		 * Constructor
		 * 
		 * @param form the model based edit form
		 */
		FormContainer(int index, ModelBasedEditForm<ID, T> form) {
			super(false, false);
			addClassName(DynamoConstants.CSS_DETAILS_EDIT_LAYOUT);
			this.form = form;
			this.index = index;
		}

		private void addRemoveButton() {
			if (!viewMode && getFormOptions().isShowRemoveButton()) {
				removeButton = new Button(messageService.getMessage("ocs.remove", VaadinUtils.getLocale()));
				removeButton.setIcon(VaadinIcon.TRASH.create());
				removeButton.addClassName(DynamoConstants.CSS_DETAIL_EDIT_LAYOUT_REMOVE_BUTTON);
				removeButton.addClickListener(event -> {
					ModelBasedEditForm<ID2, Q> enc = DetailsEditLayout.this.getEnclosingForm();
					removeEntity.accept(enc == null ? null : enc.getEntity(), this.form.getEntity());
					items.remove(this.form.getEntity());
					mainFormContainer.remove(this);
					forms.remove(this);
				});
				buttonBar.add(removeButton);
			}
		}

		public void build() {
			if (buttonBar == null) {
				buttonBar = new DefaultHorizontalLayout(false, true);

				if (getFormOptions().isDetailsEditLayoutButtonsOnSameRow()) {
					HorizontalLayout rowLayout = new DefaultHorizontalLayout(false, false);
					rowLayout.addClassName("detailsEditLayoutRow");

					rowLayout.setSizeFull();
					add(rowLayout);
					rowLayout.add(form);
					rowLayout.setFlexGrow(4, form);
					rowLayout.add(buttonBar);
					rowLayout.setFlexGrow(1, buttonBar);
					buttonBar.addClassName(DynamoConstants.CSS_DETAILS_EDIT_LAYOUT_BUTTONBAR_SAME);
				} else {
					add(form);
					add(buttonBar);
					buttonBar.addClassName(DynamoConstants.CSS_DETAILS_EDIT_LAYOUT_BUTTONBAR);
				}

				addRemoveButton();

				if (postProcessDetailButtonBar != null) {
					postProcessDetailButtonBar.accept(index, buttonBar, isViewMode());
				}
			}
		}

		public Button getDeleteButton() {
			return removeButton;
		}

		public T getEntity() {
			return form.getEntity();
		}

		public ModelBasedEditForm<ID, T> getForm() {
			return form;
		}

		@Override
		protected void onAttach(AttachEvent attachEvent) {
			super.onAttach(attachEvent);
			build();
		}

		public void setDeleteAllowed(boolean enabled) {
			if (removeButton != null) {
				removeButton.setEnabled(enabled);
			}
		}

		public void setDeleteVisible(boolean visible) {
			if (removeButton != null) {
				removeButton.setVisible(visible);
			}
		}

		public void setEntity(T t) {
			form.setEntity(t);
		}

		public void setFieldEnabled(String path, boolean enabled) {
			form.getFieldOptional(path).ifPresent(f -> ((HasEnabled) f).setEnabled(enabled));
		}

		public void setFieldVisible(String path, boolean visible) {
			form.getFieldOptional(path).ifPresent(f -> f.setVisible(visible));
		}

		public boolean validateAllFields() {
			return form.validateAllFields();
		}

	}

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The button that can be used to add forms
	 */
	@Getter
	private Button addButton;

	/**
	 * The attribute model of the attribute to display
	 */
	@Getter
	private final AttributeModel attributeModel;

	/**
	 * The comparator (will be used to sort the items)
	 */
	@Getter
	@Setter
	private Comparator<T> comparator;

	private ComponentContext<ID, T> componentContext = ComponentContext.<ID, T>builder().build();

	/**
	 * The code that is carried out to create a new entity
	 */
	@Getter
	@Setter
	private Function<Q, T> createEntity;

	@Getter
	@Setter
	private ModelBasedEditForm<ID2, Q> enclosingForm;

	@Getter
	private final EntityModel<T> entityModel;

	@Getter
	@Setter
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	@Getter
	@Setter
	private FormOptions formOptions;

	private List<FormContainer> forms = new ArrayList<>();

	private List<T> items;

	private VerticalLayout layout;

	/**
	 * Container that holds all the sub forms
	 */
	private VerticalLayout mainFormContainer;

	private final MessageService messageService;

	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessButtonBar;

	@Getter
	@Setter
	private TriConsumer<Integer, HorizontalLayout, Boolean> postProcessDetailButtonBar;

	/**
	 * Consumer for removing an entity
	 */
	@Getter
	@Setter
	private BiConsumer<Q, T> removeEntity;

	/**
	 * Service for interacting with the database
	 */
	private BaseService<ID, T> service;

	/**
	 * Whether the component is in view mode. If this is the case, editing is not
	 * allowed and no buttons will be displayed
	 */
	private boolean viewMode;

	/**
	 * Constructor
	 * 
	 * @param service        the service
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param viewMode       whether the form is in view mode
	 * @param sameRow        whether to display the fields on the same row
	 * @param formOptions    the form options
	 * @param comparator     the comparator for sorting the items
	 */
	public DetailsEditLayout(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, FormOptions formOptions, Comparator<T> comparator) {
		this.service = service;
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.comparator = comparator;
		this.items = new ArrayList<>();
		this.viewMode = viewMode;
		this.formOptions = formOptions;
	}

	/**
	 * Adds a custom converter
	 * 
	 * @param path      the attribute model the attribute model for which to add a
	 *                  custom converter
	 * @param converter the converter to add
	 */
	public void addCustomConverter(String path, Supplier<Converter<?, ?>> converter) {
		componentContext.addCustomConverter(path, converter);
	}

	/**
	 * 
	 * @param path
	 * @param function
	 */
	public void addCustomField(String path, Function<CustomFieldContext, Component> function) {
		componentContext.addCustomField(path, function);
	}

	/**
	 * Adds a custom required validator
	 * 
	 * @param attributeModel the attribute model the attribute model for which to
	 *                       add a custom converter
	 * @param converter      the validator to add
	 */
	public void addCustomRequiredValidator(String path, Supplier<Validator<?>> validator) {
		componentContext.addCustomRequiredValidator(path, validator);
	}

	/**
	 * Adds a custom validator
	 * 
	 * @param attributeModel the attribute model the attribute model for which to
	 *                       add a custom converter
	 * @param converter      the converter to add
	 */
	public void addCustomValidator(String path, Supplier<Validator<?>> validator) {
		componentContext.addCustomValidator(path, validator);
	}

	/**
	 * Adds a detail edit form
	 * 
	 * @param index  the index of the form
	 * @param entity the entity to display/edit
	 */
	private void addDetailEditForm(int index, T entity) {

		ModelBasedEditForm<ID, T> editForm = new ModelBasedEditForm<ID, T>(entity, service, entityModel, formOptions,
				fieldFilters);

		editForm.setComponentContext(componentContext);
		editForm.setFieldFilters(fieldFilters);
		editForm.setNestedMode(true);
		editForm.setViewMode(viewMode);
		editForm.build();

		FormContainer formContainer = new FormContainer(index, editForm);
		formContainer.setPostProcessDetailButtonBar(getPostProcessDetailButtonBar());
		formContainer.build();

		forms.add(formContainer);
		mainFormContainer.add(formContainer);

	}

	/**
	 * Adds an attribute entity model - this can be used to overwrite the default
	 * entity model that is used for rendering complex selection components (e.g.
	 * lookup dialogs)
	 * 
	 * @param path      the path to the field
	 * @param reference the unique ID of the entity model
	 */
	public void addFieldEntityModel(String path, String reference) {
		componentContext.addFieldEntityModel(path, reference);
	}

	/**
	 * Constructs the actual component
	 */
	@Override
	public void build() {
		if (layout == null) {
			layout = new DefaultVerticalLayout(false, false);

			mainFormContainer = new DefaultVerticalLayout(false, false);
			layout.add(mainFormContainer);

			// add the buttons
			constructButtonBar(layout);

			// initial filling
			setItems(items);
			add(layout);
		}
	}

	/**
	 * Constructs the button that is used for adding new items
	 * 
	 * @param buttonBar the button bar to which to add the button
	 */
	protected final void constructAddButton(FlexLayout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(VaadinIcon.PLUS.create());
		addButton.addClickListener(event -> {
			T t = createEntity.apply(getEnclosingForm() == null ? null : getEnclosingForm().getEntity());
			items.add(t);
			addDetailEditForm(getFormCount(), t);
		});

		addButton.setVisible(!viewMode && formOptions.isShowAddButton());
		buttonBar.add(addButton);
	}

	/**
	 * Constructs the button bar
	 * 
	 * @param parent the layout to which to add the button bar
	 */
	protected final void constructButtonBar(VerticalLayout parent) {
		FlexLayout buttonBar = new FlexLayout();

		buttonBar.setVisible(!viewMode);
		parent.add(buttonBar);

		constructAddButton(buttonBar);
		if (postProcessButtonBar != null) {
			postProcessButtonBar.accept(buttonBar);
		}
	}

	@Override
	protected Collection<T> generateModelValue() {
		return ConvertUtils.convertCollection(items == null ? new ArrayList<>() : items, attributeModel);
	}

	/**
	 * Return the entity that is managed by the form with the specified index
	 * 
	 * @param index the index of the desired form
	 * @return
	 */
	public T getEntity(int index) {
		if (index < this.forms.size()) {
			return this.forms.get(index).getEntity();
		}
		return null;
	}

	public ModelBasedEditForm<ID, T> getForm(int index) {
		return getFormContainer(index).getForm();
	}

	/**
	 * Returns the FormContainer specified by the index
	 * 
	 * @param index the zero-based index of the form container
	 * @return
	 */
	public FormContainer getFormContainer(int index) {
		if (index < this.forms.size()) {
			return forms.get(index);
		}
		return null;
	}

	/**
	 * Returns the current number of forms
	 * 
	 * @return
	 */
	public Integer getFormCount() {
		return forms.size();
	}

	@Override
	public Collection<T> getValue() {
		return ConvertUtils.convertCollection(items == null ? new ArrayList<>() : items, attributeModel);
	}

	public boolean isViewMode() {
		return viewMode;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
	}

	public void setAfterEditFormBuilt(BiConsumer<HasComponents, Boolean> afterEditFormBuilt) {
		componentContext.setAfterEditFormBuilt(afterEditFormBuilt);
	}

	public void setAfterModeChanged(BiConsumer<ModelBasedEditForm<ID, T>, Boolean> afterModeChanged) {
		componentContext.setAfterModeChanged(afterModeChanged);
	}

	/**
	 * Sets the class name
	 * 
	 * @param className the class name
	 */
	public void setClassName(String className) {
		this.getElement().setAttribute("class", className);
	}

	/**
	 * Enables or disables the delete button for a sub-form
	 * 
	 * @param index   the zero-based index of the sub-form
	 * @param allowed whether deleting is allowed
	 */
	public void setDeleteEnabled(int index, boolean allowed) {
		if (index < this.forms.size()) {
			this.forms.get(index).setDeleteAllowed(allowed);
		}
	}

	/**
	 * Sets the visibility of the delete button for a form
	 * 
	 * @param index   the zero-based index of the form
	 * @param visible the desired visibility
	 */
	public void setDeleteVisible(int index, boolean visible) {
		if (index < this.forms.size()) {
			this.forms.get(index).setDeleteVisible(visible);
		}
	}

	/**
	 * Sets the entity for a certain form to the provided entity
	 * 
	 * @param index  the zero-based index of the form
	 * @param entity the entity to set
	 */
	public void setEntity(int index, T entity) {
		if (index < this.forms.size()) {
			this.forms.get(index).setEntity(entity);
		}
	}

	/**
	 * Enables or disables a field inside a form
	 * 
	 * @param index   the zero-based index of the form
	 * @param path    the path to the attribute
	 * @param enabled whether to enable the field
	 */
	public void setFieldEnabled(int index, String path, boolean enabled) {
		if (index < this.forms.size()) {
			this.forms.get(index).setFieldEnabled(path, enabled);
		}
	}

	/**
	 * Sets the visibility of the specified field in a specified sub-form
	 * 
	 * @param index   the zero-based index of the sub-form
	 * @param path    the path to the field
	 * @param visible the desired visibility
	 */
	public void setFieldVisible(int index, String path, boolean visible) {
		if (index < this.forms.size()) {
			this.forms.get(index).setFieldVisible(path, visible);
		}
	}

	public void setGroupTogetherMode(GroupTogetherMode groupTogetherMode) {
		componentContext.setGroupTogetherMode(groupTogetherMode);
	}

	public void setGroupTogetherWidth(Integer groupTogetherWidth) {
		componentContext.setGroupTogetherWidth(groupTogetherWidth);
	}

	/**
	 * Sets the items that are specified in the layout
	 * 
	 * @param items the new set of items to be displayed
	 */
	public void setItems(Collection<T> items) {

		List<T> list = new ArrayList<>();
		if (items != null) {
			list.addAll(items);
		}
		if (comparator != null) {
			list.sort(comparator);
		}

		this.items = list;

		if (mainFormContainer != null) {
			mainFormContainer.removeAll();
			forms.clear();
			for (T t : this.items) {
				addDetailEditForm(getFormCount(), t);
			}
		}
	}

	@Override
	protected void setPresentationValue(Collection<T> value) {
		setItems(value);
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	@Override
	public void setValue(Collection<T> value) {
		setItems(value);
	}

	/**
	 * Validates all underlying forms
	 */
	public boolean validateAllFields() {
		boolean error = false;
		for (FormContainer f : forms) {
			error |= f.validateAllFields();
		}
		return error;
	}

	public void setPostProcessEditFields(Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields) {
		componentContext.setPostProcessEditFields(postProcessEditFields);
	}

}
