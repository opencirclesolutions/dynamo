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
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.ui.converter.BigDecimalConverter;
import com.ocs.dynamo.ui.converter.ConverterFactory;
import com.ocs.dynamo.ui.converter.IntToDoubleConverter;
import com.ocs.dynamo.ui.converter.LocalDateWeekCodeConverter;
import com.ocs.dynamo.ui.converter.LongToDoubleConverter;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.converter.StringToLongConverter;

/**
 * Utility for converting between data types
 * 
 * @author bas.rutten
 *
 */
public final class ConvertUtils {

    private ConvertUtils() {
        // hidden constructor
    }

    /**
     * Converts a value to its presentation value
     * 
     * @param am    the attribute model
     * @param input the input value
     * @return
     */
    public static Object convertToPresentationValue(AttributeModel am, Object input) {
        if (input == null) {
            return null;
        }

        Locale locale = VaadinUtils.getLocale();
        boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();
        boolean percentage = am.isPercentage();

        if (am.isWeek()) {
            LocalDateWeekCodeConverter converter = new LocalDateWeekCodeConverter();
            return converter.convertToPresentation((LocalDate) input, new ValueContext(locale));
        } else if (NumberUtils.isInteger(am.getType())) {
            return VaadinUtils.integerToString(grouping, percentage, (Integer) input);
        } else if (NumberUtils.isLong(am.getType())) {
            return VaadinUtils.longToString(grouping, percentage, (Long) input);
        } else if (NumberUtils.isDouble(am.getType())) {
            return VaadinUtils.doubleToString(am.isCurrency(), am.isPercentage(), grouping, am.getPrecision(), (Double) input, locale);
        } else if (BigDecimal.class.equals(am.getType())) {
            return VaadinUtils.bigDecimalToString(am.isCurrency(), am.isPercentage(), grouping, am.getPrecision(), (BigDecimal) input,
                    locale);
        }
        return input;
    }

    /**
     * Converts the search value from the presentation to the model
     * 
     * @param am    the attribute model that governs the conversion
     * @param input the search value to convert
     * @return
     */
    public static Result<? extends Object> convertToModelValue(AttributeModel am, Object value) {
        if (value == null) {
            return Result.ok(null);
        }

        boolean grouping = SystemPropertyUtils.useThousandsGroupingInEditMode();
        Locale locale = VaadinUtils.getLocale();

        if (am.isWeek()) {
            LocalDateWeekCodeConverter converter = new LocalDateWeekCodeConverter();
            return converter.convertToModel((String) value, new ValueContext(locale));
        } else if (NumberUtils.isInteger(am.getType())) {
            if (value instanceof String) {
                StringToIntegerConverter converter = ConverterFactory.createIntegerConverter(grouping, false);
                return converter.convertToModel((String) value, new ValueContext(locale));
            } else if (value instanceof Double) {
                return new IntToDoubleConverter().convertToModel((Double) value, new ValueContext(locale));
            }
        } else if (NumberUtils.isLong(am.getType())) {
            if (value instanceof String) {
                StringToLongConverter converter = ConverterFactory.createLongConverter(grouping, false);
                return converter.convertToModel((String) value, new ValueContext(locale));
            } else if (value instanceof Double) {
                return new LongToDoubleConverter().convertToModel((Double) value, new ValueContext(locale));
            }
        } else if (NumberUtils.isDouble(am.getType())) {
            StringToDoubleConverter converter = ConverterFactory.createDoubleConverter(am.isCurrency(), am.isPercentage(), grouping,
                    am.getPrecision(), SystemPropertyUtils.getDefaultCurrencySymbol());
            return converter.convertToModel((String) value, new ValueContext(locale));

        } else if (BigDecimal.class.equals(am.getType())) {

            BigDecimalConverter converter = ConverterFactory.createBigDecimalConverter(am.isCurrency(), am.isPercentage(), grouping,
                    am.getPrecision(), SystemPropertyUtils.getDefaultCurrencySymbol());
            return converter.convertToModel((String) value, new ValueContext(locale));
        }
        return Result.ok(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> convertCollection(Object value, AttributeModel am) {
        if (value == null) {
            return null;
        } else if (Set.class.isAssignableFrom(am.getType())) {
            Collection<T> col = (Collection<T>) value;
            return Sets.newHashSet(col);
        } else if (List.class.isAssignableFrom(am.getType())) {
            Collection<T> col = (Collection<T>) value;
            return Lists.newArrayList(col);
        }
        return null;
    }
}
