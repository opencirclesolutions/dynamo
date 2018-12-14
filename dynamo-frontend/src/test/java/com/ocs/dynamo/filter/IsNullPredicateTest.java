package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class IsNullPredicateTest {

	@Test
	public void test() {
		IsNullPredicate<TestEntity> p1 = new IsNullPredicate<>("name");

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName(null);
		Assert.assertTrue(p1.test(t1));
		
		t1.setName("Bob");
		Assert.assertFalse(p1.test(t1));
	}
}
