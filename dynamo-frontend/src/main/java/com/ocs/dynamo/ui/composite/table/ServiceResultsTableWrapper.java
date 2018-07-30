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
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.ui.container.ServiceQueryDefinition;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;

/**
 * A wrapper for a table that retrieves its data directly from the database
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key of the entity
 * @param <T>
 *            type of the entity
 */
public class ServiceResultsTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseTableWrapper<ID, T> implements Searchable {

	private static final long serialVersionUID = -4691108261565306844L;

	/**
	 * The search filter that is applied to the table
	 */
	private Filter filter;

	private Integer maxResults;

	/**
	 * @param service
	 *            the service object
	 * @param entityModel
	 *            the entity model
	 * @param queryType
	 *            the query type to use
	 * @param order
	 *            the default sort order
	 * @param joins
	 *            options list of fetch joins to include in the query
	 */
	public ServiceResultsTableWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			Filter filter, List<SortOrder> sortOrders, boolean allowExport, FetchJoinInformation... joins) {
		super(service, entityModel, queryType, sortOrders, allowExport, joins);
		this.filter = filter;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Container constructContainer() {
		ServiceContainer<ID, T> container = new ServiceContainer<>(getService(), getEntityModel(),
				DynamoConstants.PAGE_SIZE, getQueryType(), getJoins());
		if (getMaxResults() != null) {
			((ServiceQueryDefinition<ID, T>) container.getQueryView().getQueryDefinition())
					.setMaxQuerySize(getMaxResults());
		}
		doConstructContainer(container);
		return container;
	}

	protected Filter getFilter() {
		return filter;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	@SuppressWarnings("unchecked")
	public ModelBasedTable<ID, T> getModelBasedTable() {
		return (ModelBasedTable<ID, T>) super.getTable();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initSortingAndFiltering() {
		// set the filter (using the getQueryView() to prevent a useless query)
		((ServiceContainer<ID, T>) getContainer()).getQueryView().addFilter(filter);
		super.initSortingAndFiltering();
	}

	@Override
	public void reloadContainer() {
		if (getContainer() instanceof Searchable) {
			((Searchable) getContainer()).search(filter);
		}
	}

	@Override
	public void search(Filter filter) {
		Filter temp = beforeSearchPerformed(filter);
		if (getContainer() instanceof Searchable) {
			((Searchable) getContainer()).search(temp != null ? temp : filter);
		}
	}

	/**
	 * Sets the provided filter as the component filter and then refreshes the
	 * container
	 * 
	 * @param filter
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
		search(filter);
	}

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

}
