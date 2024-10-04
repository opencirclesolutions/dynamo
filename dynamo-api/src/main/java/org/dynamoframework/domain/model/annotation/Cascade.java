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

import org.dynamoframework.domain.model.CascadeMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define a cascade between components
 *
 * @author bas.rutten
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Cascade {

	/**
	 * The property to cascade to - if the value of the annotated property changes, then filter the
	 * values of the property that is referenced here
	 *
	 * @return
	 */
	String cascadeTo() default "";

	/**
	 * The path that is used for filtering the attribute that you cascade - i.e. how to get from the
	 * attribute that is being filtered to the property to base the filter on
	 *
	 * @return
	 */
	String filterPath() default "";

	/**
	 * When to apply the cascading - in search forms, edit forms, or both
	 *
	 * @return
	 */
	CascadeMode mode() default CascadeMode.BOTH;
}
