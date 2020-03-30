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
package com.ocs.dynamo.ui.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.grid.ModelBasedGrid;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;

/**
 * Base class for grid components that are displayed inside an edit form. These
 * components can be used to manage a one-to-many or many-to-many collection.
 * 
 * For small collections that can be managed in-memory and fetched through JPA
 * relationship fetching, use the DetailsEditGrid subclass. For larger
 * collections that should not be managed in-memory, use the
 * ServiceBasedDetailsEditGrid instead
 * 
 * @author Bas Rutten
 *
 * @param <U>  the type of the bound property
 * @param <ID> the type of the ID of the entities that are being displayed
 * @param <T>  the type of the entities that are being displayed
 */
public abstract class BaseDetailsEditGrid<U, ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<U> implements NestedComponent, UseInViewMode {

	private static final long serialVersionUID = 997617632007985450L;

	/**
	 * The button that can be used to add rows to the grid
	 */
	private Button addButton;

	/**
	 * The attribute model of the attribute that is managed by this component
	 */
	private AttributeModel attributeModel;

	/**
	 * Map with a binder for every row
	 */
	private Map<T, Binder<T>> binders = new HashMap<>();

	/**
	 * List of components to update after a detail is selected
	 */
	private List<Component> componentsToUpdate = new ArrayList<>();

	/**
	 * The supplier that is used for creating a new entity in response to a click on
	 * the Add button
	 */
	private Supplier<T> createEntitySupplier;

	/**
	 * Search dialog
	 */
	private ModelBasedSearchDialog<ID, T> dialog;

	/**
	 * The entity model of the entity to display
	 */
	private final EntityModel<T> entityModel;

	/**
	 * Optional field filters for restricting the contents of combo boxes
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	/**
	 * Form options that determine which buttons and functionalities are available
	 */
	private FormOptions formOptions;

	/**
	 * The grid for displaying the actual items
	 */
	private ModelBasedGrid<ID, T> grid;

	/**
	 * The grid height
	 */
	private String gridHeight = SystemPropertyUtils.getDefaultEditGridHeight();

	/**
	 * Code to execute after selecting one or more items in the pop-up (link the
	 * selected item to the parent)
	 */
	private Consumer<T> linkEntityConsumer;

	/**
	 * The message service
	 */
	private final MessageService messageService;

	/**
	 * Consumer that is used to remove an entity
	 */
	private Consumer<T> removeEntityConsumer;

	/**
	 * Button used to open the search dialog
	 */
	private Button searchDialogButton;

	/**
	 * Overridden entity model for the search dialog
	 */
	private EntityModel<T> searchDialogEntityModel;

	/**
	 * Filters to apply to the search dialog
	 */
	private List<SerializablePredicate<T>> searchDialogFilters;

	/**
	 * Sort order to apply to the search dialog
	 */
	private SortOrder<T> searchDialogSortOrder;

	/**
	 * The currently selected item
	 */
	private T selectedItem;

	/**
	 * The service that is used to communicate with the database
	 */
	private BaseService<ID, T> service;

	/**
	 * Indicates whether the component is used in service-based mode, in this case
	 * the values in the column cannot be edited directly
	 */
	private boolean serviceBasedEditMode;

	/**
	 * Whether the component is in view mode. If this is the case, editing is not
	 * allowed and no buttons will be displayed
	 */
	private boolean viewMode;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param attributeModel
	 * @param viewMode
	 * @param formOptions
	 * @param joins
	 */
	public BaseDetailsEditGrid(BaseService<ID, T> service, EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode, boolean serviceBasedEditMode, FormOptions formOptions) {
		this.service = service;
		this.entityModel = entityModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.viewMode = viewMode;
		this.formOptions = formOptions;
		this.attributeModel = attributeModel;
		this.serviceBasedEditMode = serviceBasedEditMode;
	}

	public void addFieldFilter(String property, SerializablePredicate<?> filter) {
		this.fieldFilters.put(property, filter);
	}

