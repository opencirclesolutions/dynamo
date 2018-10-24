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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertyValueGenerator;

/**
 * A property value generator for computing the average of a range of BigDecimals
 * 
 * @author bas.rutten
 */
public class BigDecimalAverageGenerator extends PropertyValueGenerator<BigDecimal> {

    private static final long serialVersionUID = -4000091542049888468L;

    private String propertyNameFilter;

    public BigDecimalAverageGenerator(String propertyNameFilter) {
        this.propertyNameFilter = propertyNameFilter;
    }

    @Override
    public BigDecimal getValue(Item item, Object itemId, Object propertyId) {
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        Collection<?> cols = item.getItemPropertyIds();
        for (Object o : cols) {
            if (o.toString().contains("_" + propertyNameFilter)) {
                Property<?> prop = item.getItemProperty(o);
                if (prop.getValue() != null && prop.getValue() instanceof BigDecimal) {
                    BigDecimal v = (BigDecimal) prop.getValue();
                    sum = sum.add(v);
                    count++;
                }
            }
        }
        return count == 0 ? null : sum.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP);
    }

    @Override
    public Class<BigDecimal> getType() {
        return BigDecimal.class;
    }

}
