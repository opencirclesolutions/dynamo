package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class OrPredicateTest {

	@Test
	public void test() {

		EqualsPredicate<TestEntity> p1 = new EqualsPredicate<>("name", "Bob");
		EqualsPredicate<TestEntity> p2 = new EqualsPredicate<>("age", 44L);

		OrPredicate<TestEntity> or = new OrPredicate<>(p1);

		TestEntity t1 = new TestEntity();
		t1.setName("Bob");
		t1.setAge(45L);
		assertTrue(or.test(t1));

		// first matches, second doesn't
		or = new OrPredicate<>(p1, p2);
		assertTrue(or.test(t1));

		// neither predicate matches
		t1.setName("Kevin");
		assertFalse(or.test(t1));
	}
}
