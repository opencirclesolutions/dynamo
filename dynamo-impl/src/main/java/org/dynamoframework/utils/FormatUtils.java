package org.dynamoframework.utils;

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

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.service.MessageService;
import org.dynamoframework.service.ServiceLocator;
import org.dynamoframework.service.ServiceLocatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.dynamoframework.utils.NumberUtils.numberToString;

/**
 * Utilities for formatting property values
 *
 * @author bas.rutten
 */
@UtilityClass
public final class FormatUtils {

	private static ServiceLocator locator = ServiceLocatorFactory.getServiceLocator();

	/**
	 * Formats an entity (translates it to a String based on the displayProperty)
	 *
	 * @param entityModel the entity model
	 * @param value       the value (the entity)
	 * @return the result of the formatting
	 */
	public static String formatEntity(EntityModel<?> entityModel, Object value) {
		if (value instanceof AbstractEntity<?> entity) {
			if (entityModel.getDisplayProperty() != null) {
				return ClassUtils.getFieldValueAsString(entity, entityModel.getDisplayProperty());
			} else {
				return entity.toString();
			}
		}
		return null;
	}

	/**
	 * Formats a collection of entities (translates them to string based on the
	 * value of the displayProperty)
	 *
	 * @param attributeModel the attribute model
	 * @param collection     the collection of entities to translate
	 * @param separator      the string that is used to separate the string
	 *                       representations
	 * @param locale         the locale used for the formatting
	 * @return the result of the formatting
	 */
	public static String formatEntityCollection(AttributeModel attributeModel,
												Object collection, String separator, Locale locale) {
		EntityModelFactory factory = locator.getEntityModelFactory();
		List<String> result = new ArrayList<>();
		Iterable<?> col = (Iterable<?>) collection;
		for (Object next : col) {
			if (next instanceof AbstractEntity) {
				EntityModel<?> entityModel = factory.getModel(next.getClass());
				String displayProperty = entityModel.getDisplayProperty();
				if (displayProperty != null) {
					result.add(ClassUtils.getFieldValueAsString(next, displayProperty));
				} else {
					result.add(next.toString());
				}
			} else if (next instanceof Number) {
				result.add(numberToString(attributeModel, next, true, locale));
			} else {
				result.add(next.toString());
			}
		}
		return String.join(separator, result);
	}

	/**
	 * Formats a property value
	 *
	 * @param am        the attribute model for the property that must be
	 *                  formatted
	 * @param value     the value of the property
	 * @param separator the string that is used to separate the string
	 *                  representations
	 * @param locale    the locale used for the formatting
	 * @return the formatted value
	 */
	@SuppressWarnings("unchecked")
	public static String formatPropertyValue(AttributeModel am, Object value, String separator, Locale locale) {
		if (am != null && value != null) {
			if (Boolean.class.equals(am.getType()) || boolean.class.equals(am.getType())) {
				// translate boolean to String representation
				return formatBooleanProperty(am, value, locale);
			} else if (DateUtils.isJava8DateType(am.getType())) {
				return DateUtils.formatJava8Date(am.getType(), value, am.getDisplayFormat(locale));
			} else if (NumberUtils.isNumeric(am.getType())) {
				return numberToString(am, value, true, locale);
			} else if (am.getType().isEnum()) {
				// in case of an enumeration, look it up in the message
				// bundle
				MessageService messageService = locator.getMessageService();
				return messageService.getEnumMessage((Class<Enum<?>>) am.getType(), (Enum<?>) value, locale);
			} else if (value instanceof Iterable) {
				return formatEntityCollection(am, value, separator, locale);
			} else if (AbstractEntity.class.isAssignableFrom(am.getType())) {
				// entity -> translate using the "displayProperty"
				return formatEntityWithCheck(am, value, separator, locale);
			} else if (value instanceof AbstractEntity) {
				// single entity
				Object result = ClassUtils.getFieldValue(value, am.getPath());
				return result == null ? null : result.toString();
			} else {
				// as the ultimate fallback, just call toString()
				return value.toString();
			}
		}
		return null;
	}

	/**
	 * Format a boolean property
	 *
	 * @param am     the attribute model for the property
	 * @param value  the value
	 * @param locale the locale
	 * @return the result of the formatting
	 */
	private static String formatBooleanProperty(AttributeModel am, Object value, Locale locale) {
		if (!StringUtils.isEmpty(am.getTrueRepresentation(locale)) && Boolean.TRUE.equals(value)) {
			return am.getTrueRepresentation(locale);
		} else if (!StringUtils.isEmpty(am.getFalseRepresentation(locale)) && Boolean.FALSE.equals(value)) {
			return am.getFalseRepresentation(locale);
		}
		return Boolean.toString(Boolean.TRUE.equals(value));
	}

	/**
	 * Formats an entity (with a check to see if a display property has been set)
	 *
	 * @param attributeModel the attribute model for the property that must be
	 *                       formatted
	 * @param value          the value of the property
	 * @param separator      the string that is used to separate the string
	 *                       representations
	 * @param locale         the locale used for the formatting
	 * @return the result of the formatting
	 */
	private static String formatEntityWithCheck(AttributeModel attributeModel, Object value, String separator, Locale locale) {
		EntityModel<?> detailEntityModel = attributeModel.getNestedEntityModel();
		if (detailEntityModel == null) {
			EntityModelFactory factory = locator.getEntityModelFactory();
			detailEntityModel = factory.getModel(attributeModel.getType());
		}
		String displayProperty = detailEntityModel.getDisplayProperty();
		if (displayProperty == null) {
			throw new OCSRuntimeException("No displayProperty set for entity " + detailEntityModel.getEntityClass());
		}
		AttributeModel detailModel = detailEntityModel.getAttributeModel(displayProperty);
		return formatPropertyValue(detailModel,
			ClassUtils.getFieldValue(value, displayProperty), separator, locale);
	}

}
