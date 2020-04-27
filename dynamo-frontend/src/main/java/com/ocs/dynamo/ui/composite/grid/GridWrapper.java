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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.composite.export.ExportDelegate;
import com.ocs.dynamo.ui.composite.layout.BaseCustomComponent;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

public abstract class GridWrapper<ID extends Serializable, T extends AbstractEntity<ID>, U> extends BaseCustomComponent
		implements Searchable<T> {

	private static final long serialVersionUID = -7839990909007399928L;

	/**
	 * The entity model used to create the container
	 */
	private EntityModel<T> entityModel;

	/**
	 * The export service used for generating XLS and CSV exports
	 */
	private ExportDelegate exportDelegate = getService(ExportDelegate.class);

	/**
	 * The entity model to use when exporting
	 */
	private EntityModel<T> exportEntityModel;

	/**
	 * The joins to use when exporting (needed when using exportmode FULL)
	 */
	private FetchJoinInformation[] exportJoins;

	/**
	 * The search filter that is applied to the grid
	 */
	private SerializablePredicate<T> filter;

	/**
	 * The form options
	 */
	private FormOptions formOptions;

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

	public GridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SerializablePredicate<T> filter, List<SortOrder<?>> sortOrders,
			FetchJoinInformation... joins) {
		setSpacing(false);
		setPadding(false);
		this.entityModel = entityModel;
		this.filter = filter;
		this.service = service;
		this.queryType = queryType;
		this.formOptions = formOptions;
		this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
		this.joins = joins;
	}

	/**
	 * Perform any actions that are necessary before carrying out a search
	 * 
	 * @param filter the current search filter
	 */
	protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
		// overwrite in subclasses
		return null;
	}

	public abstract DataProvider<U, SerializablePredicate<U>> getDataProvider();

	public abstract int getDataProviderSize();

	/**
	 * @return the entityModel
	 */
	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public ExportDelegate getExportDelegate() {
		return exportDelegate;
	}

	public EntityModel<T> getExportEntityModel() {
		return exportEntityModel;
	}

	public FetchJoinInformation[] getExportJoins() {
		return exportJoins;
	}

	protected SerializablePredicate<T> getFilter() {
		return filter;
	}

	public FormOptions getFormOptions() {
		return formOptions;
	}

	public abstract Grid<U> getGrid();

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
	 * 
	 * @return the sort orders
	 */
	public List<SortOrder<?>> getSortOrders() {
		return Collections.unmodifiableList(sortOrders);
	}

	protected List<SortOrder<?>> initSortingAndFiltering() {

		List<SortOrder<?>> fallBackOrders = new ArrayList<>();
		if (getSortOrders() != null && !getSortOrders().isEmpty()) {
			GridSortOrderBuilder<U> builder = new GridSortOrderBuilder<>();
			for (SortOrder<?> o : getSortOrders()) {
				if (getGrid().getColumnByKey((String) o.getSorted()) != null) {
					// only include column in sort order if it is present in the grid
					if (SortDirection.ASCENDING.equals(o.getDirection())) {
						builder.thenAsc(getGrid().getColumnByKey(o.getSorted().toString()));
					} else {
						builder.thenDesc(getGrid().getColumnByKey(o.getSorted().toString()));
					}
				} else {
					// not present in grid, add to backup
					fallBackOrders.add(o);
				}
			}
			getGrid().sort(builder.build());
		} else if (getEntityModel().getSortOrder() != null && !getEntityModel().getSortOrder().keySet().isEmpty()) {
			// sort based on the entity model
			GridSortOrderBuilder<U> builder = new GridSortOrderBuilder<>();
			for (AttributeModel am : entityModel.getSortOrder().keySet()) {
				boolean asc = entityModel.getSortOrder().get(am);
				if (getGrid().getColumnByKey(am.getPath()) != null) {
					if (asc) {
						builder.thenAsc(getGrid().getColumnByKey(am.getPath()));
					} else {
						builder.thenDesc(getGrid().getColumnByKey(am.getPath()));
					}
				} else {
					fallBackOrders.add(new SortOrder<String>(am.getActualSortPath(),
							asc ? SortDirection.ASCENDING : SortDirection.DESCENDING));
				}
			}
			getGrid().sort(builder.build());
		}
		return fallBackOrders;

	}

	public abstract void reloadDataProvider();

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void search(SerializablePredicate<T> filter) {
		SerializablePredicate<T> temp = beforeSearchPerformed(filter);
		this.filter = temp != null ? temp : filter;
		getGrid().getDataCommunicator().setDataProvider((DataProvider) getDataProvider(), temp != null ? temp : filter);
	}

	public void setExportEntityModel(EntityModel<T> exportEntityModel) {
		this.exportEntityModel = exportEntityModel;
	}

	public void setExportJoins(FetchJoinInformation[] exportJoins) {
		this.exportJoins = exportJoins;
	}

	/**
	 * Sets the provided filter as the component filter and then refreshes the
	 * container
	 * 
	 * @param filter
	 */
	public void setFilter(SerializablePredicate<T> filter) {
		this.filter = filter;
		search(filter);
	}

	public void setJoins(FetchJoinInformation[] joins) {
		this.joins = joins;
	}

	public void setSortOrders(List<SortOrder<?>> sortOrders) {
		this.sortOrders = sortOrders;
	}
}
