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
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.form.ModelBasedSearchForm;
import com.ocs.dynamo.ui.composite.grid.GridWrapper;
import com.ocs.dynamo.ui.composite.grid.PivotGridWrapper;
import com.ocs.dynamo.ui.provider.PivotAggregationType;
import com.ocs.dynamo.ui.provider.PivotDataProvider;
import com.ocs.dynamo.ui.provider.PivotedItem;
import com.ocs.dynamo.ui.provider.QueryType;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.function.SerializablePredicate;

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

	private String columnKeyProperty;

	private List<String> fixedColumnKeys;

	private PivotGridWrapper<ID, T> pivotGridWrapper;

	/**
	 * Header mapping function. Excepts the column key and the property
	 */
	private BiFunction<Object, Object, String> headerMapper = (a, b) -> a.toString();

	/**
	 * Explicitly overridden header mapper used for exporting only
	 */
	private BiFunction<Object, Object, String> exportHeaderMapper = null;

	private Function<String, String> fixedHeaderMapper = Function.identity();

	private BiFunction<String, Object, String> customFormatter;

	private List<String> pivotedProperties;

	private List<String> hiddenPivotedProperties;

	private List<Object> possibleColumnKeys;

	private String rowKeyProperty;

	private Supplier<Integer> sizeSupplier;

	private Map<String, PivotAggregationType> aggregationMap = new HashMap<>();

	private Map<String, Class<?>> aggregationClassMap = new HashMap<>();

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
				QueryType.ID_BASED, getFormOptions(), getSearchForm().extractFilter(), getSortOrders(), getJoins()) {

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
		wrapper.setFixedHeaderMapper(getFixedHeaderMapper());
		wrapper.setCustomFormatter(getCustomFormatter());
		wrapper.setSizeSupplier(getSizeSupplier());
		wrapper.setPossibleColumnKeys(getPossibleColumnKeys());
		wrapper.setFixedColumnKeys(getFixedColumnKeys());
		wrapper.setAggregationMap(aggregationMap);
		wrapper.setAggregationClassMap(aggregationClassMap);
		wrapper.setIncludeAggregateRow(isIncludeAggregateRow());
		wrapper.setExportHeaderMapper(getExportHeaderMapper());

		postConfigureGridWrapper(wrapper);
		wrapper.build();

		if (getFormOptions().isSearchImmediately()) {
			getSearchForm().setSearchable(wrapper);
		}

		return wrapper;
	}

	/**
	 * Constructs the search form
	 * 
	 * @return the search form
	 */
	@Override
	protected ModelBasedSearchForm<ID, T> constructSearchForm() {
		// by default, do not pass a searchable object, in order to prevent an
		// unnecessary and
		// potentially unfiltered search
		ModelBasedSearchForm<ID, T> result = new ModelBasedSearchForm<ID, T>(null, getEntityModel(), getFormOptions(),
				this.getDefaultFilters(), this.getFieldFilters()) {

			private static final long serialVersionUID = 8929442625027442714L;

			@Override
			protected void afterSearchFieldToggle(boolean visible) {
				PivotSearchLayout.this.afterSearchFieldToggle(visible);
			}

			@Override
			protected void afterSearchPerformed() {
				PivotSearchLayout.this.afterSearchPerformed();
			}

			@Override
			protected Component constructCustomField(EntityModel<T> entityModel, AttributeModel attributeModel) {
				return PivotSearchLayout.this.constructCustomField(entityModel, attributeModel, false, true);
			}

			@Override
			protected void postProcessButtonBar(FlexLayout buttonBar) {
				PivotSearchLayout.this.postProcessSearchButtonBar(buttonBar);
			}

			@Override
			protected void validateBeforeSearch() {
				PivotSearchLayout.this.validateBeforeSearch();
			}
		};
		result.setFieldEntityModels(getFieldEntityModels());
		result.build();
		return result;
	}

	@Override
	protected void detailsMode(T entity) {
		// not supported
	}

	public String getColumnKeyProperty() {
		return columnKeyProperty;
	}

	public BiFunction<String, Object, String> getCustomFormatter() {
		return customFormatter;
	}

	public List<String> getFixedColumnKeys() {
		return fixedColumnKeys;
	}

	public Function<String, String> getFixedHeaderMapper() {
		return fixedHeaderMapper;
	}

	@Override
	public GridWrapper<ID, T, PivotedItem> getGridWrapper() {
		if (pivotGridWrapper == null) {
			pivotGridWrapper = constructGridWrapper();
		}
		return pivotGridWrapper;
	}

	public BiFunction<Object, Object, String> getHeaderMapper() {
		return headerMapper;
	}

	public List<String> getHiddenPivotedProperties() {
		return hiddenPivotedProperties;
	}

	public List<String> getPivotedProperties() {
		return pivotedProperties;
	}

	public List<Object> getPossibleColumnKeys() {
		return possibleColumnKeys;
	}

	public String getRowKeyProperty() {
		return rowKeyProperty;
	}

	@Override
	public ModelBasedSearchForm<ID, T> getSearchForm() {
		return (ModelBasedSearchForm<ID, T>) super.getSearchForm();
	}

	public Supplier<Integer> getSizeSupplier() {
		return sizeSupplier;
	}

	public boolean isIncludeAggregateRow() {
		return includeAggregateRow;
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

	public void setColumnKeyProperty(String columnKeyProperty) {
		this.columnKeyProperty = columnKeyProperty;
	}

	public void setCustomFormatter(BiFunction<String, Object, String> customFormatter) {
		this.customFormatter = customFormatter;
	}

	public void setFixedColumnKeys(List<String> fixedColumnKeys) {
		this.fixedColumnKeys = fixedColumnKeys;
	}

	public void setFixedHeaderMapper(Function<String, String> fixedHeaderMapper) {
		this.fixedHeaderMapper = fixedHeaderMapper;
	}

	public void setHeaderMapper(BiFunction<Object, Object, String> headerMapper) {
		this.headerMapper = headerMapper;
	}

	public void setHiddenPivotedProperties(List<String> hiddenPivotedProperties) {
		this.hiddenPivotedProperties = hiddenPivotedProperties;
	}

	public void setIncludeAggregateRow(boolean includeAggregateRow) {
		this.includeAggregateRow = includeAggregateRow;
	}

	public void setPivotedProperties(List<String> pivotedProperties) {
		this.pivotedProperties = pivotedProperties;
	}

	public void setPossibleColumnKeys(List<Object> possibleColumnKeys) {
		this.possibleColumnKeys = possibleColumnKeys;
	}

	public void setRowKeyProperty(String rowKeyProperty) {
		this.rowKeyProperty = rowKeyProperty;
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

	public void setSizeSupplier(Supplier<Integer> sizeSupplier) {
		this.sizeSupplier = sizeSupplier;
	}

	public BiFunction<Object, Object, String> getExportHeaderMapper() {
		return exportHeaderMapper;
	}

	public void setExportHeaderMapper(BiFunction<Object, Object, String> exportHeaderMapper) {
		this.exportHeaderMapper = exportHeaderMapper;
	}

}
