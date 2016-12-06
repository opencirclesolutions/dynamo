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
package com.ocs.dynamo.ui.composite.table;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer;
import com.ocs.dynamo.ui.container.hierarchical.ModelBasedHierarchicalContainer.ModelBasedHierarchicalDefinition;
import com.ocs.dynamo.ui.converter.WeekCodeConverter;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;

/**
 * Several table related functions to reuse on both Table and TreeTable subclasses.
 * 
 * @author Patrick Deenen (patrick.deenen@opencirclesolutions.nl)
 */
public final class TableUtils {

	private TableUtils() {
		// hidden constructor
	}

	/**
	 * Perform the default initialization for a table
	 * 
	 * @param table
	 *            the table to initialize
	 */
	public static void defaultInitialization(Table table) {
		table.setSizeFull();
		table.setImmediate(true);
		table.setEditable(false);
		table.setMultiSelect(false);
		table.setSelectable(true);
		table.setColumnReorderingAllowed(true);
		table.setColumnCollapsingAllowed(true);
		table.setSortEnabled(true);
	}

	/**
	 * Formats a collection of entities (turns it into a comma-separated string based on the value
	 * of the "displayProperty")
	 * 
	 * @param entityModelFactory
	 * @param collection
	 * @return
	 */
	public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel,
	        Object collection) {
		StringBuilder builder = new StringBuilder();
		Iterable<?> col = (Iterable<?>) collection;
		Iterator<?> it = col.iterator();
		while (it.hasNext()) {
			if (builder.length() > 0) {
				builder.append(", ");
			}

			Object next = it.next();
			if (next instanceof AbstractEntity) {
				EntityModel<?> entityModel = entityModelFactory.getModel(next.getClass());
				String displayProperty = entityModel.getDisplayProperty();
				if (displayProperty != null) {
					builder.append(ClassUtils.getFieldValueAsString(next, displayProperty));
				} else {
					builder.append(next.toString());
				}
			} else {
				// custom formatting for numbers
				if (next instanceof Number) {
					builder.append(VaadinUtils.numberToString(attributeModel, attributeModel.getNormalizedType(), next,
					        true, VaadinUtils.getLocale()));
				} else {
					builder.append(next);
				}
			}
		}
		return builder.toString();
	}

	/**
	 * Formats a collection of entities into a comma-separated string that displays the meaningful
	 * representations of the entities
	 * 
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param property
	 *            the property
	 * @return
	 */
	public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel,
	        Property<?> property) {
		return formatEntityCollection(entityModelFactory, attributeModel, property.getValue());
	}

	/**
	 * Formats a property value
	 * 
	 * @param entityModelFactory
	 * @param entityModel
	 * @param messageService
	 * @param colId
	 * @param value
	 * @return
	 */
	public static <T> String formatPropertyValue(EntityModelFactory entityModelFactory, EntityModel<T> entityModel,
	        MessageService messageService, Object colId, Object value) {
		return formatPropertyValue(null, entityModelFactory, entityModel, messageService, colId, value,
		        VaadinUtils.getLocale());
	}

	/**
	 * Formats a property value for display in a string
	 * 
	 * @param entityModelFactory
	 * @param entityModel
	 * @param messageService
	 * @param colId
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> String formatPropertyValue(Table table, EntityModelFactory entityModelFactory,
	        EntityModel<T> entityModel, MessageService messageService, Object colId, Object value, Locale locale) {
		if (value != null) {
			AttributeModel model = entityModel.getAttributeModel((String) colId);
			if (model != null) {
				if (model.isWeek()) {
					WeekCodeConverter converter = new WeekCodeConverter();
					return converter.convertToPresentation((Date) value, String.class, null);
				} else if (Boolean.class.equals(model.getType()) || boolean.class.equals(model.getType())) {
					if (!StringUtils.isEmpty(model.getTrueRepresentation()) && Boolean.TRUE.equals(value)) {
						return model.getTrueRepresentation();
					} else if (!StringUtils.isEmpty(model.getFalseRepresentation()) && Boolean.FALSE.equals(value)) {
						return model.getFalseRepresentation();
					}
					return null;
				} else if (Date.class.equals(model.getType())) {
					// in case of a date field, use the entered display format
					SimpleDateFormat format = new SimpleDateFormat(model.getDisplayFormat());

					// set time zone for a time stamp field
					if (AttributeDateType.TIMESTAMP.equals(model.getDateType())) {
						format.setTimeZone(VaadinUtils.getTimeZone(UI.getCurrent()));
					}
					return format.format((Date) value);
				} else if (BigDecimal.class.equals(model.getType())) {
					String cs = getCurrencySymbol(table);
					return VaadinUtils.bigDecimalToString(model.isCurrency(), model.isPercentage(),
					        model.isUseThousandsGrouping(), model.getPrecision(), (BigDecimal) value, locale, cs);
				} else if (Number.class.isAssignableFrom(model.getType())) {
					// generic functionality for all numbers
					return VaadinUtils.numberToString(model, model.getType(), value, model.isUseThousandsGrouping(),
					        locale);
				} else if (model.getType().isEnum()) {
					// in case of an enum, look it up in the message bundle
					String msg = messageService.getEnumMessage((Class<Enum<?>>) model.getType(), (Enum<?>) value);
					if (msg != null) {
						return msg;
					}
				} else if (value instanceof Iterable) {
					return formatEntityCollection(entityModelFactory, model, value);
				} else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
					// check for a nested model first. If it is not there, fall
					// back to the default
					EntityModel<?> detailEntityModel = model.getNestedEntityModel();
					if (detailEntityModel == null) {
						detailEntityModel = entityModelFactory.getModel(model.getType());
					}
					String displayProperty = detailEntityModel.getDisplayProperty();
					if (displayProperty == null) {
						throw new OCSRuntimeException("No displayProperty set for entity "
						        + detailEntityModel.getEntityClass());
					}

					return formatPropertyValue(table, entityModelFactory, detailEntityModel, messageService,
					        displayProperty, ClassUtils.getFieldValue(value, displayProperty), locale);
				} else if (value instanceof AbstractEntity) {
					Object result = ClassUtils.getFieldValue(value, colId.toString());
					return result != null ? result.toString() : null;
				} else {
					// Use string value
					return value.toString();
				}
			}
		}
		return null;
	}

	/**
	 * Formats a property value
	 * 
	 * @param table
	 *            the table
	 * @param entityModelFactory
	 *            the entity model factory
	 * @param entityModel
	 *            the entity model
	 * @param messageService
	 *            the message service
	 * @param rowId
	 *            the row id
	 * @param colId
	 *            the column ID
	 * @param property
	 *            the name of the property
	 * @return
	 */
	public static <T> String formatPropertyValue(Table table, EntityModelFactory entityModelFactory,
	        EntityModel<T> entityModel, MessageService messageService, Object rowId, Object colId, Property<?> property) {
		return formatPropertyValue(table, entityModelFactory, entityModel, messageService, rowId, colId, property,
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> String formatPropertyValue(Table table, EntityModelFactory entityModelFactory,
	        EntityModel<T> entityModel, MessageService messageService, Object rowId, Object colId,
	        Property<?> property, Locale locale) {
		if (table.getContainerDataSource() instanceof ModelBasedHierarchicalContainer) {
			ModelBasedHierarchicalContainer<?> c = (ModelBasedHierarchicalContainer<?>) table.getContainerDataSource();
			ModelBasedHierarchicalDefinition def = c.getHierarchicalDefinitionByItemId(rowId);
			return TableUtils.formatPropertyValue(table, entityModelFactory, def.getEntityModel(), messageService,
			        c.unmapProperty(def, colId), property.getValue(), locale);
		}
		return formatPropertyValue(table, entityModelFactory, entityModel, messageService, colId, property.getValue(),
		        locale);
	}

	/**
	 * Returns the currency symbol to be used in a certain table. This will return the custom
	 * currency symbol for a certain table, or the default currency symbol if no custmo currency
	 * symbol is set
	 * 
	 * @param table
	 *            the table
	 * @return
	 */
	private static String getCurrencySymbol(Table table) {
		String cs = null;
		if (table instanceof ModelBasedTable) {
			cs = ((ModelBasedTable<?, ?>) table).getCurrencySymbol();
		} else if (table instanceof ModelBasedTreeTable) {
			cs = ((ModelBasedTreeTable<?, ?>) table).getCurrencySymbol();
		}
		if (cs == null) {
			cs = VaadinUtils.getCurrencySymbol();
		}
		return cs;
	}
}
