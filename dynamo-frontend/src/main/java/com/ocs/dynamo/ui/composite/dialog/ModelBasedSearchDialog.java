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
package com.ocs.dynamo.ui.composite.dialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleSearchLayout;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A dialog that contains a search form based on the Entity Model
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity to search for
 */
public class ModelBasedSearchDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends SimpleModalDialog {

    private static final long serialVersionUID = -7158664165266474097L;

    /**
     * The (default) filters to apply to any search
     */
    private List<SerializablePredicate<T>> filters;

    /**
     * The entity model
     */
    private EntityModel<T> entityModel;

    /**
     * The optional joins that determine which related data to fetch
     */
    private FetchJoinInformation[] joins;

    /**
     * Indicates whether the dialog is in multiple select mode
     */
    private boolean multiSelect;

    /**
     * Whether to immediately perform a search
     */
    private boolean searchImmediately;

    /**
     * The actual search layout
     */
    private SimpleSearchLayout<ID, T> searchLayout;

    /**
     * The service used for querying the database
     */
    private BaseService<ID, T> service;

    /**
     * The sort order
     */
    private List<SortOrder<?>> sortOrders = new ArrayList<>();

    /**
     * Constructor
     * 
     * @param service           the service used to query the database
     * @param entityModel       the entity model
     * @param filters           the search filters
     * @param sortOrders        the sort orders
     * @param multiSelect       whether multiple selection is allowed
     * @param searchImmediately whether to search immediately after the screen is
     *                          opened
     * @param joins             the fetch joins
     */
    public ModelBasedSearchDialog(BaseService<ID, T> service, EntityModel<T> entityModel, List<SerializablePredicate<T>> filters,
            List<SortOrder<?>> sortOrders, boolean multiSelect, boolean searchImmediately, FetchJoinInformation... joins) {
        super(true);
        this.service = service;
        this.entityModel = entityModel;
        this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
        this.filters = filters;
        this.multiSelect = multiSelect;
        this.joins = joins;
        this.searchImmediately = searchImmediately;
    }

    @Override
    protected void doBuild(VerticalLayout parent) {
        FormOptions formOptions = new FormOptions().setReadOnly(true).setPopup(true).setDetailsModeEnabled(false)
                .setSearchImmediately(searchImmediately);

        searchLayout = new SimpleSearchLayout<>(service, entityModel, QueryType.ID_BASED, formOptions, null, joins);
        searchLayout.setPadding(true);
        searchLayout.setDefaultFilters(filters);

        searchLayout.setGridHeight(SystemPropertyUtils.getDefaultSearchDialogGridHeight());
        for (SortOrder<?> order : sortOrders) {
            searchLayout.addSortOrder(order);
        }
        searchLayout.setMultiSelect(multiSelect);

        // add double click listener for quickly selecting item and closing the
        // dialog
        searchLayout.getGridWrapper().getGrid().addItemDoubleClickListener(event -> {
            select(event.getItem());
            getOkButton().click();
        });
        parent.add(searchLayout);
    }

    public List<SerializablePredicate<T>> getFilters() {
        return filters;
    }

    public SimpleSearchLayout<ID, T> getSearchLayout() {
        return searchLayout;
    }

    protected T getSelectedItem() {
        return searchLayout.getSelectedItem();
    }

    protected Collection<T> getSelectedItems() {
        return searchLayout.getSelectedItems();
    }

    @Override
    protected String getTitle() {
        return message("ocs.search.title", entityModel.getDisplayNamePlural(VaadinUtils.getLocale()));
    }

    /**
     * Select one or more items in the grid
     * 
     * @param selectedItems
     */
    @SuppressWarnings("unchecked")
    public void select(Object selectedItems) {
        if (selectedItems instanceof Collection) {
            Collection<T> col = (Collection<T>) selectedItems;
            for (T t : col) {
                searchLayout.getGridWrapper().getGrid().select(t);
            }
        } else {
            T t = (T) selectedItems;
            searchLayout.getGridWrapper().getGrid().select(t);
        }
    }

    public void setFilters(List<SerializablePredicate<T>> filters) {
        this.filters = filters;
        if (searchLayout != null) {
            searchLayout.setDefaultFilters(filters);
        }
    }

}
