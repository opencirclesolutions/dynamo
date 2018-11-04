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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.GridSortOrderBuilder;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.VerticalLayout;

/**
 * A base class for objects that wrap around a ModelBasedTable
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
public abstract class BaseGridWrapper<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCustomComponent
		implements Searchable<T> {

	private static final long serialVersionUID = -4691108261565306844L;

	/**
	 * Whether export of the table data is allowed
	 */
	private boolean allowExport;

	/**
	 * The data provider
	 */
	private DataProvider<T, SerializablePredicate<T>> dataProvider;

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
	private List<SortOrder<?>> sortOrders = new ArrayList<>();

	/**
	 * The wrapped grid component
	 */
	private ModelBasedGrid<ID, T> grid;

	/**
	 * Constructor
	 * 
	 * @param service     the service used to query the repository
	 * @param entityModel the entity model for the items that are displayed in the
	 *                    table
	 * @param queryType   the type of query
	 * @param sortOrders  the sort order
	 * @param joins       the fetch joins to use when executing the query
	 */
	public BaseGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			List<SortOrder<?>> sortOrders, boolean allowExport, FetchJoinInformation... joins) {
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
	protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
		// overwrite in subclasses
		return null;
	}

	/**
	 * Builds the component.
	 */
	@Override
	public void build() {
		VerticalLayout main = new VerticalLayout();

		this.dataProvider = constructDataProvider();

		// init the grid
		grid = getGrid();
		initSortingAndFiltering();
		main.addComponent(grid);

		// add a change listener that responds to the selection of an item
		grid.addSelectionListener(event -> onSelect(grid.getSelectedItems()));

		setCompositionRoot(main);
	}

	/**
	 * Creates the container that holds the data
	 * 
	 * @return the container
	 */
	protected abstract DataProvider<T, SerializablePredicate<T>> constructDataProvider();

	/**
	 * Constructs the table - override in subclasses if you need a different table
	 * implementation
	 * 
	 * @return
	 */
	protected ModelBasedGrid<ID, T> constructGrid() {
		return new ModelBasedGrid<ID, T>(this.dataProvider, entityModel, allowExport, false);
	}

	/**
	 * Callback method used to modify data provider creation
	 * 
	 * @param container
	 */
	protected void doConstructDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		// overwrite in subclasses
	}

	public DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return dataProvider;
	}

	/**
	 * @return the entityModel
	 */
	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	/**
	 * Lazily construct and return the grid
	 * 
	 * @return
	 */
	public ModelBasedGrid<ID, T> getGrid() {
		if (grid == null) {
			grid = constructGrid();
		}
		return grid;
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
	public List<SortOrder<?>> getSortOrders() {
		return Collections.unmodifiableList(sortOrders);
	}

	/**
	 * Initializes the sorting and filtering for the grid
	 */
	protected void initSortingAndFiltering() {
		if (getSortOrders() != null && !getSortOrders().isEmpty()) {
			GridSortOrderBuilder<T> builder = new GridSortOrderBuilder<>();
			for (SortOrder<?> o : getSortOrders()) {
				if (SortDirection.ASCENDING.equals(o.getDirection())) {
					builder.thenAsc(grid.getColumn(o.getSorted().toString()));
				} else {
					builder.thenDesc(grid.getColumn(o.getSorted().toString()));
				}
			}
			grid.setSortOrder(builder);
		} else if (getEntityModel().getSortOrder() != null && !getEntityModel().getSortOrder().keySet().isEmpty()) {
			// sort based on the entity model
			GridSortOrderBuilder<T> builder = new GridSortOrderBuilder<>();
			for (AttributeModel am : entityModel.getSortOrder().keySet()) {
				boolean asc = entityModel.getSortOrder().get(am);
				if (asc) {
					builder.thenAsc(grid.getColumn(am.getName()));
				} else {
					builder.thenDesc(grid.getColumn(am.getName()));
				}
			}
			grid.setSortOrder(builder);
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
	public abstract void reloadDataProvider();

	public void setDataProvider(DataProvider<T, SerializablePredicate<T>> dataProvider) {
		this.dataProvider = dataProvider;
	}

	protected void setGrid(ModelBasedGrid<ID, T> grid) {
		this.grid = grid;
	}

	public void setJoins(FetchJoinInformation[] joins) {
		this.joins = joins;
	}

	public void setSortOrders(List<SortOrder<?>> sortOrders) {
		this.sortOrders = sortOrders;
	}
}
