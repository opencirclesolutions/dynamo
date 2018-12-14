package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class NotPredicateTest {

	@Test
	public void test() {

		EqualsPredicate<TestEntity> p1 = new EqualsPredicate<>("name", "Bob");
		NotPredicate<TestEntity> not = new NotPredicate<>(p1);

		TestEntity t1 = new TestEntity();
		t1.setName("Bob");
		Assert.assertFalse(not.test(t1));

		t1.setName("Rob");
		Assert.assertTrue(not.test(t1));
	}
}
