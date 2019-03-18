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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.composite.export.ExportDelegate;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.BaseDataProvider;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.GridSortOrderBuilder;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Layout;
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
	 * The data provider
	 */
	private DataProvider<T, SerializablePredicate<T>> dataProvider;

	/**
	 * Whether the grid is editable using a row editor
	 */
	private boolean editable;

	/**
	 * The entity model used to create the container
	 */
	private EntityModel<T> entityModel;

	/**
	 * The entity model to use when exporting
	 */
	private EntityModel<T> exportEntityModel;

	/**
	 * The export service used for generating XLS and CSV exports
	 */
	private ExportDelegate exportDelegate = ServiceLocatorFactory.getServiceLocator().getService(ExportDelegate.class);

	/**
	 * Field filter map
	 */
	private Map<String, SerializablePredicate<?>> fieldFilters;

	/**
	 * The form options
	 */
	private FormOptions formOptions;

	/**
	 * The wrapped grid component
	 */
	private ModelBasedGrid<ID, T> grid;

	/**
	 * The fetch joins to use when querying
	 */
	private FetchJoinInformation[] joins;

	/**
	 * The joins to use when exporting (needed when using exportmode FULL)
	 */
	private FetchJoinInformation[] exportJoins;

	/**
	 * The layout that contains the grid
	 */
	private VerticalLayout layout;

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
	 * Constructor
	 * 
	 * @param service     the service used to query the repository
	 * @param entityModel the entity model for the items that are displayed in the
	 *                    grid
	 * @param queryType   the type of query
	 * @param sortOrders  the sort order
	 * @param joins       the fetch joins to use when executing the query
	 */
	public BaseGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, Map<String, SerializablePredicate<?>> fieldFilters, List<SortOrder<?>> sortOrders,
			boolean editable, FetchJoinInformation... joins) {
		this.service = service;
		this.fieldFilters = fieldFilters;
		this.entityModel = entityModel;
		this.queryType = queryType;
		this.formOptions = formOptions;
		this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
		this.joins = joins;
		this.editable = editable;
	}

	public FetchJoinInformation[] getExportJoins() {
		return exportJoins;
	}

	public void setExportJoins(FetchJoinInformation[] exportJoins) {
		this.exportJoins = exportJoins;
	}

	/**
	 * Adds a grid selection listener
	 */
	private void addGridSelectionListener() {
		grid.addSelectionListener(event -> onSelect(grid.getSelectedItems()));
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
		layout = new VerticalLayout();

		this.dataProvider = constructDataProvider();
		grid = getGrid();
		layout.addComponent(grid);
		initSortingAndFiltering();

		grid.setSelectionMode(SelectionMode.SINGLE);
		addGridSelectionListener();

		setCompositionRoot(layout);
	}

	/**
	 * Creates the container that holds the data
	 * 
	 * @return the container
	 */
	protected abstract DataProvider<T, SerializablePredicate<T>> constructDataProvider();

	/**
	 * Constructs the grid - override in subclasses if you need a different grid
	 * implementation
	 * 
	 * @return
	 */
	protected ModelBasedGrid<ID, T> constructGrid() {
		return new ModelBasedGrid<ID, T>(dataProvider, entityModel, fieldFilters, editable,
				getFormOptions().getGridEditMode()) {

			private static final long serialVersionUID = -4559181057050230055L;

			@Override
			protected BindingBuilder<T, ?> doBind(T t, AbstractComponent field) {
				return BaseGridWrapper.this.doBind(t, field);
			}
		};

	}

	protected BindingBuilder<T, ?> doBind(T t, AbstractComponent field) {
		return null;
	}

	/**
	 * Callback method used to modify data provider creation
	 * 
	 * @param container
	 */
	protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
		// overwrite in subclasses
	}

	public DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return dataProvider;
	}

	public int getDataProviderSize() {
		if (dataProvider instanceof BaseDataProvider) {
			return grid.getDataCommunicator().getDataProviderSize();
		} else if (dataProvider instanceof ListDataProvider) {
			ListDataProvider<T> lp = (ListDataProvider<T>) dataProvider;
			return lp.getItems().size();
		}
		return 0;
	}

	/**
	 * @return the entityModel
	 */
	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public EntityModel<T> getExportEntityModel() {
		return exportEntityModel;
	}

	public ExportDelegate getExportDelegate() {
		return exportDelegate;
	}

	public FormOptions getFormOptions() {
		return formOptions;
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

	public Layout getLayout() {
		return layout;
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
	@SuppressWarnings("unchecked")
	protected void initSortingAndFiltering() {

		List<SortOrder<?>> fallBackOrders = new ArrayList<>();
		if (getSortOrders() != null && !getSortOrders().isEmpty()) {
			GridSortOrderBuilder<T> builder = new GridSortOrderBuilder<>();
			for (SortOrder<?> o : getSortOrders()) {
				if (getGrid().getColumn((String) o.getSorted()) != null) {
					// only include column in sort order if it is present in the grid
					if (SortDirection.ASCENDING.equals(o.getDirection())) {
						builder.thenAsc(grid.getColumn(o.getSorted().toString()));
					} else {
						builder.thenDesc(grid.getColumn(o.getSorted().toString()));
					}
				} else {
					// not present in grid, add to backup
					fallBackOrders.add(o);
				}
			}
			grid.setSortOrder(builder);
		} else if (getEntityModel().getSortOrder() != null && !getEntityModel().getSortOrder().keySet().isEmpty()) {
			// sort based on the entity model
			GridSortOrderBuilder<T> builder = new GridSortOrderBuilder<>();
			for (AttributeModel am : entityModel.getSortOrder().keySet()) {
				boolean asc = entityModel.getSortOrder().get(am);
				if (getGrid().getColumn(am.getPath()) != null) {
					if (asc) {
						builder.thenAsc(grid.getColumn(am.getPath()));
					} else {
						builder.thenDesc(grid.getColumn(am.getPath()));
					}
				} else {
					fallBackOrders.add(new SortOrder<String>(am.getActualSortPath(),
							asc ? SortDirection.ASCENDING : SortDirection.DESCENDING));
				}
			}
			grid.setSortOrder(builder);
		}

		// set fall back sort orders
		if (dataProvider instanceof BaseDataProvider) {
			((BaseDataProvider<ID, T>) dataProvider).setFallBackSortOrders(fallBackOrders);
		}
	}

	/**
	 * Respond to a selection of an item in the grid
	 */
	protected void onSelect(Object selected) {
		// overwrite in subclasses
	}

	/**
	 * Reloads the data in the container
	 */
	public abstract void reloadDataProvider();

	public void setDataProvider(DataProvider<T, SerializablePredicate<T>> dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void setExportEntityModel(EntityModel<T> exportEntityModel) {
		this.exportEntityModel = exportEntityModel;
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
