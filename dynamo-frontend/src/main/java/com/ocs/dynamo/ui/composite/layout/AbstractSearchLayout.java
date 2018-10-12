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

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultHorizontalLayout;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.AbstractModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Base class for search layouts. A search layout consists of a search form with
 * a results table below it. From the result table the user can select an entity
 * and then navigate to a detail screen for managing that entity
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key of the entity to search for
 * @param <T>
 *            the type of the entity to search for
 */
public abstract class AbstractSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseCollectionLayout<ID, T> {

	private static final long serialVersionUID = 366639924823921266L;

	/**
	 * Button for adding new items. Displayed by default
	 */
	private Button addButton;

	/**
	 * The back button that is displayed above the tab sheet in "complex details"
	 * mode
	 */
	private Button complexDetailModeBackButton;

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
	 * The main layout (in edit mode)
	 */
	private VerticalLayout mainEditLayout;

	/**
	 * The main layout (in search mode)
	 */
	private VerticalLayout mainSearchLayout;

	private Button nextButton;

	private Button prevButton;

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
	 * Indicates whether the search layout has been constructed yet
	 */
	private boolean searchLayoutConstructed;

	/**
	 * The layout that contains the search results table
	 */
	private VerticalLayout searchResultsLayout;

	private Component selectedDetailLayout;

	/**
	 * The currently selected items in the search results table
	 */
	private Collection<T> selectedItems;

	/**
	 *
	 */
	private VerticalLayout tabContainerLayout;

	/**
	 * Tabbed layout for complex detail mode
	 */
	private LazyTabLayout<ID, T> tabLayout;

	/**
	 * Constructor
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
	 * Method that is called after the user clicks the "show/hide" button to
	 * show/hide the search form
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
	 * Perform any actions that are necessary before carrying out a search. Can be
	 * used to interfere with the search process
	 * 
	 * @param filter
	 *            the current search filter
	 * @return the modified search filter. If not null, then this filter will be
	 *         used for the search instead of the current filter
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
				if (!getFormOptions().isSearchImmediately()) {
					getSearchForm().getClearButton().addClickListener(e -> {
						// hide the search results table and add the label again
						Label noSearchYetLabel = new Label(message("ocs.no.search.yet"));
						searchResultsLayout.removeAllComponents();
						searchResultsLayout.addComponent(noSearchYetLabel);
						getSearchForm().setSearchable(null);
						searchLayoutConstructed = false;
					});
				}
				getSearchForm().getClearButton().addClickListener(e -> afterClear());
			}

			searchResultsLayout = new DefaultVerticalLayout(false, false);
			mainSearchLayout.addComponent(searchResultsLayout);

			if (getFormOptions().isSearchImmediately()) {
				// immediately construct the search results table
				searchResultsLayout.addComponent(getTableWrapper());
			} else {
				// do not construct the search results table yet
				Label noSearchYetLabel = new Label(message("ocs.no.search.yet"));
				searchResultsLayout.addComponent(noSearchYetLabel);

				// set up a click listener that will construct the table when
				// needed in case of a
				// deferred search
				// set up a click listener that will set the searchable when
				// needed
				getSearchForm().getSearchButton().addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
				if (getSearchForm().getSearchAnyButton() != null) {
					getSearchForm().getSearchAnyButton()
							.addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
				}
			}

			// add button
			addButton = constructAddButton();
			getButtonBar().addComponent(addButton);

			// edit/view button
			editButton = constructEditButton();
			registerButton(editButton);
			getButtonBar().addComponent(editButton);

			// remove button
			removeButton = constructRemoveButton();
			registerButton(removeButton);
			getButtonBar().addComponent(removeButton);

			// callback for adding additional buttons
			postProcessButtonBar(getButtonBar());
			mainSearchLayout.addComponent(getButtonBar());

			// post process the layout
			postProcessLayout(mainSearchLayout);
		}
		setCompositionRoot(mainSearchLayout);
	}

	/**
	 * Builds a tab layout for the display view. The definition of the tabs has to
	 * be done in the subclasses
	 * 
	 * @param entity
	 *            the currently selected entity
	 */
	protected void buildDetailsTabLayout(T entity, FormOptions formOptions) {
		tabContainerLayout = new DefaultVerticalLayout(true, true);

		HorizontalLayout buttonBar = new DefaultHorizontalLayout(false, true, true);
		tabContainerLayout.addComponent(buttonBar);

		complexDetailModeBackButton = new Button(message("ocs.back"));
		complexDetailModeBackButton.setIcon(FontAwesome.BACKWARD);
		complexDetailModeBackButton.addClickListener(e -> searchMode());
		buttonBar.addComponent(complexDetailModeBackButton);

		if (getFormOptions().isShowPrevButton()) {
			prevButton = new Button(message("ocs.previous"));
			prevButton.addClickListener(e -> {
				T prev = getPrevEntity(tabLayout.getEntity());
				if (prev != null) {
					tabLayout.setEntity(prev);
					tabLayout.reload();
				} else {
					prevButton.setEnabled(false);
				}
				if (nextButton != null) {
					nextButton.setEnabled(true);
				}
			});
			buttonBar.addComponent(prevButton);
		}

		if (getFormOptions().isShowNextButton()) {
			nextButton = new Button(message("ocs.next"));
			nextButton.addClickListener(e -> {
				T next = getNextEntity(tabLayout.getEntity());
				if (next != null) {
					tabLayout.setEntity(next);
					tabLayout.reload();
				} else {
					nextButton.setEnabled(false);
				}
				if (prevButton != null) {
					prevButton.setEnabled(true);
				}
			});
			buttonBar.addComponent(nextButton);
		}

		tabLayout = new LazyTabLayout<ID, T>(entity) {

			private static final long serialVersionUID = 1278134557026074688L;

			@Override
			protected String createTitle() {
				return AbstractSearchLayout.this.getDetailModeTabTitle();
			}

			@Override
			protected Resource getIconForTab(int index) {
				return AbstractSearchLayout.this.getIconForTab(index);
			}

			@Override
			protected String[] getTabCaptions() {
				return AbstractSearchLayout.this.getDetailModeTabCaptions();
			}

			@Override
			protected Component initTab(int index) {
				// back button and iteration buttons not needed (they are
				// displayed above
				// the tabs)
				return AbstractSearchLayout.this.initTab(getEntity(), index, formOptions, false);
			}
		};
		tabLayout.build();
		tabContainerLayout.addComponent(tabLayout);

	}