	/**
	 * Applies a filter to restrict the values to be displayed
	 */
	protected abstract void applyFilter();

	/**
	 * Checks which buttons in the button bar must be enabled after an item has been
	 * selected
	 *
	 * @param selectedItem the selected item
	 */
	protected void checkComponentState(T selectedItem) {
		for (Component b : componentsToUpdate) {
			boolean enabled = selectedItem != null && mustEnableComponent(b, selectedItem);
			if (b instanceof HasEnabled) {
				((HasEnabled) b).setEnabled(enabled);
			}
		}
	}

	/**
	 * Constructs the button that is used for adding new items
	 *
	 * @param buttonBar the button bar
	 */
	protected void constructAddButton(HorizontalLayout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(VaadinIcon.PLUS.create());
		addButton.addClickListener(event -> doAdd());
		addButton.setVisible((isGridEditEnabled()
				|| (!isViewMode() && serviceBasedEditMode && !formOptions.isDetailsGridSearchMode()))
				&& !formOptions.isHideAddButton());
		buttonBar.add(addButton);
	}

	/**
	 * Constructs the button bar
	 *
	 * @param parent the layout to which to add the button bar
	 */
	protected void constructButtonBar(VerticalLayout parent) {
		HorizontalLayout buttonBar = new DefaultHorizontalLayout();
		parent.add(buttonBar);
		constructAddButton(buttonBar);
		constructSearchButton(buttonBar);
		postProcessButtonBar(buttonBar, viewMode);
	}

	/**
	 * Callback method for creating a custom converter
	 * 
	 * @param am the attribute model
	 * @return
	 */
	protected <W, V> Converter<W, V> constructCustomConverter(AttributeModel am) {
		return null;
	}

	/**
	 * Method that is called to create a custom field. Override in subclasses if
	 * needed
	 *
	 * @param entityModel    the entity model of the entity that is displayed in the
	 *                       grid
	 * @param attributeModel the attribute model of the attribute for which we are
	 *                       constructing a field
	 * @param viewMode       whether the form is in view mode
	 * @return
	 */
	protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Constructs a button that brings up a search dialog
	 *
	 * @param buttonBar the button bar to which to add the button
	 */
	protected void constructSearchButton(HorizontalLayout buttonBar) {

		searchDialogButton = new Button(messageService.getMessage("ocs.search", VaadinUtils.getLocale()));
		searchDialogButton.setIcon(VaadinIcon.SEARCH.create());
		searchDialogButton.addClickListener(event -> {

			// service must be specified
			if (service == null) {
				throw new OCSRuntimeException(
						messageService.getMessage("ocs.no.service.specified", VaadinUtils.getLocale()));
			}

			dialog = new ModelBasedSearchDialog<ID, T>(service,
					searchDialogEntityModel != null ? searchDialogEntityModel : entityModel, searchDialogFilters,
					searchDialogSortOrder == null ? null : Lists.newArrayList(searchDialogSortOrder), true, true) {

				private static final long serialVersionUID = 1512969437992973122L;

				@Override
				protected boolean doClose() {
					// add the selected items to the grid
					Collection<T> selected = getSelectedItems();
					if (selected != null) {
						handleDialogSelection(selected);
						getDataProvider().refreshAll();
					}
					return true;
				}
			};
			dialog.build();
			dialog.open();
		});
		searchDialogButton.setVisible(!viewMode && formOptions.isDetailsGridSearchMode());
		buttonBar.add(searchDialogButton);
	}

	/**
	 * The code to carry out after clicking on the add button
	 */
	protected abstract void doAdd();

	/**
	 * The code to carry out after clicking the edit button
	 */
	protected void doEdit(T t) {
	}

	public Button getAddButton() {
		return addButton;
	}

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	public Map<T, Binder<T>> getBinders() {
		return binders;
	}

	public Supplier<T> getCreateEntitySupplier() {
		return createEntitySupplier;
	}

