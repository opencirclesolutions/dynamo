package nl.ocs.ui.container;

import java.io.Serializable;

import nl.ocs.domain.AbstractEntity;

import org.vaadin.addons.lazyquerycontainer.Query;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;
import org.vaadin.addons.lazyquerycontainer.QueryFactory;

/**
 * A factory for creating ServiceQuery objects
 * 
 * @author patrick.deenen
 * 
 */
public class ServiceQueryFactory<ID extends Serializable, T extends AbstractEntity<ID>> implements
		QueryFactory {

	/**
	 * Constructs a new query based on its definition
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Query constructQuery(QueryDefinition queryDefinition) {
		ServiceQueryDefinition<ID, T> def = (ServiceQueryDefinition<ID, T>) queryDefinition;

		switch (def.getQueryType()) {
		case PAGING:
			return new PagingServiceQuery<ID, T>((ServiceQueryDefinition<ID, T>) queryDefinition,
					null);
		case ID_BASED:
			return new IdBasedServiceQuery<ID, T>((ServiceQueryDefinition<ID, T>) queryDefinition,
					null);
		default:
			return null;
		}
	}

}
