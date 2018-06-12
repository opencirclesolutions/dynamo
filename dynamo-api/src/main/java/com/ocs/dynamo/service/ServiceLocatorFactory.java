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
package com.ocs.dynamo.service;

import org.apache.log4j.Logger;

import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.util.SystemPropertyUtils;

/**
 * Factory class for creating a service locator.
 *
 * @author bas.rutten
 *
 */
public final class ServiceLocatorFactory {

	private static volatile ServiceLocator serviceLocator;

	private static final Logger LOGGER = Logger.getLogger(ServiceLocatorFactory.class);

	private ServiceLocatorFactory() {
	}

	public static ServiceLocator getServiceLocator() {
		if (serviceLocator == null) {
			synchronized (ServiceLocatorFactory.class) {
				if (serviceLocator == null) {
					String serviceLocatorClassName = SystemPropertyUtils.getServiceLocatorClassName();
					LOGGER.info("Using service locator class " + serviceLocatorClassName);
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

}
