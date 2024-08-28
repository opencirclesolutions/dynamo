package org.dynamoframework.exception;

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

public class OCSUnmarshallException extends OCSRuntimeException {

    private static final long serialVersionUID = 6144119769190157855L;

    public OCSUnmarshallException() {
        super();
    }

    public OCSUnmarshallException(Throwable cause) {
        super(cause);
    }

    public OCSUnmarshallException(String message) {
        super(message);
    }

    public OCSUnmarshallException(String message, Throwable cause) {
        super(message, cause);
    }
}
