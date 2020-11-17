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
package com.ocs.dynamo.domain.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be placed on an entity - allows you to override metadata defaults
 * 
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Model {

    /**
     * 
     * @return the display name of the entity
     */
    String displayName() default "";

    /**
     * 
     * @return the display name (plural form) of the entity
     */
    String displayNamePlural() default "";

    /**
     * 
     * @return the textual description, will be used in tool tips
     */
    String description() default "";

    /**
     * 
     * @return the path of the property that will be used to describe the entity inside lookup
     *         components
     */
    String displayProperty() default "";

    /**
     * 
     * @return the default sort order (property name followed by option asc/desc, use commas to
     *         separate)
     */
    String sortOrder() default "";
    
    /**
     * @return the default entity model nesting depth
     */
    int nestingDepth() default -1;
}
