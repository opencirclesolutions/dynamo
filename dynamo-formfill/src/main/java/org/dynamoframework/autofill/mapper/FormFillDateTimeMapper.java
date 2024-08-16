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
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class FormFillDateTimeMapper implements FormFillMapper {

    @Override
    public boolean supports(AttributeModel model) {
        return model.getType().equals(LocalDateTime.class) ||
                model.getType().equals(Instant.class);
    }

    @Override
    public FormFillRecord map(AttributeModel model) {
        return new FormFillRecord(model, "a date and time using format 'yyyy-MM-ddTHH:mm:ss'");
    }
}
