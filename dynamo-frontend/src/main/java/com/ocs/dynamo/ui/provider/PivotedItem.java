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
