package com.ocs.dynamo.filter;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.TestEntity;
import com.vaadin.server.SerializablePredicate;

public class PredicateUtilsTest {

	@Test
	public void testIsTrue() {

		EqualsPredicate<TestEntity> eq = new EqualsPredicate<>("prop1", Boolean.TRUE);
		Assert.assertTrue(PredicateUtils.isTrue(eq, "prop1"));

		eq = new EqualsPredicate<TestEntity>("prop1", Boolean.FALSE);
		Assert.assertFalse(PredicateUtils.isTrue(eq, "prop1"));

		EqualsPredicate<TestEntity> eq2 = new EqualsPredicate<>("prop1", "someString");
		Assert.assertFalse(PredicateUtils.isTrue(eq2, "prop1"));

		// only works for Equal
		GreaterThanPredicate<TestEntity> gt = new GreaterThanPredicate<>("prop1", Boolean.TRUE);
		Assert.assertFalse(PredicateUtils.isTrue(gt, "prop1"));
	}

	@Test
	public void testExtractPredicate_Compare() {

		EqualsPredicate<TestEntity> comp = new EqualsPredicate<>("prop1", "someString");
		SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(comp, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(comp, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_CompareWrongType() {

		EqualsPredicate<TestEntity> comp = new EqualsPredicate<>("prop1", "someString");
		SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(comp, "prop1", EqualsPredicate.class);
		Assert.assertNotNull(f1);

		// wrong property
		comp = new EqualsPredicate<>("prop1", "someString");
		f1 = PredicateUtils.extractPredicate(comp, "prop1", GreaterOrEqualPredicate.class);
		Assert.assertNull(f1);
	}

	@Test
	public void testExtractPredicate_Not() {

		EqualsPredicate<TestEntity> comp = new EqualsPredicate<>("prop1", "someString");
		NotPredicate<TestEntity> not = new NotPredicate<>(comp);

		SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(not, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(not, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_Like() {

		LikePredicate<TestEntity> like = new LikePredicate<TestEntity>("prop1", "someString", true);
		SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(like, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(like, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_SimpleString() {

		SimpleStringPredicate<TestEntity> ssf = new SimpleStringPredicate<>("prop1", "someString", true, true);
		SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(ssf, "prop1");
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(ssf, "prop2");
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_Composite() {

		LikePredicate<TestEntity> compare = new LikePredicate<>("prop1", "someString", true);
		AndPredicate<TestEntity> and = new AndPredicate<TestEntity>(compare,
				new EqualsPredicate<TestEntity>("prop2", "someString"));

		// first operand
		SerializablePredicate<TestEntity> f1 = PredicateUtils.extractPredicate(and, "prop1");
		Assert.assertNotNull(f1);

		// second operand
		SerializablePredicate<TestEntity> f2 = PredicateUtils.extractPredicate(and, "prop2");
		Assert.assertNotNull(f2);

		// wrong property
		SerializablePredicate<TestEntity> f3 = PredicateUtils.extractPredicate(compare, "prop3");
		Assert.assertNull(f3);
	}

	@Test
	public void testIsPredicateValueSet() {
		Assert.assertEquals(false, PredicateUtils.isPredicateValueSet(null, new HashSet<>()));

		GreaterOrEqualPredicate<TestEntity> empty = new GreaterOrEqualPredicate<TestEntity>("test", null);
		Assert.assertFalse(PredicateUtils.isPredicateValueSet(empty, new HashSet<>()));

		GreaterOrEqualPredicate<TestEntity> eq = new GreaterOrEqualPredicate<TestEntity>("test", "bob");
		Assert.assertTrue(PredicateUtils.isPredicateValueSet(eq, Sets.newHashSet()));
		Assert.assertFalse(PredicateUtils.isPredicateValueSet(eq, Sets.newHashSet("test")));

		// test with an "and" filter
		Assert.assertTrue(PredicateUtils.isPredicateValueSet(new AndPredicate<TestEntity>(eq, empty), new HashSet<>()));

		// test with a String filter
		Assert.assertFalse(PredicateUtils.isPredicateValueSet(new SimpleStringPredicate<>("test", null, true, true),
				new HashSet<>()));
		Assert.assertTrue(PredicateUtils.isPredicateValueSet(
				new SimpleStringPredicate<TestEntity>("test", "abc", true, true), new HashSet<>()));

		// test with a "Like" filter
		Assert.assertFalse(
				PredicateUtils.isPredicateValueSet(new LikePredicate<>("test", null, true), new HashSet<>()));
		Assert.assertTrue(PredicateUtils.isPredicateValueSet(new LikePredicate<TestEntity>("test", "abc", true),
				new HashSet<>()));

		// test with a "Between" filter
		Assert.assertFalse(PredicateUtils.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", null, null),
				new HashSet<>()));
		Assert.assertTrue(PredicateUtils.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", "abc", null),
				new HashSet<>()));
		Assert.assertTrue(PredicateUtils.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", null, "def"),
				new HashSet<>()));
	}

	@Test
	public void testExtractPredicateValue_Compare() {

		EqualsPredicate<TestEntity> comp = new EqualsPredicate<TestEntity>("prop1", "someString");
		Object value = PredicateUtils.extractPredicateValue(comp, "prop1");
		Assert.assertEquals("someString", value);

		// wrong property
		value = PredicateUtils.extractPredicateValue(comp, "prop2");
		Assert.assertNull(value);
	}

	@Test
	public void testExtractPredicateValue_Composite() {

		LikePredicate<TestEntity> compare = new LikePredicate<TestEntity>("prop1", "someString1", true);
		AndPredicate<TestEntity> and = new AndPredicate<TestEntity>(compare,
				new EqualsPredicate<TestEntity>("prop2", "someString2"));

		// first operand
		Object f1 = PredicateUtils.extractPredicateValue(and, "prop1");
		Assert.assertEquals("someString1", f1);

		// second operand
		Object f2 = PredicateUtils.extractPredicateValue(and, "prop2");
		Assert.assertEquals("someString2", f2);

		// wrong property
		Object f3 = PredicateUtils.extractPredicateValue(compare, "prop3");
		Assert.assertNull(f3);
	}
}
