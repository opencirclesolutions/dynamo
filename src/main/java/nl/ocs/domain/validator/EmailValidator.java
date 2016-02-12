package nl.ocs.domain.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import nl.ocs.utils.StringUtil;

/**
 * A very simple validator for checking if a string value is a valid email
 * address
 * 
 * @author bas.rutten
 *
 */
public class EmailValidator implements ConstraintValidator<Email, String> {

	@Override
	public void initialize(Email constraintAnnotation) {
		// do nothing
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		return StringUtil.isValidEmail(value);
	}

}
