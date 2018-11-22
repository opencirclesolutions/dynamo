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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.LikePredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.SortOrder;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.TextField;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * A split layout - contains both a table and a details view - that uses a
 * service to fetch data
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T> type of the entity
 */
@SuppressWarnings("serial")
public class ServiceBasedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseSplitLayout<ID, T> {

	private static final long serialVersionUID = 1068860513192819804L;

	/**
	 * The filter used to restrict the search results. Override the
	 * <code>constructFilter</code> method to set this filter.
	 */
	private SerializablePredicate<T> filter;

	/**
	 * The query type (ID based or paging) used to query the database
	 */
	private QueryType queryType;

	/**
	 * Constructor
	 *
	 * @param service     the service for retrieving data from the database
	 * @param entityModel the entity model
	 * @param queryType   the desired query type
	 * @param formOptions the form options
	 * @param sortOrder   the sort order
	 * @param joins
	 */
	public ServiceBasedSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder<?> sortOrder, FetchJoinInformation... joins) {
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
	protected SerializablePredicate<T> constructFilter() {
		// overwrite in subclass
		return null;
	}

	/**
	 * Constructs the quick search filter - override if you need a custom filter
	 * when searching for the main attribute is not sufficient. Note that the quick
	 * search filter is applied in addition to the always active default filter
	 * returned by the "constructFilter" method
	 *
	 * @param value the value to search for
	 * @return
	 */
	protected SerializablePredicate<T> constructQuickSearchFilter(String value) {
		// override in subclasses
		return null;
	}

	/**
	 * Constructs a quick search field - this method will only be called if the
	 * "showQuickSearchField" form option is enabled. It will then look for a custom
	 * filter returned by the constructQuickSearchFilter method, and if that method
	 * returns null it will construct a filter based on the main attribute
	 */
	@Override
	protected final TextField constructSearchField() {
		if (getFormOptions().isShowQuickSearchField()) {
			TextField searchField = new TextField(message("ocs.search"));

			// respond to the user entering a search term
			searchField.addValueChangeListener(event -> {
				String text = event.getValue();
				if (!StringUtils.isEmpty(text)) {
					SerializablePredicate<T> quickFilter = constructQuickSearchFilter(text);
					if (quickFilter == null && getEntityModel().getMainAttributeModel() != null) {
						quickFilter = new LikePredicate<T>(getEntityModel().getMainAttributeModel().getPath(),
								"%" + text + "%", false);
					}

					SerializablePredicate<T> temp = quickFilter;
					if (getFilter() != null) {
						temp = new AndPredicate<T>(quickFilter, getFilter());
					}
					getGridWrapper().search(temp);
				} else {
					getGridWrapper().search(filter);
				}
			});
			return searchField;
		}
		return null;
	}

	@Override
	protected BaseGridWrapper<ID, T> constructTableWrapper() {
		ServiceBasedGridWrapper<ID, T> tw = new ServiceBasedGridWrapper<ID, T>(getService(), getEntityModel(),
				getQueryType(), filter, getSortOrders(), getFormOptions().isTableExportAllowed(), false, getJoins()) {

			@Override
			protected void doConstructDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
				ServiceBasedSplitLayout.this.doConstructDataProvider(provider);
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

	protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return getGridWrapper().getDataProvider();
	}

	public SerializablePredicate<T> getFilter() {
		return filter;
	}

	public QueryType getQueryType() {
		return queryType;
	}

	@Override
	public ServiceBasedGridWrapper<ID, T> getGridWrapper() {
		return (ServiceBasedGridWrapper<ID, T>) super.getGridWrapper();
	}

	/**
	 * Reloads the component - this will first rebuild the filter and then reload
	 * the container using that filter
	 */
	@Override
	public void reload() {
		buildFilter();
		super.reload();
		refresh();
		getGridWrapper().setFilter(filter);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				Collection<?> col = (Collection<?>) selectedItems;
				if (col.iterator().hasNext()) {
					T t = (T) col.iterator().next();
					setSelectedItem(getService().fetchById(t.getId(), getDetailJoinsFallBack()));
				} else {
					setSelectedItem(null);
					emptyDetailView();
				}
			} else {
				T t = (T) selectedItems;
				setSelectedItem(getService().fetchById(t.getId(), getDetailJoinsFallBack()));
			}
		} else {
			// nothing selected
			setSelectedItem(null);
			emptyDetailView();
		}
	}
}
