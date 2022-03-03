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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.grid.GridWrapper;
import com.ocs.dynamo.ui.composite.grid.PivotGridWrapper;
import com.ocs.dynamo.ui.provider.PivotAggregationType;
import com.ocs.dynamo.ui.provider.PivotDataProvider;
import com.ocs.dynamo.ui.provider.PivotedItem;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.Setter;

/**
 * Layout for searching in a pivoted grid
 * 
 * @author Bas Rutten
 *
 * @param <ID>
 * @param <T>
 */
public class PivotSearchLayout<ID extends Serializable, T extends AbstractEntity<ID>>
		extends AbstractSearchLayout<ID, T, PivotedItem> {

	private static final long serialVersionUID = 9118254267715180544L;

	@Getter
	@Setter
	private String columnKeyProperty;

	@Getter
	@Setter
	private List<String> fixedColumnKeys;

	private PivotGridWrapper<ID, T> pivotGridWrapper;

	/**
	 * Header mapping function. Expects the column key and the property
	 */
	@Getter
	@Setter
	private BiFunction<Object, Object, String> headerMapper = (a, b) -> a.toString();

	/**
	 * Sub header mapping function. Expects the column key and the property
	 */
	@Getter
	@Setter
	private BiFunction<Object, Object, String> subHeaderMapper = (a, b) -> b.toString();

	/**
	 * Explicitly overridden header mapper used for exporting only
	 */
	@Getter
	@Setter
	private BiFunction<Object, Object, String> exportHeaderMapper = null;

	/**
	 * Bifunction used to map pivot column subheaders for export only
	 */
	@Getter
	@Setter
	private BiFunction<Object, Object, String> exportSubHeaderMapper = null;

	@Getter
	@Setter
	private Function<String, String> fixedHeaderMapper = Function.identity();

	@Getter
	@Setter
	private BiFunction<String, Object, String> customFormatter;

	@Getter
	@Setter
	private List<String> pivotedProperties;

	@Getter
	@Setter
	private List<String> hiddenPivotedProperties;

	@Getter
	@Setter
	private List<Object> possibleColumnKeys;

	@Getter
	@Setter
	private String rowKeyProperty;

	@Getter
	@Setter
	private Supplier<Integer> sizeSupplier;

	@Getter
	@Setter
	private Map<String, PivotAggregationType> aggregationMap = new HashMap<>();

	@Getter
	@Setter
	private Map<String, Class<?>> aggregationClassMap = new HashMap<>();

	@Getter
	@Setter
	private boolean includeAggregateRow;

	/**
	 * 
	 * @param service
	 * @param entityModel
	 * @param formOptions
	 * @param sortOrder
	 * @param joins
	 */
	public PivotSearchLayout(BaseService<ID, T> service, EntityModel<T> entityModel, FormOptions formOptions,
			SortOrder<?> sortOrder, FetchJoinInformation... joins) {
		super(service, entityModel, QueryType.ID_BASED, formOptions, sortOrder, joins);
	}

	public void addAggregation(String property, PivotAggregationType type, Class<?> clazz) {
		aggregationMap.put(property, type);
		aggregationClassMap.put(property, clazz);
	}

	@Override
	public void clearGridWrapper() {
		super.clearGridWrapper();
		this.pivotGridWrapper = null;
	}

	public PivotGridWrapper<ID, T> constructGridWrapper() {

		PivotGridWrapper<ID, T> wrapper = new PivotGridWrapper<ID, T>(getService(), getEntityModel(),
				QueryType.ID_BASED, getFormOptions(), getComponentContext(), getSearchForm().extractFilter(),
				getSortOrders(), getJoins()) {

			private static final long serialVersionUID = -7522369124218869608L;

			@Override
			protected SerializablePredicate<T> beforeSearchPerformed(SerializablePredicate<T> filter) {
				return PivotSearchLayout.this.beforeSearchPerformed(filter);
			}

			@Override
			protected void postProcessDataProvider(PivotDataProvider<ID, T> provider) {
				PivotSearchLayout.this.postProcessDataProvider(provider);
			}
		};

		wrapper.setRowKeyProperty(getRowKeyProperty());
		wrapper.setColumnKeyProperty(getColumnKeyProperty());
		wrapper.setPivotedProperties(getPivotedProperties());
		wrapper.setHiddenPivotedProperties(getHiddenPivotedProperties());
		wrapper.setHeaderMapper(getHeaderMapper());
		wrapper.setSubHeaderMapper(getSubHeaderMapper());
		wrapper.setFixedHeaderMapper(getFixedHeaderMapper());
		wrapper.setCustomFormatter(getCustomFormatter());
		wrapper.setSizeSupplier(getSizeSupplier());
		wrapper.setPossibleColumnKeys(getPossibleColumnKeys());
		wrapper.setFixedColumnKeys(getFixedColumnKeys());
		wrapper.setAggregationMap(aggregationMap);
		wrapper.setAggregationClassMap(aggregationClassMap);
		wrapper.setIncludeAggregateRow(isIncludeAggregateRow());
		wrapper.setExportHeaderMapper(getExportHeaderMapper());
		wrapper.setExportSubHeaderMapper(getExportSubHeaderMapper());

		postConfigureGridWrapper(wrapper);
		wrapper.build();

		if (getFormOptions().isSearchImmediately()) {
			getSearchForm().setSearchable(wrapper);
		}

		return wrapper;
	}

	@Override
	protected ModelBasedSearchForm<ID, T> constructSearchForm() {
		ModelBasedSearchForm<ID, T> searchForm = new ModelBasedSearchForm<ID, T>(null, getEntityModel(),
				getFormOptions(), getComponentContext(), this.getDefaultFilters(), this.getFieldFilters());

		initSearchForm(searchForm);
		searchForm.build();
		return searchForm;
	}

	@Override
	protected void detailsMode(T entity) {
		// not supported
	}

	@Override
	public GridWrapper<ID, T, PivotedItem> getGridWrapper() {
		if (pivotGridWrapper == null) {
			pivotGridWrapper = constructGridWrapper();
		}
		return pivotGridWrapper;
	}

	@Override
	public ModelBasedSearchForm<ID, T> getSearchForm() {
		return (ModelBasedSearchForm<ID, T>) super.getSearchForm();
	}

	/**
	 * Select one or more items
	 * 
	 * @param selectedItems the item or items to select
	 */
	@Override
	public void select(Object selectedItems) {
		// do nothing (not supported)
	}

	/**
	 * Sets a certain search value (for a property with a single value to search on)
	 * 
	 * @param propertyId the property to search for
	 * @param value      the desired value
	 */
	@Override
	public void setSearchValue(String propertyId, Object value) {
		getSearchForm().setSearchValue(propertyId, value);
	}

	/**
	 * Sets a certain search value (for a property with an upper and a lower bound)
	 * 
	 * @param propertyId the property to search for
	 * @param value      the value of the lower bound
	 * @param auxValue   the value of the upper bound
	 */
	@Override
	public void setSearchValue(String propertyId, Object value, Object auxValue) {
		getSearchForm().setSearchValue(propertyId, value, auxValue);
	}

}
