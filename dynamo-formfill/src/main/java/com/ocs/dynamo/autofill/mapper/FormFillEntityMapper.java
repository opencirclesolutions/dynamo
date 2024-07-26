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

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.utils.ClassUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FormFillEntityMapper implements FormFillMapper {

    @Override
    public boolean supports(AttributeModel model) {
        return model.getAttributeType() == AttributeType.MASTER
                || (model.getAttributeType() == AttributeType.DETAIL && !model.isNestedDetails());
    }

    @Override
    public FormFillRecord map(AttributeModel model) {
        return new FormFillRecord(model, model.getAttributeType() == AttributeType.MASTER ? "a String"
                : "a list of Strings");
    }

    @Override
    public Object mapBack(AttributeModel model, Object value) {

        List<Map<String, Object>> results = new ArrayList<>();

        if (model.getNestedEntityModel() != null) {

            List<String> values = new ArrayList<>();
            if (value instanceof String str) {
                String[] split = str.split(",");
                values = Arrays.asList(split);
            } else if (value instanceof List) {
                values = (List<String>) value;
            }

            BaseService<?, ?> service = ServiceLocatorFactory.getServiceLocator().getServiceForEntity(model.getNestedEntityModel().getEntityClass());
            String displayProperty = model.getNestedEntityModel().getDisplayProperty();
            if (service != null) {
                for (String val : values) {
                    AbstractEntity<?> entity = service.findByUniqueProperty(displayProperty,
                            val, false);

                    if (entity == null) {
                        List<AbstractEntity<?>> partialMatches = (List<AbstractEntity<?>>) service.find(new Like(displayProperty,
                                "%" + value.toString().trim() + "%", false));
                        if (!partialMatches.isEmpty()) {
                            entity = partialMatches.getFirst();
                        }
                    }

                    if (entity != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", entity.getId());
                        map.put(model.getNestedEntityModel().getDisplayProperty(),
                                ClassUtils.getFieldValue(entity, displayProperty));
                        if (model.getAttributeType() == AttributeType.MASTER) {
                            return map;
                        }
                        results.add(map);
                    }
                }
            }
        }
        return results.isEmpty() ? null : results;
    }
}
