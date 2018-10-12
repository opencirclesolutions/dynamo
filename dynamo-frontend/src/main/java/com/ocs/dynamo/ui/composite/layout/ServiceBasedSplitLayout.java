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

import org.springframework.util.StringUtils;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.table.BaseTableWrapper;
import com.ocs.dynamo.ui.composite.table.ServiceResultsTableWrapper;
import com.ocs.dynamo.ui.container.QueryType;
import com.ocs.dynamo.ui.container.ServiceContainer;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Like;
import com.vaadin.ui.TextField;

/**
 * A split layout - contains both a table and a details view - that uses a
 * service to fetch data
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
@SuppressWarnings("serial")
public class ServiceBasedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseSplitLayout<ID, T> {

	private static final long serialVersionUID = 1068860513192819804L;

	/**
	 * The filter used to restrict the search results. Override the
	 * <code>constructFilter</code> method to set this filter.
	 */
	private Filter filter;

	/**
	 * The query type (ID based or paging) used to query the database
	 */
	private QueryType queryType;

	/**
	 * Constructor
	 *
	 * @param service
	 *            the service for retrieving data from the database
	 * @param entityModel
	 *            the entity model
	 * @param queryType
	 *            the desired query type
	 * @param formOptions
	 *            the form options
	 * @param sortOrder
	 *            the sort order
	 * @param joins
	 */
	public ServiceBasedSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
	}

	@Override
	protected final void buildFilter() {
		filter = constructFilter();
	}

	/**
	 * Creates the main search filter - overwrite in subclass if you need to
	 * actually filter the data
	 *
	 * @return
	 */
	protected Filter constructFilter() {
		// overwrite in subclass
		return null;
	}

	/**
	 * Constructs the quick search filter - override if you need a custom filter
	 * when searching for the main attribute is not sufficient. Note that the quick
	 * search filter is applied in addition to the always active default filter
	 * returned by the "constructFilter" method
	 *
	 * @param value
	 *            the value to search for
	 * @return
	 */
	protected Filter constructQuickSearchFilter(String value) {
		// override in subclasses
		return null;
	}

	/**
	 * Constructs a quick search field - this method will only be called if the
	 * "showQuickSearchField" form option is enabled. It will then look for a
	 * custom
	 * filter returned by the constructQuickSearchFilter method, and if
	 * that method
	 * returns null it will construct a filter based on the main
	 * attribute
	 */
	@Override
	protected final TextField constructSearchField() {
		if (getFormOptions().isShowQuickSearchField()) {
			TextField searchField = new TextField(message("ocs.search"));

			// respond to the user entering a search term
			searchField.addTextChangeListener(event -> {
				String text = event.getText();
				if (!StringUtils.isEmpty(text)) {
					Filter quickFilter = constructQuickSearchFilter(text);
					if (quickFilter == null && getEntityModel().getMainAttributeModel() != null) {
						quickFilter = new Like(getEntityModel().getMainAttributeModel().getPath(), "%" + text + "%",
								false);
					}

					Filter temp = quickFilter;
					if (getFilter() != null) {
						temp = new And(quickFilter, getFilter());
					}
					getContainer().search(temp);
				} else {
					getContainer().search(filter);
				}
			});
			return searchField;
		}
		return null;
	}

	@Override
	protected BaseTableWrapper<ID, T> constructTableWrapper() {
		ServiceResultsTableWrapper<ID, T> tw = new ServiceResultsTableWrapper<ID, T>(getService(), getEntityModel(),
				getQueryType(), filter, getSortOrders(), getFormOptions().isTableExportAllowed(), getJoins()) {

			@Override
			protected void doConstructContainer(Container container) {
				ServiceBasedSplitLayout.this.doConstructContainer(container);
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
		tw.setMaxResults(getMaxResults());
		tw.build();
		return tw;
	}

	@SuppressWarnings("unchecked")
	protected ServiceContainer<ID, T> getContainer() {
		return (ServiceContainer<ID, T>) getTableWrapper().getContainer();
	}

	public Filter getFilter() {
		return filter;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	@Override
	public ServiceResultsTableWrapper<ID, T> getTableWrapper() {
		return (ServiceResultsTableWrapper<ID, T>) super.getTableWrapper();
	}

	/**
	 * Reloads the component - this will first rebuild the filter and then
	 * reload
	 * the container using that filter
	 */
	@Override
	public void reload() {
		buildFilter();
		super.reload();
		refresh();
		getTableWrapper().setFilter(filter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				// the lazy query container returns an array of IDs of the
				// selected items
				Collection<?> col = (Collection<?>) selectedItems;
				ID id = (ID) col.iterator().next();
				setSelectedItem(getService().fetchById(id, getDetailJoinsFallBack()));
			} else {
				ID id = (ID) selectedItems;
				setSelectedItem(getService().fetchById(id, getDetailJoinsFallBack()));
			}
		} else {
			// nothing selected
			setSelectedItem(null);
			emptyDetailView();
		}
	}
}
