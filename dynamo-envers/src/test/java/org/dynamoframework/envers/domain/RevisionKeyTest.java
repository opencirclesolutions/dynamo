package org.dynamoframework.envers.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class RevisionKeyTest {

	@Test
	public void testEquals() {
		RevisionKey<Integer> key1 = new RevisionKey<>(1, 1);
		assertEquals(key1, key1);
		assertNotEquals(null, key1);
		assertNotEquals(key1, new Object());

		RevisionKey<Integer> key2 = new RevisionKey<>(1, 2);
		assertNotEquals(key1, key2);

		RevisionKey<Integer> key3 = new RevisionKey<>(2, 1);
		assertNotEquals(key1, key3);
	}
}
