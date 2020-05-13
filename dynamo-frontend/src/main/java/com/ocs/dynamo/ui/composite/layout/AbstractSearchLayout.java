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
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.AbstractModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.grid.GridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Base class for layout that support a search form and result grid
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public abstract class AbstractSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>, U>
        extends BaseCollectionLayout<ID, T, U> {

    private static final long serialVersionUID = 366639924823921266L;

    /**
     * The default filters that are always apply to any query
     */
    private List<SerializablePredicate<T>> defaultFilters;

    /**
     * The main layout (in search mode)
     */
    private VerticalLayout mainSearchLayout;

    /**
     * The query type
     */
    private QueryType queryType;

    /**
     * The search form
     */
    private AbstractModelBasedSearchForm<ID, T> searchForm;

    /**
     * Indicates whether the search layout has been constructed yet
     */
    private boolean searchLayoutConstructed;

    /**
     * The layout that contains the grid that contains the search results
     */
    private VerticalLayout searchResultsLayout;

    /**
     * The currently selected items in the search results grid
     */
    private Collection<T> selectedItems;

    /**
     * Constructor
     * 
     * @param service     the service that is used to query the database
     * @param entityModel the entity model of the entities to search for
     * @param queryType   the type of the query
     * @param formOptions form options that governs which buttons and options to
     *                    show
     * @param sortOrder   the default sort order
     * @param joins       the joins to include in the query
     */
    public AbstractSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType, FormOptions formOptions,
            com.vaadin.flow.data.provider.SortOrder<?> sortOrder, FetchJoinInformation... joins) {
        super(service, entityModel, formOptions, sortOrder, joins);
        this.queryType = queryType;
    }

    public void addManageDetailButtons() {
        // overwrite in subclasses
    }

    /**
     * Callback method that fires after all search filters have been cleared
     */
    protected void afterClear() {
        // overwrite in subclasses
    }

    /**
     * Callback method that fires after the visibility of the search form has been
     * toggled
     * 
     * @param visible whether the search form is currently visible
     */
    protected void afterSearchFieldToggle(boolean visible) {
        // overwrite in subclasses
    }

    /**
     * Callback method that fires after a search has been performed
     */
    protected void afterSearchPerformed() {
        // overwrite in subclasses
    }

    /**
     * Callback method that fires just before performing a search. Can be used to
     * perform any actions that are necessary before carrying out a search.
     * 
     * @param filter the current search filter
     * @return the modified search filter. If not null, then this filter will be
     *         used for the search instead of the current filter
     */
    protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
        // overwrite in subclasses
        return null;
    }

    /**
     * Lazily constructs the screen
     */
    @Override
    public void build() {
        if (mainSearchLayout == null) {
            mainSearchLayout = new DefaultVerticalLayout(false, false);
            mainSearchLayout.addClassName(DynamoConstants.CSS_MAIN_SEARCH_LAYOUT);

            // if search immediately, construct the search results grid
            if (getFormOptions().isSearchImmediately()) {
                constructSearchLayout();
                searchLayoutConstructed = true;
            }

            // listen to a click on the clear button
            mainSearchLayout.add(getSearchForm());
            if (getSearchForm().getClearButton() != null) {
                if (!getFormOptions().isSearchImmediately()) {

                    // use a consumer since the action might have to be deferred until after the
                    // user confirms the clear
                    if (getFormOptions().isConfirmClear()) {
                        getSearchForm().setAfterClearConsumer(e -> clearIfNotSearchingImmediately());
                    } else {
                        // clear right away
                        getSearchForm().getClearButton().addClickListener(e -> clearIfNotSearchingImmediately());
                    }
                } else {
                    // clear current selection and update buttons
                    if (getFormOptions().isConfirmClear()) {
                        getSearchForm().setAfterClearConsumer(e -> {
                            setSelectedItem(null);
                            checkComponentState(getSelectedItem());
                            afterClear();
                        });
                    } else {
                        getSearchForm().getClearButton().addClickListener(e -> {
                            setSelectedItem(null);
                            checkComponentState(getSelectedItem());
                            afterClear();
                        });
                    }
                }
            }

            searchResultsLayout = new DefaultVerticalLayout(false, false);
            searchResultsLayout.setClassName(DynamoConstants.CSS_SEARCH_RESULTS_LAYOUT);
            mainSearchLayout.add(searchResultsLayout);

            if (getFormOptions().isSearchImmediately()) {
                // immediately construct the search results grid
                searchResultsLayout.add(getGridWrapper());
            } else {
                // do not construct the search results grid yet
                Text noSearchYetLabel = new Text(message("ocs.no.search.yet"));
                searchResultsLayout.add(noSearchYetLabel);

                // click listener that will construct search results grid on demand
                if (getSearchForm().getSearchButton() != null) {
                    getSearchForm().getSearchButton().addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
                }
                if (getSearchForm().getSearchAnyButton() != null) {
                    getSearchForm().getSearchAnyButton().addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
                }
            }
            // clear currently selected item and update buttons
            if (getSearchForm().getSearchButton() != null) {
                getSearchForm().getSearchButton().addClickListener(e -> {
                    setSelectedItem(null);
                    checkComponentState(getSelectedItem());
                });
            }

            addManageDetailButtons();

            // callback for adding additional buttons
            postProcessButtonBar(getButtonBar());
            mainSearchLayout.add(getButtonBar());

            checkComponentState(null);

            // post process the layout
            postProcessLayout(mainSearchLayout);

            // there is a small chance that the user navigates directly
            // to the detail screen without the search layout having been
            // created before. This check is there to ensure that the
            // search layout is not appended below the detail layout
            if (getComponentCount() == 0) {
                add(mainSearchLayout);
            }
        }
    }

    /**
     * Respond to a click on the Clear button when not in "search immediately" mode
     */
    private void clearIfNotSearchingImmediately() {
        Text noSearchYetLabel = new Text(message("ocs.no.search.yet"));
        searchResultsLayout.removeAll();
        searchResultsLayout.add(noSearchYetLabel);
        getSearchForm().setSearchable(null);
        searchLayoutConstructed = false;
        setSelectedItem(null);
        checkComponentState(getSelectedItem());
        afterClear();
    }

    /**
     * Constructs a tab sheet for the tab component that is used in complex details
     * mode
     *
     * @param entity    the selected entity
     * @param index     the index of the selected tab sheet
     * @param fo        form options that specify how to construct the component
     * @param newEntity whether we are in the process of creating a new entity
     * @return
     */
    protected Component constructComplexDetailModeTab(int index, FormOptions fo, boolean newEntity) {
        // overwrite is subclasses
        return null;
    }

    /**
     * Constructs the edit button
     * 
     * @return
     */
    protected final Button constructEditButton() {
        Button eb = new Button((!getFormOptions().isEditAllowed() || !isEditAllowed()) ? message("ocs.view") : message("ocs.edit"));
        eb.setIcon(VaadinIcon.PENCIL.create());
        eb.addClickListener(e -> {
            if (getSelectedItem() != null) {
                doEdit();
            }
        });
        eb.setVisible(getFormOptions().isDetailsModeEnabled());
        return eb;
    }

    public abstract GridWrapper<ID, T, U> constructGridWrapper();

    /**
     * Constructs a search layout in response to a click on any of the search
     * buttons
     * 
     * @param noSearchYetLabel the label used to indicate that there are no search
     *                         results yet
     */
    private void constructLayoutIfNeeded(Text noSearchYetLabel) {
        if (!searchLayoutConstructed) {
            // construct search screen if it is not there yet
            try {
                validateBeforeSearch();
                searchResultsLayout.removeAll();
                clearGridWrapper();
                constructSearchLayout();
                searchResultsLayout.add(getGridWrapper());
                getSearchForm().setSearchable(getGridWrapper());
                searchResultsLayout.remove(noSearchYetLabel);
                searchLayoutConstructed = true;
                afterSearchPerformed();
            } catch (OCSValidationException ex) {
                showErrorNotification(ex.getErrors().get(0));
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
                removeEntity();
            }

            @Override
            protected String getItemToDelete() {
                T t = getSelectedItem();
                return FormatUtils.formatEntity(getEntityModel(), t);
            }

        };
        rb.setIcon(VaadinIcon.TRASH.create());
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
        // construct grid and set properties
        disableGridSorting();
        getGridWrapper().getGrid().setHeight(getGridHeight());
        getGridWrapper().getGrid().setSelectionMode(isMultiSelect() ? SelectionMode.MULTI : SelectionMode.SINGLE);

        // add a listener to respond to the selection of an item
        getGridWrapper().getGrid().addSelectionListener(e -> {
            select(getGridWrapper().getGrid().getSelectedItems());
            checkComponentState(getSelectedItem());
        });

        // select item by double clicking on row (disable this inside pop-up
        // windows)
        if (getFormOptions().isDetailsModeEnabled() && getFormOptions().isDoubleClickSelectAllowed()) {
            getGridWrapper().getGrid().addItemDoubleClickListener(event -> {
                select(event.getItem());
                doEdit();
            });
        }
    }

    /**
     * Sets the provided component as the current detail view
     * 
     * @param root the root component of the custom detail view
     */
    protected final void customDetailView(Component root) {
        removeAll();
        add(root);
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

    protected List<SerializablePredicate<T>> getDefaultFilters() {
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
        return null;
    }

    /**
     * 
     * @return the total number of configured filters
     */
    public int getFilterCount() {
        return getSearchForm().getFilterCount();
    }

    /**
     * Callback method that is used to set the icon for a tab if complex detail mode
     * is enabled
     * 
     * @param index the index of the tab
     * @return
     */
    protected Icon getIconForTab(int index) {
        // overwrite in subclasses
        return null;
    }

    public VerticalLayout getMainSearchLayout() {
        return mainSearchLayout;
    }

    public QueryType getQueryType() {
        return queryType;
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

    /**
     * Checks if a filter is set for a certain attribute
     * 
     * @param path the path to the attribute
     * @return <code>true</code> if a filter for the specified attribute has been
     *         set and <code>false</code> otherwise
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
        return Objects.equals(getComponentAt(0), mainSearchLayout);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        build();
    }

    /**
     * Post-processes the button bar that appears below the search form
     * 
     * @param buttonBar the button bar
     */
    public void postProcessSearchButtonBar(FlexLayout buttonBar) {
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
     * Reloads the entire component, reverting to search mode and clearing the
     * search form
     */
    @Override
    public void reload() {
        removeAll();
        add(mainSearchLayout);
        getSearchForm().clear();
        search();
    }

    /**
     * Reloads the details view only
     */
    public void reloadDetails() {
        this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getDetailJoins()));
        detailsMode(getSelectedItem());
    }

    /**
     * Performs the actual delete action
     */
    protected final void removeEntity() {
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
            getGridWrapper().getGrid().deselectAll();
            setSelectedItem(null);
        }
    }

    /**
     * Puts the screen in search mode (does not reset the search form)
     */
    public void searchMode() {
        removeAll();
        add(mainSearchLayout);

        getSearchForm().refresh();
        search();
    }

    /**
     * Select one or more items
     * 
     * @param selectedItems the item or items to select
     */
    @SuppressWarnings("unchecked")
    public void select(Object selectedItems) {
        if (selectedItems != null) {
            if (selectedItems instanceof Collection<?>) {
                // the lazy query container returns an array of IDs of the
                // selected items

                Collection<?> col = (Collection<?>) selectedItems;
                if (col.size() == 1) {
                    T t = (T) col.iterator().next();
                    setSelectedItem(getService().fetchById(t.getId(), getDetailJoins()));
                    this.selectedItems = Lists.newArrayList(getSelectedItem());
                } else if (col.size() > 1) {
                    // deal with the selection of multiple items
                    List<ID> ids = Lists.newArrayList();
                    for (Object c : col) {
                        ids.add(((T) c).getId());
                    }
                    this.selectedItems = getService().fetchByIds(ids, getDetailJoins());
                }
            } else {
                // single item has been selected
                T t = (T) selectedItems;
                setSelectedItem(getService().fetchById(t.getId(), getDetailJoins()));
            }
        } else {
            setSelectedItem(null);
        }
    }

    /**
     * Sets the default filters that are always applied to a search query (even
     * after all search fields have been cleared)
     *
     * @param defaultFilters the default filters
     */
    public void setDefaultFilters(List<SerializablePredicate<T>> defaultFilters) {
        this.defaultFilters = defaultFilters;
        if (searchForm != null) {
            searchForm.setDefaultFilters(defaultFilters);
        }
    }

    /**
     * Sets a predefined search value
     * 
     * @param propertyId the name of the property for which to set a value
     * @param value      the value
     */
    public abstract void setSearchValue(String propertyId, Object value);

    /**
     * Sets a predefined search value (upper and lower bound)
     * 
     * @param propertyId the name of the property for which to set a value
     * @param value      the value (lower bound)
     * @param auxValue   the auxiliary value (upper bound)
     */
    public abstract void setSearchValue(String propertyId, Object value, Object auxValue);

    /**
     * Validate before a search is carried out - if the search criteria are not
     * correctly set, throw an OCSValidationException to abort the search process
     */
    public void validateBeforeSearch() {
        // overwrite in subclasses
    }
}
