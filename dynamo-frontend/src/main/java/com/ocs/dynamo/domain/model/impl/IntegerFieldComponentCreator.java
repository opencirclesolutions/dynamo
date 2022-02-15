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
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.domain.model.NumberFieldMode;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.IntegerField;

/**
 * A component creator for creating a number field for managing integers
 * 
 * @author BasRutten
 *
 */
public class IntegerFieldComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {

		// only for basic non URL attributes
		if (!AttributeType.BASIC.equals(attributeModel.getAttributeType()) || attributeModel.isUrl()) {
			return false;
		}

		return NumberFieldMode.NUMBERFIELD.equals(attributeModel.getNumberFieldMode()) && !attributeModel.isPercentage()
				&& NumberUtils.isInteger(attributeModel.getType());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		IntegerField intField = new IntegerField();
		intField.setHasControls(true);
		intField.setStep(am.getNumberFieldStep());
		return intField;
	}

}
