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
package com.ocs.dynamo.ui.composite.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.ocs.dynamo.ui.provider.PivotAggregationType;

/**
 * Parameter object that contains data about how to export pivoted data
 * 
 * @author Bas Rutten
 *
 */
public class PivotParameters {

	/**
	 * The property that uniquely identifies a row in the pivoted data set
	 */
	private String rowKeyProperty;

	private String columnKeyProperty;

	private List<String> fixedColumnKeys;

	private List<String> pivotedProperties;

	private List<String> hiddenPivotedProperties = new ArrayList<>();

	private Map<String, PivotAggregationType> aggregationMap = new HashMap<>();

	private List<Object> possibleColumnKeys;

	private Function<String, String> fixedHeaderMapper = Function.identity();

	private BiFunction<Object, Object, String> headerMapper = (a, b) -> a.toString();

	private BiFunction<Object, Object, String> subHeaderMapper = (a, b) -> b.toString();

	private boolean includeAggregateRow;

	private Map<String, Class<?>> aggregationClassMap = new HashMap<>();

	public String getRowKeyProperty() {
		return rowKeyProperty;
	}

	public void setRowKeyProperty(String rowKeyProperty) {
		this.rowKeyProperty = rowKeyProperty;
	}

	public String getColumnKeyProperty() {
		return columnKeyProperty;
	}

	public void setColumnKeyProperty(String columnKeyProperty) {
		this.columnKeyProperty = columnKeyProperty;
	}

	public List<String> getFixedColumnKeys() {
		return fixedColumnKeys;
	}

	public void setFixedColumnKeys(List<String> fixedColumnKeys) {
		this.fixedColumnKeys = fixedColumnKeys;
	}

	public List<String> getPivotedProperties() {
		return pivotedProperties;
	}

	public void setPivotedProperties(List<String> pivotedProperties) {
		this.pivotedProperties = pivotedProperties;
	}

	public Function<String, String> getFixedHeaderMapper() {
		return fixedHeaderMapper;
	}

	public void setFixedHeaderMapper(Function<String, String> fixedHeaderMapper) {
		this.fixedHeaderMapper = fixedHeaderMapper;
	}

	public BiFunction<Object, Object, String> getHeaderMapper() {
		return headerMapper;
	}

	public void setHeaderMapper(BiFunction<Object, Object, String> headerMapper) {
		this.headerMapper = headerMapper;
	}

	public List<Object> getPossibleColumnKeys() {
		return possibleColumnKeys;
	}

	public void setPossibleColumnKeys(List<Object> possibleColumnKeys) {
		this.possibleColumnKeys = possibleColumnKeys;
	}

	public List<String> getHiddenPivotedProperties() {
		return hiddenPivotedProperties;
	}

	public void setHiddenPivotedProperties(List<String> hiddenPivotedProperties) {
		this.hiddenPivotedProperties = hiddenPivotedProperties;
	}

	public Map<String, PivotAggregationType> getAggregationMap() {
		return aggregationMap;
	}

	public void setAggregationMap(Map<String, PivotAggregationType> aggregationMap) {
		this.aggregationMap = aggregationMap;
	}

	public boolean isIncludeAggregateRow() {
		return includeAggregateRow;
	}

	public void setIncludeAggregateRow(boolean includeAggregateRow) {
		this.includeAggregateRow = includeAggregateRow;
	}

	public Map<String, Class<?>> getAggregationClassMap() {
		return aggregationClassMap;
	}

	public void setAggregationClassMap(Map<String, Class<?>> aggregationClassMap) {
		this.aggregationClassMap = aggregationClassMap;
	}

	public BiFunction<Object, Object, String> getSubHeaderMapper() {
		return subHeaderMapper;
	}

	public void setSubHeaderMapper(BiFunction<Object, Object, String> subHeaderMapper) {
		this.subHeaderMapper = subHeaderMapper;
	}

	public List<String> getShownAndHiddenProperties() {
		List<String> allProps = new ArrayList<>();
		allProps.addAll(getPivotedProperties());
		if (getHiddenPivotedProperties() != null) {
			allProps.addAll(getHiddenPivotedProperties());
		}
		return allProps;
	}

	public int getTotalNumberOfVariableColumns() {
		return possibleColumnKeys.size() * pivotedProperties.size();
	}
}
