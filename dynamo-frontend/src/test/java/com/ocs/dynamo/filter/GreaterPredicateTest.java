package com.ocs.dynamo.filter;

import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class GreaterPredicateTest {

	@Test
	public void test() {
		GreaterThanPredicate<TestEntity> p1 = new GreaterThanPredicate<>("age", 20L);
		
		assertFalse(p1.test(null));
		
		TestEntity t1 = new TestEntity();
		t1.setAge(20L);
		assertFalse(p1.test(t1));
		
		t1.setAge(24L);
		Assert.assertTrue(p1.test(t1));
		
		t1.setAge(18L);
		assertFalse(p1.test(t1));
	}
}
