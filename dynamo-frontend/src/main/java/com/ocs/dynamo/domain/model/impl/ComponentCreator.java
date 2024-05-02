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
package com.ocs.dynamo.domain.model.impl;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;

/**
 * Interface for use with the FieldFactory - contains methods that all
 * ComponentCreators must support
 * 
 * @author BasRutten
 *
 */
public interface ComponentCreator {

	/**
	 * Checks whether the ComponentCreator supports creating a component for the
	 * provided attribute model and context
	 * 
	 * @param attributeModel the attribute model
	 * @param context        the context
	 * @return true if the ComponentCreator supports creating a component
	 */
	boolean supports(AttributeModel attributeModel, FieldCreationContext context);

	default <U, V> void addConverters(AttributeModel attributeModel, BindingBuilder<U, V> builder) {
		// do nothing
	}

	default <U, V> void addValidators(AttributeModel attributeModel, BindingBuilder<U, V> builder) {
		// do nothing
	}
}
