package com.ocs.dynamo.ui.container;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

/**
 * A version of the BaseServiceQuery that retrieves data using a simple paging
 * mechanism
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class PagingServiceQuery<ID extends Serializable, T extends AbstractEntity<ID>> extends
		BaseServiceQuery<ID, T> {

	private static final long serialVersionUID = -324739194626626683L;

	/**
	 * Constructor
	 * 
	 * @param queryDefinition
	 * @param queryConfiguration
	 */
	public PagingServiceQuery(ServiceQueryDefinition<ID, T> queryDefinition,
			Map<String, Object> queryConfiguration) {
		super(queryDefinition, queryConfiguration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<T> loadBeans(int startIndex, int count) {
		Filter serviceFilter = constructFilter();
		SortOrder[] orders = constructOrder();
		ServiceQueryDefinition<ID, T> definition = getCustomQueryDefinition();
		return definition.getService().fetch(serviceFilter, startIndex / definition.getBatchSize(),
				definition.getBatchSize(), definition.getJoins(), orders);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		if (getCustomQueryDefinition().getPredeterminedCount() != null) {
			return getCustomQueryDefinition().getPredeterminedCount();
		}
		return (int) getCustomQueryDefinition().getService().count(constructFilter(), false);
	}
}
