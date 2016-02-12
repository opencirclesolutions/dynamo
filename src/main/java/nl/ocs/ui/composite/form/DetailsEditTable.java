package nl.ocs.ui.composite.form;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.AttributeModel;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.EntityModelFactory;
import nl.ocs.domain.model.impl.ModelBasedFieldFactory;
import nl.ocs.service.BaseService;
import nl.ocs.service.MessageService;
import nl.ocs.ui.ServiceLocator;
import nl.ocs.ui.component.DefaultHorizontalLayout;
import nl.ocs.ui.component.DefaultVerticalLayout;
import nl.ocs.ui.composite.dialog.ModelBasedSearchDialog;
import nl.ocs.ui.composite.table.ModelBasedTable;
import nl.ocs.ui.utils.VaadinUtils;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * A complex table component for the in-place editing of a one-to-many relation.
 * It can also be used to manage a many-to-many relation but in this case the
 * "tableReadOnly" must be set to true. You can then use the setSearchXXX
 * methods to configure the behaviour of the search dialog that can be used to
 * modify the values
 * 
 * If you need a component like this, you should override the
 * constructCustomField method and use it to construct a subclass of this
 * component
 * 
 * Note that a separate instance of this component is generated for both the
 * view mode and the edit mode of the form it appears in, so this component does
 * not contain logic for switching between the modes
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
@SuppressWarnings("serial")
public abstract class DetailsEditTable<ID extends Serializable, T extends AbstractEntity<ID>>
		extends CustomField<Collection<T>> implements SignalsParent {

	private static final long serialVersionUID = -1203245694503350276L;

	/**
	 * The container
	 */
	private BeanItemContainer<T> container;

	/**
	 * The entity model of the entity to display
	 */
	private final EntityModel<T> entityModel;

	/**
	 * The entity model factory
	 */
	private final EntityModelFactory entityModelFactory;

	/**
	 * Optional field filters for restricting the contents of combo boxes
	 */
	private Map<String, Filter> fieldFilters = new HashMap<>();

	/**
	 * Form options that determine which buttons and functionalities are
	 * available
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
	private ModelBasedEditForm<?, ?> parentForm;

	/**
	 * Button used to remove items from the table
	 */
	private Button removeButton;

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
	 * Indicates whether the table is always read only (regardless of the actual
	 * form mode)
	 */
	private boolean tableReadOnly;

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
		this.entityModelFactory = ServiceLocator.getEntityModelFactory();
		this.messageService = ServiceLocator.getMessageService();
		this.items = items;
		this.viewMode = viewMode;
		this.formOptions = formOptions;
	}

	/**
	 * Callback method that is called after selecting one or more items using
	 * the search dialog
	 * 
	 * @param selectedItems
	 */
	public void afterItemsSelected(Collection<T> selectedItems) {
		// override in subclasses
	}

	/**
	 * Constructs the button that is used for adding new items
	 * 
	 * @param buttonBar
	 */
	protected void constructAddButton(Layout buttonBar) {
		Button addButton = new Button(messageService.getMessage("ocs.add"));
		addButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				T t = createEntity();
				container.addBean(t);
				parentForm.signalDetailsTableValid(DetailsEditTable.this,
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
		constructRemoveButton(buttonBar);

		postProcessButtonBar(buttonBar);
	}

	/**
	 * Method that is called to create a custom field. Override in subclasses if
	 * needed
	 * 
	 * @param entityModel
	 *            the entity model of the entity that is displayed in the table
	 * @param attributeModel
	 *            the attribute model of the attribute for which we are
	 *            constructing a field
	 * @param viewMode
	 *            whether the form is in view mode
	 * @return
	 */
	protected Field<?> constructCustomField(EntityModel<T> entityModel,
			AttributeModel attributeModel, boolean viewMode) {
		return null;
	}

	/**
	 * Constructs the remove button
	 * 
	 * @param buttonBar
	 */
	protected void constructRemoveButton(Layout buttonBar) {
		removeButton = new Button(messageService.getMessage("ocs.remove"));
		removeButton.setEnabled(false);
		removeButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				container.removeItem(getSelectedItem());
				items.remove(getSelectedItem());
				// callback method so the entity can be removed from its
				// parent
				removeEntity(getSelectedItem());
				parentForm.signalDetailsTableValid(DetailsEditTable.this,
						VaadinUtils.allFixedTableFieldsValid(table));
				setSelectedItem(null);
				onSelect(null);
			}

		});
		removeButton.setDescription(messageService.getMessage("ocs.select.row.to.delete"));
		removeButton.setVisible(!viewMode && formOptions.isShowRemoveButton());
		buttonBar.addComponent(removeButton);
	}

	/**
	 * Constructs a button that brings up a search dialog
	 * 
	 * @param buttonBar
	 */
	protected void constructSearchButton(Layout buttonBar) {
		searchDialogButton = new Button(messageService.getMessage("ocs.search"));
		searchDialogButton.setDescription(messageService.getMessage("ocs.search.description"));
		searchDialogButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				ModelBasedSearchDialog<ID, T> dialog = new ModelBasedSearchDialog<ID, T>(service,
						searchDialogEntityModel != null ? searchDialogEntityModel : entityModel,
						searchDialogFilters, searchDialogSortOrder, true) {
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
				UI.getCurrent().addWindow(dialog);
			}
		});
		searchDialogButton.setVisible(!viewMode && formOptions.isShowSearchDialogButton());
		buttonBar.addComponent(searchDialogButton);
	}

	/**
	 * Creates a new entity - override in subclass
	 * 
	 * @return
	 */
	protected abstract T createEntity();

	/**
	 * Method that is called after an entity is selected in the form of which
	 * this table is placed - should be overridden in child classes and used to
	 * make sure that the table contains the correct values
	 */
	public void fillDetails() {
		// override in child tables
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
	@Override
	protected Component initContent() {
		container = new BeanItemContainer<T>(entityModel.getEntityClass());
		container.addAll(items);

		table = new ModelBasedTable<ID, T>(container, entityModel, entityModelFactory,
				messageService);

		// overwrite the field factory to deal with validation
		table.setTableFieldFactory(new ModelBasedFieldFactory<T>(entityModel, messageService, true,
				false) {

			@Override
			public Field<?> createField(String propertyId) {
				AttributeModel attributeModel = entityModel.getAttributeModel(propertyId);

				Field<?> field = constructCustomField(entityModel, attributeModel,
						isTableEditEnabled());
				if (field == null) {

					Filter filter = fieldFilters == null ? null : fieldFilters.get(attributeModel
							.getName());
					if (filter != null) {
						// create a filtered combo box
						field = (Field<?>) constructComboBox(attributeModel.getNestedEntityModel(),
								attributeModel, filter);
					} else {
						// delegate to the field factory
						field = super.createField(propertyId);
					}
				}

				if (field != null) {
					// add a bean validator
					field.setEnabled(isTableEditEnabled());
					field.setSizeFull();

					// adds a value change listener (for updating the save
					// button)
					if (!viewMode) {
						field.addValueChangeListener(new Property.ValueChangeListener() {

							@Override
							public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
								if (parentForm != null) {
									parentForm.signalDetailsTableValid(DetailsEditTable.this,
											VaadinUtils.allFixedTableFieldsValid(table));
								}
							}

						});
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
		table.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			@SuppressWarnings("unchecked")
			public void valueChange(Property.ValueChangeEvent event) {
				selectedItem = (T) table.getValue();
				onSelect(table.getValue());
			}
		});
		table.updateTableCaption();

		// add the buttons
		constructButtonBar(layout);

		// set the reference to the parent so the status of the save button can
		// be set correctly
		ModelBasedEditForm<?, ?> parent = VaadinUtils.getParentOfClass(this,
				ModelBasedEditForm.class);
		setParentForm(parent);

		return layout;
	}

	/**
	 * Indicates whether it is possible to add/modify items directly via the
	 * table
	 * 
	 * @return
	 */
	private boolean isTableEditEnabled() {
		return !viewMode && !tableReadOnly;
	}

	public boolean isTableReadOnly() {
		return tableReadOnly;
	}

	public boolean isViewMode() {
		return viewMode;
	}

	/**
	 * Respond to a selection of an item in the table
	 */
	protected void onSelect(Object selected) {
		if (removeButton != null) {
			removeButton.setEnabled(getSelectedItem() != null);
		}
	}

	/**
	 * Callback method that is used to modify the button bar. Override in
	 * subclasses if needed
	 * 
	 * @param buttonBar
	 */
	protected void postProcessButtonBar(Layout buttonBar) {
		// overwrite in subclass if needed
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
		this.items = items;
		if (container != null) {
			container.removeAllItems();
			container.addAll(items);
			table.refreshRowCache();
		}
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	/**
	 * This method is called to store a reference to the parent form
	 * 
	 * @param parentForm
	 */
	private void setParentForm(ModelBasedEditForm<?, ?> parentForm) {
		this.parentForm = parentForm;
		parentForm.signalDetailsTableValid(this, VaadinUtils.allFixedTableFieldsValid(table));
	}

	public void setSearchDialogEntityModel(EntityModel<T> searchDialogEntityModel) {
		this.searchDialogEntityModel = searchDialogEntityModel;
	}

	public void setSearchDialogFilters(List<Filter> searchDialogFilters) {
		this.searchDialogFilters = searchDialogFilters;
	}

	public void setSearchDialogSortOrder(SortOrder searchDialogSortOrder) {
		this.searchDialogSortOrder = searchDialogSortOrder;
	}

	public void setSelectedItem(T selectedItem) {
		this.selectedItem = selectedItem;
	}

	public void setService(BaseService<ID, T> service) {
		this.service = service;
	}

	public void setTableReadOnly(boolean tableReadOnly) {
		this.tableReadOnly = tableReadOnly;
	}

}
