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

	private final Object rowKeyValue;

	private Map<Object, Map<String, Object>> values = new HashMap<>();

	private Map<Object, Object> fixedValues = new HashMap<>();

	public PivotedItem(Object rowKeyValue) {
		this.rowKeyValue = rowKeyValue;
	}

	public Object getRowKeyValue() {
		return rowKeyValue;
	}

	public void setValue(Object columnKey, String propertyValue, Object value) {
		values.putIfAbsent(columnKey, new HashMap<>());
		values.get(columnKey).put(propertyValue, value);
	}

	public Object getValue(Object columnKey, String propertyValue) {
		if (!values.containsKey(columnKey)) {
			return null;
		}
		return values.get(columnKey).get(propertyValue);
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

	public void setFixedValue(Object key, Object value) {
		fixedValues.put(key, value);
	}

	public Object getFixedValue(Object key) {
		return fixedValues.get(key);
	}
}
