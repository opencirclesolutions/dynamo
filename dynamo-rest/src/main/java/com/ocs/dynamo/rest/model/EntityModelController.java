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
package com.ocs.dynamo.rest.model;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelAction;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.exception.OcsNotFoundException;
import com.ocs.dynamo.utils.ClassUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/model")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
public class EntityModelController {

    private final EntityModelFactory entityModelFactory;

    private final EntityModelMapper entityModelMapper;

    /**
     * Retrieves an entity model based on the name of the entity
     * @param entityName the name of the entity
     * @param reference optional reference to the specific entity model
     * @return the entity model
     */
    @GetMapping("/{entityName}")
    public EntityModelResponse getEntityModel(@PathVariable String entityName,
                                              @RequestParam(required = false) String reference) {
        Class<?> clazz = ClassUtils.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Entity model for class %s could not be found".formatted(entityName));
        }

        EntityModel<?> model = getModel(reference, clazz);
        return entityModelMapper.mapEntityModel(model);
    }

    /**
     * Retrieves a nested entity model based on the main attribute name and the name of the attribute
     * @param entityName the main entity name
     * @param attributeName the name of the attribute
     * @return the entity model
     */
    @GetMapping("/{entityName}/attribute/{attributeName}")
    public EntityModelResponse getNestedEntityModel(@PathVariable String entityName,
                                                    @PathVariable String attributeName,
                                                    @RequestParam(required = false) String reference) {
        Class<?> clazz = ClassUtils.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Entity model for class %s could not be found".formatted(entityName));
        }

        EntityModel<?> model = getModel(reference, clazz);
        AttributeModel am = model.getAttributeModel(attributeName);
        if (am == null) {
            throw new OcsNotFoundException("Attribute model for attribute %s could not be found".formatted(attributeName));
        }
        EntityModel<?> nested = am.getNestedEntityModel();

        return entityModelMapper.mapEntityModel(nested);
    }

    /**
     * Retrieves a nested entity model based on the main attribute name and the name of the attribute
     * @param entityName the main entity name
     * @param actionId the name of the attribute
     * @return the entity model
     */
    @GetMapping("/{entityName}/action/{actionId}")
    public EntityModelResponse getActionEntityModel(@PathVariable String entityName,
                                                    @PathVariable String actionId,
                                                    @RequestParam(required = false) String reference) {
        Class<?> clazz = ClassUtils.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Entity model for class %s could not be found".formatted(entityName));
        }

        EntityModel<?> model = getModel(reference, clazz);
        EntityModelAction action = model.findAction(actionId);
        if (action == null) {
            throw new OCSValidationException("Action with ID %s cannot be found"
                    .formatted(actionId));
        }

        return entityModelMapper.mapEntityModel(action.getEntityModel());
    }

    private EntityModel<?> getModel(@RequestParam(required = false) String reference, Class<?> clazz) {
        return StringUtils.isEmpty(reference) ? entityModelFactory.getModel(clazz)
                : entityModelFactory.getModel(reference, clazz);
    }


}