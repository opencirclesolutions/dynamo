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
 * An exception that is used to indicates that an entity cannot be added or update because it would
 * violate a uniqueness constraint
 * 
 * @author bas.rutten
 */
public class OCSNonUniqueException extends RuntimeException {

    private static final long serialVersionUID = -6263372299801009820L;

    public OCSNonUniqueException() {
    }

    public OCSNonUniqueException(String message) {
        super(message);
    }

    public OCSNonUniqueException(String message, Throwable cause) {
        super(message, cause);
    }
}
