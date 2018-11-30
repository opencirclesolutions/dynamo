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
package com.ocs.dynamo.ui.composite.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout for displaying various nested forms below each other
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID
 * @param <T> the type of the entity that is managed in the form
 */
public abstract class DetailsEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<Collection<T>> implements NestedComponent, UseInViewMode {

	/**
	 * 
	 * @author Bas Rutten
	 *
	 */
	private class FormContainer extends DefaultVerticalLayout {

		private static final long serialVersionUID = 3507638736422806589L;

		private ModelBasedEditForm<ID, T> form;

		private Button deleteButton;

		private HorizontalLayout buttonBar;

		/**
		 * Constructor
		 * 
		 * @param form     the model based edit form
		 * @param sameLine whether to display the form on the same line
		 */
		FormContainer(ModelBasedEditForm<ID, T> form) {
			super(false, true);
			this.form = form;

			if (!viewMode) {
				buttonBar = new DefaultHorizontalLayout(false, true, true);

				addComponent(form);
				addComponent(buttonBar);

				deleteButton = new Button(messageService.getMessage("ocs.remove", VaadinUtils.getLocale()));
				deleteButton.setIcon(VaadinIcons.TRASH);
				deleteButton.addClickListener(event -> {
					removeEntity(this.form.getEntity());
					items.remove(this.form.getEntity());
					mainFormContainer.removeComponent(this);
					forms.remove(this);
					detailComponentsValid.remove(form);
				});
				buttonBar.addComponent(deleteButton);
				postProcessButtonBar(buttonBar);
			} else {
				// read only mode
				addComponent(form);
			}
		}

		public T getEntity() {
			return form.getEntity();
		}

		@SuppressWarnings("unused")
		public ModelBasedEditForm<ID, T> getForm() {
			return form;
		}

		public void postProcessButtonBar(Layout buttonBar) {
			// overwrite in subclasses
		}

		public void setDeleteAllowed(boolean enabled) {
			if (deleteButton != null) {
				deleteButton.setEnabled(enabled);
			}
		}

		public void setDeleteVisible(boolean visible) {
			if (deleteButton != null) {
				deleteButton.setVisible(visible);
			}
		}

		public void setEntity(T t) {
			form.setEntity(t);
		}

		public void setFieldEnabled(String path, boolean enabled) {
			form.getFieldOptional(path).ifPresent(f -> f.setEnabled(enabled));
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
	private Button addButton;

	/**
	 * The entity model of the entity to display
	 */
	private final EntityModel<T> entityModel;

	/**
	 * The entity models used for rendering the individual fields (mostly useful for
	 * lookup components)
	 */
	private Map<String, String> fieldEntityModels = new HashMap<>();

	/**
	 * The attribute model of the attribute to display
	 */
	private final AttributeModel attributeModel;

	/**
	 * The comparator (will be used to sort the items)
	 */
	private Comparator<T> comparator;

	/**
	 * Optional field filters for restricting the contents of combo boxes
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	/**
	 * Form options that determine which buttons and functionalities are available
	 */
	private FormOptions formOptions;

	/**
	 * The list of items to display
	 */
	private List<T> items;

	/**
	 * The message service
	 */
	private final MessageService messageService;

	/**
	 * Whether the component is in view mode. If this is the case, editing is not
	 * allowed and no buttons will be displayed
	 */
	private boolean viewMode;

	/**
	 * Service for interacting with the database
	 */
	private BaseService<ID, T> service;

	/**
	 * The individual edit forms
	 */
	private List<FormContainer> forms = new ArrayList<>();

	private Map<NestedComponent, Boolean> detailComponentsValid = new HashMap<>();

	/**
	 * Container that holds all the sub forms
	 */
	private Layout mainFormContainer;

	/**
	 * Constructor
	 * 
	 * @param service        the service
	 * @param items          the items to display
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param viewMode       whether the form is in view mode
	 * @param formOptions    the form options
	 * @param comparator     the comparator for sorting the members
	 */
	public DetailsEditLayout(BaseService<ID, T> service, Collection<T> items, EntityModel<T> entityModel,
			AttributeModel attributeModel, boolean viewMode, FormOptions formOptions, Comparator<T> comparator) {
		this.service = service;
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.comparator = comparator;
		this.items = new ArrayList<>();
		this.items.addAll(items);
		this.viewMode = viewMode;
		this.formOptions = formOptions;
	}

	/**
	 * Adds a detail edit form
	 * 
	 * @param t the entity to display/edit
	 */
	private void addDetailEditForm(T t) {

		ModelBasedEditForm<ID, T> editForm = new ModelBasedEditForm<ID, T>(t, service, entityModel, formOptions,
				fieldFilters) {

			private static final long serialVersionUID = -7229109969816505927L;

			@Override
			protected void afterLayoutBuilt(Layout layout, boolean viewMode) {
				DetailsEditLayout.this.afterLayoutBuilt(this, viewMode);
			}

			@Override
			protected void afterModeChanged(boolean viewMode) {
				DetailsEditLayout.this.afterModeChanged(this, viewMode);
			}

			@Override
			protected AbstractField<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
					boolean viewMode) {
				return DetailsEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode);
			}

			@Override
			protected void postProcessEditFields() {
				super.postProcessEditFields();
				DetailsEditLayout.this.postProcessEditFields(this);
			}
		};
		editForm.setFieldEntityModels(getFieldEntityModels());
		editForm.setFieldFilters(fieldFilters);
		editForm.setNestedMode(true);
		editForm.setViewMode(viewMode);
		editForm.build();

