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
package org.dynamoframework.autofill.mapper;

import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.AttributeType;
import org.springframework.stereotype.Component;

@Component
public class FormFillElementCollectionMapper implements FormFillMapper {

    @Override
    public boolean supports(AttributeModel model) {
        return model.getAttributeType() == AttributeType.ELEMENT_COLLECTION;
    }

    @Override
    public FormFillRecord map(AttributeModel model) {
        String instructions = model.getType().equals(String.class) ? "A list of Strings" :
                "A list of numbers";
        return new FormFillRecord(model, instructions);
    }
}
