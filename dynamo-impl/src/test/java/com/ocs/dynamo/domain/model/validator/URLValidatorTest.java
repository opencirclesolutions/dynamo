package com.ocs.dynamo.domain.model.validator;

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
		Assert.assertTrue(validator.isValid("www.google.nl", null));
		Assert.assertTrue(validator.isValid("mijn.site.nl", null));
		Assert.assertFalse(validator.isValid("mijn.site", null));
	}

	@Test
	public void testValid_basic() {
		Assert.assertFalse(validator.isValid("nowwwnonothing", null));
		Assert.assertFalse(validator.isValid("no-www-no-nothing", null));
		Assert.assertFalse(validator.isValid("no/www/no/nothing", null));
		Assert.assertFalse(validator.isValid("no$www$no$nothing", null));
		Assert.assertFalse(validator.isValid("no@www@no@nothing", null));
		Assert.assertFalse(validator.isValid("no www no nothing", null));
	}

	@Test
	public void testValid_exoticCharacters() {
		Assert.assertTrue(validator.isValid("http://www.goqsùdfù^ùê   ù^zù'^^\"'t\"'^m§\"'ogle.nl", null));
	}

	// With ommitting http:// for validation, just about any string is valid
	// (according to the java URL validity convention),
	// which renders the need to validate useless
	// @Test
	// public void testInvalid() {
	// Assert.assertFalse(validator.isValid("not_a_url", null));
	// Assert.assertFalse(validator.isValid("www.google.nl", null));
	// }
}
