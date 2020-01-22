package com.ocs.dynamo.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class ContainsPredicateTest {

	@Test
	public void test() {
		ContainsPredicate<TestEntity> p1 = new ContainsPredicate<>("intTags", 4);

		assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		assertFalse(p1.test(t1));

		t1.getIntTags().add(5);
		assertFalse(p1.test(t1));

		t1.getIntTags().add(4);
		assertTrue(p1.test(t1));
	}
}
