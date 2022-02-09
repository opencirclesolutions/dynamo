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
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.grid.ComponentContext;
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
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
		extends CustomField<Collection<T>> implements NestedComponent, UseInViewMode {

	/**
	 * A container that holds the edit form for a single entity along with a button
	 * bar
	 * 
	 * @author Bas Rutten
	 *
	 */
	class FormContainer extends DefaultVerticalLayout {

		private static final long serialVersionUID = 3507638736422806589L;

		private HorizontalLayout buttonBar;

		private ModelBasedEditForm<ID, T> form;

		private Button removeButton;

		/**
		 * Constructor
		 * 
		 * @param form the model based edit form
		 */
		FormContainer(ModelBasedEditForm<ID, T> form) {
			super(false, false);
			addClassName(DynamoConstants.CSS_DETAILS_EDIT_LAYOUT);
			this.form = form;

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

			postProcessButtonBar(buttonBar);
		}

		private void addRemoveButton() {
			if (!viewMode && getFormOptions().isShowRemoveButton()) {
				removeButton = new Button(messageService.getMessage("ocs.remove", VaadinUtils.getLocale()));
				removeButton.setIcon(VaadinIcon.TRASH.create());
				removeButton.addClassName(DynamoConstants.CSS_DETAIL_EDIT_LAYOUT_REMOVE_BUTTON);
				removeButton.addClickListener(event -> {
					ModelBasedEditForm<ID2, Q> enc = DetailsEditLayout.this.getEnclosingForm();
					removeEntityConsumer.accept(enc == null ? null : enc.getEntity(), this.form.getEntity());
					items.remove(this.form.getEntity());
					mainFormContainer.remove(this);
					forms.remove(this);
				});
				buttonBar.add(removeButton);
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

		public void postProcessButtonBar(HorizontalLayout buttonBar) {
			// overwrite in subclasses
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

	@Getter
	@Setter
	private BiConsumer<HasComponents, Boolean> afterLayoutBuilt;

	@Getter
	@Setter
	private BiConsumer<ModelBasedEditForm<ID, T>, Boolean> afterModeChanged;

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

	private ComponentContext context = ComponentContext.builder().build();

	/**
	 * Supplier for creating a new entity
	 */
	@Getter
	@Setter
	private Function<Q, T> createEntitySupplier;

	@Getter
	private Map<AttributeModel, Supplier<Converter<?, ?>>> customConverters = new HashMap<>();

	@Getter
	private Map<AttributeModel, Supplier<Validator<?>>> customRequiredValidators = new HashMap<>();

	@Getter
	private Map<AttributeModel, Supplier<Validator<?>>> customValidators = new HashMap<>();

	@Getter
	@Setter
	private ModelBasedEditForm<ID2, Q> enclosingForm;

	@Getter
	private final EntityModel<T> entityModel;

	/**
	 * The entity models used for rendering the individual fields (most useful for
	 * lookup components)
	 */
	@Getter
	@Setter
	private Map<String, String> fieldEntityModels = new HashMap<>();

	@Getter
	@Setter
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	@Getter
	@Setter
	private FormOptions formOptions;

	private List<FormContainer> forms = new ArrayList<>();

	@Getter
	@Setter
	private GroupTogetherMode groupTogetherMode;

	@Getter
	@Setter
	private Integer groupTogetherWidth;

	/**
	 * The list of items to display
	 */
	private List<T> items;

	/**
	 * Container that holds all the sub forms
	 */
	private VerticalLayout mainFormContainer;

	/**
	 * The message service
	 */
	private final MessageService messageService;

	@Getter
	@Setter
	private Consumer<ModelBasedEditForm<ID, T>> postProcessEditFields;

	/**
	 * Consumer for removing an entity
	 */
	@Getter
	@Setter
	private BiConsumer<Q, T> removeEntityConsumer;

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

		initContent();
	}

	/**
	 * Adds an attribute entity model - this can be used to overwrite the default
	 * entity model that is used for rendering complex selection components (e.g.
	 * lookup dialogs)
	 * 
	 * @param path      the path to the field
	 * @param reference the unique ID of the entity model
	 */
	public final void addAttributeEntityModel(String path, String reference) {
		fieldEntityModels.put(path, reference);
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

//	/**
//	 * Constructs a custom converter
//	 * 
//	 * @param am
//	 * @return
//	 */
//	protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
//		return null;
//	}

	/**
	 * Adds a detail edit form
	 * 
	 * @param t the entity to display/edit
	 */
	private void addDetailEditForm(T entity) {

		ModelBasedEditForm<ID, T> editForm = new ModelBasedEditForm<ID, T>(entity, service, entityModel, formOptions,
				fieldFilters) {

			private static final long serialVersionUID = -7229109969816505927L;

//			@Override
//			protected void afterLayoutBuilt(HasComponents layout, boolean viewMode) {
//				DetailsEditLayout.this.afterLayoutBuilt(this, viewMode);
//			}

//			@Override
//			protected void afterModeChanged(boolean viewMode) {
//				DetailsEditLayout.this.afterModeChanged(this, viewMode);
//			}

//			@Override
//			protected <U, V> Converter<U, V> constructCustomConverter(AttributeModel am) {
//				return DetailsEditLayout.this.constructCustomConverter(am);
//			}

//			@Override
//			protected <V> Validator<V> constructCustomRequiredValidator(AttributeModel am) {
//				return DetailsEditLayout.this.constructCustomRequiredValidator(am, this);
//			}

//			@Override
//			protected <V> Validator<V> constructCustomValidator(AttributeModel am) {
//				return DetailsEditLayout.this.constructCustomValidator(am, this);
//			}

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
					boolean viewMode) {
				return DetailsEditLayout.this.constructCustomField(entityModel, attributeModel, viewMode);
			}

//			@Override
//			protected void postProcessEditFields() {
//				super.postProcessEditFields();
//				DetailsEditLayout.this.postProcessEditFields(this);
//			}
		};

		editForm.setPostProcessEditFields(getPostProcessEditFields());
		editForm.setFieldEntityModels(getFieldEntityModels());
		editForm.setFieldFilters(fieldFilters);
		editForm.setGroupTogetherMode(getGroupTogetherMode());
		editForm.setGroupTogetherWidth(getGroupTogetherWidth());
		editForm.setNestedMode(true);
		editForm.setViewMode(viewMode);
		editForm.setAfterLayoutBuilt(getAfterLayoutBuilt());
		editForm.setAfterModeChanged(getAfterModeChanged());

		editForm.setCustomConverters(getCustomConverters());
		editForm.setCustomValidators(getCustomValidators());
		editForm.setCustomRequiredValidators(getCustomRequiredValidators());

		editForm.build();

		FormContainer fc = new FormContainer(editForm) {

			private static final long serialVersionUID = 6186428121967857827L;

			@Override
			public void postProcessButtonBar(HorizontalLayout buttonBar) {
				DetailsEditLayout.this.postProcessDetailButtonBar(forms.size(), buttonBar, viewMode);
			}
		};
		forms.add(fc);
		mainFormContainer.add(fc);
	}

	/**
	 * Constructs the button that is used for adding new items
	 * 
	 * @param buttonBar the button bar
	 */
	protected void constructAddButton(FlexLayout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(VaadinIcon.PLUS.create());
		addButton.addClickListener(event -> {
			T t = createEntitySupplier.apply(getEnclosingForm() == null ? null : getEnclosingForm().getEntity());
			items.add(t);
			addDetailEditForm(t);
		});

		addButton.setVisible(!viewMode && !formOptions.isHideAddButton());
		buttonBar.add(addButton);

	}

	/**
	 * Constructs the button bar
	 * 
	 * @param parent the layout to which to add the button bar
	 */
	protected void constructButtonBar(VerticalLayout parent) {
		FlexLayout buttonBar = new FlexLayout();

		buttonBar.setVisible(!viewMode);
		parent.add(buttonBar);

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
	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode) {
		return null;
	}

	protected <V> Validator<V> constructCustomRequiredValidator(AttributeModel am, ModelBasedEditForm<ID, T> editForm) {
		return null;
	}

	protected <V> Validator<V> constructCustomValidator(AttributeModel am, ModelBasedEditForm<ID, T> editForm) {
		return null;
	}

	@Override
	protected Collection<T> generateModelValue() {
		return ConvertUtils.convertCollection(items == null ? new ArrayList<>() : items, attributeModel);
	}

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

	/**
	 * Constructs the actual component
	 */
	protected void initContent() {

		VerticalLayout layout = new DefaultVerticalLayout(false, false);

		mainFormContainer = new DefaultVerticalLayout(false, false);
		layout.add(mainFormContainer);

		// add the buttons
		constructButtonBar(layout);

		// initial filling
		setItems(items);

		add(layout);
		postConstruct();
	}

	public boolean isViewMode() {
		return viewMode;
	}

	protected void postConstruct() {
		// overwrite in subclasses
	}

	/**
	 * Callback method that is used to modify the main button bar that appears below
	 * the sub-forms. Override in subclasses if needed
	 * 
	 * @param buttonBar the button bar
	 */
	protected void postProcessButtonBar(FlexLayout buttonBar) {
		// overwrite in subclass if needed
	}

	/**
	 * Callback method that is used to modify the detail button bar that is rendered
	 * for every sub-form
	 * 
	 * @param index     the zero-based index of the sub-form
	 * @param buttonBar the button bar
	 * @param viewMode  whether the component is in view mode
	 */
	protected void postProcessDetailButtonBar(int index, HorizontalLayout buttonBar, boolean viewMode) {
		// overwrite in subclass if needed
	}

	/**
	 * Callback method that is used to modify the fields after creation. This method
	 * is called just once during component construction
	 * 
	 * @param editForm the edit form that contains the fields
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// override in subclasses
	}

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
				addDetailEditForm(t);
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

}
