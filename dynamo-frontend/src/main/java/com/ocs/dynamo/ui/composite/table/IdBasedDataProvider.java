package com.ocs.dynamo.ui.composite.table;

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
import com.vaadin.ui.Notification;
import org.apache.commons.lang.ObjectUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class IdBasedDataProvider<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseDataProvider<ID, T> {

	private static final long serialVersionUID = -5693366456446998962L;

	private List<ID> ids;

	private SortOrders oldSortOrder = null;

	public IdBasedDataProvider(BaseService<ID, T> service, EntityModel<T> entityModel, FetchJoinInformation... joins) {
		super(service, entityModel, joins);
	}

	@Override
	public int size(Query<T, SerializablePredicate<T>> query) {
		SortOrders so = createSortOrder(query);

		FilterConverter<T> converter = getFilterConverter();
		final Filter filter = converter.convert(query.getFilter().orElse(null));
		Long count = getService().count(filter, false);
		if (getMaxResults() != null
				&& count >= getMaxResults()) {
			Notification.show(getMessageService().getMessage("ocs.too.many.results", VaadinUtils.getLocale(),
					getMaxResults()), Notification.Type.ERROR_MESSAGE);
		}
		ids = getService().findIds(filter, getMaxResults(), so.toArray());
		return ids.size();
	}

	@Override
	public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {

		// when sort order changes, ID have to be fetched again
		SortOrders so = createSortOrder(query);
		if (!ObjectUtils.equals(so, oldSortOrder)) {
			size(query);
		}
		oldSortOrder = so;

		List<ID> results = new ArrayList<>();
		int index = query.getOffset();

		// Try to load the IDs when they have not been loaded yet
		if (ids == null) {
			size(query);
		}
		// construct a page worth of IDs
		if (ids != null && !ids.isEmpty()) {
			while (index < ids.size() && results.size() < query.getLimit()) {
				ID id = ids.get(index);
				results.add(id);
				index++;
			}
		}
		return getService().fetchByIds(results, so, getJoins()).stream();
	}

	@Override
	protected int getSize() {
		return ids == null ? 0: ids.size();
	}

}
