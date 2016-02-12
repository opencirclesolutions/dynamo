package nl.ocs.domain.validator;

import org.junit.Assert;
import org.junit.Test;

public class EmailValidatorTest {

	@Test
	public void testIsValid() {
		EmailValidator validator = new EmailValidator();

		Assert.assertTrue(validator.isValid(null, null));
		Assert.assertTrue(validator.isValid("kevin@opencirclesolutions.nl", null));

		Assert.assertFalse(validator.isValid("", null));
		Assert.assertFalse(validator.isValid("ab", null));
	}
}
