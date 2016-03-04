package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.VerticalLayout;

/**
 * A simple page that contains a search form and a table with search results
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public class SimpleSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCollectionLayout<ID, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	// the default page length
	private static final int PAGE_LENGTH = 18;

	// button for adding new items. displayed by default
	private Button addButton;

	// any additional filter that are always added to the query
	private List<Filter> additionalFilters;

	// the fetch joins to use when fetching an item for display in the detail
	// screen
	private FetchJoinInformation[] detailJoins;

	// button for opening the screen in edit mode
	private Button editButton;

	// the edit form
	private ModelBasedEditForm<ID, T> editForm;

	// map of extra filters to be applied to certain fields
	private Map<String, Filter> fieldFilters;

	// the main layout (in edit mode)
	private VerticalLayout mainEditLayout;

	// the main layout (in search mode)
	private VerticalLayout mainLayout;

	// the number of rows to display in the table
	private int pageLength = PAGE_LENGTH;

	// the query type (paging or id-based)
	private QueryType queryType;

	// the button that is used to remove an item - disabled by default, must be
	// explicitly set to visible
	private Button removeButton;

	// the search form
	protected ModelBasedSearchForm<ID, T> searchForm;

	// the set of currently selected items
	private Collection<T> selectedItems;

	// the table wrapper
	protected ServiceResultsTableWrapper<ID, T> tableWrapper;

	/**
	 * Constructor - all fields
	 * 
	 * @param service
	 *            the service that is used to query the database
	 * @param entityModel
	 *            the entity model of the entities to search for
	 * @param queryType
	 *            the type of the query
	 * @param formOptions
	 *            form options that governs which buttons and options to show
	 * @param fieldFilters
	 *            filters that are applied to individual search fields
	 * @param additionalFilters
	 *            search filters that are added to every query
	 * @param sortOrder
	 *            the default sort order
	 * @param joins
	 *            the joins to include in the query
	 */
	public SimpleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
	        QueryType queryType, FormOptions formOptions, Map<String, Filter> fieldFilters,
	        List<Filter> additionalFilters, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
		this.additionalFilters = additionalFilters;
		this.fieldFilters = fieldFilters;
	}

	/**
	 * Constructor - only the most important fields
	 * 
	 * @param service
	 *            the service that is used to query the database
	 * @param entityModel
	 *            the entity model of the entities to search for
	 * @param queryType
	 *            the type of the query
	 * @param formOptions
	 *            form options that governs which buttons and options to show
	 * @param sortOrder
	 *            the default sort order
	 * @param joins
	 *            the joins to include in the query
	 */
	public SimpleSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel,
	        QueryType queryType, FormOptions formOptions, SortOrder sortOrder,
	        FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
	}

	/**
	 * Callback method that is called after a detail entity has been selected
	 * 
	 * @param editForm
	 * @param entity
	 */
	protected void afterDetailSelected(ModelBasedEditForm<ID, T> editForm, T entity) {
		// override in subclass
	}

	/**
	 * Responds to the toggling of the visibility of the search fields
	 * 
	 * @param visible
	 */
	protected void afterSearchFieldToggle(boolean visible) {
		// do nothing
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Lazily constructs the screen
	 */
	@Override
	public void build() {
		if (mainLayout == null) {
			mainLayout = new DefaultVerticalLayout();

			// search results table
			tableWrapper = getTableWrapper();
			tableWrapper.getTable().setPageLength(pageLength);

			// add a listener to respond to the selection of an item
			tableWrapper.getTable().addValueChangeListener(new Property.ValueChangeListener() {
				@Override
				public void valueChange(ValueChangeEvent event) {
					select(tableWrapper.getTable().getValue());
					checkButtonState(getSelectedItem());
				}
			});

			mainLayout.addComponent(getSearchForm());
			mainLayout.addComponent(tableWrapper);

			setButtonBar(new DefaultHorizontalLayout());

			// add button
			getButtonBar().addComponent(constructAddButton());

			// edit/view button
			Button b = constructEditButton();
			if (b != null) {
				getButtonBar().addComponent(b);
			}

			// remove button
			getButtonBar().addComponent(constructRemoveButton());

			// callback for adding additional buttons
			postProcessButtonBar(getButtonBar());

			mainLayout.addComponent(getButtonBar());
		}
		setCompositionRoot(mainLayout);
	}

	/**
	 * Constructs the add button
	 * 
	 * @return
	 */
	protected Button constructAddButton() {
		addButton = new Button(message("ocs.add"));
		addButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				setSelectedItem(createEntity());
				detailsMode(getSelectedItem());
			}
		});
		addButton.setVisible(isEditAllowed() && !getFormOptions().isHideAddButton());
		return addButton;
	}

	/**
	 * Constructs the edit button
	 * 
	 * @return
	 */
	protected Button constructEditButton() {
		// edit button
		editButton = new Button(
		        getFormOptions().isOpenInViewMode() ? message("ocs.view") : message("ocs.edit"));
		editButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (getSelectedItem() != null) {
					detailsMode(getSelectedItem());
				}
			}
		});
		editButton.setVisible(isEditAllowed() && getFormOptions().isShowEditButton());
		return editButton;
	}

	/**
	 * Constructs the remove button
	 * 
	 * @return
	 */
	protected Button constructRemoveButton() {
		removeButton = new RemoveButton() {

			@Override
			protected void doDelete() {
				remove();
			}

		};
		removeButton.setVisible(isEditAllowed() && getFormOptions().isShowRemoveButton());
		return removeButton;
	}

	/**
	 * Open the screen in details-mode
	 * 
	 * @param entity
	 *            the entity to display
	 */
	protected void detailsMode(T entity) {
		if (mainEditLayout == null) {
			mainEditLayout = new DefaultVerticalLayout();
			mainEditLayout.setStyleName(OCSConstants.CSS_CLASS_HALFSCREEN);

			// set the form options for the detail form
			FormOptions options = new FormOptions();
			options.setOpenInViewMode(getFormOptions().isOpenInViewMode());
			if (options.isOpenInViewMode()) {
				options.setShowBackButton(true);
				options.setShowEditButton(true);
			}

			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(),
			        options, fieldFilters) {

				@Override
				protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
					// when the user is done, display the search screen again
					setCompositionRoot(mainLayout);
					search();
				}

				@Override
				protected Field<?> constructCustomField(EntityModel<T> entityModel,
			            AttributeModel attributeModel, boolean viewMode) {
					return SimpleSearchLayout.this.constructCustomField(entityModel, attributeModel,
			                true, false);
				}

				@Override
				protected void postProcessEditFields() {
					SimpleSearchLayout.this.postProcessEditFields(editForm);
				}

			};
			editForm.build();
			mainEditLayout.addComponent(editForm);
		} else {
			editForm.setViewMode(getFormOptions().isOpenInViewMode());
			editForm.setEntity(entity);
		}
		afterDetailSelected(editForm, entity);

		setCompositionRoot(mainEditLayout);
	}

	public Button getAddButton() {
		return addButton;
	}

	protected List<Filter> getAdditionalFilters() {
		return additionalFilters;
	}

	public FetchJoinInformation[] getDetailJoins() {
		return detailJoins;
	}

	public Button getEditButton() {
		return editButton;
	}

	protected Map<String, Filter> getFieldFilters() {
		return fieldFilters;
	}

	public int getPageLength() {
		return pageLength;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	/**
	 * Constructs and returns the search form
	 * 
	 * @return
	 */
	protected Component getSearchForm() {
		if (searchForm == null) {
			searchForm = new ModelBasedSearchForm<ID, T>(getTableWrapper(), getEntityModel(),
			        getFormOptions(), this.additionalFilters, this.fieldFilters) {

				@Override
				protected void afterSearchFieldToggle(boolean visible) {
					SimpleSearchLayout.this.afterSearchFieldToggle(visible);
				}

				@Override
				protected Field<?> constructCustomField(EntityModel<T> entityModel,
			            AttributeModel attributeModel) {
					return SimpleSearchLayout.this.constructCustomField(entityModel, attributeModel,
			                false, true);
				}
			};
			searchForm.build();
		}
		return searchForm;
	}

	public Collection<T> getSelectedItems() {
		return selectedItems;
	}

	public ServiceResultsTableWrapper<ID, T> getTableWrapper() {
		if (tableWrapper == null) {
			tableWrapper = new ServiceResultsTableWrapper<ID, T>(this.getService(),
			        getEntityModel(), getQueryType(), null, getSortOrder(), getJoins());
			tableWrapper.build();
		}
		return tableWrapper;
	}

	/**
	 * Post processes the edit fields
	 */
	protected void postProcessEditFields(ModelBasedEditForm<ID, T> editForm) {
		// overwrite in subclasses
	}

	/**
	 * Performs the actual delete action
	 */
	protected void remove() {
		getService().delete(getSelectedItem());
		// refresh the results so that the deleted item is no longer
		// there
		search();
	}

	/**
	 * Perform the actual search
	 */
	public void search() {
		searchForm.search();
		getTableWrapper().getTable().select(null);
		setSelectedItem(null);
		checkButtonState(getSelectedItem());
	}

	@SuppressWarnings("unchecked")
	public void select(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items

				Collection<?> col = (Collection<?>) selectedItems;
				if (col.size() == 1) {
					ID id = (ID) col.iterator().next();
					setSelectedItem(getService().fetchById(id, getDetailJoins()));
					this.selectedItems = Lists.newArrayList(getSelectedItem());
				} else if (col.size() > 1) {
					// deal with the selection of multiple items
					List<ID> ids = Lists.newArrayList();
					for (Object c : col) {
						ids.add((ID) c);
					}
					this.selectedItems = getService().fetchByIds(ids, getDetailJoins());
				}
			} else {
				// single item has been selected
				ID id = (ID) selectedItems;
				setSelectedItem(getService().fetchById(id, getDetailJoins()));
			}
		} else {
			setSelectedItem(null);
		}
	}

	public void setAdditionalFilters(List<Filter> additionalFilters) {
		this.additionalFilters = additionalFilters;
	}

	public void setDetailJoins(FetchJoinInformation[] detailJoins) {
		this.detailJoins = detailJoins;
	}

	public void setFieldFilters(Map<String, Filter> fieldFilters) {
		this.fieldFilters = fieldFilters;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

}