	/**
	 * Builds the edit form
	 * 
	 * @param entity
	 *            the currently selected entity
	 * @param options
	 *            the form options
	 */
	protected void buildEditForm(T entity, FormOptions options) {
		editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(), options, getFieldFilters()) {

			private static final long serialVersionUID = 6485097089659928131L;

			@Override
			protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
				if (getFormOptions().isOpenInViewMode()) {
					if (newObject) {
						back();
					} else {
						// if details screen opens in view mode, simply switch
						// to view mode
						setViewMode(true);
						detailsMode(entity);
					}
				} else {
					// otherwise go back to the main screen
					if (cancel || newObject
							|| (!getFormOptions().isShowNextButton() && !getFormOptions().isShowPrevButton())) {
						back();
					}
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
			protected void afterTabSelected(int tabIndex) {
				AbstractSearchLayout.this.afterTabSelected(tabIndex);
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
			protected T getNextEntity(T current) {
				return AbstractSearchLayout.this.getNextEntity(current);
			}

			@Override
			protected String getParentGroup(String childGroup) {
				return AbstractSearchLayout.this.getParentGroup(childGroup);
			}

			@Override
			protected String[] getParentGroupHeaders() {
				return AbstractSearchLayout.this.getParentGroupHeaders();
			}

			@Override
			protected T getPrevEntity(T current) {
				return AbstractSearchLayout.this.getPrevEntity(current);
			}

			@Override
			protected boolean handleCustomException(RuntimeException ex) {
				return AbstractSearchLayout.this.handleCustomException(ex);
			}

			@Override
			protected boolean hasNextEntity(T current) {
				return AbstractSearchLayout.this.hasNextEntity(current);
			}

			@Override
			protected boolean hasPrevEntity(T current) {
				return AbstractSearchLayout.this.hasPrevEntity(current);
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
		editForm.setFormTitleWidth(getFormTitleWidth());
		editForm.setSupportsIteration(true);
		editForm.setDetailJoins(getDetailJoinsFallBack());
		editForm.setFieldEntityModels(getFieldEntityModels());
		editForm.build();

	}

	/**
	 * Constructs the button that will switch the screen to the detail view.
	 * Depending on the "open in view mode" setting the caption will read either
	 * "view" or "edit"
	 * 
	 * @return
	 */
	protected final Button constructEditButton() {
		Button eb = new Button(
				(!getFormOptions().isEditAllowed() || !isEditAllowed()) ? message("ocs.view") : message("ocs.edit"));
		eb.setIcon(FontAwesome.PENCIL);
		eb.addClickListener(e -> {
			if (getSelectedItem() != null) {
				doEdit();
			}
		});
		// hide button inside popup window
		eb.setVisible(!getFormOptions().isPopup());
		return eb;
	}

	/**
	 * Constructs a search layout in response to a click on any of the search
	 * buttons
	 * 
	 * @param noSearchYetLabel
	 */
	private void constructLayoutIfNeeded(Label noSearchYetLabel) {
		if (!searchLayoutConstructed) {
			// construct search screen if it is not there yet
			try {
				validateBeforeSearch();
				searchResultsLayout.removeAllComponents();
				constructSearchLayout();
				searchResultsLayout.addComponent(getTableWrapper());
				getSearchForm().setSearchable(getTableWrapper());
				searchResultsLayout.removeComponent(noSearchYetLabel);
				searchLayoutConstructed = true;
				afterSearchPerformed();
			} catch (OCSValidationException ex) {
				showNotifification(ex.getErrors().get(0), Notification.Type.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Constructs the remove button
	 * 
	 * @return
	 */
	protected final Button constructRemoveButton() {
		Button rb = new RemoveButton(message("ocs.remove"), null) {

			private static final long serialVersionUID = -7428844985367616649L;

			@Override
			protected void doDelete() {
				remove();
			}

			@Override
			protected String getItemToDelete() {
				T t = getSelectedItem();
				return FormatUtils.formatEntity(getEntityModel(), t);
			}

		};
		rb.setIcon(FontAwesome.TRASH);
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
	public final void constructSearchLayout() {
		// construct table and set properties
		getTableWrapper().getTable().setPageLength(getPageLength());
		getTableWrapper().getTable().setSortEnabled(isSortEnabled());
		getTableWrapper().getTable().setMultiSelect(isMultiSelect());

		// add a listener to respond to the selection of an item
		getTableWrapper().getTable().addValueChangeListener(e -> {
			select(getTableWrapper().getTable().getValue());
			checkButtonState(getSelectedItem());
		});

		// select item by double clicking on row (disable this inside popup
		// windows)
		if (!getFormOptions().isPopup() && getFormOptions().isDoubleClickSelectAllowed()) {
			getTableWrapper().getTable().addItemClickListener(event -> {
				if (event.isDoubleClick()) {
					select(event.getItem().getItemProperty(DynamoConstants.ID).getValue());
					doEdit();
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
				getEntityModel(), getQueryType(), getSearchForm().extractFilter(), getSortOrders(),
				getFormOptions().isTableExportAllowed(), getJoins()) {

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
		result.setMaxResults(getMaxResults());

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
	protected final void customDetailView(Component root) {
		setCompositionRoot(root);
	}

	/**
	 * Opens the screen in details mode and selects a certain tab
	 *
	 * @param entity
	 * @param selectedTab
	 */
	protected void detailsMode(T entity, int selectedTab) {
		detailsMode(entity);
		if (editForm != null) {
			editForm.selectTab(selectedTab);
		} else if (getFormOptions().isComplexDetailsMode()) {
			tabLayout.selectTab(selectedTab);
		}
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
		}

		FormOptions options = new FormOptions();
		options.setOpenInViewMode(getFormOptions().isOpenInViewMode());
		options.setScreenMode(ScreenMode.VERTICAL);
		options.setAttributeGroupMode(getFormOptions().getAttributeGroupMode());
		options.setPreserveSelectedTab(getFormOptions().isPreserveSelectedTab());
		options.setShowNextButton(getFormOptions().isShowNextButton());
		options.setShowPrevButton(getFormOptions().isShowPrevButton());
		options.setPlaceButtonBarAtTop(getFormOptions().isPlaceButtonBarAtTop());
		options.setFormNested(true);

		// set the form options for the detail form
		if (getFormOptions().isEditAllowed()) {
			// editing in form must be possible
			options.setEditAllowed(true);
		} else {
			// read-only mode
			options.setOpenInViewMode(true).setEditAllowed(false);
		}

		if (options.isOpenInViewMode() || !isEditAllowed()) {
			options.setShowBackButton(true);
		}

		if (getFormOptions().isComplexDetailsMode() && entity != null && entity.getId() != null) {
			// complex tabbed layout, back button is placed separately
			options.setShowBackButton(false);
			options.setHideCancelButton(true);

			if (tabContainerLayout == null) {
				buildDetailsTabLayout(entity, options);
			} else {
				tabLayout.setEntity(entity);
				tabLayout.reload();
			}
			if (selectedDetailLayout == null) {
				mainEditLayout.addComponent(tabContainerLayout);
			} else {
				mainEditLayout.replaceComponent(selectedDetailLayout, tabContainerLayout);
			}
			selectedDetailLayout = tabContainerLayout;
        } else if (!getFormOptions().isComplexDetailsMode()) {
			// simple edit form
			if (editForm == null) {
				buildEditForm(entity, options);
			} else {
				editForm.setViewMode(options.isOpenInViewMode());
				editForm.setEntity(entity);
				editForm.resetTab();
			}
			if (selectedDetailLayout == null) {
				mainEditLayout.addComponent(editForm);
			} else {
				mainEditLayout.replaceComponent(selectedDetailLayout, editForm);
			}
			selectedDetailLayout = editForm;

		} else {
			// complex mode, but re-use
			Component comp = initTab(entity, 0, getFormOptions(), true);

			if (selectedDetailLayout == null) {
				mainEditLayout.addComponent(comp);
			} else {
				mainEditLayout.replaceComponent(selectedDetailLayout, comp);
			}
			selectedDetailLayout = comp;
		}

		checkButtonState(getSelectedItem());
		if (editForm != null) {
			afterEntitySelected(editForm, entity);
		}
		setCompositionRoot(mainEditLayout);

	}

	/**
	 * Callback method that is called when the user presses the edit method. Will by
	 * default open the screen in edit mode. Overwrite in subclass if needed
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

	/**
	 * Open in edit mode and select the tab with the provided index
	 *
	 * @param entity
	 *            the entity to select
	 * @param initialTab
	 *            the index of the tab to display
	 */
	public final void edit(T entity, int initialTab) {
		setSelectedItem(entity);
		doEdit();
		if (editForm != null) {
			editForm.selectTab(initialTab);
		} else {
			tabLayout.selectTab(initialTab);
		}
	}

	public Button getAddButton() {
		return addButton;
	}

	public Button getComplexDetailModeBackButton() {
		return complexDetailModeBackButton;
	}

	protected List<Filter> getDefaultFilters() {
		return defaultFilters;
	}

	/**
	 * Returns the captions for the tab pages to display within the tab sheet, for a
	 * search layout for which the complexDetailsEditMode has been set to true
	 *
	 * @return
	 */
	protected String[] getDetailModeTabCaptions() {
		// overwrite in subclasses
		return new String[0];
	}

	/**
	 * Returns the caption to display above the tab sheet, for a search layout for
	 * which the complexDetailsEditMode has been set to true
	 *
	 * @return
	 */
	protected String getDetailModeTabTitle() {
		//
		return null;
	}

	public Button getEditButton() {
		return editButton;
	}

	public ModelBasedEditForm<ID, T> getEditForm() {
		return editForm;
	}

	/**
	 * 
	 * @return the total number of configured filters
	 */
	public int getFilterCount() {
		return getSearchForm().getFilterCount();
	}

	protected Resource getIconForTab(int index) {
		// overwrite
		return null;
	}

	public VerticalLayout getMainSearchLayout() {
		return mainSearchLayout;
	}

	/**
	 * Returns the next entity in the container
	 * 
	 * @param current
	 *            the currently selected entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T getNextEntity(T current) {
		if (current != null) {
			ID id = (ID) getTableWrapper().getTable().nextItemId(current.getId());
			if (id != null) {
				T next = getService().fetchById(id, getDetailJoinsFallBack());
				getTableWrapper().getTable().select(next.getId());
				afterEntitySelected(getEditForm(), next);
				return next;
			}
		}
		return null;
	}

	/**
	 * Returns the previous entity in the container
	 * 
	 * @param current
	 *            the currently selected entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T getPrevEntity(T current) {
		if (current != null) {
			ID id = (ID) getTableWrapper().getTable().prevItemId(current.getId());
			if (id != null) {
				T prev = getService().fetchById(id, getDetailJoinsFallBack());
				getTableWrapper().getTable().select(prev.getId());
				afterEntitySelected(getEditForm(), prev);
				return prev;
			}
		}
		return null;
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

	public VerticalLayout getSearchResultsLayout() {
		return searchResultsLayout;
	}

	public Collection<T> getSelectedItems() {
		return selectedItems;
	}

	protected boolean handleCustomException(RuntimeException ex) {
		return false;
	}

	/**
	 * Check whether the container contains a next entity
	 * 
	 * @param current
	 *            the currently selected entity
	 * @return
	 */
	protected boolean hasNextEntity(T current) {
		if (current != null) {
			return getTableWrapper().getTable().nextItemId(current.getId()) != null;
		}
		return false;
	}

	/**
	 * Check whether the container contains a previous entity
	 * 
	 * @param current
	 *            the currently selected entity
	 * @return
	 */
	protected boolean hasPrevEntity(T current) {
		if (current != null) {
			return getTableWrapper().getTable().prevItemId(current.getId()) != null;
		}
		return false;
	}

	/**
	 * Constructs a tab sheet for the tab component
	 *
	 * @param entity
	 *            the selected entity
	 * @param index
	 *            the index of the selected tab sheet
	 * @param fo
	 *            form options that specify how to constructe the component
	 * @param newEntity
	 *            whether we are in the process of creating a new entity
	 * @return
	 */
	protected Component initTab(T entity, int index, FormOptions fo, boolean newEntity) {
		// overwrite is subclasses
		return null;
	}

	/**
	 * Checks if a filter is set for a certain attribute
	 * 
	 * @param path
	 *            the path to the attribute
	 * @return
	 */
	public boolean isFilterSet(String path) {
		return getSearchForm().isFilterSet(path);
	}

	/**
	 * Checks whether the layout is currently in search mode
	 *
	 * @return
	 */
	public boolean isInSearchMode() {
		return ObjectUtils.equals(getCompositionRoot(), mainSearchLayout);
	}

	/**
	 * Post-processes the button bar for the search form
	 * 
	 * @param buttonBar
	 *            the button bar
	 */
	public void postProcessSearchButtonBar(Layout buttonBar) {
		// overwrite in subclasses
	}

	/**
	 * Refreshes all lookup components but otherwise does not update the state of
	 * the screen
	 */
	@Override
	public void refresh() {
		getSearchForm().refresh();
	}

	/**
	 * Refreshes the contents of a label inside the edit form
	 * 
	 * @param propertyName
	 *            the name of the property for which to refresh the label
	 */
	public void refreshLabel(String propertyName) {
		if (editForm != null) {
			editForm.refreshLabel(propertyName);
		}
	}

	/**
	 * Reloads the entire component, reverting to search mode and clearing the
	 * search form
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
	 * Perform the actual search
	 */
	public void search() {
		boolean searched = searchForm.search();
		if (searched) {
			getTableWrapper().getTable().select(null);
			setSelectedItem(null);
		}
	}

	/**
	 * Puts the screen in search mode (does not reset the search form)
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

	/**
	 * Sets the default filters that are always applied to a search query (even
	 * after all search fields have been cleared)
	 *
	 * @param defaultFilters
	 */
	public void setDefaultFilters(List<Filter> defaultFilters) {
		this.defaultFilters = defaultFilters;
		if (searchForm != null) {
			searchForm.setDefaultFilters(defaultFilters);
		}
	}

	/**
	 * Sets the query type. Only use before the component is built.
	 *
	 * @param queryType
	 */
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
	 * Sets a predefined search value (upper and lower bound)
	 * 
	 * @param propertyId
	 *            the name of the property for which to set a value
	 * @param value
	 *            the value (lower bound)
	 * @param auxValue
	 *            the auxiliary value (upper bound)
	 */
	public abstract void setSearchValue(String propertyId, Object value, Object auxValue);

	/**
	 * Sets the tab specified by the provided index to the provided visibility
	 *
	 * @param index
	 *            the index
	 * @param visible
	 */
	public void setDetailsTabVisible(int index, boolean visible) {
		if (tabLayout != null) {
			tabLayout.getTab(index).setVisible(visible);
		}
	}

	/**
	 * Validate before a search is carried out - if the search criteria are not
	 * correctly set, throw an OCSValidationException to abort the search process
	 */
	public void validateBeforeSearch() {
		// overwrite in subclasses
	}
}


