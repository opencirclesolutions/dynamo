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
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.LikePredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * A split layout - contains both a grid and a details view - that uses a
 * service to fetch data
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 */
@SuppressWarnings("serial")
public class ServiceBasedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseSplitLayout<ID, T> {

	private static final long serialVersionUID = 1068860513192819804L;

	/**
	 * The search filter to apply
	 */
	protected SerializablePredicate<T> filter;

	/**
	 * Supplier for creating the filter when needed
	 */
	private Supplier<SerializablePredicate<T>> filterSupplier;

	/**
	 * The query type (ID based or paging) used to query the database
	 */
	private QueryType queryType;

	/**
	 * Supplier for creating the quick search filter
	 */
	private Function<String, SerializablePredicate<T>> quickSearchFilterSupplier;

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
	protected void buildFilter() {
		filter = filterSupplier == null ? null : filterSupplier.get();
	}

	@Override
	protected BaseGridWrapper<ID, T> constructGridWrapper() {
		ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<ID, T>(getService(), getEntityModel(),
				getQueryType(), getFormOptions(), filter, getFieldFilters(), getSortOrders(), false, getJoins()) {

			@Override
			protected void postProcessDataProvider(DataProvider<T, SerializablePredicate<T>> provider) {
				ServiceBasedSplitLayout.this.postProcessDataProvider(provider);
			}

			@Override
			protected void onSelect(Object selected) {
				setSelectedItems(selected);
				checkComponentState(getSelectedItem());
				if (getSelectedItem() != null) {
					detailsMode(getSelectedItem());
				}
			}
		};
		postConfigureGridWrapper(wrapper);
		wrapper.setMaxResults(getMaxResults());
		wrapper.build();
		return wrapper;
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
					SerializablePredicate<T> quickFilter = quickSearchFilterSupplier == null ? null
							: quickSearchFilterSupplier.apply(text);

					// if no custom filter is defined, filter on main attribute
					if (quickFilter == null && getEntityModel().getMainAttributeModel() != null) {
						quickFilter = new LikePredicate<>(getEntityModel().getMainAttributeModel().getPath(),
								"%" + text + "%", false);
					}

					SerializablePredicate<T> temp = quickFilter;
					if (getFilter() != null) {
						temp = new AndPredicate<>(quickFilter, getFilter());
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

	protected DataProvider<T, SerializablePredicate<T>> getDataProvider() {
		return getGridWrapper().getDataProvider();
	}

	public SerializablePredicate<T> getFilter() {
		return filter;
	}

	public Supplier<SerializablePredicate<T>> getFilterSupplier() {
		return filterSupplier;
	}

	@Override
	public ServiceBasedGridWrapper<ID, T> getGridWrapper() {
		return (ServiceBasedGridWrapper<ID, T>) super.getGridWrapper();
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public Function<String, SerializablePredicate<T>> getQuickSearchFilterSupplier() {
		return quickSearchFilterSupplier;
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

	/**
	 * Sets the function (supplier) used for constructing the default filter
	 * 
	 * @param filterSupplier
	 */
	public void setFilterSupplier(Supplier<SerializablePredicate<T>> filterSupplier) {
		this.filterSupplier = filterSupplier;
	}

	/**
	 * Sets the function used for constructing the quick search filter. This will
	 * override the default quick search filter that searches on the main attribute
	 * 
	 * @param quickSearchFilterSupplier
	 */
	public void setQuickSearchFilterSupplier(Function<String, SerializablePredicate<T>> quickSearchFilterSupplier) {
		this.quickSearchFilterSupplier = quickSearchFilterSupplier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setSelectedItems(Object selectedItems) {
		if (selectedItems != null) {
			if (selectedItems instanceof Collection<?>) {
				Collection<?> col = (Collection<?>) selectedItems;
				if (!col.isEmpty()) {
					T t = (T) col.iterator().next();
					setSelectedItem(getService().fetchById(t.getId(), getDetailJoins()));
				} else {
					setSelectedItem(null);
					emptyDetailView();
				}
			} else {
				T t = (T) selectedItems;
				setSelectedItem(getService().fetchById(t.getId(), getDetailJoins()));
			}
		} else {
			// nothing selected
			setSelectedItem(null);
			emptyDetailView();
		}
	}

}
