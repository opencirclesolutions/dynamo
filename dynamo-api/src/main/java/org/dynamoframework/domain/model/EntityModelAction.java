package org.dynamoframework.domain.model;

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

import java.util.List;
import java.util.Locale;

/**
 * Defines an action that can be defined in the service layer
 * and that will be automatically added to the UI
 */
public interface EntityModelAction {

	/**
	 * @return the ID of the action
	 */
	String getId();

	/**
	 * @return the name of the service method to invoke
	 */
	String getMethodName();

	/**
	 * Returns the display name for a certain locale
	 *
	 * @param locale the locale
	 * @return the display name in the specified locale
	 */
	String getDisplayName(Locale locale);

	/**
	 * @return the class of the entity that this model is based on
	 */
	Class<?> getEntityClass();

	/**
	 * @return the entity model corresponding to the action
	 */
	EntityModel<?> getEntityModel();

	/**
	 * @return the type (update or create) of the action
	 */
	EntityModelActionType getType();

	/**
	 * @return The icon to include on the button
	 */
	String getIcon();

	/**
	 * @return the roles that are allowed to carry out the action
	 */
	List<String> getRoles();
}
