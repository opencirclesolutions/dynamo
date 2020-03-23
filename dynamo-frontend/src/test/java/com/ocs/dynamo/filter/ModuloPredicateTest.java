package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class ModuloPredicateTest {

	@Test
	public void test() {
		ModuloPredicate<TestEntity> p1 = new ModuloPredicate<TestEntity>("age", 4, 2L);

		assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		assertFalse(p1.test(t1));

		t1.setAge(6L);
		assertTrue(p1.test(t1));

		t1.setAge(2L);
		assertTrue(p1.test(t1));

		t1.setAge(7L);
		assertFalse(p1.test(t1));
	}
	
	@Test
	public void testExpression() {
		ModuloPredicate<TestEntity> p1 = new ModuloPredicate<TestEntity>("age", "someInt", 2L);

		assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		assertFalse(p1.test(t1));

		t1.setAge(6L);
		t1.setSomeInt(4);
		assertTrue(p1.test(t1));

		t1.setAge(2L);
		assertTrue(p1.test(t1));

		t1.setAge(7L);
		assertFalse(p1.test(t1));
	}
}
