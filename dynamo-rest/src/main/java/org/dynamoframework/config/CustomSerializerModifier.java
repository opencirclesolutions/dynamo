package org.dynamoframework.config;

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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom serializer that will remove any properties from the serialization process that
 * are not backed up by an attribute from the attribute model or that are hidden
 */
@Slf4j
@RequiredArgsConstructor
public class CustomSerializerModifier extends BeanSerializerModifier {

    private final EntityModelFactory entityModelFactory;

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        Class<?> beanClass = beanDesc.getBeanClass();
        if (!AbstractEntity.class.isAssignableFrom(beanClass)) {
            return beanProperties;
        }

        List<BeanPropertyWriter> copy = new ArrayList<>(beanProperties);
        EntityModel<?> model = entityModelFactory.getModel(beanClass);
        if (model != null) {

            Iterator<BeanPropertyWriter> iterator = copy.iterator();
            while (iterator.hasNext()) {
                BeanPropertyWriter next = iterator.next();

                AttributeModel attributeModel = model.getAttributeModel(next.getName());
                if (attributeModel == null && !model.hasEmbeddedAttributeModel(next.getName())) {
                    log.info("Removing unbacked property {}", next.getName());
                    iterator.remove();
                }
            }
        }
        return copy;
    }

}
