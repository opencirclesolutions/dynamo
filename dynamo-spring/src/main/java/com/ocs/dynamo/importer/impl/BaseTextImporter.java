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
package com.ocs.dynamo.importer.impl;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.importer.XlsField;

/**
 * Base class for importers that read data from a text file
 * 
 * @author bas.rutten
 */
public abstract class BaseTextImporter extends BaseImporter<String[], String> {

    /**
     * Extracts a boolean value
     * 
     * @param unit
     * @return
     */
    protected Boolean getBooleanValue(String unit) {
        String value = unit;
        if (StringUtils.isEmpty(value)) {
            return Boolean.FALSE;
        }
        return Boolean.valueOf(value);
    }

    @Override
    protected Boolean getBooleanValueWithDefault(String unit, XlsField field) {
        Boolean result = getBooleanValue(unit);
        if (result == null && !StringUtils.isEmpty(field.defaultValue())) {
            return Boolean.valueOf(field.defaultValue());
        }
        return result;
    }

    /**
     * Reads a numeric value form a unit (i.e. a single string or a single cell)
     * 
     * @param unit
     * @return
     */
    protected Double getNumericValue(String unit) {
        String value = unit;
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new OCSImportException(value + " cannot be converted to a number");
        }
    }

    /**
     * Reads a numeric value from a unit (and falls back to a default if needed)
     * 
     * @param unit
     *            the unit (cell or string value) to read from
     * @param field
     *            the field definition (contains a default value)
     */
    @Override
    protected Double getNumericValueWithDefault(String unit, XlsField field) {
        Double value = getNumericValue(unit);
        if (value == null && !StringUtils.isEmpty(field.defaultValue())) {
            value = Double.valueOf(field.defaultValue());
        }
        return value;
    }

    /**
     * Reads a String value from a unit (and falls back to a default if needed)
     * 
     * @param unit
     *            the unit (cell or string value) to read from
     * @param field
     *            the field definition (contains a default value)
     */
    @Override
    protected String getStringValueWithDefault(String unit, XlsField field) {
        String result = unit;
        if (StringUtils.isEmpty(result) && !StringUtils.isEmpty(field.defaultValue())) {
            result = field.defaultValue();
        }
        return result;
    }

    /**
     * Retrieves the unit (single string value) from a row or line
     */
    @Override
    protected String getUnit(String[] row, XlsField field) {
        return row[field.index()];
    }

    @Override
    public boolean isPercentageCorrectionSupported() {
        return false;
    }

    /**
     * Checks whether a certain field's index is within range for a certain row
     * 
     * @param row
     *            the row
     * @param field
     *            the field
     */
    @Override
    protected boolean isWithinRange(String[] row, XlsField field) {
        return field.index() < row.length;
    }

}
