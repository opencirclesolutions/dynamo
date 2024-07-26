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
package com.ocs.dynamo.service;

import com.ocs.dynamo.domain.model.AttributeModel;

import java.util.Locale;

/**
 * A service for convenient access to message bundles - catches any exceptions
 * related to missing messages and returns either NULL or a warning when no
 * message can be found
 * 
 * @author bas.rutten
 */
public interface MessageService {

	/**
	 * Retrieves a message that is used to override an attribute model setting
	 * 
	 * @param reference      the entity model
	 * @param attributeModel the attribute model
	 * @param propertyName   the name of the property
	 * @param locale         the locale
	 * @return the message
	 */
	String getAttributeMessage(String reference, AttributeModel attributeModel, String propertyName, Locale locale);

	/**
	 * Retrieves a message that is used to override an entity model setting
	 * 
	 * @param reference    the entity class
	 * @param propertyName the name of the property
	 * @return the message
	 */
	String getEntityMessage(String reference, String propertyName, Locale locale);

	/**
	 * Retrieves a message that is used as the human-readable definition of an enum
	 * 
	 * @param enumClass the class of the enum
	 * @param value     the enum value
	 * @return the message
	 */
	<E extends Enum<?>> String getEnumMessage(Class<E> enumClass, E value, Locale locale);

	/**
	 * Retrieves a simple parameterized message - returns a warning if no such
	 * message can be found
	 * 
	 * @param key    the key of the message
	 * @param locale the locale
	 * @param args   the arguments. These are referred to using {0}, {1} etc
	 * @return the message
	 */
	String getMessage(String key, Locale locale, Object... args);

	/**
	 * Retrieves a message - does NOT fall back to a default version if the message
	 * is not found.
	 * 
	 * @param key the key of the message
	 * @return the message specified by the key, or <code>null</code> if no such
	 *         message can be found
	 */
	String getMessageNoDefault(String key, Locale locale);

	/**
	 * Retrieves a message - does NOT fall back to a default version if the message
	 * is not found.
	 * 
	 * @param key    the key of the message
	 * @param locale the desired locale
	 * @param args   the message parameters
	 * @return the message
	 */
	String getMessageNoDefault(String key, Locale locale, Object... args);

}
