package org.dynamoframework.domain.model.validator;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;

/**
 * A validator for checking if a String is a valid URL
 *
 * @author bas.rutten
 */
public class URLValidator implements ConstraintValidator<URL, String> {

	private static final String URL_PREFIX_SECURE = "https://";

	private static final String URL_PREFIX = "http://";

	@Override
	public void initialize(URL constraintAnnotation) {
		// do nothing
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isEmpty(value)) {
			return true;
		}

		if (!value.contains(URL_PREFIX) && !value.contains(URL_PREFIX_SECURE)) {
			value = URL_PREFIX + value;
		}

		try {
			new java.net.URL(value);
		} catch (MalformedURLException ex) {
			// do nothing
			return false;
		}
		return true;
	}

}