	/**
	 * Returns the data provider
	 */
	protected abstract DataProvider<T, SerializablePredicate<T>> getDataProvider();

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public Map<String, SerializablePredicate<?>> getFieldFilters() {
		return fieldFilters;
	}

	public FormOptions getFormOptions() {
		return formOptions;
	}

	public ModelBasedGrid<ID, T> getGrid() {
		return grid;
	}

	public String getGridHeight() {
		return gridHeight;
	}

	public Consumer<T> getLinkEntityConsumer() {
		return linkEntityConsumer;
	}

	public Button getSearchDialogButton() {
		return searchDialogButton;
	}

	public EntityModel<T> getSearchDialogEntityModel() {
		return searchDialogEntityModel;
	}

	public List<SerializablePredicate<T>> getSearchDialogFilters() {
		return searchDialogFilters;
	}

	public SortOrder<T> getSearchDialogSortOrder() {
		return searchDialogSortOrder;
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public BaseService<ID, T> getService() {
		return service;
	}

	/**
	 * Method that is called after closing the popup dialog - handles the selected
	 * items in the dialog
	 * 
	 * @param selected the collection of selected items
	 */
	protected abstract void handleDialogSelection(Collection<T> selected);

	/**
	 * Constructs the actual component
	 */
	protected void initContent() {
		grid = new ModelBasedGrid<ID, T>(getDataProvider(), entityModel, getFieldFilters(), isGridEditEnabled(),
				GridEditMode.SIMULTANEOUS) {

			private static final long serialVersionUID = 6143503902550597524L;

			@Override
			protected <W, V> Converter<W, V> constructCustomConverter(AttributeModel am) {
				return BaseDetailsEditGrid.this.constructCustomConverter(am);
			}

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel am) {
				return BaseDetailsEditGrid.this.constructCustomField(entityModel, am, false);
			}

			@Override
			protected BindingBuilder<T, ?> doBind(T t, Component field, String attributeName) {
				if (!binders.containsKey(t)) {
					binders.put(t, new BeanValidationBinder<>(entityModel.getEntityClass()));
					binders.get(t).setBean(t);
				}
				Binder<T> binder = binders.get(t);
				return binder.forField((HasValue<?, ?>) field);
			}

			@Override
			protected void postProcessComponent(AttributeModel am, Component comp) {
				BaseDetailsEditGrid.this.postProcessComponent(am, comp);
			}
		};

		// allow editing by showing a pop-up dialog (only for service-based version)
		if (serviceBasedEditMode && !formOptions.isDetailsGridSearchMode() && !isViewMode()) {
			getGrid().addComponentColumn((ValueProvider<T, Component>) t -> {
				Button edit = new Button();
				edit.setIcon(VaadinIcon.PENCIL.create());
				edit.addClickListener(event -> doEdit(getService().fetchById(t.getId())));
				return edit;
			});
		}

		// add a remove button directly in the grid
		if (!isViewMode() && formOptions.isShowRemoveButton()) {
			getGrid().addComponentColumn((ValueProvider<T, Component>) t -> {
				Button remove = new Button();
				remove.setIcon(VaadinIcon.TRASH.create());
				remove.addClickListener(event -> {
					binders.remove(t);
					// callback method so the entity can be removed from its
					// parent
					if (removeEntityConsumer != null) {
						removeEntityConsumer.accept(t);
					}
					if (getDataProvider() instanceof ListDataProvider) {
						((ListDataProvider<T>) getDataProvider()).getItems().remove(t);
					}
					getDataProvider().refreshAll();
				});
				return remove;
			});
		}

		grid.setHeight(gridHeight);
		grid.setSelectionMode(SelectionMode.SINGLE);

		for (Column<?> c : grid.getColumns()) {
			c.setSortable(false);
		}

		VerticalLayout layout = new DefaultVerticalLayout(false, true);
		layout.setSizeFull();
		layout.add(grid);

		// add a change listener (to make sure the buttons are correctly
		// enabled/disabled)
		grid.addSelectionListener(event -> {
			if (grid.getSelectedItems().iterator().hasNext()) {
				selectedItem = grid.getSelectedItems().iterator().next();
				onSelect(selectedItem);
				checkComponentState(selectedItem);
			}
		});

		// apply filter
		applyFilter();

		// add the buttons
		constructButtonBar(layout);

		postConstruct();
		add(layout);

	}

