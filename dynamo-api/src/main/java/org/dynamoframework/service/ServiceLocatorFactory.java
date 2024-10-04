package org.dynamoframework.service;

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

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.exception.OCSRuntimeException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Factory class for creating a service locator.
 *
 * @author bas.rutten
 */
@UtilityClass
@Slf4j
public final class ServiceLocatorFactory {

	private static volatile ServiceLocator serviceLocator;

	private static final String DEFAULT_CLASS_NAME = "org.dynamoframework.SpringWebServiceLocator";

	@SuppressWarnings("deprecation")
	public static ServiceLocator getServiceLocator() {
		if (serviceLocator == null) {
			synchronized (ServiceLocatorFactory.class) {
				if (serviceLocator == null) {
					String serviceLocatorClassName = getServiceLocatorClassName();
					log.info("Using service locator class {} ", serviceLocatorClassName);
					try {
						serviceLocator = (ServiceLocator) Class.forName(serviceLocatorClassName).newInstance();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						throw new OCSRuntimeException(e.getMessage());
					}
				}
			}
		}
		return serviceLocator;
	}

	protected static String getServiceLocatorClassName() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(ServiceLocatorFactory.class.getClassLoader().getResourceAsStream("dynamoservicelocator")))) {
			String className = in.readLine();
			if (className != null) {
				return className;
			}
		} catch (Exception e) {
			log.debug("Resource dynamoservicelocator not found or could not be read, using default service locator");
		}
		return DEFAULT_CLASS_NAME;
	}

}
