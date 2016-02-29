package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;

import com.ocs.dynamo.constants.OCSConstants;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;

/**
 * A wrapper for a table that retrieves its data directly from the database
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key of the entity
 * @param <T>
 *            type of the entity
 */
public class ServiceResultsTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseTableWrapper<ID, T> implements Searchable {

	private static final long serialVersionUID = -4691108261565306844L;

	protected Filter filter;

	/**
	 * 
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
	public ServiceResultsTableWrapper(BaseService<ID, T> service, EntityModel<T> entityModel,
			QueryType queryType, Filter filter, SortOrder order, FetchJoinInformation[] joins) {
		super(service, entityModel, queryType, order, joins);
		this.filter = filter;
	}

	@Override
	protected Container constructContainer() {
		return new ServiceContainer<ID, T>(getService(), true, OCSConstants.PAGE_SIZE,
				getQueryType(), getJoins());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initSortingAndFiltering() {
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
		if (getContainer() instanceof Searchable) {
			((Searchable) getContainer()).search(filter);
		}
	}
}
