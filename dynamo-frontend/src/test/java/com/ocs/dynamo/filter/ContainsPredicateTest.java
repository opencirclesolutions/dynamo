package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class ContainsPredicateTest {

	@Test
	public void test() {
		ContainsPredicate<TestEntity> p1 = new ContainsPredicate<>("intTags", 4);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		Assert.assertFalse(p1.test(t1));

		t1.getIntTags().add(5);
		Assert.assertFalse(p1.test(t1));

		t1.getIntTags().add(4);
		Assert.assertTrue(p1.test(t1));
	}
}
