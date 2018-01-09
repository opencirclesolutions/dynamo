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
import com.vaadin.data.Container.Filter;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * A layout for displaying various
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public abstract class DetailsEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<Collection<T>> implements SignalsParent {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The button that can be used to add rows to the table
	 */
	private Button addButton;

	/**
	 * The entity model of the entity to display
	 */
	private final EntityModel<T> entityModel;

	/**
	 * The attribute model of the attribute to display
	 */
	private final AttributeModel attributeModel;

	/**
	 * Optional field filters for restricting the contents of combo boxes
	 */
	private Map<String, Filter> fieldFilters = new HashMap<>();

	/**
	 * Form options that determine which buttons and functionalities are available
	 */
	private FormOptions formOptions;

	/**
	 * The list of items to display
	 */
	private Collection<T> items;

	/**
	 * The message service
	 */
	private final MessageService messageService;

	/**
	 * The parent form in which this component is embedded
	 */
	private ModelBasedEditForm<?, ?> parentForm;

	/**
	 * Whether the table is in view mode. If this is the case, editing is not
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

	/**
	 * Container that holds all the subforms
	 */
	private Layout mainFormContainer;

	private class FormContainer extends DefaultVerticalLayout {

		private static final long serialVersionUID = 3507638736422806589L;

		private ModelBasedEditForm<ID, T> form;

		private Button deleteButton;

		/**
		 * 
		 * @param form
		 */
		FormContainer(ModelBasedEditForm<ID, T> form) {
			super(false, true);
			this.form = form;
			addComponent(form);

			if (!viewMode) {
				deleteButton = new Button(messageService.getMessage("ocs.remove", VaadinUtils.getLocale()));
				deleteButton.addClickListener(event -> {
					removeEntity(this.form.getEntity());
					items.remove(this.form.getEntity());
					mainFormContainer.removeComponent(this);

					boolean allValid = forms.stream().allMatch(x -> x.isValid());
					parentForm.signalDetailsComponentValid(DetailsEditLayout.this, allValid);
				});
				addComponent(deleteButton);
			}
		}

		public boolean isValid() {
			return form.isValid();
		}
	}

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service
	 * @param items
	 *            the items to display
	 * @param entityModel
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @param viewMode
	 *            whether the form is in view mode
	 * @param formOptions
	 *            the form options
	 */
	public DetailsEditLayout(BaseService<ID, T> service, Collection<T> items, EntityModel<T> entityModel,
			AttributeModel attributeModel, boolean viewMode, FormOptions formOptions) {
		this.service = service;
		this.entityModel = entityModel;
		this.attributeModel = attributeModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.items = items;
		this.viewMode = viewMode;
		this.formOptions = formOptions;
	}

	/**
	 * Adds a detail edit form
	 * 
	 * @param t
	 */
	private void addDetailEditForm(T t) {
		ModelBasedEditForm<ID, T> editForm = new ModelBasedEditForm<ID, T>(t, service, entityModel, formOptions,
				fieldFilters) {

			private static final long serialVersionUID = -7229109969816505927L;

			@Override
			protected void afterLayoutBuilt(boolean viewMode) {
				DetailsEditLayout.this.afterLayoutBuilt(this, viewMode);
			}

			@Override
			protected void afterModeChanged(boolean viewMode) {
				DetailsEditLayout.this.afterModeChanged(this, viewMode);
			}

			@Override
			protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
					boolean viewMode) {
				return DetailsEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode);
			}

			@Override
			protected void postProcessEditFields() {
				DetailsEditLayout.this.postProcessEditFields(this);

				// signal parent if everything is valid
				for (Field<?> f : getFields(false)) {
					f.addValueChangeListener(event -> {
						boolean allValid = forms.stream().allMatch(x -> x.isValid());
						parentForm.signalDetailsComponentValid(DetailsEditLayout.this, allValid);
					});
				}
			}
		};
		editForm.setFieldFilters(fieldFilters);
		editForm.setNestedMode(true);
		editForm.setViewMode(viewMode);
		editForm.build();

		FormContainer fc = new FormContainer(editForm);
		forms.add(fc);

		mainFormContainer.addComponent(fc);
	}

	protected void afterLayoutBuilt(ModelBasedEditForm<ID, T> editForm, boolean viewMode) {
		// override in subclasses
	}

	protected void afterModeChanged(ModelBasedEditForm<ID, T> editForm, boolean viewMode) {
		// override
	}

	/**
	 * Constructs the button that is used for adding new items
	 * 
	 * @param buttonBar
	 *            the button bar
	 */
	protected void constructAddButton(Layout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.addClickListener(event -> {
			T t = createEntity();
			items.add(t);
			addDetailEditForm(t);

			if (parentForm != null) {
				parentForm.signalDetailsComponentValid(this, forms.stream().allMatch(f -> f.isValid()));
			}
		});
		addButton.setVisible(!viewMode && !formOptions.isHideAddButton());
		buttonBar.addComponent(addButton);
	}

	/**
	 * Constructs the button bar
	 * 
	 * @param parent
	 *            the layout to which to add the button bar
	 */
	protected void constructButtonBar(Layout parent) {
		Layout buttonBar = new DefaultHorizontalLayout();
		parent.addComponent(buttonBar);

		constructAddButton(buttonBar);
		postProcessButtonBar(buttonBar);
	}

	/**
	 * Method that is called to create a custom field. Override in subclasses if
	 * needed
	 * 
	 * @param entityModel
	 *            the entity model of the entity that is displayed in the table
	 * @param attributeModel
	 *            the attribute model of the attribute for which we are constructing
	 *            a field
	 * @param viewMode
	 *            whether the form is in view mode
	 * @return
	 */
	protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
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

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	public FormOptions getFormOptions() {
		return formOptions;
	}

	public Collection<T> getItems() {
		return items;
	}

	public ModelBasedEditForm<?, ?> getParentForm() {
		return parentForm;
	}

	/**
	 * Returns the type of the field (inherited form CustomField)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Collection<T>> getType() {
		return (Class<Collection<T>>) (Class<?>) Collection.class;
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

		for (T t : items) {
			addDetailEditForm(t);
		}

		// add the buttons
		constructButtonBar(layout);

		// set the reference to the parent so the status of the save button can
		// be set correctly
		ModelBasedEditForm<?, ?> parent = VaadinUtils.getParentOfClass(this, ModelBasedEditForm.class);
		setParentForm(parent);

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

	public void setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setFormOptions(FormOptions formOptions) {
		this.formOptions = formOptions;
	}

	@Override
	protected void setInternalValue(Collection<T> newValue) {
		setItems(newValue);
		super.setInternalValue(newValue);
	}

	/**
	 * Refreshes the items that are displayed in the table
	 * 
	 * @param items
	 *            the new set of items to be displayed
	 */
	public void setItems(Collection<T> items) {

		List<T> list = new ArrayList<>();
		list.addAll(items);
		this.items = list;

		if (mainFormContainer != null) {
			mainFormContainer.removeAllComponents();
			forms.clear();
			for (T t : items) {
				addDetailEditForm(t);
			}
		}
	}

	/**
	 * This method is called to store a reference to the parent form
	 * 
	 * @param parentForm
	 */
	private void setParentForm(ModelBasedEditForm<?, ?> parentForm) {
		this.parentForm = parentForm;
		if (parentForm != null) {
			parentForm.signalDetailsComponentValid(this, forms.stream().allMatch(f -> f.isValid()));
		}
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	@Override
	public void setValue(Collection<T> newFieldValue) {
		setItems(newFieldValue);
		super.setValue(newFieldValue);
	}

}
