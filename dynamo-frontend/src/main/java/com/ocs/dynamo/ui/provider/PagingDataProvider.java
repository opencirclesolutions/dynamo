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
package com.ocs.dynamo.ui.provider;

import java.io.Serializable;
import java.util.stream.Stream;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;

/**
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the ID of the entity
 * @param <T> the type of the entity
 */
public class PagingDataProvider<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseDataProvider<ID, T> {

	private static final long serialVersionUID = 8238057223431007376L;

	/**
	 * The number of items in the provider. This is set by doing a count query
	 */
	private int size;

	/**
	 * Constructor
	 * 
	 * @param service     the service
	 * @param entityModel the entity model
	 * @param joins       the joins to use when querying
	 */
	public PagingDataProvider(BaseService<ID, T> service, EntityModel<T> entityModel, FetchJoinInformation... joins) {
		super(service, entityModel, joins);
	}

	@Override
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {
		FilterConverter<T> converter = getFilterConverter();
		int offset = query.getOffset();
		int page = offset / query.getLimit();
		int pageSize = getMaxResults() != null && offset + query.getLimit() > getMaxResults() ? getMaxResults() - offset
				: query.getLimit();
		SortOrders sortOrders = createSortOrder(query);
		Filter filter = converter.convert(query.getFilter().orElse(null));
		return getService().fetch(filter, page, pageSize, sortOrders, getJoins()).stream();
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public int size(Query<T, SerializablePredicate<T>> query) {
		FilterConverter<T> converter = new FilterConverter<>(getEntityModel());
		size = (int) getService().count(converter.convert(query.getFilter().orElse(null)), false);
		if (getMaxResults() != null && size >= getMaxResults()) {
			showNotification(
					getMessageService().getMessage("ocs.too.many.results", VaadinUtils.getLocale(), getMaxResults()));
			size = getMaxResults();
		}
		if (getAfterCountCompleted() != null) {
			getAfterCountCompleted().accept(size);
		}
		return size;
	}

	public ID getNextItemId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ID getPreviousItemId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ID firstItemId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(ID id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T getItem(ID id) {
		throw new UnsupportedOperationException();
	}
}
