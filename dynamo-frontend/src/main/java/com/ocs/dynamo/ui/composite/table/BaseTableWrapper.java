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
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * A base class for objects that wrap around a ModelBasedTable
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public abstract class BaseTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCustomComponent implements Searchable {

    private static final long serialVersionUID = -4691108261565306844L;

    /**
     * Whether export of the table data is allowed
     */
    private boolean allowExport;

    /**
     * The container
     */
    private Container container;

    /**
     * The entity model used to create the container
     */
    private EntityModel<T> entityModel;

    /**
     * The fetch joins to use when querying
     */
    private FetchJoinInformation[] joins;

    /**
     * The type of the query
     */
    private final QueryType queryType;

    /**
     * The service used to query the database
     */
    private final BaseService<ID, T> service;

    /**
     * The sort orders
     */
    private List<SortOrder> sortOrders = new ArrayList<>();

    /**
     * The wrapped table component
     */
    private Table table;

    /**
     * Constructor
     * 
     * @param service
     *            the service used to query the repository
     * @param entityModel
     *            the entity model for the items that are displayed in the table
     * @param queryType
     *            the type of query
     * @param sortOrders
     *            the sort order
     * @param joins
     *            the fetch joins to use when executing the query
     */
    public BaseTableWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
            List<SortOrder> sortOrders, boolean allowExport, FetchJoinInformation... joins) {
        this.service = service;
        this.entityModel = entityModel;
        this.queryType = queryType;
        this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
        this.joins = joins;
        this.allowExport = allowExport;
    }

    /**
     * Perform any actions that are necessary before carrying out a search
     * 
     * @param filter
     */
    protected Filter beforeSearchPerformed(Filter filter) {
        // overwrite in subclasses
        return null;
    }

    /**
     * Builds the component.
     */
    @Override
    public void build() {
        VerticalLayout main = new VerticalLayout();

        this.container = constructContainer();

        // init the table
        table = getTable();
        table.setPageLength(DynamoConstants.PAGE_SIZE);
        initSortingAndFiltering();
        main.addComponent(table);

        // add a change listener that responds to the selection of an item
        table.addValueChangeListener(event -> onSelect(table.getValue()));
        setCompositionRoot(main);
    }

    /**
     * Creates the container that holds the data
     * 
     * @return the container
     */
    protected abstract Container constructContainer();

    /**
     * Constructs the table - override in subclasses if you need a different table implementation
     * 
     * @return
     */
    protected Table constructTable() {
        return new ModelBasedTable<>(this.container, entityModel, allowExport);
    }

    /**
     * Callback method used to modify container creation
     * 
     * @param container
     */
    protected void doConstructContainer(Container container) {
        // overwrite in subclasses
    }

    /**
     * 
     * @return the container that holds the data
     */
    public Container getContainer() {
        return container;
    }

    /**
     * @return the entityModel
     */
    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public FetchJoinInformation[] getJoins() {
        return joins;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public BaseService<ID, T> getService() {
        return service;
    }

    /**
     * Extracts the sort directions from the sort orders
     */
    protected boolean[] getSortDirections() {
        boolean[] result = new boolean[getSortOrders().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = SortDirection.ASCENDING == getSortOrders().get(i).getDirection();
        }
        return result;
    }

    /**
     * 
     * @return the sort orders
     */
    public List<SortOrder> getSortOrders() {
        return Collections.unmodifiableList(sortOrders);
    }

    /**
     * Extracts the properties to sort on from the sort orders
     */
    protected Object[] getSortProperties() {
        Object[] result = new Object[getSortOrders().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getSortOrders().get(i).getPropertyId();
        }
        return result;
    }

    /**
     * Lazily construct and return the table
     * 
     * @return
     */
    public Table getTable() {
        if (table == null) {
            table = constructTable();
        }
        return table;
    }

    /**
     * Initializes the sorting and filtering for the table
     */
	public void initSortingAndFiltering() {
        if (getSortOrders() != null && !getSortOrders().isEmpty()) {
            table.sort(getSortProperties(), getSortDirections());
        } else if (getEntityModel().getSortOrder() != null && !getEntityModel().getSortOrder().keySet().isEmpty()) {
            // sort based on the entity model
            Set<AttributeModel> keySet = getEntityModel().getSortOrder().keySet();

            Object[] properties = new Object[keySet.size()];
            boolean[] dirs = new boolean[keySet.size()];

            int i = 0;
            for (AttributeModel am : entityModel.getSortOrder().keySet()) {
                properties[i] = am.getName();
                dirs[i] = entityModel.getSortOrder().get(am);
                i++;
            }
            table.sort(properties, dirs);
        }
    }

    /**
     * Respond to a selection of an item in the table
     */
    protected void onSelect(Object selected) {
        // override in subclass if needed
    }

    /**
     * Reloads the data in the container
     */
    public abstract void reloadContainer();

    public void setJoins(FetchJoinInformation[] joins) {
        this.joins = joins;
    }

    public void setSortOrders(List<SortOrder> sortOrders) {
        this.sortOrders = sortOrders;
    }

    protected void setTable(Table table) {
        this.table = table;
    }
}
