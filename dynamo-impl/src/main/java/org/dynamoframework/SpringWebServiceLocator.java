package org.dynamoframework;

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

import org.dynamoframework.service.impl.BaseSpringServiceLocator;
import org.springframework.context.ApplicationContext;

public class SpringWebServiceLocator extends BaseSpringServiceLocator {

	/**
	 * Lazily loads the context
	 */
	protected ApplicationContext loadContext() {
		return ApplicationContextProvider.getApplicationContext();
	}
}
