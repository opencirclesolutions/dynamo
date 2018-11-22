package com.ocs.dynamo.filter;

import java.util.HashSet;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.TestEntity;
import com.vaadin.server.SerializablePredicate;

public class PredicateUtilTest {

	@Test
	public void testIsTrue() {

		EqualsPredicate<TestEntity> eq = new EqualsPredicate<TestEntity>("prop1", Boolean.TRUE);
		Assert.assertTrue(PredicateUtil.isTrue(eq, "prop1"));

		eq = new EqualsPredicate<TestEntity>("prop1", Boolean.FALSE);
		Assert.assertFalse(PredicateUtil.isTrue(eq, "prop1"));

		EqualsPredicate<TestEntity> eq2 = new EqualsPredicate<TestEntity>("prop1", "someString");
		Assert.assertFalse(PredicateUtil.isTrue(eq2, "prop1"));

		// only works for Equal
		GreaterThanPredicate<TestEntity> gt = new GreaterThanPredicate<TestEntity>("prop1", Boolean.TRUE);
		Assert.assertFalse(PredicateUtil.isTrue(gt, "prop1"));
	}

	@Test
	public void testExtractPredicate_Compare() {

		EqualsPredicate<TestEntity> comp = new EqualsPredicate<TestEntity>("prop1", "someString");
		SerializablePredicate<TestEntity> f1 = PredicateUtil.extractPredicate(comp, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtil.extractPredicate(comp, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_Like() {

		LikePredicate<TestEntity> like = new LikePredicate<TestEntity>("prop1", "someString", true);
		SerializablePredicate<TestEntity> f1 = PredicateUtil.extractPredicate(like, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtil.extractPredicate(like, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_SimpleString() {

		SimpleStringPredicate<TestEntity> ssf = new SimpleStringPredicate<TestEntity>("prop1", "someString", true,
				true);
		SerializablePredicate<TestEntity> f1 = PredicateUtil.extractPredicate(ssf, "prop1", null);
		Assert.assertNotNull(f1);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtil.extractPredicate(ssf, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testExtractPredicate_Complex() {

		LikePredicate<TestEntity> compare = new LikePredicate<TestEntity>("prop1", "someString", true);
		AndPredicate<TestEntity> and = new AndPredicate<TestEntity>(compare,
				new EqualsPredicate<TestEntity>("prop3", "someString"));

		// first operand
		SerializablePredicate<TestEntity> f1 = PredicateUtil.extractPredicate(and, "prop1", null);
		Assert.assertNotNull(f1);

		// second operand
		SerializablePredicate<TestEntity> f3 = PredicateUtil.extractPredicate(and, "prop3", null);
		Assert.assertNotNull(f3);

		// wrong property
		SerializablePredicate<TestEntity> f2 = PredicateUtil.extractPredicate(compare, "prop2", null);
		Assert.assertNull(f2);
	}

	@Test
	public void testIsPredicateValueSet() {
		Assert.assertEquals(false, PredicateUtil.isPredicateValueSet(null, new HashSet<>()));

		GreaterOrEqualPredicate<TestEntity> empty = new GreaterOrEqualPredicate<TestEntity>("test", null);
		Assert.assertFalse(PredicateUtil.isPredicateValueSet(empty, new HashSet<>()));

		GreaterOrEqualPredicate<TestEntity> eq = new GreaterOrEqualPredicate<TestEntity>("test", "bob");
		Assert.assertTrue(PredicateUtil.isPredicateValueSet(eq, Sets.newHashSet()));
		Assert.assertFalse(PredicateUtil.isPredicateValueSet(eq, Sets.newHashSet("test")));

		// test with an "and" filter
		Assert.assertTrue(PredicateUtil.isPredicateValueSet(new AndPredicate<TestEntity>(eq, empty), new HashSet<>()));

		// test with a String filter
		Assert.assertFalse(PredicateUtil.isPredicateValueSet(new SimpleStringPredicate<TestEntity>("test", null, true, true),
				new HashSet<>()));
		Assert.assertTrue(PredicateUtil.isPredicateValueSet(new SimpleStringPredicate<TestEntity>("test", "abc", true, true),
				new HashSet<>()));

		// test with a "Like" filter
		Assert.assertFalse(
				PredicateUtil.isPredicateValueSet(new LikePredicate<TestEntity>("test", null, true), new HashSet<>()));
		Assert.assertTrue(
				PredicateUtil.isPredicateValueSet(new LikePredicate<TestEntity>("test", "abc", true), new HashSet<>()));

		// test with a "Between" filter
		Assert.assertFalse(
				PredicateUtil.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", null, null), new HashSet<>()));
		Assert.assertTrue(
				PredicateUtil.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", "abc", null), new HashSet<>()));
		Assert.assertTrue(
				PredicateUtil.isPredicateValueSet(new BetweenPredicate<TestEntity>("test", null, "def"), new HashSet<>()));
	}
}
