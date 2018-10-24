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
package com.ocs.dynamo.ui.composite.grid;

import java.util.Collection;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertyValueGenerator;

/**
 * A property value generator for summing up a collection of Integers
 * 
 * @author bas.rutten
 */
public class IntegerSumGenerator extends PropertyValueGenerator<Integer> {

    private static final long serialVersionUID = -3866833325930425694L;

    private String propertyNameFilter;

    /**
     * Constructor
     * 
     * @param propertyNameFilter
     *            the property name filter - only properties that contain an underscore followed by
     *            the value of this field will be considered
     */
    public IntegerSumGenerator(String propertyNameFilter) {
        this.propertyNameFilter = propertyNameFilter;
    }

    @Override
    public Integer getValue(Item item, Object itemId, Object propertyId) {
        int sum = 0;
        Collection<?> cols = item.getItemPropertyIds();
        for (Object o : cols) {
            if (o.toString().contains("_" + propertyNameFilter)) {
                Property<?> prop = item.getItemProperty(o);
                if (prop.getValue() != null && prop.getValue() instanceof Integer) {
                    Integer v = (Integer) prop.getValue();
                    sum += v;
                }
            }
        }
        return sum;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }
}
