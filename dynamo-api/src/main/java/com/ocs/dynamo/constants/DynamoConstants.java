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
package com.ocs.dynamo.constants;

import lombok.experimental.UtilityClass;

import java.util.Locale;

/**
 * Various constants that are used by the Dynamo framework.
 *
 * @author bas.rutten
 */
@UtilityClass
public class DynamoConstants {

    /**
     * The default locale
     */
    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * The default ID field
     */
    public static final String ID = "id";

    /**
     * The version field used for optimistic locking
     */
    public static final String VERSION = "version";

    /**
     * Additional ID field
     */
    public static final String IDS = "ids";

    /**
     * Indicates whether to capitalize individual words in property names
     */
    public static final String SP_CAPITALIZE_WORDS = "ocs.capitalize.words";

    /**
     * Indicates the default mode to use for boolean components
     */
    public static final String SP_DEFAULT_BOOLEAN_FIELD_MODE = "ocs.default.boolean.field.mode";

    /**
     * Name of the system property that is used to determine the default date format
     */
    public static final String SP_DEFAULT_DATE_FORMAT = "ocs.default.date.format";

    /**
     * Name of the system property that is used to determine the default date/time
     * (time stamp) format
     */
    public static final String SP_DEFAULT_DATETIME_FORMAT = "ocs.default.datetime.format";

    /**
     * Name of the system property that is used to determine the default decimal
     * precision
     */
    public static final String SP_DEFAULT_DECIMAL_PRECISION = "ocs.default.decimal.precision";

    /**
     * Indicates the default mode to use for element collection fields
     */
    public static final String SP_DEFAULT_ELEMENT_COLLECTION_MODE = "ocs.default.element.collection.mode";

    /**
     * The default field type to use for enumeration attributes
     */
    public static final String SP_DEFAULT_ENUM_FIELD_MODE = "ocs.default.enum.field.mode";

    /**
     * Name of the system property that is used to determine the representation of
     * the value false
     */
    public static final String SP_DEFAULT_FALSE_REPRESENTATION = "ocs.default.false.representation";

    /**
     * Name of the system property that is used to determine the default group
     * together mode
     */
    public static final String SP_DEFAULT_GROUP_TOGETHER_MODE = "ocs.default.group.together.mode";

    /**
     * Name of the system property that is used to determine the column width from
     * grouping together
     */
    public static final String SP_DEFAULT_GROUP_TOGETHER_WIDTH = "ocs.default.group.together.width";

    /**
     * Name of the system property that is used to set the default locale
     */
    public static final String SP_DEFAULT_LOCALE = "ocs.default.locale";

    /**
     * Name of the system property that is used to determine the default nesting
     * depth
     */
    public static final String SP_DEFAULT_NESTING_DEPTH = "ocs.default.entity.nesting.depth";

    /**
     * Name of the system property that is used to specify the default number field
     * mode
     */
    public static final String SP_DEFAULT_NUMBER_FIELD_MODE = "ocs.default.number.field.mode";

    /**
     * Name of the system property that is used to determine the default case
     * sensitiveness for search
     */
    public static final String SP_DEFAULT_SEARCH_CASE_SENSITIVE = "ocs.default.search.case.sensitive";

    /**
     * Name of the system property that is used to determine whether search is
     * prefix only
     */
    public static final String SP_DEFAULT_SEARCH_PREFIX_ONLY = "ocs.default.search.prefix.only";

    /**
     * Name of the system property that is used to determine the default time format
     */
    public static final String SP_DEFAULT_TIME_FORMAT = "ocs.default.time.format";

    /**
     * Name of the system property that is used to determine the representation of
     * the value true
     */
    public static final String SP_DEFAULT_TRUE_REPRESENTATION = "ocs.default.true.representation";

    /**
     * The packages that contain the entity model
     */
    public static final String SP_ENTITY_MODEL_PACKAGE_NAMES = "ocs.entity.model.package.names";

    /**
     * Name of the system property that is used as the CSV escape character when
     * exporting
     */
    public static final String SP_EXPORT_CSV_ESCAPE = "ocs.export.csv.escape";

    /**
     * Name of the system property that is used as the CSV quote char when exporting
     */
    public static final String SP_EXPORT_CSV_QUOTE = "ocs.export.csv.quote";

    /**
     * Name of the system property that is used as the CSV separator when exporting
     */
    public static final String SP_EXPORT_CSV_SEPARATOR = "ocs.export.csv.separator";

    /**
     * The number of rows that must be present in a result set before resorting to a
     * streaming approach for Excel export
     */
    public static final String SP_MAX_ROWS_BEFORE_STREAMING = "ocs.max.rows.before.streaming";

    /**
     * Class name for the service locator (override to create a different service
     * locator, e.g. to use a separate service locator for integration tests)
     */
    public static final String SP_SERVICE_LOCATOR_CLASS_NAME = "ocs.service.locator.classname";

    /**
     * The name of the system property that is used to determine whether to trim
     * white space for text inputs
     */
    public static final String SP_TRIM_SPACES = "ocs.trim.spaces";

    /**
     * The name of the database function used to replace accents
     */
    public static final String SP_UNACCENT_FUNCTION_NAME = "ocs.unaccent.function.name";

    /**
     * Indicates whether to use the display name as the input prompt by default
     */
    public static final String SP_USE_DEFAULT_PROMPT_VALUE = "ocs.use.default.prompt.value";

    /**
     * Whether to use thousands grouping in XLS files
     */
    public static final String SP_XLS_THOUSANDS_GROUPING = "ocs.xls.thousands.grouping";


}
