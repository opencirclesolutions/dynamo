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
import java.util.stream.Stream;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;

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
		extends BaseGridWrapper<ID, T> implements Searchable<T> {

	private static final long serialVersionUID = -4691108261565306844L;

	/**
	 * The search filter that is applied to the table
	 */
	private SerializablePredicate<T> filter;

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
			SerializablePredicate<T> filter, List<SortOrder<?>> sortOrders, boolean allowExport,
			FetchJoinInformation... joins) {
		super(service, entityModel, queryType, sortOrders, allowExport, joins);
		this.filter = filter;
	}

	@Override
	protected DataProvider<T, SerializablePredicate<T>> constructDataProvider() {
		DataProvider<T, SerializablePredicate<T>> provider = new DataProvider<T, SerializablePredicate<T>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isInMemory() {
				return false;
			}

			@Override
			public int size(Query<T, SerializablePredicate<T>> query) {
				FilterConverter<T> converter = new FilterConverter<>(getEntityModel());
				return (int) getService().count(converter.convert(query.getFilter().orElse(null)), false);
			}

			@Override
			public void refreshItem(T item) {

			}

			@Override
			public void refreshAll() {
				// TODO
			}

			@Override
			public Registration addDataProviderListener(DataProviderListener<T> listener) {
				return null;
			}

			@Override
			public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {
				FilterConverter<T> converter = new FilterConverter<>(getEntityModel());
				int offset = query.getOffset();
				int limit = query.getLimit();

				List<T> result = getService().fetch(converter.convert(query.getFilter().orElse(null)), offset, limit,
						getJoins());
				return result.stream();
			}
		};
		doConstructDataProvider(provider);
		return provider;
	}

	protected SerializablePredicate<T> getFilter() {
		return filter;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	@SuppressWarnings("unchecked")
	public ModelBasedTable<ID, T> getModelBasedTable() {
		return (ModelBasedTable<ID, T>) super.getGrid();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initSortingAndFiltering() {
		// set the filter (using the getQueryView() to prevent a useless query)
		// ((ServiceContainer<ID, T>) getContainer()).getQueryView().addFilter(filter);
		super.initSortingAndFiltering();
	}

	@Override
	public void reloadDataProvider() {
		if (getDataProvider() instanceof Searchable) {
			// ((Searchable) getContainer()).search(filter);
		}
	}

	@Override
	public void search(SerializablePredicate<T> filter) {
		SerializablePredicate<T> temp = beforeSearchPerformed(filter);
		if (getDataProvider() instanceof Searchable) {
			// ((Searchable) getDataProvider()).search(temp != null ? temp : filter);
		}
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

	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}

}
