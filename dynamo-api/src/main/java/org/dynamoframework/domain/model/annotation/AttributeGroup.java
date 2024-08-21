package org.dynamoframework.domain.model.annotation;

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

import java.lang.annotation.*;

/**
 * An annotation that can be used to indicate that certain attribute should be
 * grouped together on the screen
 * 
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AttributeGroups.class)
public @interface AttributeGroup {

    /**
     * The names/paths of the attributes that appear in the group
     * 
     * @return
     */
    String[] attributeNames() default {};

    /**
     * The message key that is used as the identifier of the group
     * 
     * @return
     */
    String messageKey();

}
