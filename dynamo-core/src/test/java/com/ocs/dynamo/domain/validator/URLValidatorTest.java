package com.ocs.dynamo.domain.validator;

import org.junit.Assert;
import org.junit.Test;

public class URLValidatorTest {

	private URLValidator validator = new URLValidator();

	@Test
	public void testNull() {
		Assert.assertTrue(validator.isValid(null, null));
	}

	@Test
	public void testValid() {
		Assert.assertTrue(validator.isValid("http://www.google.nl", null));
		Assert.assertTrue(validator.isValid("http://www.part", null));
	}

	@Test
	public void testInvalid() {
		Assert.assertFalse(validator.isValid("not_a_url", null));
		Assert.assertFalse(validator.isValid("www.google.nl", null));
	}
}
