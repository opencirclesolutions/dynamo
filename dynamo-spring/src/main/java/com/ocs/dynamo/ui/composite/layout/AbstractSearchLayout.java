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
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.AbstractModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract classes for search layout
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity to search for
 * @param <T>
 *            the type of the entity to search for
 */
public abstract class AbstractSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseCollectionLayout<ID, T> {

	private static final long serialVersionUID = 366639924823921266L;

	/**
	 * Button for adding new items. Displayed by default
	 */
	private Button addButton;

	/**
	 * The default filters that are always apply to any query
	 */
	private List<Filter> defaultFilters;

	/**
	 * The edit button
	 */
	private Button editButton;

	/**
	 * The edit form for editing a single object
	 */
	private ModelBasedEditForm<ID, T> editForm;

	/**
	 * Label used to indicate that there are no search results yet
	 */
	private Label noSearchYetLabel;

	/**
	 * The main layout (in edit mode)
	 */
	private VerticalLayout mainEditLayout;

	/**
	 * The main layout (in search mode)
	 */
	private VerticalLayout mainSearchLayout;

	/**
	 * The layout that contains the search results table
	 */
	private VerticalLayout searchResultsLayout;

	/**
	 * The query type
	 */
	private QueryType queryType;

	/**
	 * The remove button
	 */
	private Button removeButton;

	/**
	 * The search form
	 */
	private AbstractModelBasedSearchForm<ID, T> searchForm;

	/**
	 * The currently selected items in the search results table
	 */
	private Collection<T> selectedItems;

