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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EmailValidatorTest {

    @Test
    public void testIsValid() {
        EmailValidator validator = new EmailValidator();

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("kevin@opencirclesolutions.nl", null));
        assertTrue(validator.isValid("Bob@Opencirclesolutions.nl", null));
        assertTrue(validator.isValid("", null));
        assertFalse(validator.isValid("ab", null));
    }
}
