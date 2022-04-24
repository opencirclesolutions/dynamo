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
import com.ocs.dynamo.domain.model.AttributeTextFieldMode;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Component creator that is used for creating a text area component used for
 * managing String attributes. Never used in search or grid mode
 * 
 * @author BasRutten
 *
 */
public class TextAreaComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		if (context.isSearch() || context.isEditableGrid()) {
			return false;
		}

		return AttributeType.BASIC.equals(attributeModel.getAttributeType())
				&& AttributeTextFieldMode.TEXTAREA.equals(attributeModel.getTextFieldMode())
				&& String.class.equals(attributeModel.getType());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {
		TextArea area = new TextArea();
		area.setHeight(am.getTextAreaHeight());
		return area;
	}
}
