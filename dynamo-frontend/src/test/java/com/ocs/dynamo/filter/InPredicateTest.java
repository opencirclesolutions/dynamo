package com.ocs.dynamo.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.ocs.dynamo.domain.TestEntity;

public class InPredicateTest {

	@Test
	public void test() {
		InPredicate<TestEntity> p1 = new InPredicate<TestEntity>("age", Lists.newArrayList(4L, 5L, 6L));

		assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setAge(7L);

		assertFalse(p1.test(t1));

		t1.setAge(4L);
		assertTrue(p1.test(t1));

	}
}
