package com.ocs.dynamo.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.ocs.dynamo.domain.TestEntity;

public class LikePredicateTest {

	@Test
	public void testCaseInsensitive() {
		LikePredicate<TestEntity> like = new LikePredicate<>("name", "%evi%", false);

		assertFalse(like.test(null));

		TestEntity t1 = new TestEntity();

		// regular match
		t1.setName("Kevin");
		assertTrue(like.test(t1));

		// case-insensitive match
		t1.setName("KEVIN");
		assertTrue(like.test(t1));

		// no match
		t1.setName("Kevon");
		assertFalse(like.test(t1));
	}
	
	@Test
	public void testCaseSensitive() {
		LikePredicate<TestEntity> like = new LikePredicate<>("name", "%evi%", true);

		assertFalse(like.test(null));

		TestEntity t1 = new TestEntity();

		// regular match
		t1.setName("Kevin");
		assertTrue(like.test(t1));

		// case-insensitive match
		t1.setName("KEVIN");
		assertFalse(like.test(t1));

		// no match
		t1.setName("Kevon");
		assertFalse(like.test(t1));
	}
}
