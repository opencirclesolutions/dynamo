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
package com.ocs.dynamo.ui.composite.grid;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.UI;

/**
 * A wrapper for a grid that displays a fixed number of in-memory entities
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
public class FixedGridWrapper<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseGridWrapper<ID, T> {

	private static final long serialVersionUID = -6711832174203817230L;

	/**
	 * The items to display in the grid
	 */
	private Collection<T> items;

	/**
	 * Constructor
	 * 
	 * @param service      the service used for retrieving data from the database
	 * @param entityModel  the entity model
	 * @param formOptions  the form options
	 * @param fieldFilters the field
	 * @param items        the entities to display
	 * @param sortOrders   the sort orders
	 */
	public FixedGridWrapper(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			Map<String, SerializablePredicate<?>> fieldFilters, Collection<T> items, List<SortOrder<?>> sortOrders) {
		super(service, entityModel, QueryType.NONE, formOptions, fieldFilters, sortOrders, false);
		this.items = items;
	}

	@Override
	protected DataProvider<T, SerializablePredicate<T>> constructDataProvider() {
		return new ListDataProvider<>(items);
	}

	@Override
	public void reloadDataProvider() {
		// do nothing
	}

	@Override
	public void search(SerializablePredicate<T> filter) {
		// do nothing (collection of items is fixed)
	}

	@Override
	protected void initSortingAndFiltering() {
		// right click to download
		if (getFormOptions().isExportAllowed() && getExportDelegate() != null) {
			getGrid().addContextClickListener(event -> {
				ListDataProvider<T> provider = (ListDataProvider<T>) getDataProvider();
				getExportDelegate().exportFixed(UI.getCurrent(),
						getExportEntityModel() != null ? getExportEntityModel() : getEntityModel(),
						getFormOptions().getExportMode(), provider.getItems());
			});
		}
	}
}