	/**
	 * Indicates whether the search layout has been constructed yet
	 */
	private boolean searchLayoutConstructed;

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param queryType
	 * @param formOptions
	 * @param fieldFilters
	 * @param defaultFilters
	 * @param sortOrder
	 * @param joins
	 */
	public AbstractSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
	        FormOptions formOptions, Map<String, Filter> fieldFilters, List<Filter> defaultFilters,
	        SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
		this.defaultFilters = defaultFilters;
		setFieldFilters(fieldFilters);
	}

	/**
	 * Constructor - only the most important attributes
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
	public AbstractSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
	        FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
	}

	/**
	 * Method that is called after the user clicks the "Clear" button
	 */
	protected void afterClear() {
		// overwrite in subclasses
	}

	/**
	 * Method that is called after the user clicks the "show/hide" button to show/hide the search
	 * form
	 * 
	 * @param visible
	 *            whether the search fields are visible after the toggle
	 */
	protected void afterSearchFieldToggle(boolean visible) {
		// overwrite in subclasses
	}

	/**
	 * Method that is called after a successful search has been carried out
	 */
	protected void afterSearchPerformed() {
		// overwrite in subclasses
	}

	@Override
	public void attach() {
		super.attach();
		build();
	}

	/**
	 * Perform any actions that are necessary before carrying out a search. Can be used to interfere
	 * with the search process
	 * 
	 * @param filter
	 *            the current search filter
	 * @return the modified search filter. If not null, then this filter will be used for the search
	 *         instead of the current filter
	 */
	protected Filter beforeSearchPerformed(Filter filter) {
		// overwrite in subclasses if needed
		return null;
	}

	/**
	 * Lazily constructs the screen
	 */
	@Override
	public void build() {
		if (mainSearchLayout == null) {
			mainSearchLayout = new DefaultVerticalLayout();

			// if search immediately, construct the search results table
			if (getFormOptions().isSearchImmediately()) {
				constructSearchLayout();
				searchLayoutConstructed = true;
			}

			// listen to a click on the clear button
			mainSearchLayout.addComponent(getSearchForm());

			if (getSearchForm().getClearButton() != null) {
				getSearchForm().getClearButton().addClickListener(new Button.ClickListener() {

					private static final long serialVersionUID = -2415598554878464948L;

					@Override
					public void buttonClick(ClickEvent event) {
						afterClear();
					}
				});
			}

			searchResultsLayout = new DefaultVerticalLayout(false, false);
			mainSearchLayout.addComponent(searchResultsLayout);

			if (getFormOptions().isSearchImmediately()) {
				// immediately construct the search results table
				searchResultsLayout.addComponent(getTableWrapper());
			} else {
				// do not construct the search results table yet
				noSearchYetLabel = new Label(message("ocs.no.search.yet"));
				searchResultsLayout.addComponent(noSearchYetLabel);

				// set up a click listener that will construct the table when needed in case of a
				// deferred search
				// set up a click listener that will set the searchable when
				// needed
				getSearchForm().getSearchButton().addClickListener(new Button.ClickListener() {

					private static final long serialVersionUID = -156650492974447814L;

					@Override
					public void buttonClick(ClickEvent event) {
						if (!searchLayoutConstructed) {
							// construct search screen if it is not there yet
							constructSearchLayout();

							searchResultsLayout.addComponent(getTableWrapper());
							getSearchForm().setSearchable(getTableWrapper());

							searchResultsLayout.removeComponent(noSearchYetLabel);
							searchLayoutConstructed = true;
							search();
						} else {
							// otherwise, only fire the callback method
							afterSearchPerformed();
						}
					}
				});
			}

			// add button
			addButton = constructAddButton();
			if (addButton != null) {
				getButtonBar().addComponent(addButton);
			}

			// edit/view button
			editButton = constructEditButton();
			if (editButton != null) {
				registerButton(editButton);
				getButtonBar().addComponent(editButton);
			}

			// remove button
			removeButton = constructRemoveButton();
			if (removeButton != null) {
				registerButton(removeButton);
				getButtonBar().addComponent(removeButton);
			}

			// callback for adding additional buttons
			postProcessButtonBar(getButtonBar());
			mainSearchLayout.addComponent(getButtonBar());

			postProcessLayout(mainSearchLayout);
		}
		setCompositionRoot(mainSearchLayout);
	}

	/**
	 * Constructs the button that will switch the screen to the detail view. Depending on the
	 * "open in view mode" setting the caption will read either "view" or "edit"
	 * 
	 * @return
	 */
	protected Button constructEditButton() {
		Button eb = new Button(getFormOptions().isOpenInViewMode() ? message("ocs.view") : message("ocs.edit"));
		eb.addClickListener(new Button.ClickListener() {

			private static final long serialVersionUID = -2800434669444928287L;

			@Override
			public void buttonClick(ClickEvent event) {
				if (getSelectedItem() != null) {
					doEdit();
				}
			}
		});

		// show button if editing is allowed or if detail screen opens in view mode
		eb.setVisible(getFormOptions().isShowEditButton() && (isEditAllowed() || getFormOptions().isOpenInViewMode()));
		return eb;
	}

	/**
	 * Method that is used to construct any extra search fields. These will be added at the front of
	 * the search form
	 */
	protected List<Component> constructExtraSearchFields() {
		// overwrite in subclasses
		return new ArrayList<>();
	}

	/**
	 * Constructs the remove button
	 * 
	 * @return
	 */
	protected Button constructRemoveButton() {
		Button rb = new RemoveButton() {

			private static final long serialVersionUID = -7428844985367616649L;

			@Override
			protected void doDelete() {
				remove();
			}

		};
		rb.setVisible(isEditAllowed() && getFormOptions().isShowRemoveButton());
		return rb;
	}

	/**
	 * Constructs the search form - implement in subclasses
	 * 
	 * @return
	 */
	protected abstract AbstractModelBasedSearchForm<ID, T> constructSearchForm();

	/**
	 * Constructs the search layout
	 */
	public void constructSearchLayout() {
		// construct table and set properties
		getTableWrapper().getTable().setPageLength(getPageLength());
		getTableWrapper().getTable().setSortEnabled(isSortEnabled());
		getTableWrapper().getTable().setMultiSelect(isMultiSelect());

		// add a listener to respond to the selection of an item
		getTableWrapper().getTable().addValueChangeListener(new Property.ValueChangeListener() {

			private static final long serialVersionUID = 7181225125947489648L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				select(getTableWrapper().getTable().getValue());
				checkButtonState(getSelectedItem());
			}
		});

		// double click listener - opens the detail view when the user double clicks on a row
		if (getFormOptions().isShowEditButton()) {
			getTableWrapper().getTable().addItemClickListener(new ItemClickListener() {

				private static final long serialVersionUID = 7947905411214073660L;

				@Override
				public void itemClick(ItemClickEvent event) {
					if (event.isDoubleClick() && getFormOptions().isDoubleClickSelectAllowed()) {
						select(event.getItem().getItemProperty(DynamoConstants.ID).getValue());
						doEdit();
					}
				}
			});
		}

		// table dividers
		constructTableDividers();
	}

	/**
	 * Lazily constructs the table wrapper
	 */
	@Override
	public ServiceResultsTableWrapper<ID, T> constructTableWrapper() {
		ServiceResultsTableWrapper<ID, T> result = new ServiceResultsTableWrapper<ID, T>(this.getService(),
		        getEntityModel(), getQueryType(), getSearchForm().extractFilter(), getSortOrders(), getFormOptions()
		                .isTableExportAllowed(), getJoins()) {

			private static final long serialVersionUID = 6343267378913526151L;

			@Override
			protected Filter beforeSearchPerformed(Filter filter) {
				return AbstractSearchLayout.this.beforeSearchPerformed(filter);
			}

			@Override
			protected void doConstructContainer(Container container) {
				AbstractSearchLayout.this.doConstructContainer(container);
			}
		};

		if (getFormOptions().isSearchImmediately()) {
			getSearchForm().setSearchable(result);
		}

		result.build();
		return result;
	}

	/**
	 * Opens a custom detail view
	 * 
	 * @param root
	 *            the root component of the custom detail view
	 */
	protected void customDetailView(Component root) {
		setCompositionRoot(root);
	}

	/**
	 * Open the screen in details mode
	 * 
	 * @param entity
	 *            the entity to display
	 */
	@Override
	protected void detailsMode(T entity) {
		if (mainEditLayout == null) {
			mainEditLayout = new DefaultVerticalLayout();
			mainEditLayout.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);

			// set the form options for the detail form
			FormOptions options = new FormOptions();
			options.setOpenInViewMode(getFormOptions().isOpenInViewMode());
			options.setScreenMode(ScreenMode.VERTICAL);

			if (options.isOpenInViewMode()) {
				options.setShowBackButton(true).setShowEditButton(true);
			}

			editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), options, getFieldFilters()) {

				private static final long serialVersionUID = 6485097089659928131L;

				@Override
				protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
					if (getFormOptions().isOpenInViewMode()) {
						if (newObject) {
							back();
						} else {
							// if details screen opens in view mode, simply switch to view mode
							setViewMode(true);
							detailsMode(entity);
						}
					} else {
						// otherwise go back to the main screen
						back();
					}
				}

				@Override
				protected void afterEntitySet(T entity) {
					AbstractSearchLayout.this.afterEntitySet(entity);
				}

				@Override
				protected void afterModeChanged(boolean viewMode) {
					AbstractSearchLayout.this.afterModeChanged(viewMode, editForm);
				}

				@Override
				protected void back() {
					searchMode();
				}

				@Override
				protected Field<?> constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel,
				        boolean viewMode) {
					return AbstractSearchLayout.this.constructCustomField(entityModel, attributeModel, viewMode, false);
				}

				@Override
				protected boolean isEditAllowed() {
					return AbstractSearchLayout.this.isEditAllowed();
				}

				@Override
				protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
					AbstractSearchLayout.this.postProcessDetailButtonBar(buttonBar, viewMode);
				}

				@Override
				protected void postProcessEditFields() {
					AbstractSearchLayout.this.postProcessEditFields(editForm);
				}

			};
			editForm.setDetailJoins(getDetailJoinsFallBack());
			editForm.setFieldEntityModels(getFieldEntityModels());
			editForm.build();
			mainEditLayout.addComponent(editForm);
		} else {
			editForm.setViewMode(getFormOptions().isOpenInViewMode());
			editForm.setEntity(entity);
			editForm.resetTab();
		}

		checkButtonState(getSelectedItem());
		afterEntitySelected(editForm, entity);
		setCompositionRoot(mainEditLayout);
	}

	/**
	 * Callback method that is called when the user presses the edit method. Will by default open
	 * the screen in edit mode. Overwrite in subclass if needed
	 */
	protected void doEdit() {
		detailsMode(getSelectedItem());
	}

	/**
	 * Performs the actual remove functionality - overwrite in subclass if needed
	 */
	protected void doRemove() {
		getService().delete(getSelectedItem());
	}

	/**
	 * Open the screen in edit mode for the provided entity
	 * 
	 * @param entity
	 */
	public final void edit(T entity) {
		setSelectedItem(entity);
		doEdit();
	}

	public Button getAddButton() {
		return addButton;
	}

	protected List<Filter> getDefaultFilters() {
		return defaultFilters;
	}

	public Button getEditButton() {
		return editButton;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public Button getRemoveButton() {
		return removeButton;
	}

	/**
	 * Returns the search form (lazily constructing it when needed)
	 * 
	 * @return
	 */
	public AbstractModelBasedSearchForm<ID, T> getSearchForm() {
		if (searchForm == null) {
			searchForm = constructSearchForm();
		}
		return searchForm;
	}

	public Collection<T> getSelectedItems() {
		return selectedItems;
	}

	/**
	 * Refreshes all lookup components
	 */
	@Override
	public void refresh() {
		getSearchForm().refresh();
	}

	/**
	 * Resets the layout (clears the search form)
	 */
	@Override
	public void reload() {
		setCompositionRoot(mainSearchLayout);
		getSearchForm().clear();
		search();
	}

	/**
	 * Reloads the details view only
	 */
	public void reloadDetails() {
		this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getDetailJoinsFallBack()));
		detailsMode(getSelectedItem());
	}

	/**
	 * Performs the actual delete action
	 */
	protected final void remove() {
		doRemove();
		// refresh the results so that the deleted item is no longer
		// there
		setSelectedItem(null);
		search();
	}

	/**
	 * Refreshes the contents of a label
	 * 
	 * @param propertyName
	 *            the name of the property for which to refresh the label
	 */
	public void replaceLabel(String propertyName) {
		if (editForm != null) {
			editForm.replaceLabel(propertyName);
		}
	}

	/**
	 * Perform the actual search
	 */
	public void search() {
		boolean searched = searchForm.search();
		if (searched) {
			getTableWrapper().getTable().select(null);
			setSelectedItem(null);
			afterSearchPerformed();
		}
	}

	/**
	 * Puts the screen in search mode
	 */
	public void searchMode() {
		setCompositionRoot(mainSearchLayout);
		getSearchForm().refresh();
		search();
	}

	/**
	 * Select one or more items
	 * 
	 * @param selectedItems
	 *            the item or items to select
	 */
	@SuppressWarnings("unchecked")
	public void select(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items

				Collection<?> col = (Collection<?>) selectedItems;
				if (col.size() == 1) {
					ID id = (ID) col.iterator().next();
					setSelectedItem(getService().fetchById(id, getDetailJoinsFallBack()));
					this.selectedItems = Lists.newArrayList(getSelectedItem());
				} else if (col.size() > 1) {
					// deal with the selection of multiple items
					List<ID> ids = Lists.newArrayList();
					for (Object c : col) {
						ids.add((ID) c);
					}
					this.selectedItems = getService().fetchByIds(ids, getDetailJoinsFallBack());
				}
			} else {
				// single item has been selected
				ID id = (ID) selectedItems;
				setSelectedItem(getService().fetchById(id, getDetailJoinsFallBack()));
			}
		} else {
			setSelectedItem(null);
		}
	}

	public void setDefaultFilters(List<Filter> defaultFilters) {
		this.defaultFilters = defaultFilters;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	/**
	 * Sets a predefined search value
	 * 
	 * @param propertyId
	 *            the name of the property for which to set a value
	 * @param value
	 *            the value
	 */
	public abstract void setSearchValue(String propertyId, Object value);

	/**
	 * Sets a predefined search value
	 * 
	 * @param propertyId
	 *            the name of the property for which to set a value
	 * @param value
	 *            the value (lower bound)
	 * @param auxValue
	 *            the auxiliary value (upper bound)
	 */
	public abstract void setSearchValue(String propertyId, Object value, Object auxValue);

}
