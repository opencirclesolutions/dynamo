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
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.helger.commons.functional.ITriConsumer;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Buildable;
import com.ocs.dynamo.ui.NestedComponent;
import com.ocs.dynamo.ui.UseInViewMode;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.export.ExportDelegate;
import com.ocs.dynamo.ui.composite.grid.ModelBasedGrid;
import com.ocs.dynamo.ui.composite.grid.ModelBasedSelectionGrid;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SearchOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleEditLayout;
import com.ocs.dynamo.ui.composite.type.GridEditMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;

import lombok.Getter;
import lombok.Setter;

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
 * @param <U>  the type of the property (can be a collection or a simple
 *             property) in the entity that this component binds to
 * @param <ID> the type of the ID of the entities that are being displayed
 * @param <T>  the type of the entities that are being displayed
 */
public abstract class BaseDetailsEditGrid<U, ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<U> implements NestedComponent, UseInViewMode, Buildable {

	private static final long serialVersionUID = 997617632007985450L;

	@Getter
	private Button addButton;

	/**
	 * Consumer that is called after a value has been set
	 */
	@Getter
	@Setter
	private Consumer<U> afterValueSet;

	@Getter
	private AttributeModel attributeModel;

	@Getter
	private Map<T, Binder<T>> binders = new HashMap<>();

	private HorizontalLayout buttonBar;

	@Getter
	private ComponentContext<ID, T> componentContext = ComponentContext.<ID, T>builder().build();

	/**
	 * List of components to update after a row is selected in the grid
	 */
	private List<Component> componentsToUpdate = new ArrayList<>();

	/**
	 * The supplier that is used for creating a new entity in response to a click on
	 * the Add button
	 */
	@Getter
	@Setter
	private Supplier<T> createEntitySupplier;

	/**
	 * Custom button mapping
	 */
	private Map<String, List<Component>> customButtonMap = new HashMap<>();

	/**
	 * Joins to apply when fetching a single entity for display in a pop-up
	 */
	@Getter
	private FetchJoinInformation[] detailJoins;

	/**
	 * The entity model to use when creating the details panel
	 */
	@Getter
	@Setter
	private EntityModel<T> detailsPanelEntityModel;

	/**
	 * The entity model of the entity to display
	 */
	@Getter
	private final EntityModel<T> entityModel;

	@Getter
	private ExportDelegate exportDelegate = ServiceLocatorFactory.getServiceLocator().getService(ExportDelegate.class);

	/**
	 * The entity model to use when exporting
	 */
	@Getter
	@Setter
	private EntityModel<T> exportEntityModel;

	/**
	 * Optional field filters for restricting the contents of combo boxes
	 */
	@Getter
	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	/**
	 * Form options that determine which buttons and functionalities are available
	 */
	@Getter
	private FormOptions formOptions;

	private Grid<T> grid;

	@Getter
	@Setter
	private String gridHeight = SystemPropertyUtils.getDefaultEditGridHeight();

	private VerticalLayout layout;

	/**
	 * Code to execute after selecting one or more items in the pop-up (link the
	 * selected item to the parent)
	 */
	@Getter
	@Setter
	private Consumer<T> linkEntityConsumer;

	/**
	 * The message service
	 */
	@Getter
	private final MessageService messageService;

	@Getter
	@Setter
	private BiPredicate<Component, T> mustEnableComponent;

	@Getter
	@Setter
	private BiConsumer<HorizontalLayout, Boolean> postProcessButtonBar;

	private boolean postProcessed;

	/**
	 * Runnable that is executed after the layout has been constructed
	 */
	@Getter
	@Setter
	private Runnable afterLayoutBuilt;

	/**
	 * Consumer that is used to remove an entity
	 */
	@Getter
	@Setter
	private Consumer<T> removeEntityConsumer;

	/**
	 * Search dialog
	 */
	@Getter
	private ModelBasedSearchDialog<ID, T> searchDialog;

	/**
	 * Button used to open the search dialog
	 */
	@Getter
	private Button searchDialogButton;

	/**
	 * Overridden entity model for the search dialog
	 */
	@Getter
	@Setter
	private EntityModel<T> searchDialogEntityModel;

	/**
	 * Filters to apply to the search dialog
	 */
	@Getter
	private List<SerializablePredicate<T>> searchDialogFilters;

