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
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Base class for search layouts. A search layout consists of a search form with
 * a results grid below it. From the result grid the user can select an entity
 * and then navigate to a detail screen for managing that entity
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T> the type of the entity
 */
public abstract class AbstractSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCollectionLayout<ID, T> {

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
    private List<SerializablePredicate<T>> defaultFilters;

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

    /**
     * Button for selecting the next item
     */
    private Button nextButton;

    /**
     * Button for selecting the previous item
     */
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
     * The layout that contains the grid that contains the search results
     */
    private VerticalLayout searchResultsLayout;

    /**
     * The selected detail layout
     */
    private Component selectedDetailLayout;

    /**
     * The currently selected items in the search results grid
     */
    private Collection<T> selectedItems;

    /**
     * The layout that holds the tab sheet when the component is in complex details
     * mode
     */
    private VerticalLayout tabContainerLayout;

    /**
     * Tabbed layout for complex detail mode
     */
    private LazyTabLayout<ID, T> tabLayout;

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

    /**
     * Method that is called after the user clicks the Clear button to empty the
     * search form
     */
    protected void afterClear() {
        // overwrite in subclasses
    }

    /**
     * Method that is called after the user clicks the "show/hide" button to
     * show/hide the search form
     * 
     * @param visible whether the search fields are visible after the toggle
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

    /**
     * Perform any actions that are necessary before carrying out a search. Can be
     * used to interfere with the search process
     * 
     * @param filter the current search filter
     * @return the modified search filter. If not null, then this filter will be
     *         used for the search instead of the current filter
     */
    protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
        // overwrite in subclasses if needed
        return null;
    }

    /**
     * Lazily constructs the screen
     */
    @Override
    public void build() {
        if (mainSearchLayout == null) {
            mainSearchLayout = new DefaultVerticalLayout(false, true);

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
                        getSearchForm().setAfterClearConsumer(e -> {
                            clearIfNotSearchingImmediately();
                        });
                    } else {
                        // clear right away
                        getSearchForm().getClearButton().addClickListener(e -> {
                            clearIfNotSearchingImmediately();
                        });
                    }
                } else {
                    // clear current selection and update buttons
                    if (getFormOptions().isConfirmClear()) {
                        getSearchForm().setAfterClearConsumer(e -> {
                            setSelectedItem(null);
                            checkButtonState(getSelectedItem());
                            afterClear();
                        });
                    } else {
                        getSearchForm().getClearButton().addClickListener(e -> {
                            setSelectedItem(null);
                            checkButtonState(getSelectedItem());
                            afterClear();
                        });
                    }
                }
            }

            searchResultsLayout = new DefaultVerticalLayout(false, false);
            mainSearchLayout.add(searchResultsLayout);

            if (getFormOptions().isSearchImmediately()) {
                // immediately construct the search results grid
                searchResultsLayout.add(getGridWrapper());
            } else {
                // do not construct the search results grid yet
                Text noSearchYetLabel = new Text(message("ocs.no.search.yet"));
                searchResultsLayout.add(noSearchYetLabel);

                // click listener that will construct search results grid on demand
                getSearchForm().getSearchButton().addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
                if (getSearchForm().getSearchAnyButton() != null) {
                    getSearchForm().getSearchAnyButton().addClickListener(e -> constructLayoutIfNeeded(noSearchYetLabel));
                }
            }
            // clear currently selected item and update buttons
            if (getSearchForm().getSearchButton() != null) {
                getSearchForm().getSearchButton().addClickListener(e -> {
                    setSelectedItem(null);
                    checkButtonState(getSelectedItem());
                });
            }

            // add button
            addButton = constructAddButton();
            getButtonBar().add(addButton);

            // edit/view button
            editButton = constructEditButton();
            registerComponent(editButton);
            getButtonBar().add(editButton);

            // remove button
            removeButton = constructRemoveButton();
            registerComponent(removeButton);
            getButtonBar().add(removeButton);

            // callback for adding additional buttons
            postProcessButtonBar(getButtonBar());
            mainSearchLayout.add(getButtonBar());

            checkButtonState(null);

            // post process the layout
            postProcessLayout(mainSearchLayout);
            add(mainSearchLayout);
        }
    }

    /**
     * Builds a tab layout for the display view. The definition of the tabs has to
     * be done in the subclasses
     * 
     * @param entity the currently selected entity
     */
    protected final void buildDetailsTabLayout(T entity, FormOptions formOptions) {
        tabContainerLayout = new DefaultVerticalLayout(false, true);

        HorizontalLayout buttonBar = new DefaultHorizontalLayout(false, true);
        tabContainerLayout.add(buttonBar);

        complexDetailModeBackButton = new Button(message("ocs.back"));
        complexDetailModeBackButton.setIcon(VaadinIcon.BACKWARDS.create());
        complexDetailModeBackButton.addClickListener(e -> searchMode());
        buttonBar.add(complexDetailModeBackButton);

        if (getFormOptions().isShowPrevButton()) {
            prevButton = new Button(message("ocs.previous"));
            prevButton.addClickListener(e -> {
                T prev = getPreviousEntity();
                if (prev != null) {
                    tabLayout.setEntity(prev, getFormOptions().isPreserveSelectedTab());
                    tabLayout.reload();
                } else {
                    prevButton.setEnabled(false);
                }
                if (nextButton != null) {
                    nextButton.setEnabled(true);
                }
            });
            prevButton.setEnabled(hasPrevEntity());
            buttonBar.add(prevButton);
        }

        if (getFormOptions().isShowNextButton()) {
            nextButton = new Button(message("ocs.next"));
            nextButton.addClickListener(e -> {
                T next = getNextEntity();
                if (next != null) {
                    tabLayout.setEntity(next, getFormOptions().isPreserveSelectedTab());
                    tabLayout.reload();
                } else {
                    nextButton.setEnabled(false);
                }
                if (prevButton != null) {
                    prevButton.setEnabled(true);
                }
            });
            nextButton.setEnabled(hasNextEntity());
            buttonBar.add(nextButton);
        }

        tabLayout = new LazyTabLayout<ID, T>(entity) {

            private static final long serialVersionUID = 1278134557026074688L;

            @Override
            protected String createTitle() {
                return AbstractSearchLayout.this.getDetailModeTabTitle();
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
                return AbstractSearchLayout.this.constructComplexDetailModeTab(getEntity(), index, formOptions, false);
            }

            @Override
            protected Icon getIconForTab(int index) {
                return AbstractSearchLayout.this.getIconForTab(index);
            }
        };
        tabLayout.build();
        tabContainerLayout.add(tabLayout);
    }

    /**
     * Builds the edit form
     * 
     * @param entity  the currently selected entity
     * @param options the form options
     */
    protected final void buildEditForm(T entity, FormOptions options) {
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
                    if (cancel || newObject || (!getFormOptions().isShowNextButton() && !getFormOptions().isShowPrevButton())) {
                        back();
                    }
                }
            }

            @Override
            protected void afterEntitySet(T entity) {
                AbstractSearchLayout.this.afterEntitySet(entity);
            }

            @Override
            protected void afterEntitySelected(T entity) {
                AbstractSearchLayout.this.afterEntitySelected(editForm, entity);
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
            protected Converter<String, ?> constructCustomConverter(AttributeModel am) {
                return AbstractSearchLayout.this.constructCustomConverter(am);
            }

            @Override
            protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel, boolean viewMode) {
                return AbstractSearchLayout.this.constructCustomField(entityModel, attributeModel, viewMode, false);
            }

            @Override
            protected T getNextEntity() {
                return AbstractSearchLayout.this.getNextEntity();
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
            protected T getPreviousEntity() {
                return AbstractSearchLayout.this.getPreviousEntity();
            }

            @Override
            protected boolean handleCustomException(RuntimeException ex) {
                return AbstractSearchLayout.this.handleCustomException(ex);
            }

            @Override
            protected boolean hasNextEntity() {
                return AbstractSearchLayout.this.hasNextEntity();
            }

            @Override
            protected boolean hasPrevEntity() {
                return AbstractSearchLayout.this.hasPrevEntity();
            }

            @Override
            protected boolean isEditAllowed() {
                return AbstractSearchLayout.this.isEditAllowed();
            }

            @Override
            protected void postProcessButtonBar(FlexLayout buttonBar, boolean viewMode) {
                AbstractSearchLayout.this.postProcessDetailButtonBar(buttonBar, viewMode);
            }

            @Override
            protected void postProcessEditFields() {
                AbstractSearchLayout.this.postProcessEditFields(editForm);
            }

        };
        editForm.setCustomSaveConsumer(getCustomSaveConsumer());
        editForm.setSupportsIteration(true);
        editForm.setDetailJoins(getDetailJoins());
        editForm.setFieldEntityModels(getFieldEntityModels());
        editForm.build();
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
        checkButtonState(getSelectedItem());
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
    protected Component constructComplexDetailModeTab(T entity, int index, FormOptions fo, boolean newEntity) {
        // overwrite is subclasses
        return null;
    }

    /*
     * Constructs the button that will switch the screen to the detail view.
     * Depending on the "open in view mode" setting the caption will read either
     * "view" or "edit"
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

    /**
     * Lazily constructs the grid wrapper
     */
    @Override
    public ServiceBasedGridWrapper<ID, T> constructGridWrapper() {
        ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<ID, T>(this.getService(), getEntityModel(), getQueryType(),
                getFormOptions(), getSearchForm().extractFilter(), getFieldFilters(), getSortOrders(), false, getJoins()) {

            private static final long serialVersionUID = 6343267378913526151L;

            @Override
            protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
                return AbstractSearchLayout.this.beforeSearchPerformed(filter);
            }

            @Override
            protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
                AbstractSearchLayout.this.postProcessDataProvider(provider);
            }

        };
        postConfigureGridWrapper(wrapper);
        wrapper.setMaxResults(getMaxResults());
        wrapper.build();

        if (getFormOptions().isSearchImmediately()) {
            getSearchForm().setSearchable(wrapper);
        }

        return wrapper;
    }

    /**
     * Constructs a search layout in response to a click on any of the search
     * buttons
     * 
     * @param noSearchYetLabel the label used to indicate that there are no search
     *                         results yet
     */
    private final void constructLayoutIfNeeded(Text noSearchYetLabel) {
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
        getGridWrapper().getGrid().setHeight(getGridHeight() + "px");
        getGridWrapper().getGrid().setSelectionMode(isMultiSelect() ? SelectionMode.MULTI : SelectionMode.SINGLE);

        // add a listener to respond to the selection of an item
        getGridWrapper().getGrid().addSelectionListener(e -> {
            select(getGridWrapper().getGrid().getSelectedItems());
            checkButtonState(getSelectedItem());
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
     * Opens a custom detail view
     * 
     * @param root the root component of the custom detail view
     */
    protected final void customDetailView(Component root) {
        removeAll();
        add(root);
    }

    /**
     * Open the screen in details mode
     * 
     * @param entity the entity to display
     */
    @Override
    protected void detailsMode(T entity) {

        if (mainEditLayout == null) {
            mainEditLayout = new DefaultVerticalLayout(false, true);
        }

        FormOptions copy = new FormOptions();
        copy.setOpenInViewMode(getFormOptions().isOpenInViewMode());
        copy.setScreenMode(ScreenMode.VERTICAL);
        copy.setAttributeGroupMode(getFormOptions().getAttributeGroupMode());
        copy.setPreserveSelectedTab(getFormOptions().isPreserveSelectedTab());
        copy.setShowNextButton(getFormOptions().isShowNextButton());
        copy.setShowPrevButton(getFormOptions().isShowPrevButton());
        copy.setPlaceButtonBarAtTop(getFormOptions().isPlaceButtonBarAtTop());
        copy.setFormNested(true);
        copy.setConfirmSave(getFormOptions().isConfirmSave());

        // set the form options for the detail form
        if (getFormOptions().isEditAllowed()) {
            // editing in form must be possible
            copy.setEditAllowed(true);
        } else {
            // read-only mode
            copy.setOpenInViewMode(true).setEditAllowed(false);
        }

        if (copy.isOpenInViewMode() || !isEditAllowed()) {
            copy.setShowBackButton(true);
        }

        if (getFormOptions().isComplexDetailsMode() && entity != null && entity.getId() != null) {
            // complex tab layout, back button is placed separately
            copy.setShowBackButton(false);
            copy.setHideCancelButton(true);

            if (tabContainerLayout == null) {
                buildDetailsTabLayout(entity, copy);
            } else {
                tabLayout.setEntity(entity, getFormOptions().isPreserveSelectedTab());
                tabLayout.reload();
            }
            if (selectedDetailLayout == null) {
                mainEditLayout.add(tabContainerLayout);
            } else {
                mainEditLayout.replace(selectedDetailLayout, tabContainerLayout);
            }
            selectedDetailLayout = tabContainerLayout;
        } else if (!getFormOptions().isComplexDetailsMode()) {
            // simple edit form
            if (editForm == null) {
                buildEditForm(entity, copy);
            } else {
                editForm.setViewMode(copy.isOpenInViewMode());
                editForm.setEntity(entity);
                editForm.resetTabsheetIfNeeded();
            }
            if (selectedDetailLayout == null) {
                mainEditLayout.add(editForm);
            } else {
                mainEditLayout.replace(selectedDetailLayout, editForm);
            }
            selectedDetailLayout = editForm;
        } else {
            // complex details mode for creating a new entity, re-use the first tab
            Component comp = constructComplexDetailModeTab(entity, 0, getFormOptions(), true);

            if (selectedDetailLayout == null) {
                mainEditLayout.add(comp);
            } else {
                mainEditLayout.replace(selectedDetailLayout, comp);
            }
            selectedDetailLayout = comp;
        }

        checkButtonState(getSelectedItem());
        removeAll();
        add(mainEditLayout);
    }

    /**
     * Opens the screen in details mode and selects a certain tab
     *
     * @param entity      the entity to display
     * @param selectedTab the currently selected tab
     */
    protected void detailsMode(T entity, int selectedTabIndex) {
        detailsMode(entity);
        if (editForm != null) {
            editForm.selectTab(selectedTabIndex);
        } else if (getFormOptions().isComplexDetailsMode()) {
            tabLayout.selectTab(selectedTabIndex);
        }
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
     * @param entity     the entity to select
     * @param initialTab the index of the tab to display
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

    /**
     * Returns the resource that is to be used as an icon for a tab in a tab sheet
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

    /**
     * Returns the next item that is available in the data provider
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected final T getNextEntity() {
        BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
        ID nextId = provider.getNextItemId();
        T next = null;
        if (nextId != null) {
            next = getService().fetchById(nextId, getDetailJoins());
            getGridWrapper().getGrid().select(next);
        }
        return next;
    }

    /**
     * Returns the previous entity that is available in the data provider
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected final T getPreviousEntity() {
        BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
        ID prevId = provider.getPreviousItemId();
        T prev = null;
        if (prevId != null) {
            prev = getService().fetchById(prevId, getDetailJoins());
            getGridWrapper().getGrid().select(prev);
        }
        return prev;
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

    /**
     * Check whether the data provider contains a next item
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected boolean hasNextEntity() {
        BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
        return provider.hasNextItemId();
    }

    /**
     * Check whether the data provider contains a previous item
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected boolean hasPrevEntity() {
        BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
        return provider.hasPreviousItemId();
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
     * Refreshes the contents of a label inside the edit form
     * 
     * @param propertyName the name of the property for which to refresh the label
     */
    public void setLabelValue(String propertyName, String value) {
        if (editForm != null) {
            editForm.setLabelValue(propertyName, value);
        }
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
     * @param defaultFilters
     */
    public void setDefaultFilters(List<SerializablePredicate<T>> defaultFilters) {
        this.defaultFilters = defaultFilters;
        if (searchForm != null) {
            searchForm.setDefaultFilters(defaultFilters);
        }
    }

    /**
     * Sets the tab specified by the provided index to the provided visibility (for
     * use in complex details mode)
     *
     * @param index   the index
     * @param visible whether the tabs must be visible
     */
    public void setDetailsTabVisible(int index, boolean visible) {
        if (tabLayout != null) {
            tabLayout.setTabVisible(index, visible);
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

    @Override
    @SuppressWarnings("unchecked")
    public void setSelectedItem(T selectedItem) {
        super.setSelectedItem(selectedItem);
        // communicate selected item ID to provider
        BaseDataProvider<ID, T> provider = (BaseDataProvider<ID, T>) getGridWrapper().getDataProvider();
        provider.setCurrentlySelectedId(selectedItem == null ? null : selectedItem.getId());
        if (prevButton != null) {
            prevButton.setEnabled(hasPrevEntity());
        }
        if (nextButton != null) {
            nextButton.setEnabled(hasNextEntity());
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
