package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class LessOrEqualPredicateTest {

	@Test
	public void test() {
		LessOrEqualPredicate<TestEntity> p1 = new LessOrEqualPredicate<>("age", 20L);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setAge(20L);
		Assert.assertTrue(p1.test(t1));

		t1.setAge(24L);
		Assert.assertFalse(p1.test(t1));

		t1.setAge(18L);
		Assert.assertTrue(p1.test(t1));
	}
}
