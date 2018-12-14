package com.ocs.dynamo.filter;

import org.junit.Assert;
import org.junit.Test;

import com.ocs.dynamo.domain.TestEntity;

public class LikePredicateTest {

	@Test
	public void testCaseInsensitive() {
		LikePredicate<TestEntity> like = new LikePredicate<>("name", "%evi%", false);

		Assert.assertFalse(like.test(null));

		TestEntity t1 = new TestEntity();

		// regular match
		t1.setName("Kevin");
		Assert.assertTrue(like.test(t1));

		// case-insensitive match
		t1.setName("KEVIN");
		Assert.assertTrue(like.test(t1));

		// no match
		t1.setName("Kevon");
		Assert.assertFalse(like.test(t1));
	}
	
	@Test
	public void testCaseSensitive() {
		LikePredicate<TestEntity> like = new LikePredicate<>("name", "%evi%", true);

		Assert.assertFalse(like.test(null));

		TestEntity t1 = new TestEntity();

		// regular match
		t1.setName("Kevin");
		Assert.assertTrue(like.test(t1));

		// case-insensitive match
		t1.setName("KEVIN");
		Assert.assertFalse(like.test(t1));

		// no match
		t1.setName("Kevon");
		Assert.assertFalse(like.test(t1));
	}
}
