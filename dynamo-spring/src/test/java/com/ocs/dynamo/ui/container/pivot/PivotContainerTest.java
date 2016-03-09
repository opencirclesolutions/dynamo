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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.ui.container.EnergyUsage;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;

public class PivotContainerTest {

    PivotContainer pivotContainer;

    List<EnergyUsage> usage;

    @Before
    public void setup() {
        // Define source container
        usage = Lists.newArrayList(new EnergyUsage("ABC", 1, 1), new EnergyUsage("ABC", 2, 2),
                new EnergyUsage("ABC", 3, 3), new EnergyUsage("ABC", 4, 4),
                new EnergyUsage("DEF", 1, 1), new EnergyUsage("DEF", 2, 2),
                new EnergyUsage("DEF", 3, 3), new EnergyUsage("DEF", 4, 4),
                new EnergyUsage("GHI", 2, 2), new EnergyUsage("GHI", 3, 3),
                new EnergyUsage("JKL", 4, 4));
        BeanItemContainer<EnergyUsage> sourceContainer = new BeanItemContainer<EnergyUsage>(
                EnergyUsage.class, usage);
        pivotContainer = new PivotContainer(sourceContainer, "week", "ean",
                Lists.newArrayList(1, 2, 3, 4), Lists.newArrayList("test"), 4);
    }

    @Test
    public void testPivotIdList() {

        Iterator<?> it = pivotContainer.getItemIds().iterator();
        while (it.hasNext()) {
            it.next();
        }

        Assert.assertEquals(0, pivotContainer.getPivotIdList().indexOf(0));
        Assert.assertEquals(1, pivotContainer.getPivotIdList().indexOf(4));
        Assert.assertEquals(2, pivotContainer.getPivotIdList().indexOf(8));
        Assert.assertEquals(-1, pivotContainer.getPivotIdList().indexOf(1));
    }

    @Test
    public void testNormalGet() {
        // Get a index
        Collection<?> ids = pivotContainer.getItemIds();
        Iterator<?> it = ids.iterator();
        Object id = it.next();
        id = it.next();
        assertNotNull(id);
        assertTrue(id instanceof Integer);
        Integer pid = (Integer) id;
        assertEquals(4, pid.intValue());

        // Get a row
        PivotItem row = (PivotItem) pivotContainer.getItem(pid);
        assertNotNull(row);
        assertEquals("DEF", row.getItemProperty("ean").getValue());
        assertNull(row.getItemProperty("week"));
        assertNotNull(row.getItemProperty("usage").getValue());
        assertEquals(1, row.getItemProperty("1_week").getValue());
        assertEquals(1, row.getItemProperty("1_usage").getValue());
        assertEquals("DEF", row.getItemProperty("2_ean").getValue());
        assertEquals(2, row.getItemProperty("2_week").getValue());
        assertEquals(2, row.getItemProperty("2_usage").getValue());
    }

    @Test
    public void testMissingColumns() {
        // Get a index
        Collection<?> ids = pivotContainer.getItemIds();
        Iterator<?> it = ids.iterator();
        Object id = it.next();
        id = it.next();
        id = it.next();
        assertNotNull(id);
        assertTrue(id instanceof Integer);
        Integer pid = (Integer) id;
        assertEquals(8, ((Integer) id).intValue());

        // Get a row
        PivotItem row = (PivotItem) pivotContainer.getItem(pid);
        assertNotNull(row);
        assertEquals("GHI", row.getItemProperty("ean").getValue());
        assertNull(row.getItemProperty("week"));
        assertNotNull(row.getItemProperty("usage").getValue());
        assertEquals("GHI", row.getItemProperty("2_ean").getValue());
        assertEquals(2, row.getItemProperty("2_week").getValue());
        assertEquals(2, row.getItemProperty("2_usage").getValue());
        // Test missing columns
        Property<?> p = row.getItemProperty("1_week");
        assertNotNull(p);
        assertNull(p.getValue());
        p = row.getItemProperty("4_week");
        assertNotNull(p);
        assertNull(p.getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateColumns() {
        // Get a index
        Collection<?> ids = pivotContainer.getItemIds();
        Iterator<?> it = ids.iterator();
        Object id = it.next();

        // Update a value
        PivotItem row = (PivotItem) pivotContainer.getItem(id);
        assertNotNull(row);
        assertEquals(2, row.getItemProperty("2_usage").getValue());
        row.getItemProperty("2_usage").setValue(8);
        assertEquals(8, row.getItemProperty("2_usage").getValue());
    }

    @Test
    public void testType() {
        // Test the type
        assertEquals(Integer.class, pivotContainer.getType("usage"));
        assertEquals(Integer.class, pivotContainer.getType("2_usage"));
    }

    @Test
    public void testFirstNextAndPrevious() {
        Integer first = (Integer) pivotContainer.firstItemId();
        Assert.assertEquals(0, first.intValue());

        Assert.assertTrue(pivotContainer.isFirstId(first));

        Integer second = (Integer) pivotContainer.nextItemId(first);
        Assert.assertEquals(4, second.intValue());

        Integer previous = (Integer) pivotContainer.prevItemId(second);
        Assert.assertEquals(0, previous.intValue());

        Integer last = (Integer) pivotContainer.lastItemId();
        Assert.assertEquals(10, last.intValue());
        Assert.assertTrue(pivotContainer.isLastId(10));
    }
}
