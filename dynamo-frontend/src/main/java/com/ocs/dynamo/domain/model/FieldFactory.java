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
package com.ocs.dynamo.domain.model;

import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.vaadin.ui.AbstractComponent;

/**
 * Interface for model based component factories
 * 
 * @author patrickdeenen
 *
 */
public interface FieldFactory {

	/**
	 * 
	 * @param context
	 * @return
	 */
	AbstractComponent constructField(FieldFactoryContext context);

	public static FieldFactory getInstance() {
		return ServiceLocatorFactory.getServiceLocator().getService(FieldFactory.class);
	}
}
