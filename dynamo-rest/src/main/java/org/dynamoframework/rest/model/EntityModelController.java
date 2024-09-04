package org.dynamoframework.rest.model;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelAction;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.exception.OCSValidationException;
import org.dynamoframework.exception.OcsNotFoundException;
import org.dynamoframework.utils.ClassUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "#{@'dynamoframework-org.dynamoframework.configuration.DynamoConfigurationProperties'.defaults.endpoints.model}")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@Tag(name = "Model", description = "Dynamo Model controller")
public class EntityModelController {

    private final EntityModelFactory entityModelFactory;

    private final EntityModelMapper entityModelMapper;

    /**
     * Retrieves an entity model based on the name of the entity
     *
     * @param entityName the name of the entity
     * @param reference  optional reference to the specific entity model
     * @return the entity model
     */
    @GetMapping("/{entityName}")
    @Operation(summary = "Retrieve an entity model")
    public EntityModelResponse getEntityModel(@PathVariable @Parameter(description = "The name of the entity") String entityName,
                                              @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference) {
        Class<?> clazz = ClassUtils.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Entity model for class %s could not be found".formatted(entityName));
        }

        EntityModel<?> model = getModel(reference, clazz);
        return entityModelMapper.mapEntityModel(model);
    }

    /**
     * Retrieves a nested entity model based on the main attribute name and the name of the attribute
     *
     * @param entityName    the main entity name
     * @param attributeName the name of the attribute
     * @return the entity model
     */
    @GetMapping("/{entityName}/attribute/{attributeName}")
    @Operation(summary = "Retrieve a nested entity model")
    public EntityModelResponse getNestedEntityModel(@PathVariable @Parameter(description = "The name of the entity") String entityName,
                                                    @PathVariable @Parameter(description = "The name of the attribute") String attributeName,
                                                    @RequestParam(required = false) @Parameter(description = "The entity model reference") String reference) {
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
     *
     * @param entityName the main entity name
     * @param actionId   the ID of the action
     * @return the entity model
     */
    @GetMapping("/{entityName}/action/{actionId}")
    @Operation(summary = "Retrieve an action entity model")
    public EntityModelResponse getActionEntityModel(@PathVariable @Parameter(description = "The name of the entity") String entityName,
                                                    @PathVariable @Parameter(description = "The ID of the action") String actionId,
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
