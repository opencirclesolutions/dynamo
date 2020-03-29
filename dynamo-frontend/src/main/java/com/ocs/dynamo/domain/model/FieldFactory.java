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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;

/**
 * Interface for model based component factories
 * 
 * @author patrickdeenen
 *
 */
public interface FieldFactory {

	/**
	 * 
	 * @return the singleton instance of the field factory
	 */
	static FieldFactory getInstance() {
		return ServiceLocatorFactory.getServiceLocator().getServiceByName("fieldFactory", FieldFactory.class);
	}

	/**
	 * Adds converters and validators for a field
	 * 
	 * @param builder         the binding builder to which to add the converters and
	 *                        validators
	 * @param am              the attribute model for the field
	 * @param customConverter custom converter to be used for data conversion
	 */
	<U, V> void addConvertersAndValidators(BindingBuilder<U, V> builder, AttributeModel am,
			Converter<V, U> customConverter, Validator<V> customValidator, Validator<V> customRequiredValidator);

	/**
	 * Constructs a field based on the provided attribute model (given the default
	 * context)
	 * 
	 * @param am the attribute model
	 * @return
	 */
	Component constructField(AttributeModel am);

	/**
	 * Constructs a field based on the provided context
	 * 
	 * @param context the context
	 * @return
	 */
	Component constructField(FieldFactoryContext context);
}
