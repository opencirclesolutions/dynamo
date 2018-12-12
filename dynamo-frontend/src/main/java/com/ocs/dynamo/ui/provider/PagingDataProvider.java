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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Notification;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	 * 
	 * @param service
	 * @param entityModel
	 * @param joins
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
		SortOrders so = createSortOrder(query);
		List<T> result = getService().fetch(converter.convert(query.getFilter().orElse(null)), page, pageSize, so,
				getJoins());

		ids = result.stream().map(t -> t.getId()).collect(Collectors.toList());
		return result.stream();
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
			Notification.show(
					getMessageService().getMessage("ocs.too.many.results", VaadinUtils.getLocale(), getMaxResults()),
					Notification.Type.ERROR_MESSAGE);
			size = getMaxResults();
		}
		return size;
	}

}
