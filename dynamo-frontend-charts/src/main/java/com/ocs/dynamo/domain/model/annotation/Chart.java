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
 * An interface that can be used to specify the properties of an chart - this will override any defaults
 * 
 * @author Patrick.Deenen@opencirclesolutions.nl
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Chart {

	/** @return the subtitle */
	String subTitle() default "";

	/** @return the tooltip */
	String tooltip() default "";

	/** @return the series attribute path */
	String seriesPath() default "";

	/** @return the name attribute path */
	String namePath() default "";

	/** @return the data attribute path */
	String dataPath() default "";
}
