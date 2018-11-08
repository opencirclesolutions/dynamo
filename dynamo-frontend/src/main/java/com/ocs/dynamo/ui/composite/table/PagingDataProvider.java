package com.ocs.dynamo.ui.composite.table;

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
import java.util.stream.Stream;

/**
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public class PagingDataProvider<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseDataProvider<ID, T> {

	private static final long serialVersionUID = 8238057223431007376L;

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
	public int size(Query<T, SerializablePredicate<T>> query) {
		FilterConverter<T> converter = new FilterConverter<>(getEntityModel());
		size = (int) getService().count(converter.convert(query.getFilter().orElse(null)), false);
		if (getMaxResults() != null
				&& size >= getMaxResults()) {
			Notification.show(getMessageService().getMessage("ocs.too.many.results", VaadinUtils.getLocale(),
					getMaxResults()), Notification.Type.ERROR_MESSAGE);
			size = getMaxResults();
		}
		return size;
	}

	@Override
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {
		FilterConverter<T> converter = getFilterConverter();
		int offset = query.getOffset();
		int page = offset / query.getLimit();
		int pageSize = getMaxResults() != null && offset + query.getLimit() > getMaxResults() ? getMaxResults() - offset : query.getLimit();
		SortOrders so = createSortOrder(query);
		List<T> result = getService().fetch(converter.convert(query.getFilter().orElse(null)), page, pageSize,
				so, getJoins());
		return result.stream();
	}

	@Override
	protected int getSize() {
		return size;
	}

}
