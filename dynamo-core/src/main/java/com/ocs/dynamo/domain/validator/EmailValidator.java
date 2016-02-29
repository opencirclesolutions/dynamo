package com.ocs.dynamo.domain.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A very simple {@link ConstraintValidator} for checking if a string value is a valid email address.
 * 
 * @author bas.rutten
 */
public class EmailValidator implements ConstraintValidator<Email, String> {

	/** Regular expression for an e-mail pattern. */
	private static final String EMAIL_PATTERN = "(.+)@(.+)";

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#isValid(java.lang.Object, javax.validation.ConstraintValidatorContext)
	 */
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return isValidEmail(value);
	}

	/* (non-Javadoc)
	 * @see javax.validation.ConstraintValidator#initialize(java.lang.annotation.Annotation)
	 */
	@Override
	public void initialize(Email constraintAnnotation) {}
	
	/**
	 * Checks if an value is a valid email address - this is actually a very
	 * simple check that only checks for the @-sign.
	 * 
	 * @param value the value to check.
	 */	
	private boolean isValidEmail(String value) {
		if (value == null) {
			return true;
		}

		return value.matches(EMAIL_PATTERN);
	}
}
