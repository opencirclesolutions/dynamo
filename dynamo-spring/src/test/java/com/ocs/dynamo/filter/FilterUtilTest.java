package com.ocs.dynamo.filter;

import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;
import com.vaadin.data.util.filter.Compare.Equal;

public class FilterUtilTest {

	private EntityModelFactoryImpl emf = new EntityModelFactoryImpl();

	@Test
	public void testReplaceFilter() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("b", 24));

		FilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "b", false);

		Filter f = and.getFilters().get(1);
		Assert.assertTrue(f instanceof Compare.Equal);
		Assert.assertEquals("c", ((Compare.Equal) f).getPropertyId());
	}

	/**
	 * Check that both filters are replaced
	 */
	@Test
	public void testReplaceFilterAll() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("a", 12));

		FilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "a", false);

		Filter f0 = and.getFilters().get(0);
		Assert.assertTrue(f0 instanceof Compare.Equal);
		Assert.assertEquals("c", ((Compare.Equal) f0).getPropertyId());

		Filter f1 = and.getFilters().get(1);
		Assert.assertTrue(f1 instanceof Compare.Equal);
		Assert.assertEquals("c", ((Compare.Equal) f1).getPropertyId());
	}

	/**
	 * Check that only the first filter is replaced
	 */
	@Test
	public void testReplaceFilterFirstOnly() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("a", 12));

		FilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "a", true);

		Filter f0 = and.getFilters().get(0);
		Assert.assertTrue(f0 instanceof Compare.Equal);
		Assert.assertEquals("c", ((Compare.Equal) f0).getPropertyId());

		Filter f1 = and.getFilters().get(1);
		Assert.assertTrue(f1 instanceof Compare.Equal);
		Assert.assertEquals("a", ((Compare.Equal) f1).getPropertyId());
	}

	@Test
	public void testReplaceNot() {

		Not not = new Not(new Compare.Equal("a", 12));

		FilterUtil.replaceFilter(not, new Compare.Equal("d", 13), "a", false);

		Filter f = not.getFilter();
		Assert.assertTrue(f instanceof Compare.Equal);
		Assert.assertEquals("d", ((Compare.Equal) f).getPropertyId());
	}

	@Test
	public void testReplaceComplex() {

		Not not = new Not(new Compare.Equal("a", 12));
		And and = new And(new Compare.Equal("b", 12), new Compare.Equal("c", 24));
		Or or = new Or(not, and);

		FilterUtil.replaceFilter(or, new And(new Compare.Greater("f", 4), new Compare.Less("g", 7)), "b", false);

		// get the AND filter
		Filter f = or.getFilters().get(1);
		Assert.assertTrue(f instanceof And);

		And inner = (And) ((And) f).getFilters().get(0);
		Assert.assertEquals("f", ((PropertyFilter) inner.getFilters().get(0)).getPropertyId());
	}

	@Test
	public void testIsTrue() {

		com.vaadin.data.util.filter.Compare.Equal eq = new com.vaadin.data.util.filter.Compare.Equal("prop1",
		        Boolean.TRUE);
		Assert.assertTrue(FilterUtil.isTrue(eq, "prop1"));

		eq = new com.vaadin.data.util.filter.Compare.Equal("prop1", Boolean.FALSE);
		Assert.assertFalse(FilterUtil.isTrue(eq, "prop1"));

		com.vaadin.data.util.filter.Compare.Equal eq2 = new com.vaadin.data.util.filter.Compare.Equal("prop1",
		        "someString");
		Assert.assertFalse(FilterUtil.isTrue(eq2, "prop1"));

		// only works for Equal
		com.vaadin.data.util.filter.Compare.Greater gt = new com.vaadin.data.util.filter.Compare.Greater("prop1",
		        Boolean.TRUE);
		Assert.assertFalse(FilterUtil.isTrue(gt, "prop1"));
	}

	@Test
	public void testExtractFilter_Compare() {

		Equal compare = new Equal("prop1", "someString");
		com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(compare, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(compare, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_Like() {

		com.vaadin.data.util.filter.Like like = new com.vaadin.data.util.filter.Like("prop1", "someString");
		com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(like, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(like, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_SimpleString() {

		com.vaadin.data.util.filter.SimpleStringFilter ssf = new com.vaadin.data.util.filter.SimpleStringFilter(
		        "prop1", "someString", true, true);
		com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(ssf, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(ssf, "prop2", null);
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
	public void testExtractFilter_Between() {

		Between between = new Between("prop1", 100, 200);
		com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(between, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		com.ocs.dynamo.filter.Filter f2 = FilterUtil.extractFilter(between, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_Complex() {

		com.vaadin.data.util.filter.Like compare = new com.vaadin.data.util.filter.Like("prop1", "someString");
		com.vaadin.data.util.filter.And and = new com.vaadin.data.util.filter.And(compare,
		        new com.vaadin.data.util.filter.Compare.Equal("prop3", "someString"));

		// first operand
		com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(and, "prop1", null);
		Assert.assertNotNull(f1);

		// second operand
		com.vaadin.data.Container.Filter f3 = FilterUtil.extractFilter(and, "prop3", null);
		Assert.assertNotNull(f3);

		// wrong property
		com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(compare, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_Complex2() {

		com.ocs.dynamo.filter.Like compare = new com.ocs.dynamo.filter.Like("prop1", "someString");
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(compare, new com.ocs.dynamo.filter.Compare.Equal(
		        "prop3", "someString"));

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
	public void testExtractFilter_In() {

		com.ocs.dynamo.filter.In in = new com.ocs.dynamo.filter.In("prop1", Lists.newArrayList("a", "b"));
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(in, new com.ocs.dynamo.filter.Compare.Equal(
		        "prop3", "someString"));

		// first operand
		com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(and, "prop1");
		Assert.assertTrue(f1 instanceof In);
	}

	@Test
	public void testExtractFilter_Contains() {

		com.ocs.dynamo.filter.Contains contains = new com.ocs.dynamo.filter.Contains("prop1", "a");
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(contains,
		        new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

		// first operand
		com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(and, "prop1");
		Assert.assertTrue(f1 instanceof Contains);
	}

	@Test
	public void testExtractFilter_Compare2() {

		com.ocs.dynamo.filter.Compare.Equal compare = new com.ocs.dynamo.filter.Compare.Equal("prop1", "someString");
		com.ocs.dynamo.filter.Filter f1 = FilterUtil.extractFilter(compare, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		com.ocs.dynamo.filter.Filter f2 = FilterUtil.extractFilter(compare, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testFlattenAnd() {
		com.ocs.dynamo.filter.Like compare = new com.ocs.dynamo.filter.Like("prop1", "someString");
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(compare, new com.ocs.dynamo.filter.Compare.Equal(
		        "prop3", "someString"));

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

		com.ocs.dynamo.filter.Compare.Equal compare1 = new com.ocs.dynamo.filter.Compare.Equal("prop1", "someString");
		com.ocs.dynamo.filter.Compare.Equal compare2 = new com.ocs.dynamo.filter.Compare.Equal("prop2", "someString");
		com.ocs.dynamo.filter.Compare.Equal compare3 = new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString");

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

	/**
	 * Test that a filter that searches for a detail
	 */
	@Test
	public void testReplaceMasterAndDetailFilters1() {
		EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

		And and = new And(new Compare.Equal("tags", Lists.newArrayList("abc")));

		// check that the equals filter is replaced by an Or-filter that consists of "contains"
		// clauses
		FilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Or);
		Or or = (Or) replaced;
		Assert.assertTrue(or.getFilters().get(0) instanceof Contains);
		Contains ct = (Contains) or.getFilters().get(0);
		Assert.assertEquals("abc", ct.getValue());

		// now once more but now without the intermediate OR filter
		and = new And(new Compare.Equal("tags", "def"));

		FilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Contains);
		ct = (Contains) replaced;
		Assert.assertEquals("def", ct.getValue());

	}

	/**
	 * Test that a filter that searches on a master field is replaced by an "In" filter
	 */
	@Test
	public void testReplaceMasterAndDetailFilters2() {
		EntityModel<TestEntity2> model = emf.getModel(TestEntity2.class);

		And and = new And(new Compare.Equal("testEntity", Lists.newArrayList(new TestEntity(), new TestEntity())));

		// check that the equals filter is replaced by an "in" filter
		FilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof In);
		In in = (In) replaced;
		Assert.assertEquals(2, in.getValues().size());

		// if there is just one value then no replacement is needed
		and = new And(new Compare.Equal("testEntity", new TestEntity()));
		FilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Compare.Equal);
	}

	@Test
	public void testReplaceMasterAndDetailFilters3() {
		EntityModel<TestEntity2> model = emf.getModel(TestEntity2.class);

		And and = new And(new Compare.Equal("testEntity", Lists.newArrayList(new TestEntity(), new TestEntity())));

		// check that the equals filter is replaced by an "in" filter
		FilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof In);
		In in = (In) replaced;
		Assert.assertEquals(2, in.getValues().size());

		// if there is just one value then no replacement is needed
		and = new And(new Compare.Equal("testEntity", new TestEntity()));
		FilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Compare.Equal);
	}

	@Test
	public void testExtractFilterValue() {

		com.vaadin.data.util.filter.Like like = new com.vaadin.data.util.filter.Like("prop1", "value1");
		com.vaadin.data.util.filter.Compare.GreaterOrEqual ge = new com.vaadin.data.util.filter.Compare.GreaterOrEqual(
		        "prop2", 10);
		com.vaadin.data.util.filter.Compare.LessOrEqual le = new com.vaadin.data.util.filter.Compare.LessOrEqual(
		        "prop2", 5);
		com.vaadin.data.util.filter.And and = new com.vaadin.data.util.filter.And(like, ge, le);

		Object value = FilterUtil.extractFilterValue(and, "prop2",
		        com.vaadin.data.util.filter.Compare.LessOrEqual.class);
		Assert.assertEquals(5, value);
	}

	@Test
	public void testIsFilterValueSet() {
		Assert.assertFalse(FilterUtil.isFilterValueSet(null, new HashSet<String>()));

		com.vaadin.data.util.filter.Compare.GreaterOrEqual empty = new com.vaadin.data.util.filter.Compare.GreaterOrEqual(
		        "test", null);
		Assert.assertFalse(FilterUtil.isFilterValueSet(empty, new HashSet<String>()));

		com.vaadin.data.util.filter.Compare.GreaterOrEqual eq = new com.vaadin.data.util.filter.Compare.GreaterOrEqual(
		        "test", "bob");
		Assert.assertTrue(FilterUtil.isFilterValueSet(eq, new HashSet<String>()));
		Assert.assertFalse(FilterUtil.isFilterValueSet(eq, Sets.newHashSet("test")));

		// test with an "and" filter
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.And(eq, empty),
		        new HashSet<String>()));

		// test with a String filter
		Assert.assertFalse(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.SimpleStringFilter("test", "",
		        true, true), new HashSet<String>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.SimpleStringFilter("test", "abc",
		        true, true), new HashSet<String>()));

		// test with a "Like" filter
		Assert.assertFalse(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Like("test", "", true),
		        new HashSet<String>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Like("test", "abc", true),
		        new HashSet<String>()));

		// test with a "Between" filter
		Assert.assertFalse(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Between("test", null, null),
		        new HashSet<String>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Between("test", "abc", null),
		        new HashSet<String>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Between("test", null, "def"),
		        new HashSet<String>()));
	}
}
