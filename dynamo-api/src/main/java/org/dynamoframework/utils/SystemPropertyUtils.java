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
package org.dynamoframework.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.constants.DynamoConstants;
import org.dynamoframework.domain.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility methods for retrieving system property values
 *
 * @author bas.rutten
 */
@Slf4j
@UtilityClass
public final class SystemPropertyUtils {

    private static final int DEFAULT_DECIMAL_PRECISION = 2;

    private static final String DEFAULT_FALSE_REPRESENTATION = "false";

    private static final String DEFAULT_TRUE_REPRESENTATION = "true";

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            InputStream resourceAsStream = SystemPropertyUtils.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            if (resourceAsStream != null) {
                PROPERTIES.load(resourceAsStream);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Looks up the value of a boolean property by scanning the system properties
     * first and falling back to application.properties
     *
     * @param propertyName the name of the property
     * @param defaultValue the default value
     * @return the value of the property
     */
    private static Boolean getBooleanProperty(String propertyName, Boolean defaultValue) {
        String sys = System.getProperty(propertyName);
        if (sys == null) {
            sys = PROPERTIES.getProperty(propertyName, defaultValue == null ? null : defaultValue.toString());
        }
        return Boolean.valueOf(sys);
    }

    /**
     * @return the CSV escape character
     */
    public static String getCsvEscapeChar() {
        return getStringProperty(DynamoConstants.SP_EXPORT_CSV_ESCAPE, "\"\"");
    }

    /**
     * @return the CSV quote character
     */
    public static String getCsvQuoteChar() {
        return getStringProperty(DynamoConstants.SP_EXPORT_CSV_QUOTE, "\"");
    }

    /**
     * @return the CSV separator character
     */
    public static String getCsvSeparator() {
        return getStringProperty(DynamoConstants.SP_EXPORT_CSV_SEPARATOR, ";");
    }

    /**
     * @return the default format for formatting dates
     */
    public static String getDefaultDateFormat() {
        return getStringProperty(DynamoConstants.SP_DEFAULT_DATE_FORMAT, "dd-MM-yyyy");
    }

    /**
     * @return the default date/time format (dd-MM-yyyy HH:mm:ss)
     */
    public static String getDefaultDateTimeFormat() {
        return getStringProperty(DynamoConstants.SP_DEFAULT_DATETIME_FORMAT, "dd-MM-yyyy HH:mm:ss");
    }

    /**
     * @return the default decimal precision
     */
    public static int getDefaultDecimalPrecision() {
        return getIntProperty(DynamoConstants.SP_DEFAULT_DECIMAL_PRECISION, DEFAULT_DECIMAL_PRECISION);
    }

    /**
     * @return the default textual representation of the boolean literal "false"
     */
    public static String getDefaultFalseRepresentation() {
        return getStringProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION, DEFAULT_FALSE_REPRESENTATION);
    }

    /**
     * Looks up the default false representation for the provided locale. This will
     * look for a system property names ocs.default.false.representation.<lan> where
     * <lan> is the language code of the locale, e.g. "en", "nl"
     *
     * @param locale the locale
     * @return the default false representation
     */
    public static String getDefaultFalseRepresentation(Locale locale) {
        return getStringProperty(DynamoConstants.SP_DEFAULT_FALSE_REPRESENTATION + "." + locale.getLanguage(), null);
    }

    /**
     * @return the default "group together" mode. This determines how the fields
     * that are group together behave with respect to responsiveness
     */
    public static GroupTogetherMode getDefaultGroupTogetherMode() {
        String s = getStringProperty(DynamoConstants.SP_DEFAULT_GROUP_TOGETHER_MODE, "pixel");
        return GroupTogetherMode.valueOf(s.toUpperCase());
    }

    /**
     * @return the default threshold width for group together columns (when mode =
     * pixel)
     */
    public static Integer getDefaultGroupTogetherWidth() {
        return getIntProperty(DynamoConstants.SP_DEFAULT_GROUP_TOGETHER_WIDTH, 300);
    }

    /**
     * @return the default locale used for e.g. the decimal and thousands separators
     */
    public static Locale getDefaultLocale() {
        String localeString = getStringProperty(DynamoConstants.SP_DEFAULT_LOCALE, DynamoConstants.DEFAULT_LOCALE.toString());
        return constructLocale(localeString);
    }

    /**
     * @return the default enumeration field mode to use
     */
    public static AttributeEnumFieldMode getDefaultEnumFieldMode() {
        return AttributeEnumFieldMode.valueOf(
                getStringProperty(DynamoConstants.SP_DEFAULT_ENUM_FIELD_MODE,
                        AttributeEnumFieldMode.DROPDOWN.name()));
    }

    private static Locale constructLocale(String localeString) {
        int split = localeString.indexOf("_");
        if (split > -1) {
            return new Locale.Builder().setLanguage(localeString.substring(0, split)
            ).setRegion(localeString.substring(split + 1)).build();
        }
        return new Locale.Builder().setLanguage(localeString).build();
    }

    /**
     * @return the default nesting depth for entity models
     */
    public static int getDefaultNestingDepth() {
        return getIntProperty(DynamoConstants.SP_DEFAULT_NESTING_DEPTH, 2);
    }

