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

public final class StringUtil {

    private static final String EMAIL_PATTERN = "(.+)@(.+)";

    private StringUtil() {
        // private constructor
    }

    /**
     * Replaces all HTML breaks ("<br/>
     * ") by commas
     * 
     * @param value
     * @return
     */
    public static String replaceHtmlBreaks(String value) {
        if (value == null) {
            return null;
        }
        value = value.replaceAll("<br/>", ", ").trim();
        if (value.endsWith(",")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    /**
     * Restricts a string value to the maximum length of a certain field
     * 
     * @param value
     * @param clazz
     * @param fieldName
     * @return
     */
    public static String restrictToMaxFieldLength(String value, Class<?> clazz, String fieldName) {
        if (value == null) {
            return null;
        } else {
            int maxLength = ClassUtils.getMaxLength(clazz, fieldName);
            if (maxLength >= 0 && value.length() > maxLength) {
                value = value.substring(0, maxLength);
            }
        }
        return value;
    }

    /**
     * Checks if an value is a valid email address - this is actually a very simple check that only
     * checks for the @-sign
     * 
     * @param value
     *            the value to check
     */
    public static boolean isValidEmail(String value) {
        if (value == null) {
            return true;
        }

        return value.matches(EMAIL_PATTERN);
    }

}
