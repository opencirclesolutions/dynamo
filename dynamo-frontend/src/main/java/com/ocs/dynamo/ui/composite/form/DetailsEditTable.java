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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.ModelBasedFieldFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.table.ModelBasedTable;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A complex table component for the in-place editing of a one-to-many relation.
 * It can also be used to manage a many-to-many relation but in this case the
 * "setDetailsTableSearchMode" on the FormOptions must be set to true. You can
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
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
@SuppressWarnings("serial")
public abstract class DetailsEditTable<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<Collection<T>> implements SignalsParent, UseInViewMode {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The button that can be used to add rows to the table
	 */
	private Button addButton;

	/**
	 * The comparator (will be used to sort the items)
	 */
	private Comparator<T> comparator;

	/**
	 * The container
	 */
	private BeanItemContainer<T> container;

	/**
	 * The entity model of the entity to display
	 */
	private final EntityModel<T> entityModel;

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
	 * The number of rows to display - this default to 3 but can be overwritten
	 */
	private int pageLength = 3;

	/**
	 * The parent form in which this component is embedded
	 */
	private ReceivesSignal receiver;

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
	private List<Filter> searchDialogFilters;

	/**
	 * Sort order to apply to the search dialog
	 */
	private SortOrder searchDialogSortOrder;

	/**
	 * the currently selected item in the table
	 */
	private T selectedItem;

	/**
	 * The service that is used to communicate with the database
	 */
	private BaseService<ID, T> service;

	/**
	 * The table for displaying the actual items
	 */
	private ModelBasedTable<ID, T> table;

	/**
	 * List of buttons to update after a detail is selected
	 */
	private List<Button> toUpdate = new ArrayList<>();

	/**
	 * The UI
	 */
	private UI ui = UI.getCurrent();

	private ModelBasedSearchDialog<ID, T> dialog;

	/**
	 * Whether the table is in view mode. If this is the case, editing is not
	 * allowed and no buttons will be displayed
	 */
	private boolean viewMode;

	/**
	 * Constructor
	 * 
	 * @param items
	 *            the entities to display
	 * @param entityModel
	 *            the entity model of the entities to display
	 * @param viewMode
	 *            the view mode
	 * @param formOptions
	 *            the form options that determine how the table
	 */
	public DetailsEditTable(Collection<T> items, EntityModel<T> entityModel, boolean viewMode,
			FormOptions formOptions) {
		this.entityModel = entityModel;
		this.messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();
		this.items = items;
		this.viewMode = viewMode;
		this.formOptions = formOptions;
	}

	public void addEntity(T t) {
		container.addBean(t);
		if (receiver != null) {
			receiver.signalDetailsComponentValid(DetailsEditTable.this,
					VaadinUtils.allFixedTableFieldsValid(table));
		}
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
	 * @param buttonBar
	 *            the button bar
	 */
	protected void constructAddButton(Layout buttonBar) {
		addButton = new Button(messageService.getMessage("ocs.add", VaadinUtils.getLocale()));
		addButton.setIcon(FontAwesome.PLUS);
		addButton.addClickListener(event -> {
			T t = createEntity();
			container.addBean(t);
			if (receiver != null) {
				receiver.signalDetailsComponentValid(DetailsEditTable.this,
						VaadinUtils.allFixedTableFieldsValid(table));
			}
		});
		addButton.setVisible(isTableEditEnabled() && !formOptions.isHideAddButton());
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
		constructSearchButton(buttonBar);

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
	 * Constructs a button that brings up a search dialog
	 * 
	 * @param buttonBar
	 */
	protected void constructSearchButton(Layout buttonBar) {

		searchDialogButton = new Button(messageService.getMessage("ocs.search", VaadinUtils.getLocale()));
		searchDialogButton.setIcon(FontAwesome.SEARCH);
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
				@Override
				protected boolean doClose() {

					// add the selected items to the table
					Collection<T> selected = getSelectedItems();
					if (selected != null) {
						afterItemsSelected(selected);
						for (T t : selected) {
							container.addBean(t);
						}
					}
					return true;
				}
			};
			dialog.build();
			ui.addWindow(dialog);
		});
		searchDialogButton.setVisible(!viewMode && formOptions.isDetailsTableSearchMode());
		buttonBar.addComponent(searchDialogButton);
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

	public ReceivesSignal getReceiver() {
		return receiver;
	}

	public Button getSearchDialogButton() {
		return searchDialogButton;
	}

	public EntityModel<T> getSearchDialogEntityModel() {
		return searchDialogEntityModel;
	}

	public List<Filter> getSearchDialogFilters() {
		return searchDialogFilters;
	}

	public SortOrder getSearchDialogSortOrder() {
		return searchDialogSortOrder;
	}

	public T getSelectedItem() {
		return selectedItem;
	}

	public BaseService<ID, T> getService() {
		return service;
	}