    /**
     * @return the default number field mode
     */
    public static NumberFieldMode getDefaultNumberFieldMode() {
        String s = getStringProperty(DynamoConstants.SP_DEFAULT_NUMBER_FIELD_MODE,
                NumberFieldMode.TEXTFIELD.toString());
        return NumberFieldMode.valueOf(s.toUpperCase());
    }

    /**
     * @return whether searches on text fields will be case-sensitive by default
     */
    public static boolean getDefaultSearchCaseSensitive() {
        return getBooleanProperty(DynamoConstants.SP_DEFAULT_SEARCH_CASE_SENSITIVE, false);
    }

    /**
     * @return default search prefix only. False if not specified
     */
    public static boolean getDefaultSearchPrefixOnly() {
        return getBooleanProperty(DynamoConstants.SP_DEFAULT_SEARCH_PREFIX_ONLY, false);
    }

    /**
     * @return the default format for formatting attributes of type LocalTime or
     * Java 8 dates that only consist of a time stamp
     */
    public static String getDefaultTimeFormat() {
        return getStringProperty(DynamoConstants.SP_DEFAULT_TIME_FORMAT, "HH:mm:ss");
    }

    /**
     * @return the default true representation (if no further locale is specified)
     */
    public static String getDefaultTrueRepresentation() {
        return getStringProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION, DEFAULT_TRUE_REPRESENTATION);
    }

    /**
     * Looks up the default true representation for the provided locale. This will
     * look for a system property names ocs.default.true.representation.<lan> where
     * <lan> is the language code of the locale, e.g. "en", "nl"
     *
     * @param locale the locale
     * @return the true representation
     */
    public static String getDefaultTrueRepresentation(Locale locale) {
        return getStringProperty(DynamoConstants.SP_DEFAULT_TRUE_REPRESENTATION + "." + locale.getLanguage(), null);
    }

    /**
     * Looks up the value for an integer property
     *
     * @param propertyName the name of the property
     * @param defaultValue the default value
     * @return the property value
     */
    private static Integer getIntProperty(String propertyName, Integer defaultValue) {
        Integer sys = Integer.getInteger(propertyName);
        if (sys == null) {
            String s = PROPERTIES.getProperty(propertyName, defaultValue == null ? null : defaultValue.toString());
            sys = s == null ? null : Integer.parseInt(s);
        }
        return sys;
    }

    /**
     * @return the maximum number of rows that a result set is allowed to have
     * before resorting to a streaming approach when doing Excel exports
     */
    public static Integer getMaxExportRowsBeforeStreaming() {
        return getIntProperty(DynamoConstants.SP_MAX_ROWS_BEFORE_STREAMING, 1000);
    }

    /**
     * @return the name of the service locator to use. Used internally by the
     * framework, highly unlikely this needs to be modified directly
     */
    public static String getServiceLocatorClassName() {
        return getStringProperty(DynamoConstants.SP_SERVICE_LOCATOR_CLASS_NAME,
                "org.dynamoframework.SpringWebServiceLocator");
    }

    /**
     * Looks up the value of a String property by scanning the system properties
     * first and falling back to application.properties
     *
     * @param propertyName the name of the property
     * @param defaultValue the default value
     * @return the property
     */
    private static String getStringProperty(String propertyName, String defaultValue) {
        String sys = System.getProperty(propertyName);
        if (sys == null) {
            sys = PROPERTIES.getProperty(propertyName, defaultValue);
        }
        return sys;
    }

    /**
     * @return whether to capitalize every word in a property name
     */
    public static boolean isCapitalizeWords() {
        return getBooleanProperty(DynamoConstants.SP_CAPITALIZE_WORDS, true);
    }

    /**
     * @return whether to trim spaces in text and text area fields
     */
    public static boolean isDefaultTrimSpaces() {
        return getBooleanProperty(DynamoConstants.SP_TRIM_SPACES, false);
    }

    /**
     * @return whether to use the display name of an attribute as the "prompt" value
     * (hint/placeholder) inside the component
     */
    public static boolean useDefaultPromptValue() {
        return getBooleanProperty(DynamoConstants.SP_USE_DEFAULT_PROMPT_VALUE, true);
    }

    /**
     * @return whether to use thousands grouping in XLS export
     */
    public static boolean useXlsThousandsGrouping() {
        return getBooleanProperty(DynamoConstants.SP_XLS_THOUSANDS_GROUPING, false);
    }

    public static String getUnAccentFunctionName() {
        return getStringProperty(DynamoConstants.SP_UNACCENT_FUNCTION_NAME, "");
    }

    public static String getEntityModelPackageNames() {
        return getStringProperty(DynamoConstants.SP_ENTITY_MODEL_PACKAGE_NAMES, "");
    }

    public static AttributeBooleanFieldMode getDefaultBooleanFieldMode() {
        return AttributeBooleanFieldMode.valueOf(getStringProperty(
                DynamoConstants.SP_DEFAULT_BOOLEAN_FIELD_MODE,
                AttributeBooleanFieldMode.CHECKBOX.name()
        ));
    }

    public ElementCollectionMode getDefaultElementCollectionMode() {
        return ElementCollectionMode.valueOf(getStringProperty(
                DynamoConstants.SP_DEFAULT_ELEMENT_COLLECTION_MODE, "CHIPS"
        ));
    }

}
