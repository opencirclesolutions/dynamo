/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;

/**
 * Service query object based on the "driving query" pattern - first retrieves the IDs of the
 * entities that match, then uses these IDs to retrieve a page of relevant entities
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class IdBasedServiceQuery<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseServiceQuery<ID, T> {

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
	public IdBasedServiceQuery(ServiceQueryDefinition<ID, T> queryDefinition, Map<String, Object> queryConfiguration) {
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
		return getCustomQueryDefinition().getService().fetchByIds(results, new SortOrders(constructOrder()),
		        getCustomQueryDefinition().getJoins());
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