	/**
	 * Indicates whether it is possible to add/modify items directly via the grid
	 *
	 * @return
	 */
	private boolean isGridEditEnabled() {
		return !viewMode && !formOptions.isDetailsGridSearchMode() && !formOptions.isReadOnly()
				&& !serviceBasedEditMode;
	}

	/**
	 * 
	 * @return whether the component is in view mode
	 */
	public boolean isViewMode() {
		return viewMode;
	}

	/**
	 * Method that is called in order to enable/disable a button after selecting an
	 * item in the grid
	 *
	 * @param button
	 * @return
	 */
	protected boolean mustEnableComponent(Component component, T selectedItem) {
		// overwrite in subclasses if needed
		return true;
	}

	/**
	 * Respond to a selection of an item in the grid
	 */
	protected void onSelect(Object selected) {
		// overwrite when needed
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
	 * @param buttonBar the button bar
	 */
	protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
		// overwrite in subclass if needed
	}

	/**
	 * Callback method that is used to post-process an input component
	 * 
	 * @param am   the attribute model for the component
	 * @param comp the component
	 */
	protected void postProcessComponent(AttributeModel am, Component comp) {
		// overwrite in subclass if needed
	}

	/**
	 * Registers a button that must be enabled/disabled after an item is selected.
	 * use the "mustEnableButton" callback method to impose additional constraints
	 * on when the button must be enabled
	 *
	 * @param button the button to register
	 */
	public void registerComponent(Component component) {
		if (component != null) {
			if (component instanceof HasEnabled) {
				((HasEnabled) component).setEnabled(false);
			}
			componentsToUpdate.add(component);
		}
	}

	public void setCreateEntitySupplier(Supplier<T> createEntitySupplier) {
		this.createEntitySupplier = createEntitySupplier;
	}

	public void setFormOptions(FormOptions formOptions) {
		this.formOptions = formOptions;
	}

	public void setGridHeight(String gridHeight) {
		this.gridHeight = gridHeight;
	}

	public void setLinkEntityConsumer(Consumer<T> linkEntityConsumer) {
		this.linkEntityConsumer = linkEntityConsumer;
	}

	public void setRemoveEntityConsumer(Consumer<T> removeEntityConsumer) {
		this.removeEntityConsumer = removeEntityConsumer;
	}

	public void setSearchDialogEntityModel(EntityModel<T> searchDialogEntityModel) {
		this.searchDialogEntityModel = searchDialogEntityModel;
	}

	public void setSearchDialogFilters(List<SerializablePredicate<T>> searchDialogFilters) {
		this.searchDialogFilters = searchDialogFilters;
		if (dialog != null) {
			dialog.setFilters(searchDialogFilters);
		}
	}

	public void setSearchDialogSortOrder(SortOrder<T> searchDialogSortOrder) {
		this.searchDialogSortOrder = searchDialogSortOrder;
	}

	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
		checkComponentState(selectedItem);
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	@Override
	public boolean validateAllFields() {
		boolean error = false;
		for (Entry<T, Binder<T>> entry : binders.entrySet()) {
			entry.getValue().setBean(entry.getKey());
			BinderValidationStatus<T> status = entry.getValue().validate();
			error |= !status.isOk();
		}
		return error;
	}

	@Override
	public void setValue(U value) {
		super.setValue(value);
		afterValueSet(value);
	}

	protected void afterValueSet(U value) {
		// overwrite in subclasses
	}
}
