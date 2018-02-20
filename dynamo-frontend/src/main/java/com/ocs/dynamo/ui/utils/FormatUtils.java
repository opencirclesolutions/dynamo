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
package com.ocs.dynamo.ui.utils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.table.TableUtils;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * Utilities for formatting property values
 * 
 * @author bas.rutten
 *
 */
public final class FormatUtils {

	private static MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	private FormatUtils() {
		// private constructor
	}

	/**
	 * Formats an entity
	 * 
	 * @param entityModel
	 *            the entity model
	 * @param value
	 *            the value (must be an instance of AbstractEntity)
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
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param collection
	 *            the collection of entities to format
	 * @return
	 */
	public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel,
			Object collection, String separator) {
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
				result.add(VaadinUtils.numberToString(attributeModel, attributeModel.getNormalizedType(), next, true,
						VaadinUtils.getLocale()));
			} else {
				result.add(next.toString());
			}
		}
		return result.stream().collect(Collectors.joining(separator + " "));
	}

	/**
	 * Formats a collection of entities into a comma-separated string that displays
	 * the meaningful representations of the entities
	 * 
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param attributeModel
	 *            the attribute model
	 * @param property
	 *            the property
	 * @return
	 */
	public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel,
			Property<?> property) {
		return formatEntityCollection(entityModelFactory, attributeModel, property.getValue(), ", ");
	}

	/**
	 * Formats a property value
	 * 
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param model
	 *            the attribute model for the property
	 * @param value
	 *            the value of the property
	 * @return
	 */
	public static String formatPropertyValue(EntityModelFactory entityModelFactory, AttributeModel model,
			Object value) {
		return formatPropertyValue(null, entityModelFactory, model, value, VaadinUtils.getLocale());
	}

	/**
	 * Formats a property value
	 * 
	 * @param table
	 *            the table in which the property occurs
	 * @param entityModelFactory
	 *            the entity model factor
	 * @param entityModel
	 *            the entity model
	 * @param rowId
	 *            the row ID of the property
	 * @param colId
	 *            the column ID/property
	 * @param property
	 *            the property
	 * @return
	 */
	public static <T> String formatPropertyValue(Table table, EntityModelFactory entityModelFactory,
			EntityModel<T> entityModel, Object rowId, Object colId, Property<?> property) {
		return formatPropertyValue(table, entityModelFactory, entityModel, rowId, colId, property,
				VaadinUtils.getLocale());
	}

	/**
	 * Formats a property value - for use with a hierarchical table
	 * 
	 * @param table
	 * @param entityModelFactory
	 * @param entityModel
	 * @param messageService
	 * @param rowId
	 * @param colId
	 * @param property
	 * @param locale
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> String formatPropertyValue(Table table, EntityModelFactory entityModelFactory,
			EntityModel<T> entityModel, Object rowId, Object colId, Property<?> property, Locale locale) {
		if (table.getContainerDataSource() instanceof ModelBasedHierarchicalContainer) {
			ModelBasedHierarchicalContainer<?> c = (ModelBasedHierarchicalContainer<?>) table.getContainerDataSource();
			ModelBasedHierarchicalDefinition def = c.getHierarchicalDefinitionByItemId(rowId);
			Object path = c.unmapProperty(def, colId);
			return formatPropertyValue(table, entityModelFactory,
					path == null ? null : def.getEntityModel().getAttributeModel(path.toString()), property.getValue(),
					locale);
		}
		return formatPropertyValue(table, entityModelFactory, entityModel.getAttributeModel(colId.toString()),
				property.getValue(), locale);
	}

	/**
	 * Formats a property value
	 * 
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param entityModel
	 *            the entity model
	 * @param model
	 *            the attribute model
	 * @param value
	 *            the property value
	 * @param locale
	 *            the locale to use
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String formatPropertyValue(Table table, EntityModelFactory entityModelFactory, AttributeModel model,
			Object value, Locale locale) {
		if (model != null && value != null) {
			if (model.isWeek()) {
				if (value instanceof Date) {
					return DateUtils.toWeekCode((Date) value);
				} else if (value instanceof LocalDate) {
					return DateUtils.toWeekCode((LocalDate) value);
				}
			} else if (Boolean.class.equals(model.getType()) || boolean.class.equals(model.getType())) {
				if (!StringUtils.isEmpty(model.getTrueRepresentation()) && Boolean.TRUE.equals(value)) {
					return model.getTrueRepresentation();
				} else if (!StringUtils.isEmpty(model.getFalseRepresentation()) && Boolean.FALSE.equals(value)) {
					return model.getFalseRepresentation();
				}
				return Boolean.toString(Boolean.TRUE.equals(value));
			} else if (Date.class.equals(model.getType())) {
				// in case of a date field, use the entered display format
				SimpleDateFormat format = new SimpleDateFormat(model.getDisplayFormat());
				// set time zone for a time stamp field
				if (AttributeDateType.TIMESTAMP.equals(model.getDateType())) {
					format.setTimeZone(VaadinUtils.getTimeZone(UI.getCurrent()));
				}
				return format.format((Date) value);
			} else if (DateUtils.isJava8DateType(model.getType())) {
				return DateUtils.formatJava8Date(model.getType(), value, model.getDisplayFormat());
			} else if (BigDecimal.class.equals(model.getType())) {
				String cs = TableUtils.getCurrencySymbol(table);
				return VaadinUtils.bigDecimalToString(model.isCurrency(), model.isPercentage(),
						model.isUseThousandsGrouping(), model.getPrecision(), (BigDecimal) value, locale, cs);
			} else if (NumberUtils.isNumeric(model.getType())) {
				// generic functionality for all other numbers
				return VaadinUtils.numberToString(model, model.getType(), value, model.isUseThousandsGrouping(),
						locale);
			} else if (model.getType().isEnum()) {
				// in case of an enumeration, look it up in the message
				// bundle
				String msg = messageService.getEnumMessage((Class<Enum<?>>) model.getType(), (Enum<?>) value,
						VaadinUtils.getLocale());
				if (msg != null) {
					return msg;
				}
			} else if (value instanceof Iterable) {
				String result = formatEntityCollection(entityModelFactory, model, value, ",");
				return table == null ? result : restrictToMaxLength(result, model);
			} else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
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
				return formatPropertyValue(table, entityModelFactory, detailModel,
						ClassUtils.getFieldValue(value, displayProperty), locale);
			} else if (value instanceof AbstractEntity) {
				// single entity
				Object result = ClassUtils.getFieldValue(value, model.getPath());
				if (result == null) {
					return null;
				}
				return table == null ? result.toString() : restrictToMaxLength(result.toString(), model);
			} else {
				// just use the String value
				return table == null ? value.toString() : restrictToMaxLength(value.toString(), model);
			}
		}
		return null;
	}

	/**
	 * Restricts a value to its maximum length defined in the attribute model
	 * 
	 * @param input
	 *            the input value
	 * @param am
	 *            the attribute model
	 * @return
	 */
	private static String restrictToMaxLength(String input, AttributeModel am) {
		if (am.getMaxLengthInTable() != null && input != null && input.length() > am.getMaxLengthInTable()) {
			return input.substring(0, am.getMaxLengthInTable()) + "...";
		}
		return input;
	}

}
