package nl.ocs.ui.composite.layout;

import java.io.Serializable;
import java.util.Collection;

import nl.ocs.dao.query.FetchJoinInformation;
import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.domain.model.impl.ModelBasedFieldFactory;
import nl.ocs.exception.OCSValidationException;
import nl.ocs.service.BaseService;
import nl.ocs.ui.Reloadable;
import nl.ocs.ui.component.DefaultHorizontalLayout;
import nl.ocs.ui.component.DefaultVerticalLayout;
import nl.ocs.ui.composite.form.FormOptions;
import nl.ocs.ui.composite.table.BaseTableWrapper;
import nl.ocs.ui.composite.table.ServiceResultsTableWrapper;
import nl.ocs.ui.container.QueryType;
import nl.ocs.ui.container.ServiceContainer;
import nl.ocs.ui.utils.VaadinUtils;

import org.apache.log4j.Logger;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * A page for editing items directly in a table - this is built around the lazy
 * query container
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public abstract class TabularEditLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCollectionLayout<ID, T> implements Reloadable {

	private static final Logger LOG = Logger.getLogger(TabularEditLayout.class);

	// the default page length
	private static final int PAGE_LENGTH = 18;

	private static final long serialVersionUID = 4606800218149558500L;

	private Filter filter;

	// the main layout
	private VerticalLayout mainLayout;

	private int pageLength = PAGE_LENGTH;

	private Button removeButton;

	private Button saveButton;

	private Button editButton;

	private Button addButton;

	private Button cancelButton;

	private BaseTableWrapper<ID, T> tableWrapper;

	private boolean viewmode;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param parent
	 * @param parentService
	 * @param formOptions
	 */
	public TabularEditLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
			FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
	}

	/**
	 * Callback method that is called after a remove operation has been carried
	 * out
	 */
	protected void afterRemove() {
		// do nothing
	}

	/**
	 * Callback method that is called after a save operation has been carried
	 * out
	 */
	protected void afterSave() {
		// do nothing
	}

	@Override
	public void attach() {
		super.attach();
		this.filter = createFilter();
		build();
	}

	/**
	 * Callback method that is called before a save operation is carried out
	 */
	protected void beforeSave() {
		// do nothing
	}

	/**
	 * Lazily builds the actual layout
	 */
	@Override
	public void build() {
		if (mainLayout == null) {

			setViewmode(!isEditAllowed() || getFormOptions().isOpenInViewMode());

			mainLayout = new DefaultVerticalLayout(true, true);

			// construct the table
			constructTable();

			setButtonBar(new DefaultHorizontalLayout());
			mainLayout.addComponent(getButtonBar());

			// add button
			if (!getFormOptions().isHideAddButton() && isEditAllowed()) {
				addButton = new Button(message("ocs.add"));
				addButton.addClickListener(new Button.ClickListener() {

					@Override
					@SuppressWarnings("unchecked")
					public void buttonClick(ClickEvent event) {
						// delegate the construction of a new item to the lazy
						// query container
						ID id = (ID) getContainer().addItem();
						constructEntity(getEntityFromTable(id));
						getTableWrapper().getTable().setCurrentPageFirstItemId(id);
					}
				});
				getButtonBar().addComponent(addButton);
				addButton.setVisible(!isViewmode());
			}

			// remove button
			if (getFormOptions().isShowRemoveButton()) {
				removeButton = new RemoveButton() {

					@Override
					protected void doDelete() {
						if (getSelectedItem() != null) {
							getTableWrapper().getTable().removeItem(getSelectedItem().getId());
							getContainer().commit();
							setSelectedItem(null);
							afterRemove();
						}
					}
				};
				getButtonBar().addComponent(removeButton);
				removeButton.setVisible(!isViewmode());
				registerDetailButton(removeButton);
			}

			// save button
			saveButton = new Button(message("ocs.save"));
			saveButton.setEnabled(false);
			saveButton.addClickListener(new Button.ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						beforeSave();
						getContainer().commit();
						afterSave();
					} catch (OCSValidationException ex) {
						LOG.error(ex.getMessage(), ex);
						Notification.show(ex.getErrors().get(0), Notification.Type.ERROR_MESSAGE);
					}
				}
			});
			getButtonBar().addComponent(saveButton);
			saveButton.setVisible(!isViewmode());

			if (getFormOptions().isShowEditButton()) {
				editButton = new Button(message("ocs.edit"));
				editButton.addClickListener(new Button.ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						toggleViewMode(false);

					}

				});
				editButton.setVisible(isViewmode());
				getButtonBar().addComponent(editButton);

				cancelButton = new Button(message("ocs.cancel"));
				cancelButton.addClickListener(new Button.ClickListener() {

					@Override
					public void buttonClick(ClickEvent event) {
						reload();
						toggleViewMode(true);
					}

				});
				cancelButton.setVisible(!isViewmode());
				getButtonBar().addComponent(cancelButton);
			}

			postProcessButtonBar(getButtonBar());
			postProcessLayout(mainLayout);
		}

		setCompositionRoot(mainLayout);
	}

	/**
	 * Sets any additional fields on a new entity
	 * 
	 * @param t
	 *            the newly created entity that has to be initialized
	 * @return
	 */
	protected T constructEntity(T t) {
		return t;
	}

	protected void constructTable() {
		if (tableWrapper == null) {
			tableWrapper = new ServiceResultsTableWrapper<ID, T>(getService(), getEntityModel(),
					QueryType.ID_BASED, filter, null, getJoins()) {

				@Override
				protected void onSelect(Object selected) {
					setSelectedItems(selected);
					checkButtonState(getSelectedItem());
				}
			};
			tableWrapper.build();

		}

		final Table table = getTableWrapper().getTable();

		// make sure the table can be edited
		table.setEditable(!isViewmode());
		// make sure changes are not persisted right away
		table.setBuffered(true);
		table.setMultiSelect(false);
		table.setColumnCollapsingAllowed(false);
		// set a higher cache rate to allow for smoother scrolling
		table.setCacheRate(2.0);
		table.setSortEnabled(isSortEnabled());
		table.setPageLength(getPageLength());

		// default sorting
		if (getSortOrder() != null) {
			table.setSortContainerPropertyId(getSortOrder().getPropertyId());
			table.setSortAscending(SortDirection.ASCENDING.equals(getSortOrder().getDirection()));
		}

		EntityModel<T> entityModel = getEntityModelFactory()
				.getModel(getService().getEntityClass());

		// overwrite the field factory to handle validation
		table.setTableFieldFactory(new ModelBasedFieldFactory<T>(entityModel, getMessageService(),
				true, false) {

			@Override
			public Field<?> createField(String propertyId) {
				final Field<?> field = super.createField(propertyId);

				if (field != null && field.isEnabled()) {
					field.addValueChangeListener(new Property.ValueChangeListener() {

						@Override
						public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
							if (saveButton != null) {
								saveButton.setEnabled(VaadinUtils
										.allFixedTableFieldsValid(getTableWrapper().getTable()));
							}
						}

					});

					postProcessField(propertyId, field);
				}

				return field;
			}
		});
		mainLayout.addComponent(tableWrapper);
	}

	/**
	 * Creates the filter used for searching
	 * 
	 * @return
	 */
	protected abstract Filter createFilter();

	@SuppressWarnings("unchecked")
	protected ServiceContainer<ID, T> getContainer() {
		return (ServiceContainer<ID, T>) getTableWrapper().getContainer();
	}

	/**
	 * Retrieves an entity with a certain ID from the lazy query container
	 * 
	 * @param id
	 *            the ID of the entity
	 * @return
	 */
	protected T getEntityFromTable(ID id) {
		return VaadinUtils.getEntityFromContainer(getContainer(), id);
	}

	public int getPageLength() {
		return pageLength;
	}

	public BaseTableWrapper<ID, T> getTableWrapper() {
		return tableWrapper;
	}

	public boolean isViewmode() {
		return viewmode;
	}

	protected void postProcessField(Object propertyId, Field<?> field) {
		// overwrite in subclass
	}

	@Override
	public void reload() {
		getContainer().search(filter);
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	@SuppressWarnings("unchecked")
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items
				Collection<?> col = (Collection<?>) selectedItems;
				ID id = (ID) col.iterator().next();
				setSelectedItem(getEntityFromTable(id));
			} else {
				ID id = (ID) selectedItems;
				setSelectedItem(getEntityFromTable(id));
			}
		} else {
			setSelectedItem(null);
		}
	}

	protected void setTableWrapper(BaseTableWrapper<ID, T> tableWrapper) {
		this.tableWrapper = tableWrapper;
	}

	protected void setViewmode(boolean viewmode) {
		this.viewmode = viewmode;
	}

	/**
	 * Sets the view mode of the screen, and adapts the table and all buttons
	 * accordingly
	 * 
	 * @param viewMode
	 */
	protected void toggleViewMode(boolean viewMode) {
		setViewmode(viewMode);
		getTableWrapper().getTable().setEditable(!isViewmode());

		if (saveButton != null) {
			saveButton.setVisible(!isViewmode());
		}
		if (addButton != null) {
			addButton.setVisible(!isViewmode());
		}
		if (removeButton != null) {
			removeButton.setVisible(!isViewmode());
		}
		if (editButton != null) {
			editButton.setVisible(isViewmode());
		}
		if (cancelButton != null) {
			cancelButton.setVisible(!isViewmode());
		}
	}
}
