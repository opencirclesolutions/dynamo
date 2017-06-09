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
package com.ocs.dynamo.exception;

/**
 * Base class for any runtime exceptions. Must always be used instead of a plain
 * java.lang.RuntimeException. Subclass when appropriate
 * 
 * @author bas.rutten
 */
public class OCSRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -6263372299801009820L;

    public OCSRuntimeException() {
    	// default constructor
    }

    public OCSRuntimeException(String message) {
        super(message);
    }

    public OCSRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
