package com.ocs.dynamo.service;

import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;

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
	 * @param model
	 *            the entity model
	 * @param attributeModel
	 *            the attribute model
	 * @param propertyName
	 *            the name of the property
	 * @return
	 */
	public String getAttributeMessage(String reference, AttributeModel attributeModel,
	        String propertyName);

	/**
	 * Retrieves a message that is used to override an entity model setting
	 * 
	 * @param clazz
	 *            the entity class
	 * @param propertyName
	 *            the name of the property
	 * @return
	 */
	public String getEntityMessage(String reference, String propertyName);

	/**
	 * Retrieves a message that is used as the human-readable definition of an
	 * enum
	 * 
	 * @param enumClass
	 *            the class of the enum
	 * @param value
	 *            the enum value
	 * @return
	 */
	public <E extends Enum<?>> String getEnumMessage(Class<E> enumClass, E value);

	/**
	 * Retrieves a simple message - returns a warning if no such message can be
	 * found
	 * 
	 * @param key
	 *            the key of the message
	 * @return
	 */
	String getMessage(String key);

	/**
	 * Retrieves a simple parameterized message - returns a warning if no such
	 * message can be found
	 * 
	 * @param key
	 *            the key of the message
	 * @param args
	 *            the arguments. These are referred to using {0}, {1} etc
	 * @return
	 */
	String getMessage(String key, Object... args);

	/**
	 * Retrieves a simple parameterized message - returns a warning if no such
	 * message can be found
	 * 
	 * @param key
	 *            the key of the message
	 * @param locale
	 *            the locale of the message
	 * @param args
	 *            the arguments. These are referred to using {0}, {1} etc
	 * @return
	 */
	String getMessage(String key, Locale locale, Object... args);

	/**
	 * Retrieves a message - does NOT fall back to a default version if the
	 * message is not found.
	 * 
	 * @param key
	 *            the key of the message
	 * @return the message specified by the key, or <code>null</code> if no such
	 *         message can be found
	 */
	String getMessageNoDefault(String key);

	/**
	 * Retrieves a message - does NOT fall back to a default version if the
	 * message is not found.
	 * 
	 * @param key
	 *            the key of the message
	 * @param args
	 * @return the message specified by the key, or <code>null</code> if no such
	 *         message can be found
	 */
	String getMessageNoDefault(String key, Object... args);

	/**
	 * Retrieves a message - does NOT fall back to a default version if the
	 * message is not found.
	 * 
	 * @param key
	 *            the key of the message
	 * @param locale
	 *            the desired locale
	 * @param args
	 *            the message parameters
	 * @return
	 */
	String getMessageNoDefault(String key, Locale locale, Object... args);

}