	public ModelBasedTable<ID, T> getTable() {
		return table;
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
	@SuppressWarnings("unchecked")
	@Override
	protected Component initContent() {
		container = new BeanItemContainer<>(entityModel.getEntityClass());
		container.addAll(items);

		table = new ModelBasedTable<>(container, entityModel, false);

		// add a remove button directly in the table
		if (!isViewMode() && formOptions.isShowRemoveButton()) {
			final String removeMsg = messageService.getMessage("ocs.detail.remove", VaadinUtils.getLocale());
			table.addGeneratedColumn(removeMsg, (ColumnGenerator) (source, itemId, columnId) -> {
				Button remove = new Button(removeMsg);
				remove.setIcon(FontAwesome.TRASH);
				remove.addClickListener(event -> {
					container.removeItem(itemId);
					items.remove(itemId);
					// callback method so the entity can be removed from its
					// parent
					removeEntity((T) itemId);
					if (receiver != null) {
						receiver.signalDetailsComponentValid(DetailsEditTable.this,
								VaadinUtils.allFixedTableFieldsValid(table));
					}

				});
				return remove;
			});
		}

		// overwrite the field factory to deal with validation
		table.setTableFieldFactory(new ModelBasedFieldFactory<T>(entityModel, messageService, true, false) {

			@Override
			public Field<?> createField(String propertyId, EntityModel<?> fieldEntityModel) {
				AttributeModel attributeModel = entityModel.getAttributeModel(propertyId);

				Field<?> field = constructCustomField(entityModel, attributeModel, isTableEditEnabled());
				if (field == null) {
					Filter filter = fieldFilters == null ? null : fieldFilters.get(attributeModel.getName());
					if (filter != null) {
						// create a filtered combo box
						field = constructComboBox(attributeModel.getNestedEntityModel(), attributeModel, filter, false);
					} else {
						// delegate to the field factory
						field = super.createField(propertyId, fieldEntityModel);
					}
				}

				if (field != null) {
					// add a bean validator
					field.setEnabled(isTableEditEnabled());
					field.setSizeFull();

					// adds a value change listener (for updating the save
					// button)
					if (!viewMode) {
						field.addValueChangeListener(event -> {
							if (receiver != null) {
								receiver.signalDetailsComponentValid(DetailsEditTable.this,
										VaadinUtils.allFixedTableFieldsValid(table));
							}
						});

						postProcessTableField(propertyId, field);
					}
				}
				return field;
			}
		});

		table.setEditable(isTableEditEnabled());
		table.setMultiSelect(false);
		table.setPageLength(pageLength);
		table.setColumnCollapsingAllowed(false);

		VerticalLayout layout = new DefaultVerticalLayout(false, true);
		layout.addComponent(table);

		// add a change listener (to make sure the buttons are correctly
		// enabled/disabled)
		table.addValueChangeListener(event -> {
			selectedItem = (T) table.getValue();
			onSelect(table.getValue());
			checkButtonState(selectedItem);
		});
		table.updateTableCaption();

		// add the buttons
		constructButtonBar(layout);

		// set the reference to the parent so the status of the save button can
		// be set correctly
		ReceivesSignal receiver = VaadinUtils.getParentOfClass(this, ReceivesSignal.class);
		setReceiver(receiver);

		postConstruct();

		return layout;
	}

	/**
	 * Indicates whether it is possible to add/modify items directly via the table
	 * 
	 * @return
	 */
	private boolean isTableEditEnabled() {
		return !viewMode && !formOptions.isDetailsTableSearchMode() && !formOptions.isReadOnly();
	}

	public boolean isViewMode() {
		return viewMode;
	}

	/**
	 * Method that is called in order to enable/disable a button after selecting an
	 * item in the table
	 * 
	 * @param button
	 * @return
	 */
	protected boolean mustEnableButton(Button button, T selectedItem) {
		// overwrite in subclasses if needed
		return true;
	}

	/**
	 * Respond to a selection of an item in the table
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

	public void postProcessTableField(String propertyId, Field<?> field) {

	}

	/**
	 * Registers a button that must be enabled/disabled after an item is selected.
	 * use the "mustEnableButton" callback method to impose additional constraints
	 * on when the button must be enabled
	 * 
	 * @param button
	 *            the button to register
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
		if (comparator != null) {
			list.sort(comparator);
		}

		this.items = list;
		if (container != null) {
			container.removeAllItems();
			container.addAll(this.items);
		}
		// clear the selection
		setSelectedItem(null);
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	/**
	 * This method is called to store a reference to the parent form
	 * 
	 * @param parentForm
	 */
	private void setReceiver(ReceivesSignal receiver) {
		this.receiver = receiver;
		if (receiver != null) {
			receiver.signalDetailsComponentValid(this, VaadinUtils.allFixedTableFieldsValid(table));
		}
	}

	public void setSearchDialogEntityModel(EntityModel<T> searchDialogEntityModel) {
		this.searchDialogEntityModel = searchDialogEntityModel;
	}

	public void setSearchDialogFilters(List<Filter> searchDialogFilters) {
		this.searchDialogFilters = searchDialogFilters;
		if (dialog != null) {
			dialog.setFilters(searchDialogFilters);
		}
	}

	public void setSearchDialogSortOrder(SortOrder searchDialogSortOrder) {
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
		setItems(newFieldValue);
		super.setValue(newFieldValue);
	}

	@Override
	public boolean validateAllFields() {
		boolean error = false;
		Iterator<Component> component = table.iterator();
		while (component.hasNext()) {
			Component next = component.next();
			try {
				if (next instanceof AbstractField) {
					((AbstractField<?>) next).validate();
					((AbstractField<?>) next).setComponentError(null);
				}
			} catch (InvalidValueException ex) {
				error = true;
				((AbstractField<?>) next).setComponentError(new UserError(ex.getLocalizedMessage()));
			}
		}
		return error;
	}

}
