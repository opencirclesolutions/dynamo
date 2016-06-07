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
import java.util.Map;

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.component.DefaultVerticalLayout;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedEditForm;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.composite.type.ScreenMode;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
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
public class SimpleSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseCollectionLayout<ID, T> {

    private static final long serialVersionUID = 4606800218149558500L;

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
    private VerticalLayout mainSearchLayout;

    // the number of columns in the search form
    private int nrOfColumns = 1;

    // the query type (paging or id-based)
    private QueryType queryType;

    // the button that is used to remove an item - disabled by default, must be
    // explicitly set to visible
    private Button removeButton;

    // the search form
    private ModelBasedSearchForm<ID, T> searchForm;

    // the set of currently selected items
    private Collection<T> selectedItems;

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
     * Responds to the toggling of the visibility of the search fields
     * 
     * @param visible
     *            whether the search fields are now visible
     */
    protected void afterSearchFieldToggle(boolean visible) {
        // overwrite in subclasses
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
        if (mainSearchLayout == null) {
            mainSearchLayout = new DefaultVerticalLayout();

            // construct table and set properties
            getTableWrapper().getTable().setPageLength(getPageLength());
            getTableWrapper().getTable().setSortEnabled(isSortEnabled());

            // add a listener to respond to the selection of an item
            getTableWrapper().getTable().addValueChangeListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    select(getTableWrapper().getTable().getValue());
                    checkButtonState(getSelectedItem());
                }
            });

            mainSearchLayout.addComponent(getSearchForm());
            mainSearchLayout.addComponent(getTableWrapper());

            // add button
            addButton = constructAddButton();
            if (addButton != null) {
                getButtonBar().addComponent(addButton);
            }

            // edit/view button
            editButton = constructEditButton();
            if (editButton != null) {
                registerDetailButton(editButton);
                getButtonBar().addComponent(editButton);
            }

            // remove button
            removeButton = constructRemoveButton();
            if (removeButton != null) {
                registerDetailButton(removeButton);
                getButtonBar().addComponent(removeButton);
            }

            // callback for adding additional buttons
            postProcessButtonBar(getButtonBar());
            mainSearchLayout.addComponent(getButtonBar());

            constructTableDividers();
            postProcessLayout(mainSearchLayout);
        }
        setCompositionRoot(mainSearchLayout);
    }

    /**
     * Constructs the edit button - this will display the details view when clicked
     * 
     * @return
     */
    protected Button constructEditButton() {
        // edit button
        Button eb = new Button(getFormOptions().isOpenInViewMode() ? message("ocs.view")
                : message("ocs.edit"));
        eb.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (getSelectedItem() != null) {
                    doEdit();
                }
            }
        });

        // show button if editing is allowed or if detail screen opens in view mode
        eb.setVisible(getFormOptions().isShowEditButton()
                && (isEditAllowed() || getFormOptions().isOpenInViewMode()));
        return eb;
    }

    /**
     * Constructs the remove button
     * 
     * @return
     */
    protected Button constructRemoveButton() {
        Button rb = new RemoveButton() {

            @Override
            protected void doDelete() {
                remove();
            }

        };
        rb.setVisible(isEditAllowed() && getFormOptions().isShowRemoveButton());
        return rb;
    }

    /**
     * Lazily constructs the search form
     * 
     * @return
     */
    protected ModelBasedSearchForm<ID, T> constructSearchform() {
        ModelBasedSearchForm<ID, T> result = new ModelBasedSearchForm<ID, T>(getTableWrapper(),
                getEntityModel(), getFormOptions(), this.additionalFilters, this.fieldFilters) {

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
        result.setNrOfColumns(getNrOfColumns());
        result.setFieldEntityModels(getFieldEntityModels());
        result.build();

        return result;
    }

    /**
     * Lazily constructs the table wrapper
     */
    @Override
    public ServiceResultsTableWrapper<ID, T> constructTableWrapper() {
        ServiceResultsTableWrapper<ID, T> result = new ServiceResultsTableWrapper<ID, T>(
                this.getService(), getEntityModel(), getQueryType(), null, getSortOrders(),
                getJoins());
        result.build();
        return result;
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
            mainEditLayout.setStyleName(DynamoConstants.CSS_CLASS_HALFSCREEN);

            // set the form options for the detail form
            FormOptions options = new FormOptions();
            options.setOpenInViewMode(getFormOptions().isOpenInViewMode());
            options.setScreenMode(ScreenMode.VERTICAL);

            if (options.isOpenInViewMode()) {
                options.setShowBackButton(true);
                options.setShowEditButton(true);
            }

            editForm = new ModelBasedEditForm<ID, T>(entity, getService(), getEntityModel(),
                    options, fieldFilters) {

                @Override
                protected void afterEditDone(boolean cancel, boolean newObject, T entity) {
                    if (getFormOptions().isOpenInViewMode()) {
                        // if details screen opens in view mode, simply switch to view mode
                        setViewMode(true);
                        detailsMode(entity);
                    } else {
                        // otherwise go back to the main screen
                        back();
                    }
                }

                @Override
                protected void afterModeChanged(boolean viewMode) {
                    SimpleSearchLayout.this.afterModeChanged(viewMode, editForm);
                }

                @Override
                protected void back() {
                    setCompositionRoot(mainSearchLayout);
                    search();
                }

                @Override
                protected Field<?> constructCustomField(EntityModel<T> entityModel,
                        AttributeModel attributeModel, boolean viewMode) {
                    return SimpleSearchLayout.this.constructCustomField(entityModel,
                            attributeModel, true, false);
                }

                @Override
                protected boolean isEditAllowed() {
                    return SimpleSearchLayout.this.isEditAllowed();
                }

                @Override
                protected void postProcessButtonBar(HorizontalLayout buttonBar, boolean viewMode) {
                    SimpleSearchLayout.this.postProcessDetailButtonBar(buttonBar, viewMode);
                }

                @Override
                protected void postProcessEditFields() {
                    SimpleSearchLayout.this.postProcessEditFields(editForm);
                }

            };
            editForm.setFieldEntityModels(getFieldEntityModels());
            editForm.build();
            mainEditLayout.addComponent(editForm);
        } else {
            editForm.setViewMode(getFormOptions().isOpenInViewMode());
            editForm.setEntity(entity);
        }

        checkButtonState(getSelectedItem());
        afterDetailSelected(editForm, entity);
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

    public int getNrOfColumns() {
        return nrOfColumns;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    protected ModelBasedSearchForm<ID, T> getSearchForm() {
        if (searchForm == null) {
            searchForm = constructSearchform();
        }
        return searchForm;
    }

    public Collection<T> getSelectedItems() {
        return selectedItems;
    }

    /**
     * Reloads the details view only
     */
    public void reloadDetails() {
        this.setSelectedItem(getService().fetchById(this.getSelectedItem().getId(), getJoins()));
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
        checkButtonState(getSelectedItem());
        search();
    }

    /**
     * Perform the actual search
     */
    public void search() {
        getSearchForm().search();
        getTableWrapper().getTable().select(null);
        setSelectedItem(null);
        checkButtonState(getSelectedItem());
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

    public void setNrOfColumns(int nrOfColumns) {
        this.nrOfColumns = nrOfColumns;
    }

    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

}
