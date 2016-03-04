package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

public class FilterConverterTest {

	private FilterConverter converter = new FilterConverter();

	private com.vaadin.data.util.filter.Compare.Equal f1 = new com.vaadin.data.util.filter.Compare.Equal(
	        "test", "test");

	private com.vaadin.data.util.filter.Compare.Equal f2 = new com.vaadin.data.util.filter.Compare.Equal(
	        "test", "test");

	@Test
	public void testCompareEqual() {
		Filter result = converter
		        .convert(new com.vaadin.data.util.filter.Compare.Equal("test", "test"));
		Assert.assertTrue(result instanceof Compare.Equal);
	}

	@Test
	public void testCompareLess() {
		Filter result = converter
		        .convert(new com.vaadin.data.util.filter.Compare.Less("test", "test"));
		Assert.assertTrue(result instanceof Compare.Less);
	}

	@Test
	public void testCompareLessOrEqual() {
		Filter result = converter
		        .convert(new com.vaadin.data.util.filter.Compare.LessOrEqual("test", "test"));
		Assert.assertTrue(result instanceof Compare.LessOrEqual);
	}

	@Test
	public void testCompareGreater() {
		Filter result = converter
		        .convert(new com.vaadin.data.util.filter.Compare.Greater("test", "test"));
		Assert.assertTrue(result instanceof Compare.Greater);
	}

	@Test
	public void testCompareGreaterOrEqual() {
		Filter result = converter
		        .convert(new com.vaadin.data.util.filter.Compare.GreaterOrEqual("test", "test"));
		Assert.assertTrue(result instanceof Compare.GreaterOrEqual);
	}

	@Test
	public void testNot() {
		Filter result = converter.convert(new com.vaadin.data.util.filter.Not(f1));
		Assert.assertTrue(result instanceof Not);
	}

	@Test
	public void testAnd() {
		Filter result = converter.convert(new com.vaadin.data.util.filter.And(f1, f2));
		Assert.assertTrue(result instanceof And);
	}

	@Test
	public void testOr() {
		Filter result = converter.convert(new com.vaadin.data.util.filter.Or(f1, f2));
		Assert.assertTrue(result instanceof Or);
	}

	@Test
	public void testBetween() {
		Filter result = converter.convert(new com.vaadin.data.util.filter.Between("test", 1, 10));
		Assert.assertTrue(result instanceof Between);
	}

	@Test
	public void testLike() {
		Filter result = converter.convert(new com.vaadin.data.util.filter.Like("test", "%test%"));
		Assert.assertTrue(result instanceof Like);
	}

	/**
	 * Simple string filter - case sensitive prefix only
	 */
	@Test
	public void testSimpleStringFilter1() {
		Filter result = converter.convert(
		        new com.vaadin.data.util.filter.SimpleStringFilter("test", "test", false, true));
		Assert.assertTrue(result instanceof Like);
		Like like = (Like) result;
		Assert.assertTrue(like.isCaseSensitive());
		Assert.assertEquals("test%", like.getValue());
	}

	/**
	 * Simple string filter - case insensitive infix
	 */
	@Test
	public void testSimpleStringFilter2() {
		Filter result = converter.convert(
		        new com.vaadin.data.util.filter.SimpleStringFilter("test", "test", true, false));
		Assert.assertTrue(result instanceof Like);
		Like like = (Like) result;
		Assert.assertFalse(like.isCaseSensitive());
		Assert.assertEquals("%test%", like.getValue());
	}
}
