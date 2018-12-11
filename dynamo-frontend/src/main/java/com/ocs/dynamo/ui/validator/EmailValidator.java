package com.ocs.dynamo.ui.validator;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;

/**
 * A validator for email fields
 * 
 * @author Bas Rutten
 *
 */
public class EmailValidator implements Validator<String> {

	/** General Email Regex (RFC 5322 Official Standard) */
	private static final String EMAIL_PATTERN = "(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[a-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	private String message;

	private static final long serialVersionUID = 680372854650555066L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public EmailValidator(String message) {
		this.message = message;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if (value == null || "".equals(value)) {
			return ValidationResult.ok();
		}

		if (!value.toLowerCase().matches(EMAIL_PATTERN)) {
			return ValidationResult.error(message);
		}
		return ValidationResult.ok();
	}
}
