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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.importer.ImportField;
import com.ocs.dynamo.utils.SystemPropertyUtils;

/**
 * Base class for importers that read data from a text file
 * 
 * @author bas.rutten
 */
public abstract class BaseTextImporter extends BaseImporter<String[], String> {

	/**
	 * Gets a boolean value from a String
	 * 
	 * @param unit
	 *            the String
	 * @return
	 */
	protected Boolean getBooleanValue(String unit) {
		String value = unit;
		if (StringUtils.isEmpty(value)) {
			return Boolean.FALSE;
		}
		return Boolean.valueOf(value);
	}

	protected Date getDateValue(String unit) {
		String value = unit;
		if (!StringUtils.isEmpty(value)) {
			try {
				return new SimpleDateFormat(SystemPropertyUtils.getDefaultDateFormat()).parse(value);
			} catch (ParseException e) {
				throw new OCSImportException(value + " cannot be converted to a date");
			}
		}
		return null;
	}

	@Override
	protected Date getDateValueWithDefault(String unit, ImportField field) {
		Date value = getDateValue(unit);
		if (value == null && field.defaultValue() != null) {
			try {
				return new SimpleDateFormat(SystemPropertyUtils.getDefaultDateFormat()).parse(field.defaultValue());
			} catch (ParseException e) {
				throw new OCSImportException(field.defaultValue() + " cannot be converted to a date");
			}
		}
		return value;
	}

	@Override
	protected Boolean getBooleanValueWithDefault(String unit, ImportField field) {
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
	protected Double getNumericValueWithDefault(String unit, ImportField field) {
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
	protected String getStringValueWithDefault(String unit, ImportField field) {
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
	protected String getUnit(String[] row, ImportField field) {
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
	protected boolean isWithinRange(String[] row, ImportField field) {
		return field.index() < row.length;
	}

}
