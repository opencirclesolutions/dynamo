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
package com.ocs.dynamo.ui.grid;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;

public class BigDecimalAverageGeneratorTest {

    @Test
    public void test() {

        TestZ test = new TestZ();
        test.setWeek1_prop(new BigDecimal(12));
        test.setWeek2_prop(new BigDecimal(13));
        test.setWeek3_prop(new BigDecimal(15));
        test.setWeek4_prop(15);
        test.setSkip(new BigDecimal(20));

        BeanItemContainer<TestZ> container = new BeanItemContainer<TestZ>(TestZ.class,
                Lists.newArrayList(test));
        Table table = new Table("", container);

        BigDecimalAverageGenerator generator = new BigDecimalAverageGenerator("prop");
        Assert.assertEquals(BigDecimal.class, generator.getType());

        Object itemId = table.getItemIds().iterator().next();

        // three values 12, 13, and 15 should be averaged - other values should
        // be skipped
        BigDecimal value = generator.getValue(table.getItem(itemId), itemId, null);
        Assert.assertEquals(13.33, value.doubleValue(), 0.001);
    }

}