	/**
	 * Sort order to apply to the search dialog
	 */
	@Getter
	@Setter
	private SortOrder<T> searchDialogSortOrder;

	private SimpleEditLayout<ID, T> selectedDetailsLayout;

	/**
	 * A panel for displaying the item that was selected in the grid
	 */
	private VerticalLayout selectedDetailsPanel;

	/**
	 * The currently selected item
	 */
	@Getter
	private T selectedItem;

	/**
	 * The service that is used to communicate with the database
	 */
	@Getter
	private BaseService<ID, T> service;

	/**
	 * Indicates whether the component is used in service-based mode, in this case
	 * the values in the grid cannot be edited directly
	 */
	private boolean serviceBasedEditMode;

	/**
	 * Whether the component is in view mode. If this is the case, editing is not
	 * allowed and no buttons will be displayed
	 */
	@Getter
	private boolean viewMode;

	@Getter
	@Setter
	private ITriConsumer<ID, AttributeModel, Component> postProcessComponent;

	@Getter
	@Setter
	private Runnable onAdd = () -> {
	};

	@Getter
	@Setter
	private Consumer<T> onEdit = t -> {
	};

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param attributeModel
	 * @param viewMode
	 * @param serviceBasedEditMode
	 * @param formOptions
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

	protected abstract void addDownloadMenu();

	/**
	 * Adds an edit button to the grid when in not in edit or search mode
	 */
	private void addEditButtonToGrid() {
		if (serviceBasedEditMode && !formOptions.isDetailsGridSearchMode() && !isViewMode()
				&& formOptions.isShowEditButton()) {
			getGrid().addComponentColumn((ValueProvider<T, Component>) t -> {
				Button edit = new Button();
				edit.setIcon(VaadinIcon.PENCIL.create());
				edit.addClickListener(event -> onEdit.accept(getService().fetchById(t.getId(), getDetailJoins())));
				return edit;
			});
		}
	}

	/**
	 * Adds a field filter
	 * 
	 * @param property the property for which to add a field filter
	 * @param filter   the field filter to add
	 */
	public void addFieldFilter(String property, SerializablePredicate<?> filter) {
		this.fieldFilters.put(property, filter);
	}

	private void addRemoveButtonToGrid() {
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
	}

	/**
	 * Applies a filter to restrict the values to be displayed
	 */
	protected abstract void applyFilter();

	/**
	 * Constructs the actual component
	 */
	public void build() {
		if (layout == null) {

			boolean checkBoxesForMultiSelect = SystemPropertyUtils.useGridSelectionCheckBoxes();
			if (checkBoxesForMultiSelect) {
				createCheckboxSelectGrid();
			} else {
				createMultiSelectGrid();
			}

			addEditButtonToGrid();
			addRemoveButtonToGrid();

			grid.setHeight(gridHeight);
			grid.setSelectionMode(getFormOptions().getDetailsGridSelectionMode());

			layout = new DefaultVerticalLayout(false, true);
			layout.setSizeFull();
			layout.add(grid);

			// add a change listener (to make sure the buttons are correctly
			// enabled/disabled)
			grid.addSelectionListener(event -> {
				if (grid.getSelectedItems().iterator().hasNext()) {
					selectedItem = grid.getSelectedItems().iterator().next();
					onSelect(selectedItem);
					checkComponentState(selectedItem);

					if (getFormOptions().isShowDetailsGridDetailsPanel()) {
						showInDetailsPanel(selectedItem);
					}
				}
			});

			disableSorting();
			applyFilter();
			constructButtonBar(layout);

			add(layout);

			addDownloadMenu();
		}
	}

