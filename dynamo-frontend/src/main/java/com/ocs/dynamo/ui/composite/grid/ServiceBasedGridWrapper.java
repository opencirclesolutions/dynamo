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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.provider.IdBasedDataProvider;
import com.ocs.dynamo.ui.provider.PagingDataProvider;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A wrapper for a grid that retrieves its data directly from the database
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key of the entity
 * @param <T>  type of the entity
 */
public class ServiceBasedGridWrapper<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseGridWrapper<ID, T>
        implements Searchable<T> {

    private static final long serialVersionUID = -4691108261565306844L;

    /**
     * The maximum number of results
     */
    private Integer maxResults;

    /**
     * @param service     the service that is used for retrieving data
     * @param entityModel the entity model
     * @param queryType   the query type to use
     * @param order       the default sort order
     * @param joins       options list of fetch joins to include in the query
     */
    public ServiceBasedGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType, FormOptions formOptions,
            SerializablePredicate<T> filter, Map<String, SerializablePredicate<?>> fieldFilters, List<SortOrder<?>> sortOrders,
            boolean editable, FetchJoinInformation... joins) {
        super(service, entityModel, queryType, formOptions, filter, fieldFilters, sortOrders, editable, joins);
    }

    @Override
    protected DataProvider<T, SerializablePredicate<T>> constructDataProvider() {
        BaseDataProvider<ID, T> provider;
        if (QueryType.PAGING.equals(getQueryType())) {
            provider = new PagingDataProvider<>(getService(), getEntityModel(),
                    getFormOptions().isShowNextButton() || getFormOptions().isShowPrevButton(), getJoins());
        } else {
            provider = new IdBasedDataProvider<>(getService(), getEntityModel(), getJoins());
        }
        provider.setMaxResults(maxResults);
        provider.setAfterCountCompleted(x -> updateCaption(x));
        postProcessDataProvider(provider);

        return provider;
    }

    /**
     * Forces a search
     */
    public void forceSearch() {
        getDataProvider().size(new Query<T, SerializablePredicate<T>>(getFilter()));
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    @Override
    protected List<SortOrder<?>> initSortingAndFiltering() {
        List<SortOrder<?>> fallBacks = super.initSortingAndFiltering();
        // sets the initial filter
        getGrid().getDataCommunicator().setDataProvider(getDataProvider(), getFilter());
        getGrid().addSelectionListener(event -> onSelect(getGrid().getSelectedItems()));

        // right click to download
        if (getFormOptions().isExportAllowed() && getExportDelegate() != null) {

            GridContextMenu<T> contextMenu = getGrid().addContextMenu();
            Button downloadButton = new Button(message("ocs.download"));
            downloadButton.addClickListener(event -> {
                List<SortOrder<?>> orders = new ArrayList<>();
                List<GridSortOrder<T>> so = getGrid().getSortOrder();
                for (GridSortOrder<T> gso : so) {
                    orders.add(new SortOrder<String>(gso.getSorted().getKey(), gso.getDirection()));
                }
                getExportDelegate().export(getExportEntityModel() != null ? getExportEntityModel() : getEntityModel(),
                        getFormOptions().getExportMode(), getFilter(), !orders.isEmpty() ? orders : getSortOrders(),
                        getExportJoins() != null ? getExportJoins() : getJoins());

            });
            contextMenu.add(downloadButton);
        }
        return fallBacks;
    }

    @Override
    public void reloadDataProvider() {
        search(getFilter());
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int getDataProviderSize() {
        return ((BaseDataProvider<ID, T>) getGrid().getDataCommunicator().getDataProvider()).getSize();
    }

}
