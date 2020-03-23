package com.ocs.dynamo.filter;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class BetweenPredicateTest {

	@Test
	public void test() {
		BetweenPredicate<TestEntity> p1 = new BetweenPredicate<>("age", 10L, 20L);

		assertFalse(p1.test(null));

		// too low
		TestEntity t1 = new TestEntity();
		t1.setAge(4L);
		assertFalse(p1.test(t1));

		// lower boundary
		t1.setAge(10L);
		assertTrue(p1.test(t1));

		// inside
		t1.setAge(15L);
		assertTrue(p1.test(t1));

		// upper boundary
		t1.setAge(20L);
		assertTrue(p1.test(t1));

		// too high
		t1.setAge(21L);
		assertFalse(p1.test(t1));
	}
}