	/**
	 * Checks which buttons in the button bar must be enabled after an item has been
	 * selected
	 *
	 * @param selectedItem the selected item
	 */
	protected void checkComponentState(T selectedItem) {
		for (Component comp : componentsToUpdate) {
			boolean enabled = selectedItem != null
					&& (mustEnableComponent == null || mustEnableComponent.test(comp, selectedItem));
			if (comp instanceof HasEnabled) {
				((HasEnabled) comp).setEnabled(enabled);
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
		addButton.addClickListener(event -> {
			hideSelectedDetailsPanel();
			if (onAdd != null) {
				onAdd.run();
			}
		});
		addButton.setVisible((isGridEditEnabled()
				|| (!isViewMode() && serviceBasedEditMode && !formOptions.isDetailsGridSearchMode()))
				&& formOptions.isShowAddButton());
		buttonBar.add(addButton);
	}

	/**
	 * Constructs the button bar
	 *
	 * @param parent the layout to which to add the button bar
	 */
	protected void constructButtonBar(VerticalLayout parent) {
		buttonBar = new DefaultHorizontalLayout();
		parent.add(buttonBar);
		constructAddButton(buttonBar);
		constructSearchButton(buttonBar);
	}

	/**
	 * Constructs the panel used to display the entity that is selected in the grid
	 */
	private void constructDetailsPanel() {
		if (selectedDetailsPanel == null) {
			selectedDetailsPanel = new DefaultVerticalLayout(true, false);
			selectedDetailsPanel.addClassName(DynamoConstants.CSS_GRID_DETAILS_PANEL);
			selectedDetailsPanel.setVisible(false);
			FormOptions cloned = formOptions.createCopy().setReadOnly(true).setShowEditFormCaption(true);

			EntityModel<T> em = detailsPanelEntityModel != null ? detailsPanelEntityModel : entityModel;

			selectedDetailsLayout = new SimpleEditLayout<>(
					getCreateEntitySupplier() == null ? null : getCreateEntitySupplier().get(), service, em, cloned,
					getDetailJoins());
			selectedDetailsPanel.add(selectedDetailsLayout);

			layout.add(selectedDetailsPanel);
		}
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

			SearchOptions options = SearchOptions.builder().advancedSearchMode(false).multiSelect(true)
					.searchImmediately(true).build();

			searchDialog = new ModelBasedSearchDialog<ID, T>(service,
					searchDialogEntityModel != null ? searchDialogEntityModel : entityModel, searchDialogFilters,
					searchDialogSortOrder == null ? null : List.of(searchDialogSortOrder), options);

			searchDialog.setOnClose(() -> {
				Collection<T> selected = searchDialog.getSelectedItems();
				if (selected != null) {
					handleDialogSelection(selected);
					getDataProvider().refreshAll();
				}
				return true;
			});
			searchDialog.buildAndOpen();
		});
		searchDialogButton.setVisible(!viewMode && formOptions.isDetailsGridSearchMode());
		buttonBar.add(searchDialogButton);
	}