		FormContainer fc = new FormContainer(editForm) {

			private static final long serialVersionUID = 6186428121967857827L;

			@Override
			public void postProcessButtonBar(Layout buttonBar) {
				DetailsEditLayout.this.postProcessDetailButtonBar(forms.size(), buttonBar, viewMode);
			}
		};
		forms.add(fc);
		mainFormContainer.addComponent(fc);

		detailComponentsValid.put(editForm, editForm.isValid());
	}

	/**
	 * Adds a field entity model - this can be used to overwrite the default entity
	 * model that is used for rendering complex selection components (lookup
	 * dialogs)
	 * 
	 * @param path      the path to the field
	 * @param reference the unique ID of the entity model
	 */
	public final void addFieldEntityModel(String path, String reference) {
		fieldEntityModels.put(path, reference);
	}

	protected void afterLayoutBuilt(ModelBasedEditForm<ID, T> editForm, boolean viewMode) {
		// override in subclasses
	}

	protected void afterModeChanged(ModelBasedEditForm<ID, T> editForm, boolean viewMode) {
		// override in subclasses
	}

	/**
	 * Constructs the button that is used for adding new items
	 * 
	 * @param buttonBar the button bar
	 */
	protected void constructAddButton(HorizontalLayout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(VaadinIcons.PLUS);
		addButton.addClickListener(event -> {
			T t = createEntity();
			items.add(t);
			addDetailEditForm(t);
		});

		addButton.setVisible(!viewMode && !formOptions.isHideAddButton());
		buttonBar.addComponent(addButton);

	}

	/**
	 * Constructs the button bar
	 * 
	 * @param parent the layout to which to add the button bar
	 */
	protected void constructButtonBar(Layout parent) {
		HorizontalLayout buttonBar = new DefaultHorizontalLayout();

		buttonBar.setVisible(!viewMode);
		parent.addComponent(buttonBar);

		constructAddButton(buttonBar);
		postProcessButtonBar(buttonBar);
	}

