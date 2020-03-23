package com.ocs.dynamo.envers.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RevisionKeyTest {

	@Test
	public void testEquals() {
		RevisionKey<Integer> key1 = new RevisionKey<>(1, 1);
		assertTrue(key1.equals(key1));
		assertFalse(key1.equals(null));
		assertFalse(key1.equals(new Object()));

		RevisionKey<Integer> key2 = new RevisionKey<>(1, 2);
		assertFalse(key1.equals(key2));

		RevisionKey<Integer> key3 = new RevisionKey<>(2, 1);
		assertFalse(key1.equals(key3));
	}
}
