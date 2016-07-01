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

import com.ocs.dynamo.constants.DynamoConstants;

public final class SystemPropertyUtils {

    private static final int DEFAULT_DECIMAL_PRECISION = 2;

    private static final int DEFAULT_LISTSELECT_ROWS = 3;

    private SystemPropertyUtils() {
    }

    /**
     * Whether to allow dat export from tables
     * 
     * @return
     */
    public static boolean allowTableExport() {
        return Boolean.getBoolean(DynamoConstants.SP_ALLOW_TABLE_EXPORT);
    }

    /**
     * The default date format
     * 
     * @return
     */
    public static String getDefaultDateFormat() {
        return System.getProperty(DynamoConstants.SP_DEFAULT_DATE_FORMAT, "dd-MM-yyyy");
    }

    /**
     * The default date/time format
     * 
     * @return
     */
    public static String getDefaultDateTimeFormat() {
        return System
                .getProperty(DynamoConstants.SP_DEFAULT_DATETIME_FORMAT, "dd-MM-yyyy HH:mm:ss");
    }

    /**
     * The default decimal precision
     * 
     * @return
     */
    public static int getDefaultDecimalPrecision() {
        return Integer.getInteger(DynamoConstants.SP_DECIMAL_PRECISION, DEFAULT_DECIMAL_PRECISION);
    }

    /**
     * The default number of rows in a list select component
     * 
     * @return
     */
    public static int getDefaultListSelectRows() {
        return Integer.getInteger(DynamoConstants.SP_DEFAULT_LISTSELECT_ROWS,
                DEFAULT_LISTSELECT_ROWS);
    }

    /**
     * The default date/time format
     * 
     * @return
     */
    public static String getDefaultTimeFormat() {
        return System.getProperty(DynamoConstants.SP_DEFAULT_TIME_FORMAT, "HH:mm:ss");
    }

    /**
     * Whether to include thousands groupings in edit mode
     * 
     * @return
     */
    public static boolean useThousandsGroupingInEditMode() {
        return Boolean.getBoolean(DynamoConstants.SP_THOUSAND_GROUPING);
    }

    /**
     * The default currency symbol
     * 
     * @return
     */
    public static String getDefaultCurrencySymbol() {
        return System.getProperty(DynamoConstants.SP_DEFAULT_CURRENCY_SYMBOL, "€");
    }
}
