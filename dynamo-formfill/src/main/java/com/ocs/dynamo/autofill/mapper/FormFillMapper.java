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
package com.ocs.dynamo.autofill.mapper;

import com.ocs.dynamo.domain.model.AttributeModel;

public interface FormFillMapper {

    boolean supports(AttributeModel model);

    FormFillRecord map(AttributeModel model);

    default Object mapBack(AttributeModel model, Object value) {
        return value;
    }
}