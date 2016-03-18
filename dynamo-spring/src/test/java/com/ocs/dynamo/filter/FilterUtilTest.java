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
package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.data.util.filter.Compare.Equal;

public class FilterUtilTest {

    @Test
    public void testReplaceFilter() {

        And and = new And(new Compare.Equal("a", 12), new Compare.Equal("b", 24));

        FilterUtil.replaceFilter(null, and, new Compare.Equal("c", 13), "b");

        Filter f = and.getFilters().get(1);
        Assert.assertTrue(f instanceof Compare.Equal);
        Assert.assertEquals("c", ((Compare.Equal) f).getPropertyId());
    }

    @Test
    public void testReplaceNot() {

        Not not = new Not(new Compare.Equal("a", 12));

        FilterUtil.replaceFilter(null, not, new Compare.Equal("d", 13), "a");

        Filter f = not.getFilter();
        Assert.assertTrue(f instanceof Compare.Equal);
        Assert.assertEquals("d", ((Compare.Equal) f).getPropertyId());
    }

    @Test
    public void testReplaceComplex() {

        Not not = new Not(new Compare.Equal("a", 12));
        And and = new And(new Compare.Equal("b", 12), new Compare.Equal("c", 24));
        Or or = new Or(not, and);

        FilterUtil.replaceFilter(null, or,
                new And(new Compare.Greater("f", 4), new Compare.Less("g", 7)), "b");

        // get the AND filter
        Filter f = or.getFilters().get(1);
        Assert.assertTrue(f instanceof And);

        And inner = (And) ((And) f).getFilters().get(0);
        Assert.assertEquals("f", ((PropertyFilter) inner.getFilters().get(0)).getPropertyId());
    }

    @Test
    public void testIsTrue() {

        com.vaadin.data.util.filter.Compare.Equal eq = new com.vaadin.data.util.filter.Compare.Equal(
                "prop1", Boolean.TRUE);
        Assert.assertTrue(FilterUtil.isTrue(eq, "prop1"));

        eq = new com.vaadin.data.util.filter.Compare.Equal("prop1", Boolean.FALSE);
        Assert.assertFalse(FilterUtil.isTrue(eq, "prop1"));

        com.vaadin.data.util.filter.Compare.Equal eq2 = new com.vaadin.data.util.filter.Compare.Equal(
                "prop1", "someString");
        Assert.assertFalse(FilterUtil.isTrue(eq2, "prop1"));

        // only works for Equal
        com.vaadin.data.util.filter.Compare.Greater gt = new com.vaadin.data.util.filter.Compare.Greater(
                "prop1", Boolean.TRUE);
        Assert.assertFalse(FilterUtil.isTrue(gt, "prop1"));
    }

    @Test
    public void testExtractFilter_Compare() {

        Equal compare = new Equal("prop1", "someString");
        com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(compare, "prop1");
        Assert.assertNotNull(f1);

        // wrong property
        com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(compare, "prop2");
        Assert.assertNull(f2);
    }

    @Test
    public void testExtractFilter_Like() {

        com.vaadin.data.util.filter.Like like = new com.vaadin.data.util.filter.Like("prop1",
                "someString");
        com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(like, "prop1");
        Assert.assertNotNull(f1);

        // wrong property
        com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(like, "prop2");
        Assert.assertNull(f2);
    }

    @Test
    public void testExtractFilter_Like2() {

        com.ocs.dynamo.filter.Like like = new com.ocs.dynamo.filter.Like("prop1", "someString");
        com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(like, "prop1");
        Assert.assertNotNull(f1);

        // wrong property
        com.ocs.dynamo.filter.Filter f2 = FilterUtil.extractFilter(like, "prop2");
        Assert.assertNull(f2);
    }

    @Test
    public void testExtractFilter_Complex() {

        com.vaadin.data.util.filter.Like compare = new com.vaadin.data.util.filter.Like("prop1",
                "someString");
        com.vaadin.data.util.filter.And and = new com.vaadin.data.util.filter.And(compare,
                new com.vaadin.data.util.filter.Compare.Equal("prop3", "someString"));

        // first operand
        com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(and, "prop1");
        Assert.assertNotNull(f1);

        // second operand
        com.vaadin.data.Container.Filter f3 = FilterUtil.extractFilter(and, "prop3");
        Assert.assertNotNull(f3);

        // wrong property
        com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(compare, "prop2");
        Assert.assertNull(f2);
    }

    @Test
    public void testExtractFilter_Complex2() {

        com.ocs.dynamo.filter.Like compare = new com.ocs.dynamo.filter.Like("prop1", "someString");
        com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(compare,
                new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

        // first operand
        com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(and, "prop1");
        Assert.assertNotNull(f1);

        // second operand
        com.ocs.dynamo.filter.Filter f3 = FilterUtil.extractFilter(and, "prop3");
        Assert.assertNotNull(f3);

        // wrong property
        com.ocs.dynamo.filter.Filter f2 = FilterUtil.extractFilter(compare, "prop2");
        Assert.assertNull(f2);
    }

    @Test
    public void testExtractFilter_Compare2() {

        com.ocs.dynamo.filter.Compare.Equal compare = new com.ocs.dynamo.filter.Compare.Equal(
                "prop1", "someString");
        com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(compare, "prop1");
        Assert.assertNotNull(f1);

        // wrong property
        com.ocs.dynamo.filter.Filter f2 = FilterUtil.extractFilter(compare, "prop2");
        Assert.assertNull(f2);
    }
}
