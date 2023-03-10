/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ocs.dynamo.ui.validator;

import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

/**
 * Vaadin validator for checking if an field value is a valid URL
 * 
 * @author bas.rutten
 *
 */
public class URLValidator implements Validator<String> {

	private static final String URL_PREFIX_SECURE = "https://";

	private static final String URL_PREFIX = "http://";

	private final String message;

	private static final long serialVersionUID = 680372854650555066L;

	/**
	 * Constructor
	 * 
	 * @param message the message to show when an error occurs
	 */
	public URLValidator(String message) {
		this.message = message;
	}

	@Override
	public ValidationResult apply(String value, ValueContext context) {
		if (value == null || "".equals(value)) {
			return ValidationResult.ok();
		}

		String str = value;
		if (!str.contains(URL_PREFIX) && !str.contains(URL_PREFIX_SECURE)) {
			str = URL_PREFIX + str;
		}

		try {
			new java.net.URL(str);
		} catch (MalformedURLException ex) {
			return ValidationResult.error(message);
		}

		// assume at least 2 dots
		if (StringUtils.countMatches(str, ".") < 2) {
			return ValidationResult.error(message);
		}

		return ValidationResult.ok();
	}

}
