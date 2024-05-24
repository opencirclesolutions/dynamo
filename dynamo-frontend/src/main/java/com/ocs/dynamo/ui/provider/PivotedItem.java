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
package com.ocs.dynamo.ui.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import com.ocs.dynamo.ui.utils.VaadinUtils;

/**
 * A data container class that represents a single row in a pivoted grid
 * 
 * @author Bas Rutten
 *
 */
public class PivotedItem {

	/**
	 * Unique identifying key for this row
	 */
	private final Object rowKeyValue;

	/**
	 * Mapping from column names to values
	 */
	private final Map<Object, Map<String, Object>> values = new HashMap<>();

	/**
	 * Mapping from fixed column name to fixed column value
	 */
	private final Map<Object, Object> fixedValues = new HashMap<>();

	/**
	 * Mapping from property name to sum of values
	 */
	private final Map<String, BigDecimal> sumValues = new HashMap<>();

	/**
	 * Mapping from property name to sum of values
	 */
	private final Map<String, Integer> countValues = new HashMap<>();

	public PivotedItem(Object rowKeyValue) {
		this.rowKeyValue = rowKeyValue;
	}

	/**
	 * Returns the average value for a property
	 * 
	 * @param property the property name
	 * @return the resulting average
	 */
	public BigDecimal getAverageValue(String property) {
		BigDecimal bv = sumValues.get(property);
		if (bv != null) {
			int cv = countValues.get(property);
			if (cv > 0) {
				return bv.divide(BigDecimal.valueOf(cv), 2, RoundingMode.HALF_UP);
			}
			return BigDecimal.ZERO;
		}
		return null;
	}

	public Integer getCountValue(String property) {
		return countValues.get(property);
	}

	public Object getFixedValue(Object key) {
		return fixedValues.get(key);
	}

	public String getFormattedValue(Object columnKey, String propertyValue) {
		if (!values.containsKey(columnKey)) {
			return null;
		}
		Object obj = values.get(columnKey).get(propertyValue);
		if (obj instanceof BigDecimal) {
			return VaadinUtils.bigDecimalToString(false, true, (BigDecimal) obj);
		} else if (obj instanceof Long) {
			return VaadinUtils.longToString(true, false, (Long) obj);
		} else if (obj instanceof Integer) {
			return VaadinUtils.integerToString(true, false, (Integer) obj);
		}
		return obj == null ? "" : obj.toString();
	}

	public Object getRowKeyValue() {
		return rowKeyValue;
	}

	public BigDecimal getSumValue(String property) {
		return sumValues.get(property);
	}

	public Object getValue(Object columnKey, String propertyValue) {
		if (!values.containsKey(columnKey)) {
			return null;
		}
		return values.get(columnKey).get(propertyValue);
	}

	public void setFixedValue(Object key, Object value) {
		fixedValues.put(key, value);
	}

	public void setValue(Object columnKey, String propertyValue, Object value) {
		values.putIfAbsent(columnKey, new HashMap<>());
		values.get(columnKey).put(propertyValue, value);

		if (value instanceof Number number) {
			sumValues.putIfAbsent(propertyValue, BigDecimal.ZERO);
			BigDecimal bd = sumValues.get(propertyValue);
			bd = bd.add(BigDecimal.valueOf(number.doubleValue()));
			sumValues.put(propertyValue, bd);

			countValues.putIfAbsent(propertyValue, 0);
			countValues.put(propertyValue, countValues.get(propertyValue) + 1);
		}
	}
}
