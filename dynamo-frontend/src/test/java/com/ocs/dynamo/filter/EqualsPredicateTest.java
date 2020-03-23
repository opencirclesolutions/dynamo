package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class EqualsPredicateTest {

	@Test
	public void test() {
		EqualsPredicate<TestEntity> predicate = new EqualsPredicate<TestEntity>("name", "Bob");

		assertFalse(predicate.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName("Bob");
		assertTrue(predicate.test(t1));

		t1.setName("Kevin");
		assertFalse(predicate.test(t1));
	}
}
