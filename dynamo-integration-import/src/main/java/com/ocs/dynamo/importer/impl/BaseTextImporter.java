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
import com.ocs.dynamo.util.SystemPropertyUtils;

/**
 * Base class for importers that read data from a text file
 * 
 * @author bas.rutten
 */
public abstract class BaseTextImporter extends BaseImporter<String[], String> {

	/**
	 * Tries to convert a String value to a Boolean (empty String or null
	 * resolves to false)
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Boolean getBooleanValueWithDefault(String unit, ImportField field) {
		Boolean result = getBooleanValue(unit);
		if (result == null && !StringUtils.isEmpty(field.defaultValue())) {
			return Boolean.valueOf(field.defaultValue());
		}
		return result;
	}

	/**
	 * Tries to convert a String value to a Date
	 * 
	 * @param unit
	 *            the String value to convert
	 * @return
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * Tries to convert a String to a numeric value
	 * 
	 * @param unit
	 *            the String value to convert
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
	 * {@inheritDoc}
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
	 * {@inheritDoc}
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
	 * {@inheritDoc}
	 */
	@Override
	protected String getUnit(String[] row, ImportField field) {
		return row[field.index()];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPercentageCorrectionSupported() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isWithinRange(String[] row, ImportField field) {
		return field.index() < row.length;
	}

}
