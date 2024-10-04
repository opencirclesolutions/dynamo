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

import lombok.NoArgsConstructor;

/**
 * An exception that is used to indicate that an entity cannot be added or update because it would
 * violate a uniqueness constraint
 *
 * @author bas.rutten
 */
@NoArgsConstructor
public class OCSNonUniqueException extends OCSRuntimeException {

	private static final long serialVersionUID = -6263372299801009820L;

	public OCSNonUniqueException(String message) {
		super(message);
	}

	public OCSNonUniqueException(String message, Throwable cause) {
		super(message, cause);
	}
}
