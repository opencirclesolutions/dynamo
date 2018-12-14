package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class SimpleStringPredicateTest {

	@Test
	public void testPrefixCaseSensitive() {
		SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", true, true);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName("Kevin");
		Assert.assertFalse(p1.test(t1));
		
		// case match at start
		t1.setName("evin");
		Assert.assertTrue(p1.test(t1));
		
		// case does not match
		t1.setName("Evin");
		Assert.assertFalse(p1.test(t1));
	}
	
	@Test
	public void testPrefixCaseInsensitive() {
		SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", true, false);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName("Kevin");
		Assert.assertFalse(p1.test(t1));
		
		// case match at start
		t1.setName("evin");
		Assert.assertTrue(p1.test(t1));
		
		// case does not match
		t1.setName("Evin");
		Assert.assertTrue(p1.test(t1));
	}
	
	@Test
	public void testAnyWhereCaseSensitive() {
		SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", false, true);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName("Kevin");
		Assert.assertTrue(p1.test(t1));
		
		// case match at start
		t1.setName("evin");
		Assert.assertTrue(p1.test(t1));
		
		// case does not match
		t1.setName("Evin");
		Assert.assertFalse(p1.test(t1));
	}
	
	@Test
	public void testAnyWhereCaseInsensitive() {
		SimpleStringPredicate<TestEntity> p1 = new SimpleStringPredicate<>("name", "evi", false, false);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName("Kevin");
		Assert.assertTrue(p1.test(t1));
		
		// case match at start
		t1.setName("evin");
		Assert.assertTrue(p1.test(t1));
		
		// case does not match
		t1.setName("Evin");
		Assert.assertTrue(p1.test(t1));
	}
}
