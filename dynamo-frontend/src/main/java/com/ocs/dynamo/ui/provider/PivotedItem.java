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

import java.util.HashMap;
import java.util.Map;

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

    public void setFixedValue(Object key, Object value) {
        fixedValues.put(key, value);
    }

    public Object getFixedValue(Object key) {
        return fixedValues.get(key);
    }
}
