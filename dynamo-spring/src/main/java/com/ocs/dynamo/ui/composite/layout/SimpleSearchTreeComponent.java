package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.vaadin.addons.lazyquerycontainer.CompositeItem;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTreeTableWrapper;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.hierarchical.HierarchicalFetchJoinInformation;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Component;

/**
 * Extends simple search with the support of hierarchy in a tree table.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
@SuppressWarnings("serial")
public class SimpleSearchTreeComponent<ID extends Serializable, T extends AbstractEntity<ID>>
        extends SimpleSearchLayout<ID, T> {

	protected List<BaseService<?, ?>> services;
	private Object selectedItem;

	/**
	 * @param service
	 * @param entityModel
	 * @param queryType
	 * @param formOptions
	 */
	@SuppressWarnings("unchecked")
	public SimpleSearchTreeComponent(List<BaseService<?, ?>> services, EntityModel<T> entityModel,
	        QueryType queryType, FormOptions formOptions, FetchJoinInformation... joins) {
		super((BaseService<ID, T>) services.get(0), entityModel, queryType, formOptions, null,
		        joins);
		this.services = services;
	}

	/**
	 * @param service
	 * @param entityModel
	 * @param queryType
	 * @param formOptions
	 * @param fieldFilters
	 * @param additionalFilters
	 * @param joins
	 */
	@SuppressWarnings("unchecked")
	public SimpleSearchTreeComponent(List<BaseService<?, ?>> services, EntityModel<T> entityModel,
	        QueryType queryType, FormOptions formOptions, Map<String, Filter> fieldFilters,
	        List<Filter> additionalFilters, FetchJoinInformation[] joins) {
		super((BaseService<ID, T>) services.get(0), entityModel, queryType, formOptions,
		        fieldFilters, additionalFilters, null, joins);
		this.services = services;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ocs.dynamo.ui.page.SimpleSearchPanel#getTableWrapper()
	 */
	@Override
	public ServiceResultsTableWrapper<ID, T> getTableWrapper() {
		if (tableWrapper == null) {
			tableWrapper = new ServiceResultsTreeTableWrapper<ID, T>(services, getEntityModel(),
			        getQueryType(), null, getJoins());
			tableWrapper.build();
		}
		return tableWrapper;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Component getSearchForm() {
		ModelBasedHierarchicalContainer<T> c = (ModelBasedHierarchicalContainer<T>) getTableWrapper()
		        .getContainer();
		if (searchForm == null && !c.getHierarchy().isEmpty()) {
			ModelBasedHierarchicalDefinition def = c.getHierarchicalDefinition(0);
			searchForm = new ModelBasedSearchForm(getTableWrapper(), def.getEntityModel(),
			        getFormOptions(), getAdditionalFilters(), getFieldFilters());
			searchForm.build();
		}
		return searchForm;
	}

	@Override
	public HierarchicalFetchJoinInformation[] getJoins() {
		return (HierarchicalFetchJoinInformation[]) super.getJoins();
	}

	@Override
	public void build() {
		getTableWrapper();
		super.build();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void select(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items
				Collection<?> col = (Collection<?>) selectedItems;
				if (!col.isEmpty()) {
					Object id = col.iterator().next();
					if (id != null) {
						ModelBasedHierarchicalContainer<T> c = (ModelBasedHierarchicalContainer<T>) getTableWrapper()
						        .getContainer();
						Item item = c.getItem(id);
						if (item instanceof BeanItem) {
							setSelectedHierarchicalItem(((BeanItem<?>) item).getBean());
						} else if (item instanceof CompositeItem) {
							setSelectedHierarchicalItem(
							        ((CompositeItem) item).getItem(CompositeItem.DEFAULT_ITEM_KEY));
						} else {
							setSelectedHierarchicalItem(item);
						}
					}
				}
			}
		} else {
			setSelectedHierarchicalItem(null);
		}
	}

	protected void setSelectedHierarchicalItem(Object selectedItem) {
		this.selectedItem = selectedItem;
	}

	public Object getSelectedHierarchicalItem() {
		return selectedItem;
	}
}