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

import com.vaadin.data.Validator;

/**
 * Vaadin validator for checking if an field value is a vallid URL
 * 
 * @author bas.rutten
 *
 */
public class URLValidator implements Validator {

    private String message;

    private static final long serialVersionUID = 680372854650555066L;

    public URLValidator(String message) {
        this.message = message;
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        if (value == null) {
            return;
        }

        if (!(value instanceof String)) {
            throw new InvalidValueException(message);
        }

        try {
            new java.net.URL((String) value);
        } catch (MalformedURLException ex) {
            throw new InvalidValueException(message);
        }
    }

}
