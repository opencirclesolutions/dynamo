package com.ocs.dynamo.ui.validator;

import java.util.Collection;

import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;

public class CollectionSizeValidator implements Validator<Collection<?>> {

	private String message;

	private static final long serialVersionUID = 680372854650555066L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public CollectionSizeValidator(String message) {
		this.message = message;
	}

	@Override
	public ValidationResult apply(Collection<?> value, ValueContext context) {
		if (value == null || value.isEmpty()) {
			return ValidationResult.error(message);
		}
		return ValidationResult.ok();
	}
}
