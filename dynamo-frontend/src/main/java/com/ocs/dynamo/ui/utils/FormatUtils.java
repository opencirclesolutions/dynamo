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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import com.ocs.dynamo.ui.composite.grid.GridUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.component.grid.Grid;

/**
 * 
 * Utilities for formatting property values
 * 
 * @author bas.rutten
 *
 */
public final class FormatUtils {

    private static MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

    private static EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

    /**
     * Extracts a field value from an object and formats it
     * 
     * @param grid the grid in which the value is displayed
     * @param am   the attribute model
     * @param obj  the object from which to extract the value
     * @return
     */
    public static <T> String extractAndFormat(Grid<T> grid, AttributeModel am, Object obj) {
        Object value = ClassUtils.getFieldValue(obj, am.getPath());
        return formatPropertyValue(grid, entityModelFactory, am, value, VaadinUtils.getLocale(), ", ");
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
    public static String formatEntityCollection(EntityModelFactory entityModelFactory, AttributeModel attributeModel, Object collection,
            String separator) {
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
                result.add(
                        VaadinUtils.numberToString(attributeModel, next, true, VaadinUtils.getLocale(), VaadinUtils.getCurrencySymbol()));
            } else {
                result.add(next.toString());
            }
        }
        return result.stream().collect(Collectors.joining(separator));
    }

    /**
     * Formats a property value
     * 
     * @param entityModelFactory the entity model factory
     * @param model              the attribute model for the property
     * @param value              the value of the property
     * @return
     */
    public static String formatPropertyValue(EntityModelFactory entityModelFactory, AttributeModel model, Object value, String separator) {
        return formatPropertyValue(null, entityModelFactory, model, value, VaadinUtils.getLocale(), separator);
    }

    /**
     * Formats a property value
     *
     * @param grid               the grid that the value is displayed in
     * @param entityModelFactory the entity model factory
     * @param entityModel        the entity model
     * @param model              the attribute model
     * @param value              the property value
     * @param locale             the locale to use
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> String formatPropertyValue(Grid<T> grid, EntityModelFactory entityModelFactory, AttributeModel model, Object value,
            Locale locale, String separator) {
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
            } else if (LocalDate.class.equals(model.getType())) {
                // in case of a date field, use the entered display format
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(model.getDisplayFormat());

                // set time zone for a time stamp field
                if (AttributeDateType.TIMESTAMP.equals(model.getDateType())) {
                    //dateTimeFormatter = dateTimeFormatter.withZone(VaadinUtils.getTimeZone(UI.getCurrent()));
                }
                return dateTimeFormatter.format((LocalDate) value);
            } else if (DateUtils.isJava8DateType(model.getType())) {
                return DateUtils.formatJava8Date(model.getType(), value, model.getDisplayFormat());
            }
            if (NumberUtils.isNumeric(model.getType())) {
                String cs = GridUtils.getCurrencySymbol(grid);
                // generic functionality for all other numbers
                return VaadinUtils.numberToString(model, value, model.isThousandsGrouping(), locale, cs);
            } else if (model.getType().isEnum()) {
                // in case of an enumeration, look it up in the message
                // bundle
                String msg = messageService.getEnumMessage((Class<Enum<?>>) model.getType(), (Enum<?>) value, VaadinUtils.getLocale());
                if (msg != null) {
                    return msg;
                }
            } else if (value instanceof Iterable) {
                String result = formatEntityCollection(entityModelFactory, model, value, separator);
                return grid == null ? result : restrictToMaxLength(result, model);
            } else if (AbstractEntity.class.isAssignableFrom(model.getType())) {
                // entity -> translate using the "displayProperty"
                EntityModel<?> detailEntityModel = model.getNestedEntityModel();
                if (detailEntityModel == null) {
                    detailEntityModel = entityModelFactory.getModel(model.getType());
                }
                String displayProperty = detailEntityModel.getDisplayProperty();
                if (displayProperty == null) {
                    throw new OCSRuntimeException("No displayProperty set for entity " + detailEntityModel.getEntityClass());
                }
                AttributeModel detailModel = detailEntityModel.getAttributeModel(displayProperty);
                return formatPropertyValue(grid, entityModelFactory, detailModel, ClassUtils.getFieldValue(value, displayProperty), locale,
                        separator);
            } else if (value instanceof AbstractEntity) {
                // single entity
                Object result = ClassUtils.getFieldValue(value, model.getPath());
                if (result == null) {
                    return null;
                }
                return grid == null ? result.toString() : restrictToMaxLength(result.toString(), model);
            } else {
                // just use the String value
                return grid == null ? value.toString() : restrictToMaxLength(value.toString(), model);
            }
        }
        return null;
    }

    /**
     * Restricts a value to its maximum length defined in the attribute model
     *
     * @param input the input value
     * @param am    the attribute model
     * @return
     */
    private static String restrictToMaxLength(String input, AttributeModel am) {
        if (am.getMaxLengthInGrid() != null && input != null && input.length() > am.getMaxLengthInGrid()) {
            return input.substring(0, am.getMaxLengthInGrid()) + "...";
        }
        return input;
    }

    private FormatUtils() {
        // private constructor
    }
}
