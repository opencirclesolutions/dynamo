package com.ocs.dynamo.filter;

import java.util.List;

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

        FilterUtil.replaceFilter(null, or, new And(new Compare.Greater("f", 4), new Compare.Less(
                "g", 7)), "b");

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
    public void testExtractFilter_SimpleString() {

        com.vaadin.data.util.filter.SimpleStringFilter ssf = new com.vaadin.data.util.filter.SimpleStringFilter(
                "prop1", "someString", true, true);
        com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(ssf, "prop1");
        Assert.assertNotNull(f1);

        // wrong property
        com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(ssf, "prop2");
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

    @Test
    public void testFlattenAnd() {
        com.ocs.dynamo.filter.Like compare = new com.ocs.dynamo.filter.Like("prop1", "someString");
        com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(compare,
                new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

        com.ocs.dynamo.filter.Like compare2 = new com.ocs.dynamo.filter.Like("prop1", "someString2");
        com.ocs.dynamo.filter.And and2 = new com.ocs.dynamo.filter.And(and, compare2);

        List<Filter> flattened2 = FilterUtil.flattenAnd(and);
        Assert.assertEquals(2, flattened2.size());
        Assert.assertEquals(compare, flattened2.get(0));

        List<Filter> flattened = FilterUtil.flattenAnd(and2);
        Assert.assertEquals(3, flattened.size());
        Assert.assertEquals(compare, flattened.get(0));
        Assert.assertEquals(compare2, flattened.get(2));
    }

    @Test
    public void testRemoveFilters() {

        com.ocs.dynamo.filter.Compare.Equal compare1 = new com.ocs.dynamo.filter.Compare.Equal(
                "prop1", "someString");
        com.ocs.dynamo.filter.Compare.Equal compare2 = new com.ocs.dynamo.filter.Compare.Equal(
                "prop2", "someString");
        com.ocs.dynamo.filter.Compare.Equal compare3 = new com.ocs.dynamo.filter.Compare.Equal(
                "prop3", "someString");

        And and = new And(compare1, compare2, compare3);

        // remove a filter and check there are still 2 left
        FilterUtil.removeFilters(and, "prop1");
        Assert.assertEquals(2, and.getFilters().size());

        and = new And(compare1, compare2, compare3);
        // remove a non-existing filter and check there are still 2 left
        FilterUtil.removeFilters(and, "prop4");
        Assert.assertEquals(3, and.getFilters().size());

        // remove nested filters and check that the empty filter on the top level is removed
        And nested = new And(compare1, new And(compare2, compare3));
        FilterUtil.removeFilters(nested, "prop2", "prop3");
        Assert.assertEquals(1, nested.getFilters().size());

        and = new And(compare1, new Not(compare2));
        FilterUtil.removeFilters(and, "prop2");
        Assert.assertEquals(1, and.getFilters().size());

        and = new And(compare1, new Not(new And(compare2, compare3)));
        FilterUtil.removeFilters(and, "prop2", "prop3");
        Assert.assertEquals(1, and.getFilters().size());
    }
}