	@SuppressWarnings("unchecked")
	private void createCheckboxSelectGrid() {

		grid = new ModelBasedGrid<ID, T>(getDataProvider(), entityModel, getFieldFilters(),
				formOptions.createCopy().setGridEditMode(GridEditMode.SIMULTANEOUS),
				componentContext.toBuilder().editable(isGridEditEnabled()).build()) {

			private static final long serialVersionUID = 6143503902550597524L;

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel am) {
				return BaseDetailsEditGrid.this.findCustomComponent(entityModel, am, false);
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
			protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
				if (postProcessComponent != null) {
					postProcessComponent.accept(id, am, comp);
				}
			}
		};
		((ModelBasedGrid<ID, T>) grid).build();
	}

	@SuppressWarnings("unchecked")
	private void createMultiSelectGrid() {
		grid = new ModelBasedSelectionGrid<ID, T>(getDataProvider(), entityModel, getFieldFilters(),
				formOptions.createCopy().setGridEditMode(GridEditMode.SIMULTANEOUS),
				componentContext.toBuilder().editable(isGridEditEnabled()).build()) {

			private static final long serialVersionUID = 6143503902550597524L;

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel am) {
				return findCustomComponent(entityModel, am, false);
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
			protected void postProcessComponent(ID id, AttributeModel am, Component comp) {
				if (postProcessComponent != null) {
					postProcessComponent.accept(id, am, comp);
				}
			}
		};

		((ModelBasedSelectionGrid<ID, T>) grid).build();
	}

	private void disableSorting() {
		if (!getFormOptions().isDetailsGridSortable()) {
			for (Column<?> col : grid.getColumns()) {
				col.setSortable(false);
			}
		}
	}

	/**
	 * Constructs a custom field for a specified entity model and attribute model
	 * 
	 * @param entityModel    the entity model
	 * @param attributeModel the attribute model
	 * @param viewMode       whether the component is in view mode
	 * @return
	 */
	private Component findCustomComponent(EntityModel<?> entityModel, AttributeModel attributeModel, boolean viewMode) {
		Function<CustomFieldContext, Component> customFieldCreator = getComponentContext()
				.getCustomFieldCreator(attributeModel.getPath());
		if (customFieldCreator != null) {
			return customFieldCreator.apply(CustomFieldContext.builder().entityModel(entityModel)
					.attributeModel(attributeModel).viewMode(viewMode).build());
		}
		return null;
	}

	protected abstract DataProvider<T, SerializablePredicate<T>> getDataProvider();

	public Grid<T> getGrid() {
		if (grid == null) {
			build();
		}
		return grid;
	}

	/**
	 * Callback method that is executed after closing the pop-up dialog - handles
	 * the selected items in the dialog
	 * 
	 * @param selected the entities that were selected in the dialog
	 */
	protected abstract void handleDialogSelection(Collection<T> selected);

	/**
	 * Hides the panel that is used to show the details of the entity that is
	 * selected in the grid
	 */
	private void hideSelectedDetailsPanel() {
		if (selectedDetailsPanel != null) {
			selectedDetailsPanel.setVisible(false);
		}
	}

	/**
	 * Check whether the specified component is a custom component stored under the
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
	 * Indicates whether it is possible to add/modify items directly via the grid
	 *
	 * @return
	 */
	private boolean isGridEditEnabled() {
		return !viewMode && !formOptions.isDetailsGridSearchMode() && !formOptions.isReadOnly()
				&& !serviceBasedEditMode;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		build();
		postProcess();
	}

	/**
	 * Callback method that is executed after an entity in the grid is selected
	 * 
	 * @param selected the selected entity
	 */
	protected void onSelect(T selected) {
		// overwrite when needed
	}

	protected void postProcess() {
		if (!postProcessed) {
			if (afterLayoutBuilt != null) {
				afterLayoutBuilt.run();
			}

			if (postProcessButtonBar != null) {
				postProcessButtonBar.accept(buttonBar, viewMode);
			}

			postProcessed = true;
		}
	}

	/**
	 * Registers a component that must be enabled/disabled after an item is
	 * selected. use the "mustEnableComponent" callback method to impose additional
	 * constraints on when the button must be enabled
	 *
	 * @param component the component to register
	 */
	public void registerComponent(Component component) {
		if (component != null) {
			if (component instanceof HasEnabled) {
				((HasEnabled) component).setEnabled(false);
			}
			componentsToUpdate.add(component);
		}
	}

	public void setEditColumnThresholds(List<String> editColumnThresholds) {
		componentContext.setEditColumnThresholds(editColumnThresholds);
	}

	public void setSearchDialogFilters(List<SerializablePredicate<T>> searchDialogFilters) {
		this.searchDialogFilters = searchDialogFilters;
		if (searchDialog != null) {
			searchDialog.setFilters(searchDialogFilters);
		}
	}

	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
		checkComponentState(selectedItem);
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void setValue(U value) {
		super.setValue(value);
		if (value instanceof Collection) {
			updateCaption(((Collection) value).size());
		}

		hideSelectedDetailsPanel();

		if (afterValueSet != null) {
			afterValueSet.accept(value);
		}
	}

	/**
	 * 
	 * @return whether to display the details panel when the component is in edit
	 *         mode
	 */
	protected abstract boolean showDetailsPanelInEditMode();

	/**
	 * Displays the item that is selected in the grid in a panel directly below the
	 * grid
	 * 
	 * @param selectedItem
	 */
	private void showInDetailsPanel(T selectedItem) {
		constructDetailsPanel();

		boolean show = isViewMode() || showDetailsPanelInEditMode();

		if (selectedItem != null && selectedItem.getId() != null && show) {
			selectedDetailsPanel.setVisible(true);
			selectedDetailsLayout.setEntity(selectedItem);
		} else {
			selectedDetailsPanel.setVisible(false);
		}
	}

	/**
	 * Registers a component and stores it under the provided key
	 * 
	 * @param key       the key
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
		customButtonMap.putIfAbsent(key, new ArrayList<>());
		customButtonMap.get(key).add(component);
	}

	/**
	 * Updates the caption
	 * 
	 * @param size the number of items currently being shown in the grid
	 */
	protected void updateCaption(int size) {
		this.setLabel(attributeModel.getDisplayName(VaadinUtils.getLocale()) + " "
				+ messageService.getMessage("ocs.showing.results", VaadinUtils.getLocale(), size));
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

}
