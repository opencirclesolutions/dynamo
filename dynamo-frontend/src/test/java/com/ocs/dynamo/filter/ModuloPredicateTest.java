package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class ModuloPredicateTest {

	@Test
	public void test() {
		ModuloPredicate<TestEntity> p1 = new ModuloPredicate<TestEntity>("age", 4, 2L);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		Assert.assertFalse(p1.test(t1));

		t1.setAge(6L);
		Assert.assertTrue(p1.test(t1));

		t1.setAge(2L);
		Assert.assertTrue(p1.test(t1));

		t1.setAge(7L);
		Assert.assertFalse(p1.test(t1));
	}
	
	@Test
	public void testExpression() {
		ModuloPredicate<TestEntity> p1 = new ModuloPredicate<TestEntity>("age", "someInt", 2L);

		Assert.assertFalse(p1.test(null));

		TestEntity t1 = new TestEntity();
		Assert.assertFalse(p1.test(t1));

		t1.setAge(6L);
		t1.setSomeInt(4);
		Assert.assertTrue(p1.test(t1));

		t1.setAge(2L);
		Assert.assertTrue(p1.test(t1));

		t1.setAge(7L);
		Assert.assertFalse(p1.test(t1));
	}
}
