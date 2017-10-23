package com.ocs.dynamo.envers.domain;

import org.junit.Assert;
import org.junit.Test;

public class RevisionKeyTest {

	@Test
	public void testEquals() {
		RevisionKey<Integer> key1 = new RevisionKey<>(1, 1);
		Assert.assertTrue(key1.equals(key1));
		Assert.assertFalse(key1.equals(null));
		Assert.assertFalse(key1.equals(new Object()));

		RevisionKey<Integer> key2 = new RevisionKey<>(1, 2);
		Assert.assertFalse(key1.equals(key2));

		RevisionKey<Integer> key3 = new RevisionKey<>(2, 1);
		Assert.assertFalse(key1.equals(key3));
	}
}
