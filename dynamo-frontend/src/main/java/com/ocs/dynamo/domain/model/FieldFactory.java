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
	 * Adds the converters and validators that are to be used for a component. This
	 * is separate from the actual component creation since the converters and
	 * validators must be configured on a binding builder
	 * 
	 * @param <U> the input type of the conversion
	 * @param <V> the target type of the conversion
	 * @param builder                 the builder
	 * @param am                      the attribute model
	 * @param context                 the component creation context
	 * @param customConverter         the custom converter to add
	 * @param customValidator         the custom validator to add
	 * @param customRequiredValidator the custom required validator to add
	 */
	<U, V> void addConvertersAndValidators(BindingBuilder<U, V> builder, AttributeModel am,
			FieldCreationContext context, Converter<V, U> customConverter, Validator<V> customValidator,
			Validator<V> customRequiredValidator);

	/**
	 * Convenience method for adding converters and validators, assumes no custom
	 * converters or validators
	 *
	 * @param <U> the input type of the conversion
	 * @param <V> the target type of the conversion
	 * @param builder the binding builder
	 * @param am      the attribute mode
	 */
	<U, V> void addConvertersAndValidators(BindingBuilder<U, V> builder, AttributeModel am);

	/**
	 * Constructs a field based on the provided attribute model (given the default
	 * context)
	 * 
	 * @param am the attribute model
	 * @return the constructed field
	 */
	Component constructField(AttributeModel am);

	/**
	 * Constructs a field based on the provided context
	 * 
	 * @param context the context
	 * @return the constructed field
	 */
	Component constructField(FieldCreationContext context);
}
