package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class EqualsPredicateTest {

	@Test
	public void test() {
		EqualsPredicate<TestEntity> predicate = new EqualsPredicate<TestEntity>("name", "Bob");

		Assert.assertFalse(predicate.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName("Bob");
		Assert.assertTrue(predicate.test(t1));

		t1.setName("Kevin");
		Assert.assertFalse(predicate.test(t1));
	}
}
