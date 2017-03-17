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

import com.google.common.collect.Lists;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class IntegerSumGeneratorTest {

    @Test
    public void test() {

        TestY test = new TestY();
        test.setWeek1_prop(12);
        test.setWeek2_prop(13);
        test.setWeek3_prop(14);
        // this value will NOT be added
        test.setWeek4_prop(new BigDecimal(15));
        // this value will NOT be added
        test.setSkip(15);

        BeanItemContainer<TestY> container = new BeanItemContainer<>(TestY.class,
                Lists.newArrayList(test));
        Table table = new Table("", container);

        IntegerSumGenerator generator = new IntegerSumGenerator("prop");
        Assert.assertEquals(Integer.class, generator.getType());

        Object itemId = table.getItemIds().iterator().next();

        Integer value = generator.getValue(table.getItem(itemId), itemId, null);
        Assert.assertEquals(39, value.intValue());
    }

}
