package nl.ocs.ui.composite.table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.ocs.domain.AbstractEntity;
import nl.ocs.domain.model.EntityModel;
import nl.ocs.service.BaseService;
import nl.ocs.ui.Searchable;
import nl.ocs.ui.container.hierarchical.HierarchicalFetchJoinInformation;
import nl.ocs.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import nl.ocs.ui.container.hierarchical.HierarchicalContainer.HierarchicalDefinition;
import nl.ocs.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import nl.ocs.ui.container.QueryType;
import nl.ocs.ui.container.ServiceContainer;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

/**
 * Simple search of hierarchical information presented in tree table. Uses
 * ModelBasedHierachicalContainer, hence also those assumptions.
 * <p>
 * Additionally it will by default only generate search fields on the entity
 * that is on the lowest level of the hierarchy.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 *
 */
public class ServiceResultsTreeTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>>
		extends ServiceResultsTableWrapper<ID, T> {

	private static final long serialVersionUID = -9054619694421055983L;

	protected List<BaseService<?, ?>> services;

	/**
	 * Constructor
	 * 
	 * @param rootEntityModel
	 * @param queryType
	 * @param order
	 * @param joins
	 * @param services
	 */
	@SuppressWarnings("unchecked")
	public ServiceResultsTreeTableWrapper(EntityModel<T> rootEntityModel, QueryType queryType,
			SortOrder order, HierarchicalFetchJoinInformation[] joins,
			BaseService<?, ?>... services) {
		super((BaseService<ID, T>) services[0], rootEntityModel, queryType, null, order, joins);
		this.services = new ArrayList<>();
		this.services.addAll(Arrays.asList(services));
	}

	/**
	 * Constructor
	 * 
	 * @param services
	 * @param entityModel
	 * @param queryType
	 * @param order
	 * @param joins
	 */
	@SuppressWarnings("unchecked")
	public ServiceResultsTreeTableWrapper(List<BaseService<?, ?>> services,
			EntityModel<T> rootEntityModel, QueryType queryType, SortOrder order,
			HierarchicalFetchJoinInformation[] joins) {
		super((BaseService<ID, T>) services.get(0), rootEntityModel, queryType, null, order, joins);
		this.services = services;
	}

	/**
	 * Creates the container
	 */
	@Override
	protected Container constructContainer() {
		return new ModelBasedHierarchicalContainer<T>(getMessageService(), getEntityModelFactory(),
				getEntityModel(), services, (HierarchicalFetchJoinInformation[]) getJoins(),
				getQueryType());
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelBasedHierarchicalContainer<T> getContainer() {
		return (ModelBasedHierarchicalContainer<T>) super.getContainer();
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ModelBasedTreeTable<ID, T> getTable() {
		if (table == null) {
			table = new ModelBasedTreeTable<ID, T>(getContainer(), getEntityModelFactory());
		}
		return (ModelBasedTreeTable<ID, T>) table;
	}

	@Override
	protected void initSortingAndFiltering() {
		if (!getContainer().getHierarchy().isEmpty()) {
			// get the definition on the lowest level
			HierarchicalDefinition def = getContainer().getHierarchy().get(
					getContainer().getHierarchy().size() - 1);
			if (filter != null && def.getContainer() instanceof ServiceContainer<?, ?>) {
				((ServiceContainer<?, ?>) def.getContainer()).getQueryView().addFilter(filter);
			}
		}
		if (getSortOrder() != null) {
			table.sort(new Object[] { getSortOrder().getPropertyId() },
					new boolean[] { SortDirection.ASCENDING == getSortOrder().getDirection() });
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
		if (getContainer() != null && !getContainer().getHierarchy().isEmpty()) {
			ModelBasedHierarchicalDefinition def = (ModelBasedHierarchicalDefinition) getContainer()
					.getHierarchicalDefinition(0);
			if (def.getContainer() instanceof Searchable) {
				((Searchable) def.getContainer()).search(filter);
			}
		}
	}
}
