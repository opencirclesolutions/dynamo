package nl.ocs.ui.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.ocs.domain.AbstractEntity;

/**
 * Service query object based on the "driving query" pattern - first retrieves
 * the IDs of the entities that match, then uses these IDs to retrieve a page of
 * relevant entities
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class IdBasedServiceQuery<ID extends Serializable, T extends AbstractEntity<ID>> extends
		BaseServiceQuery<ID, T> {

	private static final long serialVersionUID = -1910477652022230437L;

	/**
	 * the list of the IDs of the objects to display
	 */
	private List<ID> ids;

	/**
	 * Constructor
	 * 
	 * @param queryDefinition
	 * @param queryConfiguration
	 */
	public IdBasedServiceQuery(ServiceQueryDefinition<ID, T> queryDefinition,
			Map<String, Object> queryConfiguration) {
		super(queryDefinition, queryConfiguration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<T> loadBeans(int firstIndex, int maxResults) {
		List<ID> results = new ArrayList<>();
		int index = firstIndex;

		// Try to load the IDs when they have not been loaded yet
		if (ids == null) {
			size();
		}
		// construct a page worth of IDs
		if (ids != null && !ids.isEmpty()) {
			while (index < ids.size() && results.size() < maxResults) {
				ID id = ids.get(index);
				results.add(id);
				index++;
			}
		}
		return getCustomQueryDefinition().getService().fetchByIds(results,
				getCustomQueryDefinition().getJoins(), constructOrder());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		// retrieve the IDs of the relevant records and store them for easy
		// reference
		ids = getCustomQueryDefinition().getService().findIds(constructFilter(), constructOrder());
		return ids.size();
	}

}
