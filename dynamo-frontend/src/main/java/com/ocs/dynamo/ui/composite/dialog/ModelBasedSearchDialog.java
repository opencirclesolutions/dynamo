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
package com.ocs.dynamo.ui.composite.dialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.ComponentContext;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.ui.composite.layout.SearchOptions;
import com.ocs.dynamo.ui.composite.layout.SimpleSearchLayout;
import com.ocs.dynamo.ui.provider.QueryType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * A dialog that contains a search form based on the Entity Model
 * 
 * @author bas.rutten
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity to search for
 */
@Getter
@Setter
public class ModelBasedSearchDialog<ID extends Serializable, T extends AbstractEntity<ID>> extends SimpleModalDialog {

	private static final long serialVersionUID = -7158664165266474097L;

	private List<String> columnThresholds = new ArrayList<>();

	private EntityModel<T> entityModel;

	private Map<String, SerializablePredicate<?>> fieldFilters = new HashMap<>();

	private List<SerializablePredicate<T>> filters;

	private FetchJoinInformation[] joins;

	private SimpleSearchLayout<ID, T> searchLayout;

	private BaseService<ID, T> service;

	private List<SortOrder<?>> sortOrders;

	private Runnable afterOpen;

	private SearchOptions searchOptions;

	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessSearchButtonBar;

	@Getter
	@Setter
	private Consumer<FlexLayout> postProcessMainButtonBar;

	/**
	 * Constructor
	 * 
	 * @param service            the service used to query the database
	 * @param entityModel        the entity model
	 * @param filters            the search filters
	 * @param sortOrders         the sort orders
	 * @param joins              the fetch joins
	 */
	public ModelBasedSearchDialog(BaseService<ID, T> service, EntityModel<T> entityModel,
			List<SerializablePredicate<T>> filters, List<SortOrder<?>> sortOrders, SearchOptions searchOptions,
			FetchJoinInformation... joins) {
		super(true);

		this.service = service;
		this.entityModel = entityModel;
		this.sortOrders = sortOrders != null ? sortOrders : new ArrayList<>();
		this.filters = filters;
		this.searchOptions = searchOptions;
		this.joins = joins;

		setTitle(message("ocs.search.title", entityModel.getDisplayNamePlural(VaadinUtils.getLocale())));
		buildMain();

	}

	/**
	 * Adds a field filter for a property
	 * 
	 * @param property the property for which to add a field filter
	 * @param filter   the field filter
	 */
	public void addFieldFilter(String property, SerializablePredicate<?> filter) {
		this.fieldFilters.put(property, filter);
	}

	private void buildMain() {
		setBuildMainLayout(parent -> {

			ComponentContext<ID, T> context = ComponentContext.<ID, T>builder().popup(true)
					.multiSelect(searchOptions.isMultiSelect())
					.useCheckboxesForMultiSelect(searchOptions.isUseCheckboxesForMultiSelect()).build();

			FormOptions formOptions = new FormOptions().setReadOnly(true).setDetailsModeEnabled(false)
					.setSearchImmediately(searchOptions.isSearchImmediately())
					.setEnableAdvancedSearchMode(searchOptions.isAdvancedSearchMode());

			searchLayout = new SimpleSearchLayout<>(service, entityModel, QueryType.ID_BASED, formOptions, null, joins);
			searchLayout.setComponentContext(context);
			searchLayout.setDefaultFilters(filters);

			searchLayout.setGridHeight(SystemPropertyUtils.getDefaultSearchDialogGridHeight());
			for (SortOrder<?> order : sortOrders) {
				searchLayout.addSortOrder(order);
			}

			searchLayout.setSearchColumnThresholds(getColumnThresholds());
			searchLayout.setPostProcessSearchButtonBar(postProcessSearchButtonBar);
			searchLayout.setPostProcessMainButtonBar(postProcessMainButtonBar);

			this.fieldFilters.forEach((key, value) -> searchLayout.addFieldFilter(key, value));

			// add double click listener for quickly selecting item and closing the
			// dialog
			searchLayout.getGridWrapper().getGrid().addItemDoubleClickListener(event -> {
				select(event.getItem());
				getOkButton().click();
			});
			parent.add(searchLayout);
		});
	}

	public T getSelectedItem() {
		return searchLayout.getSelectedItem();
	}

	public Collection<T> getSelectedItems() {
		return searchLayout.getSelectedItems();
	}

	@Override
	public void open() {
		super.open();
		if (afterOpen != null) {
			afterOpen.run();
		}
	}

	public void search() {
		searchLayout.search();
	}

	/**
	 * Select one or more items in the grid
	 * 
	 * @param selectedItems the items to select
	 */
	@SuppressWarnings("unchecked")
	public void select(Object selectedItems) {
		if (selectedItems instanceof Collection) {
			Collection<T> collection = (Collection<T>) selectedItems;
			for (T entity : collection) {
				searchLayout.getGridWrapper().getGrid().select(entity);
			}
		} else {
			T entity = (T) selectedItems;
			searchLayout.getGridWrapper().getGrid().select(entity);
		}
	}

	public void setFilters(List<SerializablePredicate<T>> filters) {
		this.filters = filters;
		if (searchLayout != null) {
			searchLayout.setDefaultFilters(filters);
		}
	}
}
