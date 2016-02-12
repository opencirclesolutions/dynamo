package nl.ocs.ui.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.ocs.constants.OCSConstants;
import nl.ocs.dao.SortOrder;
import nl.ocs.domain.AbstractEntity;
import nl.ocs.filter.Filter;
import nl.ocs.filter.FilterConverter;
import nl.ocs.utils.ClassUtils;

import org.vaadin.addons.lazyquerycontainer.AbstractBeanQuery;
import org.vaadin.addons.lazyquerycontainer.QueryDefinition;

import com.vaadin.data.Container;
import com.vaadin.data.util.filter.And;

/**
 * A lazy container query that retrieves data using a service
 * 
 * @author patrick.deenen
 * 
 */
public abstract class BaseServiceQuery<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractBeanQuery<T> {

	private static final long serialVersionUID = 4128040933505878355L;

	// local variable used as a counter for assigning temporary IDs
	private int countDown;

	/**
	 * Constructor
	 * 
	 * @param queryDefinition
	 * @param queryConfiguration
	 */
	public BaseServiceQuery(ServiceQueryDefinition<ID, T> queryDefinition,
			Map<String, Object> queryConfiguration) {
		super(queryDefinition, queryConfiguration, null, null);
	}

	/**
	 * Creates an instance of the entity class
	 */
	@Override
	protected T constructBean() {
		Class<T> ec = getCustomQueryDefinition().getService().getEntityClass();

		// the lazy query container cannot deal with situations in which the
		// new object doesn't have an ID
		// to circumvent this, we give the object a temporary ID which we clear
		// before actually persisting the object
		T result = ClassUtils.instantiateClass(ec);
		ClassUtils.setFieldValue(result, OCSConstants.ID, Integer.MAX_VALUE - countDown);
		countDown--;

		return result;
	}

	/**
	 * Constructs the search filter
	 * 
	 * @return
	 */
	protected Filter constructFilter() {
		final List<Container.Filter> filters = new ArrayList<>();
		filters.addAll(getCustomQueryDefinition().getDefaultFilters());
		filters.addAll(getCustomQueryDefinition().getFilters());

		Container.Filter first;
		if (!filters.isEmpty()) {
			first = filters.remove(0);
		} else {
			first = null;
		}
		while (!filters.isEmpty()) {
			final Container.Filter filter = filters.remove(0);
			first = new And(first, filter);
		}

		return new FilterConverter().convert(first);
	}

	/**
	 * Sets order clause of Service query according to query definition sort
	 * states.
	 * 
	 * @return an array containing the constructed Order objects
	 */
	protected SortOrder[] constructOrder() {
		Object[] sortPropertyIds;
		boolean[] sortPropertyAscendingStates;
		QueryDefinition queryDefinition = getCustomQueryDefinition();

		if (queryDefinition.getSortPropertyIds().length == 0) {
			sortPropertyIds = queryDefinition.getDefaultSortPropertyIds();
			sortPropertyAscendingStates = queryDefinition.getDefaultSortPropertyAscendingStates();
		} else {
			sortPropertyIds = queryDefinition.getSortPropertyIds();
			sortPropertyAscendingStates = queryDefinition.getSortPropertyAscendingStates();
		}

		final SortOrder[] orders = new SortOrder[sortPropertyIds.length];
		if (sortPropertyIds.length > 0) {
			for (int i = 0; i < sortPropertyIds.length; i++) {
				orders[i] = new SortOrder(sortPropertyAscendingStates[i] ? SortOrder.Direction.ASC
						: SortOrder.Direction.DESC, sortPropertyIds[i].toString());
			}
		}
		return orders;
	}

	@Override
	protected void saveBeans(List<T> addedBeans, List<T> modifiedBeans, List<T> removedBeans) {

		// it is possible to first add/edit an item and then remove it - weed
		// out the items that
		// have already been removed here
		modifiedBeans.removeAll(removedBeans);
		addedBeans.removeAll(removedBeans);

		getCustomQueryDefinition().getService().save(modifiedBeans);

		// any beans that have not been persisted before don't actually have to
		// be removed - remove them from the collection before saving
		Iterator<T> it = removedBeans.iterator();
		while (it.hasNext()) {
			T t = it.next();
			if (getCustomQueryDefinition().getService().findById(t.getId()) == null) {
				it.remove();
			}
		}
		getCustomQueryDefinition().getService().delete(removedBeans);

		// clear the IDs of the newly added bean and let the database assign
		// proper ones
		for (T added : addedBeans) {
			added.setId(null);
		}

		// reset the counter so we can start again
		countDown = 0;

		getCustomQueryDefinition().getService().save(addedBeans);
	}

	@SuppressWarnings("unchecked")
	protected ServiceQueryDefinition<ID, T> getCustomQueryDefinition() {
		return (ServiceQueryDefinition<ID, T>) super.getQueryDefinition();
	}

}
