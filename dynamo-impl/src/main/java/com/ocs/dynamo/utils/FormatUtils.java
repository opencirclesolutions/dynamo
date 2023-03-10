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
package com.ocs.dynamo.utils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 
 * Utilities for formatting property values
 * 
 * @author bas.rutten
 *
 */
@UtilityClass
public final class FormatUtils {

	/**
	 * Extracts ands formats a value
	 * 
	 * @param entityModelFactory the entity model factory
	 * @param messageService     the message service
	 * @param am                 the attribute model
	 * @param obj                the object from which to extract the value
	 * @param locale             the locale used for the formatting
	 * @param zoneId             the zone ID
	 * @param currencySymbol     the currency symbol
	 * @return the result of the formatting
	 */
	public static String extractAndFormat(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel am, Object obj, Locale locale, ZoneId zoneId, String currencySymbol) {
		Object value = ClassUtils.getFieldValue(obj, am.getPath());
		return formatPropertyValue(entityModelFactory, messageService, am, value, ", ", locale, zoneId, currencySymbol);
	}

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
	 * @param entityModelFactory the entity model factory
	 * @param attributeModel     the attribute model
	 * @param collection         the collection of entities to translate
	 * @param separator          the string that is used to separate the string
	 *                           representations
	 * @param locale             the locale used for the formatting
	 * @param currencySymbol     the currency symbol used for the formatting
	 * @return the result of the formatting
	 */
	public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel,
			Object collection, String separator, Locale locale, String currencySymbol) {
		List<String> result = new ArrayList<>();
		Iterable<?> col = (Iterable<?>) collection;
		for (Object next : col) {
			if (next instanceof AbstractEntity) {
				EntityModel<?> entityModel = entityModelFactory.getModel(next.getClass());
				String displayProperty = entityModel.getDisplayProperty();
				if (displayProperty != null) {
					result.add(ClassUtils.getFieldValueAsString(next, displayProperty));
				} else {
					result.add(next.toString());
				}
			} else if (next instanceof Number) {
				result.add(NumberUtils.numberToString(attributeModel, next, true, locale, currencySymbol));
			} else {
				result.add(next.toString());
			}
		}
		return String.join(separator, result);
	}

	/**
	 * Formats a property value
	 * 
	 * @param entityModelFactory the entity model factory
	 * @param messageService     the message service
	 * @param attributeModel     the attribute model for the property that must be
	 *                           formatted
	 * @param value              the value of the property
	 * @param separator          the string that is used to separate the string
	 *                           representations
	 * @param locale             the locale used for the formatting
	 * @param zoneId             the zone ID of the time zone used for time stamp
	 *                           formatting
	 * @return the result of the formatting
	 */
	public static String formatPropertyValue(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel attributeModel, Object value, String separator, Locale locale, ZoneId zoneId) {
		return formatPropertyValue(entityModelFactory, messageService, attributeModel, value, separator, locale, zoneId,
				SystemPropertyUtils.getDefaultCurrencySymbol());
	}

	/**
	 * Formats a property value
	 * 
	 * @param entityModelFactory the entity model factory
	 * @param messageService     the message service
	 * @param am                 the attribute model for the property that must be
	 *                           formatted
	 * @param value              the value of the property
	 * @param separator          the string that is used to separate the string
	 *                           representations
	 * @param locale             the locale used for the formatting
	 * @param zoneId             the zone ID of the time zone used for time stamp
	 *                           formatting
	 * @param currencySymbol     the currency symbol used for the formatting
	 * @return the formatted value
	 */
	@SuppressWarnings("unchecked")
	public static String formatPropertyValue(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel am, Object value, String separator, Locale locale, ZoneId zoneId, String currencySymbol) {
		if (am != null && value != null) {
			if (am.isWeek()) {
				if (value instanceof LocalDate) {
					return DateUtils.toWeekCode((LocalDate) value);
				}
			} else if (Boolean.class.equals(am.getType()) || boolean.class.equals(am.getType())) {
				// translate boolean to String representation
				return formatBooleanProperty(am, value, locale);
			} else if (DateUtils.isJava8DateType(am.getType())) {
				// other Java 8 dates
				return DateUtils.formatJava8Date(am.getType(), value, am.getDisplayFormat(), zoneId);
			} else if (NumberUtils.isNumeric(am.getType())) {
				return NumberUtils.numberToString(am, value, am.useThousandsGroupingInViewMode(), locale,
						currencySymbol);
			} else if (am.getType().isEnum()) {
				// in case of an enumeration, look it up in the message
				// bundle
				return messageService.getEnumMessage((Class<Enum<?>>) am.getType(), (Enum<?>) value, locale);
			} else if (value instanceof Iterable) {
				return formatEntityCollection(entityModelFactory, am, value, separator, locale, currencySymbol);
			} else if (AbstractEntity.class.isAssignableFrom(am.getType())) {
				// entity -> translate using the "displayProperty"
				return formatEntityWithCheck(entityModelFactory, messageService, am, value, separator, locale, zoneId,
						currencySymbol);
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
	 * 
	 * Formats an entity (with a check to see if a display property has bene set)
	 * 
	 * @param entityModelFactory the entity model factory
	 * @param messageService     the message service
	 * @param attributeModel     the attribute model for the property that must be
	 *                           formatted
	 * @param value              the value of the property
	 * @param separator          the string that is used to separate the string
	 *                           representations
	 * @param locale             the locale used for the formatting
	 * @param zoneId             the zone ID of the time zone used for time stamp
	 *                           formatting
	 * @param currencySymbol     the currency symbol used for the formatting
	 * @return the result of the formatting
	 */
	private static String formatEntityWithCheck(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel attributeModel, Object value, String separator, Locale locale, ZoneId zoneId,
			String currencySymbol) {
		EntityModel<?> detailEntityModel = attributeModel.getNestedEntityModel();
		if (detailEntityModel == null) {
			detailEntityModel = entityModelFactory.getModel(attributeModel.getType());
		}
		String displayProperty = detailEntityModel.getDisplayProperty();
		if (displayProperty == null) {
			throw new OCSRuntimeException("No displayProperty set for entity " + detailEntityModel.getEntityClass());
		}
		AttributeModel detailModel = detailEntityModel.getAttributeModel(displayProperty);
		return formatPropertyValue(entityModelFactory, messageService, detailModel,
				ClassUtils.getFieldValue(value, displayProperty), separator, locale, zoneId, currencySymbol);
	}

}
