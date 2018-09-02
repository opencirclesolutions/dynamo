package com.ocs.dynamo.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;
import com.ocs.dynamo.domain.TestEntity2;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.impl.EntityModelFactoryImpl;

public class DynamoFilterUtilTest {

	private EntityModelFactoryImpl emf = new EntityModelFactoryImpl();

	@Test
	public void testReplaceFilter() {

		And and = new And(new Compare.Equal("a", 12), new Compare.Equal("b", 24));

		DynamoFilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "b", false);

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

		DynamoFilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "a", false);

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

		DynamoFilterUtil.replaceFilter(and, new Compare.Equal("c", 13), "a", true);

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

		DynamoFilterUtil.replaceFilter(not, new Compare.Equal("d", 13), "a", false);

		Filter f = not.getFilter();
		Assert.assertTrue(f instanceof Compare.Equal);
		Assert.assertEquals("d", ((Compare.Equal) f).getPropertyId());
	}

	@Test
	public void testReplaceComplex() {

		Not not = new Not(new Compare.Equal("a", 12));
		And and = new And(new Compare.Equal("b", 12), new Compare.Equal("c", 24));
		Or or = new Or(not, and);

		DynamoFilterUtil.replaceFilter(or, new And(new Compare.Greater("f", 4), new Compare.Less("g", 7)), "b", false);

		// get the AND filter
		Filter f = or.getFilters().get(1);
		Assert.assertTrue(f instanceof And);

		And inner = (And) ((And) f).getFilters().get(0);
		Assert.assertEquals("f", ((PropertyFilter) inner.getFilters().get(0)).getPropertyId());
	}

	@Test
	public void testExtractFilter_Like2() {

		com.ocs.dynamo.filter.Like like = new com.ocs.dynamo.filter.Like("prop1", "someString");
		com.ocs.dynamo.filter.Filter f1 = DynamoFilterUtil.extractFilter(like, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		com.ocs.dynamo.filter.Filter f2 = DynamoFilterUtil.extractFilter(like, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_Between() {

		Between between = new Between("prop1", 100, 200);
		com.ocs.dynamo.filter.Filter f1 = DynamoFilterUtil.extractFilter(between, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		com.ocs.dynamo.filter.Filter f2 = DynamoFilterUtil.extractFilter(between, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_Complex2() {

		com.ocs.dynamo.filter.Like compare = new com.ocs.dynamo.filter.Like("prop1", "someString");
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(compare,
				new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

		// first operand
		com.ocs.dynamo.filter.Filter f1 = DynamoFilterUtil.extractFilter(and, "prop1");
		Assert.assertNotNull(f1);

		// second operand
		com.ocs.dynamo.filter.Filter f3 = DynamoFilterUtil.extractFilter(and, "prop3");
		Assert.assertNotNull(f3);

		// wrong property
		com.ocs.dynamo.filter.Filter f2 = DynamoFilterUtil.extractFilter(compare, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractFilter_In() {

		com.ocs.dynamo.filter.In in = new com.ocs.dynamo.filter.In("prop1", Lists.newArrayList("a", "b"));
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(in,
				new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

		// first operand
		com.ocs.dynamo.filter.Filter f1 = DynamoFilterUtil.extractFilter(and, "prop1");
		Assert.assertTrue(f1 instanceof In);
	}

	@Test
	public void testExtractFilter_Contains() {

		com.ocs.dynamo.filter.Contains contains = new com.ocs.dynamo.filter.Contains("prop1", "a");
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(contains,
				new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

		// first operand
		com.ocs.dynamo.filter.Filter f1 = DynamoFilterUtil.extractFilter(and, "prop1");
		Assert.assertTrue(f1 instanceof Contains);
	}

	@Test
	public void testExtractFilter_Compare2() {

		com.ocs.dynamo.filter.Compare.Equal compare = new com.ocs.dynamo.filter.Compare.Equal("prop1", "someString");
		com.ocs.dynamo.filter.Filter f1 = DynamoFilterUtil.extractFilter(compare, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		com.ocs.dynamo.filter.Filter f2 = DynamoFilterUtil.extractFilter(compare, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testFlattenAnd() {
		com.ocs.dynamo.filter.Like compare = new com.ocs.dynamo.filter.Like("prop1", "someString");
		com.ocs.dynamo.filter.And and = new com.ocs.dynamo.filter.And(compare,
				new com.ocs.dynamo.filter.Compare.Equal("prop3", "someString"));

		com.ocs.dynamo.filter.Like compare2 = new com.ocs.dynamo.filter.Like("prop1", "someString2");
		com.ocs.dynamo.filter.And and2 = new com.ocs.dynamo.filter.And(and, compare2);

		List<Filter> flattened2 = DynamoFilterUtil.flattenAnd(and);
		Assert.assertEquals(2, flattened2.size());
		Assert.assertEquals(compare, flattened2.get(0));

		List<Filter> flattened = DynamoFilterUtil.flattenAnd(and2);
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
		DynamoFilterUtil.removeFilters(and, "prop1");
		Assert.assertEquals(2, and.getFilters().size());

		and = new And(compare1, compare2, compare3);
		// remove a non-existing filter and check there are still 2 left
		DynamoFilterUtil.removeFilters(and, "prop4");
		Assert.assertEquals(3, and.getFilters().size());

		// remove nested filters and check that the empty filter on the top level is
		// removed
		And nested = new And(compare1, new And(compare2, compare3));
		DynamoFilterUtil.removeFilters(nested, "prop2", "prop3");
		Assert.assertEquals(1, nested.getFilters().size());

		and = new And(compare1, new Not(compare2));
		DynamoFilterUtil.removeFilters(and, "prop2");
		Assert.assertEquals(1, and.getFilters().size());

		and = new And(compare1, new Not(new And(compare2, compare3)));
		DynamoFilterUtil.removeFilters(and, "prop2", "prop3");
		Assert.assertEquals(1, and.getFilters().size());
	}

	/**
	 * Test that a filter that searches for a detail
	 */
	@Test
	public void testReplaceMasterAndDetailFilters1() {
		EntityModel<TestEntity> model = emf.getModel(TestEntity.class);

		And and = new And(new Compare.Equal("tags", Lists.newArrayList("abc")));

		// check that the equals filter is replaced by an Or-filter that consists of
		// "contains"
		// clauses
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Or);
		Or or = (Or) replaced;
		Assert.assertTrue(or.getFilters().get(0) instanceof Contains);
		Contains ct = (Contains) or.getFilters().get(0);
		Assert.assertEquals("abc", ct.getValue());

		// now once more but now without the intermediate OR filter
		and = new And(new Compare.Equal("tags", "def"));

		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Contains);
		ct = (Contains) replaced;
		Assert.assertEquals("def", ct.getValue());

	}

	/**
	 * Test that a filter that searches on a master field is replaced by an "In"
	 * filter
	 */
	@Test
	public void testReplaceMasterAndDetailFilters2() {
		EntityModel<TestEntity2> model = emf.getModel(TestEntity2.class);

		And and = new And(new Compare.Equal("testEntity", Lists.newArrayList(new TestEntity(), new TestEntity())));

		// check that the equals filter is replaced by an "in" filter
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof In);
		In in = (In) replaced;
		Assert.assertEquals(2, in.getValues().size());

		// if there is just one value then no replacement is needed
		and = new And(new Compare.Equal("testEntity", new TestEntity()));
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Compare.Equal);
	}

	@Test
	public void testReplaceMasterAndDetailFilters3() {
		EntityModel<TestEntity2> model = emf.getModel(TestEntity2.class);

		And and = new And(new Compare.Equal("testEntity", Lists.newArrayList(new TestEntity(), new TestEntity())));

		// check that the equals filter is replaced by an "in" filter
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		Filter replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof In);
		In in = (In) replaced;
		Assert.assertEquals(2, in.getValues().size());

		// if there is just one value then no replacement is needed
		and = new And(new Compare.Equal("testEntity", new TestEntity()));
		DynamoFilterUtil.replaceMasterAndDetailFilters(and, model);
		replaced = and.getFilters().get(0);
		Assert.assertTrue(replaced instanceof Compare.Equal);
	}

}
