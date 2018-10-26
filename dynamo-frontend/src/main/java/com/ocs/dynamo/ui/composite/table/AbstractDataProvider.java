package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.Registration;
import com.vaadin.shared.data.sort.SortDirection;

/**
 * Abstract class for data providers
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public abstract class AbstractDataProvider<ID extends Serializable, T extends AbstractEntity<ID>>
		implements DataProvider<T, SerializablePredicate<T>> {

	private static final long serialVersionUID = 7409567551591729117L;

	private final BaseService<ID, T> service;

	private final EntityModel<T> entityModel;

	private final FetchJoinInformation[] joins;

	/**
	 * 
	 * @param service
	 * @param entityModel
	 * @param joins
	 */
	public AbstractDataProvider(BaseService<ID, T> service, EntityModel<T> entityModel, FetchJoinInformation... joins) {
		this.service = service;
		this.entityModel = entityModel;
		this.joins = joins;
	}

	@Override
	public Registration addDataProviderListener(DataProviderListener<T> listener) {
		return null;
	}

	protected SortOrders createSortOrder(Query<T, SerializablePredicate<T>> query) {
		List<QuerySortOrder> orders = query.getSortOrders();
		SortOrders so = new SortOrders();
		for (QuerySortOrder order : orders) {
			so.addSortOrder(new com.ocs.dynamo.dao.SortOrder(
					SortDirection.ASCENDING.equals(order.getDirection()) ? Direction.ASC : Direction.DESC,
					order.getSorted().toString()));
		}
		return so;
	}

	public EntityModel<T> getEntityModel() {
		return entityModel;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	public BaseService<ID, T> getService() {
		return service;
	}

	@Override
	public boolean isInMemory() {
		return false;
	}

	@Override
	public void refreshAll() {
		// TODO not clear when needed
	}
	
	@Override
	public void refreshItem(T item) {
		// TODO not clear when needed
	}
}
