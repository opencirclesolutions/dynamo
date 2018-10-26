package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;

/**
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public class PagingDataProvider<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractDataProvider<ID, T> {

	private static final long serialVersionUID = 8238057223431007376L;

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
	public int size(Query<T, SerializablePredicate<T>> query) {
		FilterConverter<T> converter = new FilterConverter<>(getEntityModel());
		return (int) getService().count(converter.convert(query.getFilter().orElse(null)), false);
	}

	@Override
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {
		FilterConverter<T> converter = getFilterConverter();
		int offset = query.getOffset();
		int page = offset / query.getLimit();

		SortOrders so = createSortOrder(query);
		List<T> result = getService().fetch(converter.convert(query.getFilter().orElse(null)), page, query.getLimit(),
				so, getJoins());
		return result.stream();
	}

}
