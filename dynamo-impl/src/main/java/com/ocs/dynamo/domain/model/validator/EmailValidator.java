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
package com.ocs.dynamo.domain.model.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A very simple {@link ConstraintValidator} for checking if a string value is a valid email
 * address.
 * 
 * @author bas.rutten
 */
public class EmailValidator implements ConstraintValidator<Email, String> {

    /** Regular expression for an e-mail pattern. */
    private static final String EMAIL_PATTERN = "(.+)@(.+)";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return isValidEmail(value);
    }

    @Override
    public void initialize(Email constraintAnnotation) {
        // do nothing
    }

    /**
     * Checks if an value is a valid email address - this is actually a very simple check that only
     * checks for the @-sign.
     * 
     * @param value
     *            the value to check.
     */
    private boolean isValidEmail(String value) {
        if (value == null) {
            return true;
        }

        return value.matches(EMAIL_PATTERN);
    }
}
