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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;

/**
 * Component creator for creating a check box for modifying a Boolean attribute
 * 
 * @author BasRutten
 *
 */
public class BooleanCheckboxCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		return !context.isSearch() && attributeModel.isBoolean();
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		return new Checkbox();
	}
}
