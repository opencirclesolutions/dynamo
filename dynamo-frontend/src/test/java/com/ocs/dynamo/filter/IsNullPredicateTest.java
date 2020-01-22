package com.ocs.dynamo.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class IsNullPredicateTest {

	@Test
	public void test() {
		IsNullPredicate<TestEntity> p1 = new IsNullPredicate<>("name");

		assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		t1.setName(null);
		assertTrue(p1.test(t1));
		
		t1.setName("Bob");
		assertFalse(p1.test(t1));
	}
}
