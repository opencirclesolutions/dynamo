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

/**
 * Utility methods for dealing with numbers
 * 
 * @author bas.rutten
 *
 */
public final class NumberUtils {

    private NumberUtils() {
        // default constructor
    }

    /**
     * Formats a value
     * 
     * @param value
     *            the value to format
     * @return
     */
    public static String format(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            value = String.format("%.2f", (BigDecimal) value);
        } else if (value instanceof Double) {
            value = String.format("%.2f", (Double) value);
        } else if (value instanceof Float) {
            value = String.format("%.2f", (Float) value);
        }

        return value.toString();
    }

    /**
     * Checks if a class is a float (either wrapper or primitive)
     * 
     * @param clazz
     *            the class to check
     * @return
     */
    public static boolean isFloat(Class<?> clazz) {
        return Float.class.equals(clazz) || float.class.equals(clazz);
    }

    /**
     * Checks if a class is an integer (either wrapper or primitive)
     * 
     * @param clazz
     *            the class to check
     * @return
     */
    public static boolean isInteger(Class<?> clazz) {
        return Integer.class.equals(clazz) || int.class.equals(clazz);
    }

    public static boolean isInteger(Object value) {
        if (value == null) {
            return false;
        }
        return value.getClass().equals(Integer.class) || value.getClass().equals(int.class);
    }

    /**
     * Checks if a class is a long (either wrapper or primitive)
     * 
     * @param clazz
     *            the class to check
     * @return
     */
    public static boolean isLong(Class<?> clazz) {
        return Long.class.equals(clazz) || long.class.equals(clazz);
    }

    public static boolean isLong(Object value) {
        if (value == null) {
            return false;
        }
        return value.getClass().equals(Long.class) || value.getClass().equals(long.class);
    }

    /**
     * Indicates whether a certain class is a numeric class (either a primitive or a wrapper that
     * extends the Number class) that is supported by the framework
     * 
     * @param clazz
     * @return
     */
    public static boolean isNumeric(Class<?> clazz) {
        return Number.class.isAssignableFrom(clazz) || float.class.equals(clazz) || double.class.equals(clazz)
                || int.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)
                || byte.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz);
    }

}
