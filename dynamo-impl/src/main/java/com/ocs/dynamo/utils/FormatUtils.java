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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.util.SystemPropertyUtils;

/**
 * 
 * Utilities for formatting property values
 * 
 * @author bas.rutten
 *
 */
public final class FormatUtils {

	/**
	 * Extracts a field value from an object and formats it
	 * 
	 * @param am             the attribute model
	 * @param obj            the object from which to extract the value
	 * @param local          the locale
	 * @param currencySymbol the currency symbol
	 * @return
	 */
	public static <T> String extractAndFormat(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel am, Object obj, Locale locale, ZoneId zoneId, String currencySymbol) {
		Object value = ClassUtils.getFieldValue(obj, am.getPath());
		return formatPropertyValue(entityModelFactory, messageService, am, value, ", ", locale, zoneId, currencySymbol);
	}

	/**
	 * Formats and entity
	 * 
	 * @param entityModel the entity model for the entity
	 * @param value       the entity
	 * @return
	 */
	public static String formatEntity(EntityModel<?> entityModel, Object value) {
		if (value instanceof AbstractEntity) {
			AbstractEntity<?> entity = (AbstractEntity<?>) value;
			if (entityModel.getDisplayProperty() != null) {
				return ClassUtils.getFieldValueAsString(entity, entityModel.getDisplayProperty());
			} else {
				return entity.toString();
			}
		}
		return null;
	}

	/**
	 * Formats a collection of entities (turns it into a comma-separated string
	 * based on the value of the "displayProperty")
	 *
	 * @param entityModelFactory the entity model factory
	 * @param collection         the collection of entities to format
	 * @return
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
		return result.stream().collect(Collectors.joining(separator));
	}

	/**
	 * Formats a property value
	 * 
	 * @param <T>
	 * @param entityModelFactory
	 * @param model
	 * @param value
	 * @param separator
	 * @param locale
	 * @return
	 */
	public static <T> String formatPropertyValue(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel model, Object value, String separator, Locale locale, ZoneId zoneId) {
		return formatPropertyValue(entityModelFactory, messageService, model, value, separator, locale, zoneId,
				SystemPropertyUtils.getDefaultCurrencySymbol());
	}

	/**
	 * Formats a property value
	 *
	 * @param entityModelFactory the entity model factory
	 * @param entityModel        the entity model
	 * @param model              the attribute model
	 * @param value              the property value
	 * @param locale             the locale to use
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> String formatPropertyValue(EntityModelFactory entityModelFactory, MessageService messageService,
			AttributeModel model, Object value, String separator, Locale locale, ZoneId zoneId, String currencySymbol) {
		if (model != null && value != null) {
			if (model.isWeek()) {
				if (value instanceof LocalDate) {
					return DateUtils.toWeekCode((LocalDate) value);
				}
			} else if (Boolean.class.equals(model.getType()) || boolean.class.equals(model.getType())) {
				// translate boolean to String representation
				if (!StringUtils.isEmpty(model.getTrueRepresentation(locale)) && Boolean.TRUE.equals(value)) {
					return model.getTrueRepresentation(locale);
				} else if (!StringUtils.isEmpty(model.getFalseRepresentation(locale)) && Boolean.FALSE.equals(value)) {
					return model.getFalseRepresentation(locale);
				}
				return Boolean.toString(Boolean.TRUE.equals(value));
			} else if (DateUtils.isJava8DateType(model.getType())) {
				// other Java 8 dates
				return DateUtils.formatJava8Date(model.getType(), value, model.getDisplayFormat(), zoneId);
			} else if (NumberUtils.isNumeric(model.getType())) {
				return NumberUtils.numberToString(model, value, model.isThousandsGrouping(), locale, currencySymbol);
			} else if (model.getType().isEnum()) {
				// in case of an enumeration, look it up in the message
				// bundle
				String msg = messageService.getEnumMessage((Class<Enum<?>>) model.getType(), (Enum<?>) value, locale);
				if (msg != null) {
					return msg;
				}
			} else if (value instanceof Iterable) {
				return formatEntityCollection(entityModelFactory, model, value, separator, locale, currencySymbol);
			} else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
				// entity -> translate using the "displayProperty"
				EntityModel<?> detailEntityModel = model.getNestedEntityModel();
				if (detailEntityModel == null) {
					detailEntityModel = entityModelFactory.getModel(model.getType());
				}
				String displayProperty = detailEntityModel.getDisplayProperty();
				if (displayProperty == null) {
					throw new OCSRuntimeException(
							"No displayProperty set for entity " + detailEntityModel.getEntityClass());
				}
				AttributeModel detailModel = detailEntityModel.getAttributeModel(displayProperty);
				return formatPropertyValue(entityModelFactory, messageService, detailModel,
						ClassUtils.getFieldValue(value, displayProperty), separator, locale, zoneId, currencySymbol);
			} else if (value instanceof AbstractEntity) {
				// single entity
				Object result = ClassUtils.getFieldValue(value, model.getPath());
				return result == null ? null : result.toString();
			} else {
				// just use the String value
				return value.toString();
			}
		}
		return null;
	}

	private FormatUtils() {
		// private constructor
	}
}
