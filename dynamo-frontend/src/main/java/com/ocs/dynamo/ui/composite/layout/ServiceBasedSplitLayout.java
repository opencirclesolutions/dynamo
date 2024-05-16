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
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.AndPredicate;
import com.ocs.dynamo.filter.LikePredicate;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.UIHelper;
import com.ocs.dynamo.ui.composite.dialog.ModelBasedSearchDialog;
import com.ocs.dynamo.ui.composite.grid.BaseGridWrapper;
import com.ocs.dynamo.ui.composite.grid.ServiceBasedGridWrapper;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A split layout - contains both a grid and a details view - that uses a
 * service to fetch data
 * 
 * @author bas.rutten
 * @param <ID> type of the primary key
 * @param <T>  type of the entity
 */
public class ServiceBasedSplitLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseSplitLayout<ID, T> {

	private static final long serialVersionUID = 1068860513192819804L;

	/**
	 * The search filter used to limit the results
	 */
	@Getter
	protected SerializablePredicate<T> filter;

	/**
	 * Supplier for creating the filter when needed
	 */
	@Getter
	@Setter
	private Supplier<SerializablePredicate<T>> filterCreator;

	/**
	 * The query type (ID based or paging) used to query the database
	 */
	@Getter
	private final QueryType queryType;

	/**
	 * Supplier for creating the quick search filter
	 */
	@Getter
	@Setter
	private Function<String, SerializablePredicate<T>> quickSearchFilterCreator;

	/**
	 * Constructor
	 *
	 * @param service     the service for retrieving data from the database
	 * @param entityModel the entity model
	 * @param queryType   the desired query type
	 * @param formOptions the form options
	 * @param sortOrder   the sort order
	 * @param joins the fetch joints to use
	 */
	public ServiceBasedSplitLayout(BaseService<ID, T> service, EntityModel<T> entityModel, QueryType queryType,
			FormOptions formOptions, SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, formOptions, sortOrder, joins);
		this.queryType = queryType;
	}

	@Override
	protected void buildFilter() {
		filter = filterCreator == null ? null : filterCreator.get();
	}

	@Override
	protected BaseGridWrapper<ID, T> constructGridWrapper() {

		// restore stored sort orders
		UIHelper helper = ServiceLocatorFactory.getServiceLocator().getService(UIHelper.class);
		if (helper != null) {
			List<SortOrder<?>> retrievedOrders = helper.retrieveSortOrders();
			if (getFormOptions().isPreserveSortOrders() && retrievedOrders != null && !retrievedOrders.isEmpty()) {
				setSortOrders(retrievedOrders);
			}
		}

		ServiceBasedGridWrapper<ID, T> wrapper = new ServiceBasedGridWrapper<>(getService(), getEntityModel(),
				getQueryType(), getFormOptions(), getComponentContext(), filter, getFieldFilters(), getSortOrders(),
				false, getJoins()) {

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
			TextField searchField = new TextField("");
			searchField.addClassName("quickSearchField");

			// respond to the user entering a search term
			searchField.addValueChangeListener(event -> {
				String text = event.getValue();
				if (!StringUtils.isEmpty(text)) {
					executeQuickSearch(text);
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
			if (selectedItems instanceof Collection<?> col) {
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

	/**
	 * Executes a quick search
	 * 
	 * @param text the text string to search form
	 */
	private void executeQuickSearch(String text) {
		SerializablePredicate<T> quickFilter = quickSearchFilterCreator == null ? null
				: quickSearchFilterCreator.apply(text);

		// if no custom filter is defined, filter on main attribute
		if (quickFilter == null && getEntityModel().getMainAttributeModel() != null) {
			quickFilter = new LikePredicate<>(getEntityModel().getMainAttributeModel().getPath(), "%" + text + "%",
					false);
		}

		SerializablePredicate<T> temp = quickFilter;
		if (getFilter() != null) {
			temp = new AndPredicate<>(quickFilter, getFilter());
		}
		emptyDetailView();
		getGridWrapper().search(temp);
	}

	protected List<SerializablePredicate<T>> createSearchDialogFilter() {
		return filterCreator == null ? Collections.emptyList() : List.of(filterCreator.get());
	}

	@Override
	protected Button constructPopupSearchButton() {
		if (getFormOptions().isShowSplitLayoutSearchButton()) {
			Button button = new Button(message("ocs.search"));
			button.setIcon(VaadinIcon.SEARCH.create());
			button.addClickListener(event -> {
				ModelBasedSearchDialog<ID, T> searchDialog = new ModelBasedSearchDialog<>(getService(),
						getEntityModel(), createSearchDialogFilter(), getSortOrders(),
						SearchOptions.builder().multiSelect(false).advancedSearchMode(false).build(), getDetailJoins());
				searchDialog.setOnClose(() -> onSearchDialogClose(searchDialog));
				searchDialog.buildAndOpen();
			});

			return button;
		}
		return null;
	}

	@Override
	protected Button constructPopupClearButton() {
		if (getFormOptions().isShowSplitLayoutSearchButton()) {
			Button button = new Button(message("ocs.clear"));
			button.setIcon(VaadinIcon.ERASER.create());
			button.addClickListener(event -> {
				if (getQuickSearchField() != null) {
					getQuickSearchField().clear();
				}
				reload();
			});

			return button;
		}
		return null;
	}

	/**
	 * Respond to the user closing the search dialog by applying the search filters
	 * from the dialog to the grid
	 * 
	 * @param dialog the search dialog
	 * @return whether to close the dialog
	 */
	private boolean onSearchDialogClose(ModelBasedSearchDialog<ID, T> dialog) {

		if (getQuickSearchField() != null) {
			getQuickSearchField().clear();
		}

		SerializablePredicate<T> filter = dialog.getSearchLayout().getSearchForm().extractFilter();
		this.filter = filter;
		refresh();
		getGridWrapper().setFilter(filter);
		emptyDetailView();

		return true;
	}
}