	/**
	 * Method that is called to create a custom field. Override in subclasses if
	 * needed
	 * 
	 * @param entityModel    the entity model of the entity that is displayed in the
	 *                       component
	 * @param attributeModel the attribute model of the attribute for which we are
	 *                       constructing a field
	 * @param viewMode       whether the form is in view mode
	 * @return
	 */
	protected AbstractField<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode) {
		return null;
	}

	/**
	 * Creates a new entity - override in subclass
	 * 
	 * @return
	 */
	protected abstract T createEntity();

	public Button getAddButton() {
		return addButton;
	}

	public Comparator<T> getComparator() {
		return comparator;
	}

	public T getEntity(int index) {
		if (index < this.forms.size()) {
			return this.forms.get(index).getEntity();
		}
		return null;
	}

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public Map<String, String> getFieldEntityModels() {
		return fieldEntityModels;
	}

	public Map<String, SerializablePredicate<?>> getFieldFilters() {
		return fieldFilters;
	}

	public FormOptions getFormOptions() {
		return formOptions;
	}

	public Collection<T> getItems() {
		return items;
	}

	/**
	 * Constructs the actual component
	 */
	@Override
	protected Component initContent() {

		VerticalLayout layout = new DefaultVerticalLayout(false, true);

		setCaption(attributeModel.getDisplayName());

		mainFormContainer = new DefaultVerticalLayout(false, false);
		layout.addComponent(mainFormContainer);

		if (comparator != null) {
			items.sort(comparator);
		}

		for (T t : items) {
			addDetailEditForm(t);
		}

		// add the buttons
		constructButtonBar(layout);

		postConstruct();

		return layout;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	/**
	 * Perform any necessary post construction
	 */
	protected void postConstruct() {
		// overwrite in subclasses
	}

	/**
	 * Callback method that is used to modify the button bar. Override in subclasses
	 * if needed
	 * 
	 * @param buttonBar
	 */
	protected void postProcessButtonBar(Layout buttonBar) {
		// overwrite in subclass if needed
	}

	protected void postProcessDetailButtonBar(int index, Layout buttonBar, boolean viewMode) {

	}

	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// override in subclasses
	}

	/**
	 * Callback method that is called when the remove button is clicked - allows
	 * decoupling the entity from its master
	 * 
	 * @param toRemove
	 */
	protected abstract void removeEntity(T toRemove);

	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	/**
	 * Enables or disables the delete button
	 * 
	 * @param index
	 * @param allowed
	 */
	public void setDeleteEnabled(int index, boolean allowed) {
		if (index < this.forms.size()) {
			this.forms.get(index).setDeleteAllowed(allowed);
		}
	}

	public void setDeleteVisible(int index, boolean visible) {
		if (index < this.forms.size()) {
			this.forms.get(index).setDeleteVisible(visible);
		}
	}

	public void setEntity(int index, T entity) {
		if (index < this.forms.size()) {
			this.forms.get(index).setEntity(entity);
		}
	}

	/**
	 * Enables or disables a field
	 * 
	 * @param index
	 * @param path
	 * @param enabled
	 */
	public void setFieldEnabled(int index, String path, boolean enabled) {
		if (index < this.forms.size()) {
			this.forms.get(index).setFieldEnabled(path, enabled);
		}
	}

	public void setFieldEntityModels(Map<String, String> fieldEntityModels) {
		this.fieldEntityModels = fieldEntityModels;
	}

	public void setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setFieldVisible(int index, String path, boolean visible) {
		if (index < this.forms.size()) {
			this.forms.get(index).setFieldVisible(path, visible);
		}
	}

	public void setFormOptions(FormOptions formOptions) {
		this.formOptions = formOptions;
	}

	/**
	 * Refreshes the items that are displayed in the layout
	 * 
	 * @param items the new set of items to be displayed
	 */
	public void setItems(Collection<T> items) {

		List<T> list = new ArrayList<>();
		list.addAll(items);
		if (comparator != null) {
			list.sort(comparator);
		}

		this.items = list;

		if (mainFormContainer != null) {
			mainFormContainer.removeAllComponents();
			forms.clear();
			for (T t : items) {
				addDetailEditForm(t);
			}
		}
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	@Override
	protected void doSetValue(Collection<T> value) {
		setItems(value);
	}

	public void signalModeChange(boolean viewMode) {
		// override in subclasses
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

	@Override
	public Collection<T> getValue() {
		// TODO: this does not appear to actually have to return anything for the
		// component to work
		return null;
	}
}
