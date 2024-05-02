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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import com.ocs.dynamo.domain.model.AttributeModel;

/**
 * Utility methods for dealing with numbers
 *
 * @author bas.rutten
 */
public final class NumberUtils {

    /**
     * Appends a percentage sign to the provided string if needed
     *
     * @param input         the string
     * @param percentage whether to append the sign
     * @return the input string with a percentage sign appended to it
     */
    private static String appendPercentage(String input, boolean percentage) {
        if (input == null) {
            return null;
        }
        return percentage ? input + "%" : input;
    }

    /**
     * * Converts a BigDecimal value to a String
     *
     * @param currency       whether the value represents a currency
     * @param percentage     whether the value represents a percentage
     * @param useGrouping    whether to use a thousand grouping
     * @param value          the value
     * @param currencySymbol the currency symbol
     * @param locale         the locale to use
     * @return the result of the conversion
     */
    public static String bigDecimalToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
                                            BigDecimal value, Locale locale, String currencySymbol) {
        return fractionalToString(currency, percentage, useGrouping, precision, value, locale, currencySymbol);
    }

    /**
     * Formats a value
     *
     * @param value the value to format
     * @return the result of the formatting
     */
    public static String format(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            value = String.format("%.2f", value);
        } else if (value instanceof Double) {
            value = String.format("%.2f", value);
        } else if (value instanceof Float) {
            value = String.format("%.2f", value);
        }

        return value.toString();
    }

    /**
     * Converts a double to a String
     *
     * @param currency       whether to include a currency symbol
     * @param percentage     whether to include a percentage sign
     * @param useGrouping    whether to use a thousand grouping separator
     * @param precision      the desired precision
     * @param value          the value to convert
     * @param currencySymbol the currency symbol to use
     * @param locale         the locale to use
     * @return the result of the conversion
     */
    public static String doubleToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
                                        Double value, Locale locale, String currencySymbol) {
        return fractionalToString(currency, percentage, useGrouping, precision, value, locale, currencySymbol);
    }

    /**
     * Converts a fractional value to a String
     *
     * @param currency       whether to include a currency symbol
     * @param percentage     whether to include a percentage sign
     * @param useGrouping    whether to use a thousand grouping separator
     * @param precision      the desired precision
     * @param value          the value to convert
     * @param currencySymbol the currency symbol to use
     * @param locale         the locale to use
     * @return the result of the conversion
     */
    public static String fractionalToString(boolean currency, boolean percentage, boolean useGrouping, int precision,
                                            Number value, Locale locale, String currencySymbol) {
        if (value == null) {
            return null;
        }

        DecimalFormat df;
        if (currency) {
            df = (DecimalFormat) DecimalFormat.getCurrencyInstance(locale);
            DecimalFormatSymbols s = df.getDecimalFormatSymbols();
            s.setCurrencySymbol(currencySymbol);
            df.setDecimalFormatSymbols(s);
        } else {
            df = (DecimalFormat) DecimalFormat.getInstance(locale);
        }
        df.setGroupingUsed(useGrouping);
        df.setMaximumFractionDigits(precision);
        df.setMinimumFractionDigits(precision);

        String s = df.format(value);
        return appendPercentage(s, percentage);
    }

    /**
     * Converts an Integer to a String, using the Vaadin converters
     *
     * @param grouping indicates whether grouping separators must be used
     * @param value    the value to convert
     * @param locale   the locale
     * @return the result of the conversion
     */
    public static String integerToString(boolean grouping, boolean percentage, Integer value, Locale locale) {
        if (value == null) {
            return null;
        }
        NumberFormat format = NumberFormat.getInstance(locale);
        format.setGroupingUsed(grouping);
        String s = format.format(value);
        return appendPercentage(s, percentage);
    }

    /**
     * Checks whether a class is a double (either wrapper or primitive)
     *
     * @param clazz the class to check
     * @return true if the provided class is a double or Double
     */
    public static boolean isDouble(Class<?> clazz) {
        return Double.class.equals(clazz) || double.class.equals(clazz);
    }

    /**
     * Checks if an object is a double (either wrapper or primitive)
     *
     * @param value the object to check
     * @return true if the provided object is a double or Double
     */
    public static boolean isDouble(Object value) {
        if (value == null) {
            return false;
        }
        return value.getClass().equals(Double.class);
    }

    /**
     * Checks if a class is represents a float (either wrapper or primitive)
     *
     * @param clazz the class to check
     * @return true if the provided class is a Float or float
     */
    public static boolean isFloat(Class<?> clazz) {
        return Float.class.equals(clazz) || float.class.equals(clazz);
    }

    /**
     * Checks if a class represents an integer (either wrapper or primitive)
     *
     * @param clazz the class to check
     * @return true  if the provided class is Integer or integer
     */
    public static boolean isInteger(Class<?> clazz) {
        return Integer.class.equals(clazz) || int.class.equals(clazz);
    }

    /**
     * Checks if an object is an integer (either wrapper or primitive)
     *
     * @param value the value to check
     * @return true if this is the case, false otherwise
     */
    public static boolean isInteger(Object value) {
        if (value == null) {
            return false;
        }
        return value.getClass().equals(Integer.class);
    }

    /**
     * Checks if a class is a long (either wrapper or primitive)
     *
     * @param clazz the class to check
     * @return true if this is the case, false otherwise
     */
    public static boolean isLong(Class<?> clazz) {
        return Long.class.equals(clazz) || long.class.equals(clazz);
    }

    /**
     * Checks if an object is a Long (either primitive or wrapper)
     *
     * @param value the value to check
     * @return true if this is the case, false otherwise
     */
    public static boolean isLong(Object value) {
        if (value == null) {
            return false;
        }
        return value.getClass().equals(Long.class);
    }

    /**
     * Indicates whether a certain class is a numeric class (either a primitive or a
     * wrapper that extends the Number class) that is supported by the framework
     *
     * @param clazz the class to check
     * @return true if this is the case, false otherwise
     */
    public static boolean isNumeric(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz) || float.class.equals(clazz) || double.class.equals(clazz)
                || int.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)
                || byte.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz);
    }

    /**
     * Converts a Long to a String, using the Vaadin converters
     *
     * @param grouping indicates whether grouping separators must be used
     * @param value    the value to convert
     * @param locale   the locale
     * @return the resulting String value
     */
    public static String longToString(boolean grouping, boolean percentage, Long value, Locale locale) {
        if (value == null) {
            return null;
        }

        NumberFormat format = NumberFormat.getInstance(locale);
        format.setGroupingUsed(grouping);
        String s = format.format(value);
        return appendPercentage(s, percentage);
    }

    /**
     * Converts a number to a String based on an attribute model and additional options
     *
     * @param am             the attribute model of the attribute to convert
     * @param value          the value to convert
     * @param grouping       the thousand grouping symbol
     * @param locale         the locale used in the formatting
     * @param currencySymbol the currency symbol
     * @return the resulting String value
     */
    public static <T> String numberToString(AttributeModel am, T value, boolean grouping, Locale locale,
                                            String currencySymbol) {
        if (NumberUtils.isInteger(am.getNormalizedType())) {
            return integerToString(grouping, am.isPercentage(), (Integer) value, locale);
        } else if (NumberUtils.isLong(am.getNormalizedType())) {
            return longToString(grouping, am.isPercentage(), (Long) value, locale);
        } else if (NumberUtils.isDouble(am.getNormalizedType()) || BigDecimal.class.equals(am.getNormalizedType())) {
            return fractionalToString(am.isCurrency(), am.isPercentage(), grouping, am.getPrecision(), (Number) value,
                    locale, currencySymbol);
        }
        return null;
    }

    private NumberUtils() {
        // default constructor
    }
}
