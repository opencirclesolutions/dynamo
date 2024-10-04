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
	 * @return additional context/instructions for automatically filling form
	 */
	String autofillInstructions() default "";

	/**
	 * @return whether "delete" operations are allowed
	 */
	boolean deleteAllowed() default false;

	/**
	 * @return the display name of the entity
	 */
	String displayName() default "";

	/**
	 * @return the display name (plural form) of the entity
	 */
	String displayNamePlural() default "";

	/**
	 * @return the textual description, will be used in tool tips
	 */
	String description() default "";

	/**
	 * @return the path of the property that will be used to describe the entity inside lookup
	 * components
	 */
	String displayProperty() default "";

	/**
	 * @return Whether "list" operations are allowed
	 */
	boolean listAllowed() default true;

	/**
	 * @return the maximum allowed number of search results
	 */
	int maxSearchResults() default Integer.MAX_VALUE;

	/**
	 * @return the default entity model nesting depth
	 */
	int nestingDepth() default -1;

	/**
	 * @return the default sort order (property name followed by option asc/desc, use commas to
	 * separate)
	 */
	String sortOrder() default "";

	/**
	 * @return whether creating new entities is allowed
	 */
	boolean createAllowed() default true;

	/**
	 * @return whether updating existing entities is allowed
	 */
	boolean updateAllowed() default true;

	/**
	 * @return whether "search" operations are allowed
	 */
	boolean searchAllowed() default true;

	/**
	 * @return whether exporting is allowed
	 */
	boolean exportAllowed() default true;

}
