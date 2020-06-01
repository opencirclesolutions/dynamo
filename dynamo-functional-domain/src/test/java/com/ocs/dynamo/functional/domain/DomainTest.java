package com.ocs.dynamo.functional.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DomainTest {

	@Test
	public void testEquals() {

		Country c1 = new Country("NL", "Nederland");
		c1.setId(1);

		Country c2 = new Country("NL", "Nederland");
		c2.setId(1);

		Country c3 = new Country("NL", "Nederland");
		Country c4 = new Country("NL", "Nederland");

		Region r1 = new Region(null, "Europe");
		r1.setId(1);

		assertFalse(c1.equals(null));
		assertFalse(c1.equals(new Object()));

		// IDs match but class is different
		assertFalse(c1.equals(r1));

		// IDs are the same
		assertTrue(c1.equals(c2));
		// no IDs, check the code and type instead
		assertTrue(c3.equals(c4));

		// partial IDs
		assertTrue(c1.equals(c4));
	}
}
