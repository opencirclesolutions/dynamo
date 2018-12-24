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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ocs.dynamo.ui.utils.ConvertUtils;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.data.BeanValidationBinder;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.Converter;
import com.vaadin.data.HasValue;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A complex grid component for the in-place editing of a one-to-many relation.
 * It can also be used to manage a many-to-many relation but in this case the
 * "setDetailsGridSearchMode" on the FormOptions must be set to true. You can
 * then use the setSearchXXX methods to configure the behaviour of the search
 * dialog that can be used to modify the values If you need a component like
 * this, you should override the constructCustomField method and use it to
 * construct a subclass of this component
 * 
 * Note that a separate instance of this component is generated for both the
 * view mode and the edit mode of the form it appears in, so this component does
 * not contain logic for switching between the modes
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public abstract class DetailsEditGrid<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<Collection<T>> implements NestedComponent, UseInViewMode {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The button that can be used to add rows to the grid
	 */
	private Button addButton;

	/**
	 * The comparator (will be used to sort the items)
	 */
	private Comparator<T> comparator;

	/**
	 * The data provider
	 */
	private ListDataProvider<T> provider;

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
	 * The message service
	 */
	private final MessageService messageService;

	/**
	 * The number of rows to display - this default to 3 but can be overwritten
	 */
	private int pageLength = SystemPropertyUtils.getDefaultListSelectRows();

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
	 * the currently selected item
	 */
	private T selectedItem;

	/**
	 * The service that is used to communicate with the database
	 */
	private BaseService<ID, T> service;

	/**
	 * The grid for displaying the actual items
	 */
	private ModelBasedGrid<ID, T> grid;

	/**
	 * List of buttons to update after a detail is selected
	 */
	private List<Button> toUpdate = new ArrayList<>();

	/**
	 * The UI
	 */
	private UI ui = UI.getCurrent();

	/**
	 * Search dialog
	 */
	private ModelBasedSearchDialog<ID, T> dialog;

	/**
	 * Whether the component is in view mode. If this is the case, editing is not
	 * allowed and no buttons will be displayed
	 */
	private boolean viewMode;

	/**
	 * Map with a binder for every row
	 */
	private Map<T, Binder<T>> binders = new HashMap<>();

	/**
	 * The attribute model
	 */
	private AttributeModel attributeModel;

	/**
	 * Constructor
	 *
	 * @param entityModel    the entity model of the entities to display
	 * @param attributeModel the attribute model of the attribute to display
	 * @param viewMode       the view mode
	 * @param formOptions    the form options that determine how the grid behaves
	 */
	public DetailsEditGrid(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode,
			FormOptions formOptions) {
		this.attributeModel = attributeModel;
		this.provider = new ListDataProvider<>(new ArrayList<>());
		this.entityModel = entityModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.viewMode = viewMode;
		this.formOptions = formOptions;
	}

	/**
	 * Callback method that is called after selecting one or more items using the
	 * search dialog
	 *
	 * @param selectedItems
	 */
	public void afterItemsSelected(Collection<T> selectedItems) {
		// override in subclasses
	}

	/**
	 * Checks which buttons in the button bar must be enabled
	 *
	 * @param selectedItem
	 */
	protected void checkButtonState(T selectedItem) {
		for (Button b : toUpdate) {
			b.setEnabled(selectedItem != null && mustEnableButton(b, selectedItem));
		}
	}

	/**
	 * Constructs the button that is used for adding new items
	 *
	 * @param buttonBar the button bar
	 */
	protected void constructAddButton(Layout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(VaadinIcons.PLUS);
		addButton.addClickListener(event -> {
			T t = createEntity();
			provider.getItems().add(t);
			provider.refreshAll();

		});
		addButton.setVisible(isGridEditEnabled() && !formOptions.isHideAddButton());
		buttonBar.addComponent(addButton);
	}

	/**
	 * Constructs the button bar
	 *
	 * @param parent the layout to which to add the button bar
	 */
	protected void constructButtonBar(Layout parent) {
		Layout buttonBar = new DefaultHorizontalLayout();
		parent.addComponent(buttonBar);

		constructAddButton(buttonBar);
		constructSearchButton(buttonBar);
		postProcessButtonBar(buttonBar);
	}

	/**
	 * Callback method for inserting custom converter
	 * 
	 * @param am
	 * @return
	 */
	protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
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
	protected AbstractComponent constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
			boolean viewMode) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Constructs a button that brings up a search dialog
	 *
	 * @param buttonBar
	 */
	protected void constructSearchButton(Layout buttonBar) {

		searchDialogButton = new Button(messageService.getMessage("ocs.search", VaadinUtils.getLocale()));
		searchDialogButton.setIcon(VaadinIcons.SEARCH);
		searchDialogButton.setDescription(messageService.getMessage("ocs.search.description", VaadinUtils.getLocale()));
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
						afterItemsSelected(selected);
						for (T t : selected) {
							provider.getItems().add(t);
						}
						provider.refreshAll();
					}
					return true;
				}
			};
			dialog.build();
			ui.addWindow(dialog);
		});
		searchDialogButton.setVisible(!viewMode && formOptions.isDetailsGridSearchMode());
		buttonBar.addComponent(searchDialogButton);
	}

	/**
	 * Creates a new entity and wire it to the parent entity - override in subclass
	 *
	 * @return
	 */
	protected abstract T createEntity();

	@Override
	protected void doSetValue(Collection<T> value) {
		List<T> list = new ArrayList<>();
		list.addAll(value);
		if (comparator != null) {
			list.sort(comparator);
		}

		binders.clear();
		if (provider != null) {
			provider.getItems().clear();
			provider.getItems().addAll(list);
			provider.refreshAll();
		}

		// clear the selection
		setSelectedItem(null);
	}

	public Button getAddButton() {
		return addButton;
	}

	public Comparator<T> getComparator() {
		return comparator;
	}

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

	public int getItemCount() {
		return provider.getItems().size();
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

	@Override
	public Collection<T> getValue() {
		return provider == null ? new ArrayList<>()
				: ConvertUtils.convertCollection(provider.getItems(), attributeModel);
	}

	/**
	 * Constructs the actual component
	 */
	@Override
	protected Component initContent() {
		grid = new ModelBasedGrid<ID, T>(provider, entityModel, isGridEditEnabled(), true) {

			private static final long serialVersionUID = 6143503902550597524L;

			@Override
			protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
				return DetailsEditGrid.this.constructCustomConverter(am);
			}

			@Override
			protected AbstractComponent constructCustomField(EntityModel<T> entityModel,
					AttributeModel attributeModel) {
				return DetailsEditGrid.this.constructCustomField(entityModel, attributeModel, false);
			}

			@Override
			protected BindingBuilder<T, ?> doBind(T t, AbstractComponent field) {
				if (!binders.containsKey(t)) {
					binders.put(t, new BeanValidationBinder<>(entityModel.getEntityClass()));
					binders.get(t).setBean(t);
				}
				Binder<T> binder = binders.get(t);
				return binder.forField((HasValue<?>) field);
			}

			@Override
			protected void postProcessComponent(AttributeModel am, AbstractComponent comp) {
				DetailsEditGrid.this.postProcessComponent(am, comp);
			}
		};

		// add a remove button directly in the grid
		if (!isViewMode() && formOptions.isShowRemoveButton()) {
			final String removeMsg = messageService.getMessage("ocs.detail.remove", VaadinUtils.getLocale());
			getGrid().addComponentColumn((ValueProvider<T, Component>) t -> {
				Button remove = new Button(removeMsg);
				remove.setIcon(VaadinIcons.TRASH);
				remove.addClickListener(event -> {
					provider.getItems().remove(t);
					binders.remove(t);
					provider.refreshAll();
					// callback method so the entity can be removed from its
					// parent
					removeEntity((T) t);
				});
				return remove;
			});
		}

		grid.setHeightByRows(pageLength);
		grid.setSelectionMode(SelectionMode.SINGLE);

		VerticalLayout layout = new DefaultVerticalLayout(false, true);
		layout.addComponent(grid);

		// add a change listener (to make sure the buttons are correctly
		// enabled/disabled)
		grid.addSelectionListener(event -> {
			selectedItem = (T) grid.getSelectedItems().iterator().next();
			onSelect(selectedItem);
			checkButtonState(selectedItem);
		});
		grid.getDataProvider().addDataProviderListener(event -> grid.updateCaption());
		grid.updateCaption();

		// add the buttons
		constructButtonBar(layout);

		postConstruct();
		return layout;
	}

	/**
	 * Indicates whether it is possible to add/modify items directly via the grid
	 *
	 * @return
	 */
	private boolean isGridEditEnabled() {
		return !viewMode && !formOptions.isDetailsGridSearchMode() && !formOptions.isReadOnly();
	}

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
	protected boolean mustEnableButton(Button button, T selectedItem) {
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
	 * @param buttonBar
	 */
	protected void postProcessButtonBar(Layout buttonBar) {
		// overwrite in subclass if needed
	}

	protected void postProcessComponent(AttributeModel am, AbstractComponent comp) {
		// override in subclass
	}

	/**
	 * Registers a button that must be enabled/disabled after an item is selected.
	 * use the "mustEnableButton" callback method to impose additional constraints
	 * on when the button must be enabled
	 *
	 * @param button the button to register
	 */
	public void registerButton(Button button) {
		if (button != null) {
			button.setEnabled(false);
			toUpdate.add(button);
		}
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

	public void setFieldFilters(Map<String, SerializablePredicate<?>> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setFormOptions(FormOptions formOptions) {
		this.formOptions = formOptions;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
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
		checkButtonState(selectedItem);
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	@Override
	public void setValue(Collection<T> newFieldValue) {
		super.setValue(newFieldValue);
	}

	@Override
	public boolean validateAllFields() {
		boolean error = false;
		for (Binder<T> binder : binders.values()) {
			BinderValidationStatus<T> status = binder.validate();
			error |= !status.isOk();
		}
		return error;
	}

}
