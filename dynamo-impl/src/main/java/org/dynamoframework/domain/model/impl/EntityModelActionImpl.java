package org.dynamoframework.domain.model.impl;

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
import lombok.Setter;
import lombok.ToString;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelAction;
import org.dynamoframework.domain.model.EntityModelActionType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class EntityModelActionImpl implements EntityModelAction {

    private String id;

    private String methodName;

    @ToString.Exclude
    private final List<AttributeModel> attributeModels = new ArrayList<>();

    private String defaultDisplayName;

    private Class<?> entityClass;

    private String reference;

    private EntityModel<?> entityModel;

    private EntityModelActionType type;

    private String icon;

    private List<String> roles;

    private Map<String, Optional<String>> displayNames = new ConcurrentHashMap<>();

    @Override
    public String getDisplayName(Locale locale) {
        return lookup(displayNames, locale, EntityModel.DISPLAY_NAME, defaultDisplayName);
    }
    private String lookup(Map<String, Optional<String>> source, Locale locale, String key, String fallBack) {
        // look up in message bundle and add to cache
        if (!source.containsKey(locale.toString())) {
            try {
                ResourceBundle rb = ResourceBundle.getBundle("META-INF/entitymodel", locale);
                String str = rb.getString(id + "." + key);
                source.put(locale.toString(), Optional.of(str));
            } catch (MissingResourceException ex) {
                source.put(locale.toString(), Optional.empty());
            }
        }

        // look up or return fallback value
        Optional<String> optional = source.get(locale.toString());
        return optional.orElse(fallBack);
    }
}

