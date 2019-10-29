package com.ocs.dynamo.ui.validator;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

public class EmailValidatorTest {

	@Test
	public void test() {
		EmailValidator validator = new EmailValidator("message");

		ValidationResult res = validator.apply(null, new ValueContext());
		Assert.assertFalse(res.isError());

		res = validator.apply("", new ValueContext());
		Assert.assertFalse(res.isError());

		res = validator.apply("test@test.nl", new ValueContext());
		Assert.assertFalse(res.isError());

		res = validator.apply("bogus", new ValueContext());
		Assert.assertTrue(res.isError());
	}
}
