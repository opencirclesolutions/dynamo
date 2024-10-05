package org.dynamoframework.rest;

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

import lombok.Getter;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.exception.OcsNotFoundException;
import org.dynamoframework.service.impl.EntityScanner;
import org.dynamoframework.utils.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseController {

	@Autowired
	@Getter
	private EntityModelFactory entityModelFactory;

    @Autowired
    private EntityScanner entityScanner;

    /**
     * Looks up the class corresponding to the provided entity name. Throws an exception when no
     * matching class could be found
     *
     * @param entityName the entity name
     * @return the class
     */
    @SuppressWarnings("unchecked")
    protected <T> Class<T> findClass(String entityName) {
        Class<T> clazz = (Class<T>) entityScanner.findClass(entityName);
        if (clazz == null) {
            throw new OcsNotFoundException("Endpoint for entity %s not found".formatted(entityName));
        }
        return clazz;
    }

    /**
     * Retrieves the entity model for a class
     *
     * @param reference the reference to the entity model
     * @param clazz     the class
     * @return the entity model
     */
    protected <T> EntityModel<T> findEntityModel(String reference, Class<T> clazz) {
        return reference == null ? entityModelFactory.getModel(clazz) :
                entityModelFactory.getModel(reference, clazz);
    }
}
