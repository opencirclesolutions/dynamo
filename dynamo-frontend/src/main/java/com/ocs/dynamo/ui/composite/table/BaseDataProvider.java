package com.ocs.dynamo.ui.composite.table;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.vaadin.data.provider.AbstractDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.data.sort.SortDirection;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract class for data providers
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public abstract class BaseDataProvider<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractDataProvider<T, SerializablePredicate<T>> {

	private static final long serialVersionUID = 7409567551591729117L;

	private final BaseService<ID, T> service;

	private final EntityModel<T> entityModel;

	private Integer maxResults;

	private final FetchJoinInformation[] joins;

	private EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * Constructor
	 * 
	 * @param service     the service used for retrieving data from the database
	 * @param entityModel the entity model
	 * @param joins       the join data to use
	 */
	public BaseDataProvider(BaseService<ID, T> service, EntityModel<T> entityModel, FetchJoinInformation... joins) {
		this.service = service;
		this.entityModel = entityModel;
		this.joins = joins;
	}

	/**
	 * Creates sort orders based on the Vaadin query
	 * 
	 * @param query the vaadin query
	 * @return
	 */
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

	protected FilterConverter<T> getFilterConverter() {
		EntityModel<T> em = getEntityModel();
		if (em == null) {
			em = entityModelFactory.getModel(getService().getEntityClass());
		}
		return new FilterConverter<>(em);
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	public BaseService<ID, T> getService() {
		return service;
	}

	/**
	 * Returns the number of items 
	 * @return
	 */
	protected abstract int getSize();

	@Override
	public boolean isInMemory() {
		return false;
	}

	public Integer getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(final Integer maxResults) {
		this.maxResults = maxResults;
	}

	public MessageService getMessageService() {
		return messageService;
	}
}
