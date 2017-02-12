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
package com.ocs.dynamo.ui.composite.layout;

import java.io.Serializable;
import java.util.Collection;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.FormOptions;
import com.ocs.dynamo.ui.composite.table.BaseTableWrapper;
import com.ocs.dynamo.ui.composite.table.FixedTableWrapper;
import com.vaadin.data.Container;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.TextField;

/**
 * A layout for displaying a fixed collection of items, that contains both a table view and a
 * details view
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
@SuppressWarnings("serial")
public abstract class FixedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseSplitLayout<ID, T> {

	private static final long serialVersionUID = 4606800218149558500L;

	// the items to display in the table
	private Collection<T> items;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service
	 * @param entityModel
	 *            the entity model that is used to construct the layout
	 * @param formOptions
	 *            formoptions that govern how the screen behaves
	 * @param fieldFilters
	 *            field filters applied to fields in the detail view
	 * @param sortOrder
	 */
	public FixedSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
	        SortOrder sortOrder) {
		super(service, entityModel, formOptions, sortOrder);
	}

	@Override
	protected void afterReload(T t) {
		getTableWrapper().getTable().select(t);
	}

	/**
	 * The initialization consists of retrieving the required items
	 */
	@Override
	public void buildFilter() {
		this.items = loadItems();
	}

	@Override
	protected final TextField constructSearchField() {
		// do nothing - not supported for this component
		return null;
	}

	@Override
	protected BaseTableWrapper<ID, T> constructTableWrapper() {
		FixedTableWrapper<ID, T> tw = new FixedTableWrapper<ID, T>(getService(), getEntityModel(), getItems(),
		        getSortOrders(), getFormOptions().isTableExportAllowed()) {
			@Override
			protected void doConstructContainer(Container container) {
				FixedSplitLayout.this.doConstructContainer(container);
			}

			@Override
			protected void onSelect(Object selected) {
				setSelectedItems(selected);
				checkButtonState(getSelectedItem());
				if (getSelectedItem() != null) {
					detailsMode(getSelectedItem());
				}
			}
		};
		tw.build();
		return tw;
	}

	public Collection<T> getItems() {
		return items;
	}

	/**
	 * Loads the items that are to be displayed
	 */
	protected abstract Collection<T> loadItems();

	/**
	 * Reloads the data after an update
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void reload() {
		buildFilter();
		super.reload();
		// remove all items from the container and add the new ones
		BeanItemContainer<T> beanContainer = (BeanItemContainer<T>) getTableWrapper().getContainer();
		beanContainer.removeAllItems();
		beanContainer.addAll(getItems());
		setSelectedItem(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				Collection<?> col = (Collection<?>) selectedItems;
				T t = (T) col.iterator().next();
				// fetch the item again so that any details are loaded
				setSelectedItem(getService().fetchById(t.getId()));
			} else {
				// single item selected
				T t = (T) selectedItems;
				setSelectedItem(t);
			}
		} else {
			setSelectedItem(null);
			emptyDetailView();
		}
	}
}
