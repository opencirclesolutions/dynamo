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
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalContainer.HierarchicalDefinition;
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalFetchJoinInformation;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;

/**
 * Simple search of hierarchical information presented in tree table. Uses
 * ModelBasedHierachicalContainer, hence also those assumptions.
 * <p>
 * Additionally it will by default only generate search fields on the entity that is on the lowest
 * level of the hierarchy.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
public class ServiceResultsTreeTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>> extends
        ServiceResultsTableWrapper<ID, T> {

	private static final long serialVersionUID = -9054619694421055983L;

	/**
	 * The list of services (ordered by level, highest level first)
	 */
	private List<BaseService<?, ?>> services;

	/**
	 * Whether exporting is allowed
	 */
	private boolean allowExport;

	/**
	 * Constructor
	 * 
	 * @param services
	 *            the list of services (ordered by level, highest level first)
	 * @param rootEntityModel
	 *            the root entity model
	 * @param queryType
	 *            the query type
	 * @param sortOrders
	 *            the sorders
	 * @param joins
	 *            the relations to fetch
	 */
	@SuppressWarnings("unchecked")
	public ServiceResultsTreeTableWrapper(List<BaseService<?, ?>> services, EntityModel<T> rootEntityModel,
	        QueryType queryType, List<SortOrder> sortOrders, boolean allowExport,
	        HierarchicalFetchJoinInformation... joins) {
		super((BaseService<ID, T>) services.get(0), rootEntityModel, queryType, null, sortOrders, true, joins);
		this.services = services;
		this.allowExport = allowExport;
	}

	/**
	 * Creates the container
	 */
	@Override
	protected Container constructContainer() {
		ModelBasedHierarchicalContainer<T> c = new ModelBasedHierarchicalContainer<>(getMessageService(),
		        getEntityModelFactory(), getEntityModel(), services, (HierarchicalFetchJoinInformation[]) getJoins(),
		        getQueryType());
		doConstructContainer(c);
		return c;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelBasedHierarchicalContainer<T> getContainer() {
		return (ModelBasedHierarchicalContainer<T>) super.getContainer();
	}

	@Override
	protected ModelBasedTreeTable<ID, T> constructTable() {
		return new ModelBasedTreeTable<>(getContainer(), getEntityModelFactory(), allowExport);
	}

	@Override
	public void initSortingAndFiltering() {
		if (!getContainer().getHierarchy().isEmpty()) {
			// get the definition on the lowest level
			HierarchicalDefinition def = getContainer().getHierarchy().get(getContainer().getHierarchy().size() - 1);
			if (getFilter() != null && def.getContainer() instanceof ServiceContainer<?, ?>) {
				((ServiceContainer<?, ?>) def.getContainer()).getQueryView().addFilter(getFilter());
			}
		}
		if (getSortOrders() != null && getSortOrders().size() > 0) {
			getTable().sort(getSortProperties(), getSortDirections());
		}
	}

	/**
	 * Perform a search
	 * 
	 * @param filter
	 *            the search filter
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public void search(Filter filter) {
		Filter temp = beforeSearchPerformed(filter);
		if (getContainer() != null && !getContainer().getHierarchy().isEmpty()) {
			ModelBasedHierarchicalDefinition def = getContainer()
			        .getHierarchicalDefinition(0);
			if (def.getContainer() instanceof Searchable) {
				((Searchable) def.getContainer()).search(temp != null ? temp : filter);
			}
		}
	}
}
