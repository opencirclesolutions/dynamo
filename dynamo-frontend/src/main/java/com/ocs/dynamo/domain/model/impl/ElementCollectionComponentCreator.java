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

import java.math.BigDecimal;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.FieldCreationContext;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.ui.component.ElementCollectionGrid;
import com.ocs.dynamo.ui.composite.layout.FormOptions;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.Component;

/**
 * Component creator that is used for creating a grid for managing an element
 * collection
 * 
 * @author BasRutten
 *
 */
public class ElementCollectionComponentCreator implements SimpleComponentCreator {

	@Override
	public boolean supports(AttributeModel attributeModel, FieldCreationContext context) {
		return !context.isSearch() && AttributeType.ELEMENT_COLLECTION.equals(attributeModel.getAttributeType());
	}

	@Override
	public Component createComponent(AttributeModel am, FieldCreationContext context) {

		// use a "collection grid" for an element collection
		FormOptions fo = new FormOptions().setShowRemoveButton(true);

		boolean allowed = String.class.equals(am.getMemberType()) || NumberUtils.isLong(am.getMemberType())
				|| NumberUtils.isInteger(am.getMemberType()) || BigDecimal.class.equals(am.getMemberType());
		if (allowed) {
			return new ElementCollectionGrid<>(am, fo);
		} else {
			// other types not supported for now
			throw new OCSRuntimeException(
					"Element collections of type " + am.getMemberType() + " are currently not supported");
		}
	}

}
