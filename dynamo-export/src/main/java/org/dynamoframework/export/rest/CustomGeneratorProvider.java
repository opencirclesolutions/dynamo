package org.dynamoframework.export.rest;

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

import org.dynamoframework.export.CustomXlsStyleGenerator;

/**
 * A provider for creating a custom generator for a specific entity class and optional reference
 */
public interface CustomGeneratorProvider {

	/**
	 * Returns whether the custom generator matches
	 *
	 * @param entityClass the entity class
	 * @param reference   the reference
	 * @return true if the custom generator matches the entity class and reference being requested
	 */
	boolean matches(Class<?> entityClass, String reference);

	/**
	 * @return the generator
	 */
	CustomXlsStyleGenerator getGenerator();
}
