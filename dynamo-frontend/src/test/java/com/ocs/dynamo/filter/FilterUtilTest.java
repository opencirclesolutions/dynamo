package com.ocs.dynamo.filter;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.vaadin.data.util.filter.Compare.Equal;

public class FilterUtilTest {

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

		com.vaadin.data.util.filter.SimpleStringFilter ssf = new com.vaadin.data.util.filter.SimpleStringFilter("prop1",
				"someString", true, true);
		com.vaadin.data.Container.Filter f1 = FilterUtil.extractFilter(ssf, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		com.vaadin.data.Container.Filter f2 = FilterUtil.extractFilter(ssf, "prop2", null);
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
	public void testIsFilterValueSet() {
		Assert.assertEquals(false, FilterUtil.isFilterValueSet(null, new HashSet<>()));

		com.vaadin.data.util.filter.Compare.GreaterOrEqual empty = new com.vaadin.data.util.filter.Compare.GreaterOrEqual(
				"test", null);
		Assert.assertFalse(FilterUtil.isFilterValueSet(empty, new HashSet<>()));

		com.vaadin.data.util.filter.Compare.GreaterOrEqual eq = new com.vaadin.data.util.filter.Compare.GreaterOrEqual(
				"test", "bob");
		Assert.assertTrue(FilterUtil.isFilterValueSet(eq, new HashSet<>()));
		Assert.assertFalse(FilterUtil.isFilterValueSet(eq, Sets.newHashSet("test")));

		// test with an "and" filter
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.And(eq, empty), new HashSet<>()));

		// test with a String filter
		Assert.assertFalse(FilterUtil.isFilterValueSet(
				new com.vaadin.data.util.filter.SimpleStringFilter("test", "", true, true), new HashSet<>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(
				new com.vaadin.data.util.filter.SimpleStringFilter("test", "abc", true, true), new HashSet<>()));

		// test with a "Like" filter
		Assert.assertFalse(
				FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Like("test", "", true), new HashSet<>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Like("test", "abc", true),
				new HashSet<>()));

		// test with a "Between" filter
		Assert.assertFalse(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Between("test", null, null),
				new HashSet<>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Between("test", "abc", null),
				new HashSet<>()));
		Assert.assertTrue(FilterUtil.isFilterValueSet(new com.vaadin.data.util.filter.Between("test", null, "def"),
				new HashSet<>()));
	}
}
