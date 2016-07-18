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

import java.util.Locale;

/**
 * Various constants that are used by the Dynamo framework.
 *
 * @author bas.rutten
 */
public final class DynamoConstants {

    /**
     * Maximum cache size (for lazy query container)
     */
    public static final int CACHE_SIZE = 10000;

    /**
     * CSS style for button bar components
     */
    public static final String CSS_BUTTON_BAR = "buttonBar";

    /**
     * Custom style for forms that should span 50% of the screen
     */
    public static final String CSS_CLASS_HALFSCREEN = "halfScreen";

    /**
     * The CSS class that is given to an image component used to display an uploaded file
     */
    public static final String CSS_CLASS_UPLOAD = "fileUpload";

    /**
     * The CSS class that indicates a dangerous value
     */
    public static final String CSS_DANGER = "danger";

    /**
     * The CSS class used to indicate a divider row in a table
     */
    public static final String CSS_DIVIDER = "divider";

    /**
     * The CSS class that is assigned to numerical cells in a table
     */
    public static final String CSS_NUMERICAL = "numerical";
    
    public static final String CSS_NESTED = "nested";

    /**
     * Currency symbol
     */
    public static final String CURRENCY_SYMBOL = "currencySymbol";

    /**
     * The default locale
     */
    public static final Locale DEFAULT_LOCALE = Locale.UK;

    /**
     * Larger page size (for lazy query container)
     */
    public static final int EXTENDED_PAGE_SIZE = 500;

    /**
     * The default ID field
     */
    public static final String ID = "id";

    /**
     * The default page size for the lazy query container.
     */
    public static final int PAGE_SIZE = 20;

    /**
     * The screen mode
     */
    public static final String SCREEN_MODE = "screenMode";

    /**
     * The name of the variable that keeps track of which tab is selected
     */
    public static final String SELECTED_TAB = "selectedTab";

    /**
     * Name of the system property that is used to determine if table export is allowed
     */
    public static final String SP_ALLOW_TABLE_EXPORT = "ocs.allow.table.export";

    /**
     * Name of the system property that is used to determine the default decimal precision
     */
    public static final String SP_DECIMAL_PRECISION = "ocs.default.decimal.precision";

    /**
     * Name of the system property that is used to determine the default currency symbol
     */
    public static final String SP_DEFAULT_CURRENCY_SYMBOL = "ocs.default.currency.symbol";

    /**
     * Name of the system property that is used to determine the default date format
     */
    public static final String SP_DEFAULT_DATE_FORMAT = "ocs.default.date.format";

    /**
     * Name of the system property that is used to determine the default date/time (timestamp)
     * format
     */
    public static final String SP_DEFAULT_DATETIME_FORMAT = "ocs.default.datetime.format";

    /**
     * Name of the system property that is used to determine the default decimal precision
     */
    public static final String SP_DEFAULT_DECIMAL_PRECISION = "ocs.default.decimal.precision";

    /**
     * Name of the system property that is used to determine the amount of rows in a list select
     */
    public static final String SP_DEFAULT_LISTSELECT_ROWS = "ocs.default.listselect.rows";

    /**
     * Name of the system property that is used to determine the default time format
     */
    public static final String SP_DEFAULT_TIME_FORMAT = "ocs.default.time.format";

    /**
     * Name of the system property that indicates the maximum number of items to display in an
     * entity lookup field in multiple select mode
     */
    public static final String SP_LOOKUP_FIELD_MAX_ITEMS = "ocs.default.lookupfield.max.items";

    /**
     * System property that indicates whether to use the thousands grouping separator in edit mode
     */
    public static final String SP_THOUSAND_GROUPING = "ocs.edit.thousands.grouping";

    /**
     * The name of the variable that is used to store the user
     */
    public static final String USER = "user";

    /**
     * The name of the variable that is used to store the user name in the session
     */
    public static final String USER_NAME = "userName";

    /**
     * the UTF-8 character set
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Constructor for OCSConstants.
     */
    private DynamoConstants() {
    }
}
