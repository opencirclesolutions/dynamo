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
package com.ocs.dynamo.ui.container.pivot;

import java.util.Collection;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;

/**
 * @author Patrick Deenen
 */
public class PivotItem implements Item {

    private static final long serialVersionUID = 3972300096945691561L;

    private Map<Object, Item> columns;

    private Item firstColumn;

    private PivotContainer pivotContainer;

    /**
     * Constructs the composite item
     */
    public PivotItem(Map<Object, Item> columns, PivotContainer pivotContainer) {
        if (columns == null) {
            throw new AssertionError("columns is mandatory");
        }
        if (pivotContainer == null) {
            throw new AssertionError("pivotContainer is mandatory");
        }
        this.columns = columns;
        this.pivotContainer = pivotContainer;
    }

    /**
     * Get the property in the row. When the name is in the form "columnValue_columnPropertyId" then
     * a property in the column designated by the columnValue is assumed. Otherwise the property is
     * assumed to be equal over all columns and will be taken from the first item.
     * 
     * @see com.vaadin.data.Item#getItemProperty(java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Property getItemProperty(Object id) {
        Property result = null;
        if (id != null) {
            String[] ids = id.toString().split("_");
            if (ids.length > 1) {
                // Column
                if (columns.containsKey(ids[0])) {
                    result = columns.get(ids[0]).getItemProperty(ids[1]);
                } else {
                    result = new ObjectProperty(null, pivotContainer.getType(ids[1]));
                }
            } else if (ids.length > 0 && pivotContainer.getPropertyIds().contains(id)) {
                // Row?
                if (firstColumn == null && !columns.isEmpty()) {
                    firstColumn = columns.values().iterator().next();
                }
                if (firstColumn != null) {
                    result = firstColumn.getItemProperty(id);
                }
            }
        }
        return result;
    }

    public Item getColumn(Object id) {
        Item column = null;
        if (id != null) {
            String[] ids = id.toString().split("_");
            if (ids.length > 1) {
                // Column
                column = columns.get(ids[0]);
            } else if (ids.length > 0) {
                // Row?
                column = columns.get(id);
            }
        }
        return column;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Item#getItemPropertyIds()
     */
    @Override
    public Collection<?> getItemPropertyIds() {
        return pivotContainer.getPropertyIds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Item#addItemProperty(java.lang.Object, com.vaadin.data.Property)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public boolean addItemProperty(Object id, Property property) {
        Item column = getColumn(id);
        if (column != null) {
            // Existing column
            return column.addItemProperty(id, property);
        } else {
            // New column, create source item
            if (id != null) {
                String[] ids = id.toString().split("_");
                if (ids.length > 1) {
                    Item ni = createColumn(ids[0], ids[1], property.getValue());
                    if (ni != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings({ "unchecked" })
    protected Item createColumn(Object columnId, Object propertyId, Object columnValue) {
        // New column, create source item
        Container sc = pivotContainer.getSourceContainer();
        Object nid = sc.addItem();
        if (nid != null) {
            Item ni = sc.getItem(nid);
            Item c = columns.values().iterator().next();
            ni.getItemProperty(pivotContainer.getRowPropertyId())
                    .setValue(c.getItemProperty(pivotContainer.getRowPropertyId()).getValue());
            ni.getItemProperty(pivotContainer.getColumnPropertyId()).setValue(columnId);
            ni.getItemProperty(propertyId).setValue(columnValue);
            columns.put(columnId, ni);
            return ni;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Item#removeItemProperty(java.lang.Object)
     */
    @Override
    public boolean removeItemProperty(Object id) {
        Item column = getColumn(id);
        if (column != null) {
            return column.removeItemProperty(id);
        }
        return false;
    }
}
