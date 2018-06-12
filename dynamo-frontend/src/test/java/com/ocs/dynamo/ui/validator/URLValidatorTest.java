package com.ocs.dynamo.ui.validator;

import org.junit.Test;

import com.vaadin.data.Validator.InvalidValueException;

public class URLValidatorTest {

	@Test(expected = InvalidValueException.class)
	public void testFalse1() {
		URLValidator validator = new URLValidator("Not a valid URL");
		validator.validate("test");
	}

	@Test(expected = InvalidValueException.class)
	public void testFalse2() {
		URLValidator validator = new URLValidator("Not a valid URL");
		validator.validate("44");
	}

	@Test
	public void testCorrect1() {
		URLValidator validator = new URLValidator("Not a valid URL");
		validator.validate("http://www.google.nl");
	}

	@Test
	public void testCorrect2() {
		URLValidator validator = new URLValidator("Not a valid URL");
		validator.validate("www.google.nl");
	}

	@Test
	public void testCorrect3() {
		URLValidator validator = new URLValidator("Not a valid URL");
		validator.validate("mijn.site.nl");
	}
}
