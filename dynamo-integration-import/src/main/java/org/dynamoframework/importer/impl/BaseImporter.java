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
package org.dynamoframework.importer.impl;

import org.apache.commons.lang3.StringUtils;
import org.dynamoframework.exception.OCSImportException;
import org.dynamoframework.importer.ImportField;
import org.dynamoframework.importer.dto.AbstractDTO;
import org.dynamoframework.utils.ClassUtils;
import org.dynamoframework.utils.NumberUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.lang.String.format;

/**
 * Base class for smart upload functionality
 * 
 * @author bas.rutten
 * @param <R> the type of a single row
 * @param <U> the type of a single cell or field
 */
public abstract class BaseImporter<R, U> {

	private static final double PERCENTAGE_FACTOR = 100.;

	/**
	 * Counts the number of rows in the input. This method will count all rows,
	 * including the header, and will not check if any of the rows are valid
	 * 
	 * @param bytes      the byte representation of the input file
	 * @param sheetIndex the index of the sheet (if appropriate)
	 * @return the number of rows
	 */
	public abstract int countRows(byte[] bytes, int sheetIndex);

	/**
	 * Retrieves a boolean value from the input and falls back to a default if the
	 * value is empty or not defined
	 * 
	 * @param unit  the unit of data value to process (string, Excel cell etc)
	 * @param field the field definition
	 * @return the Boolean value
	 */
	protected abstract Boolean getBooleanValueWithDefault(U unit, ImportField field);

	/**
	 * Retrieves a date value from the input and falls back to a default if the
	 * value is empty or not defined
	 * 
	 * @param unit  the unit of data to process
	 * @param field the field definition
	 * @return the LocalDate value
	 */
	protected abstract LocalDate getDateValueWithDefault(U unit, ImportField field);

	/**
	 * Retrieves a value from a unit of data
	 * 
	 * @param d     the property descriptor that tells the process the type of the
	 *              value to retrieve
	 * @param unit  the unit of data to process
	 * @param field the field definition
	 * @return the field value
	 */
	@SuppressWarnings("unchecked")
	protected Object getFieldValue(PropertyDescriptor d, U unit, ImportField field) {
		Object obj = null;
		if (String.class.equals(d.getPropertyType())) {
			String value = getStringValueWithDefault(unit, field);
			if (value != null) {
				value = value.trim();
			}
			obj = StringUtils.isEmpty(value) ? null : value;
		} else if (d.getPropertyType().isEnum()) {
			String value = getStringValueWithDefault(unit, field);
			if (value != null) {
				value = value.trim();
				try {
					@SuppressWarnings("rawtypes")
					Class<? extends Enum> enumType = d.getPropertyType().asSubclass(Enum.class);
					obj = Enum.valueOf(enumType, value.toUpperCase());
				} catch (IllegalArgumentException ex) {
					throw new OCSImportException("Value " + value + " cannot be translated to an enumeration value",
							ex);
				}
			}
		} else if (isNumeric(d.getPropertyType())) {
			// numeric field
			Double value = getNumericValueWithDefault(unit, field);
			if (value != null) {

				// if the field represents a percentage but it is
				// received as a
				// fraction, we multiply it by 100
				if (field.percentage() && isPercentageCorrectionSupported()) {
					value = PERCENTAGE_FACTOR * value;
				}

				// illegal negative value
				if (field.cannotBeNegative() && value < 0.0) {
					throw new OCSImportException("Negative value " + value + " found for field '" + d.getName() + "'");
				}

				// round to the nearest integer, then use intValue() or longValue()
				BigDecimal rounded = BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP);
				Class<?> pType = d.getPropertyType();
				if (NumberUtils.isInteger(pType)) {
					obj = rounded.intValue();
				} else if (NumberUtils.isLong(pType)) {
					obj = rounded.longValue();
				} else if (NumberUtils.isFloat(pType)) {
					obj = value.floatValue();
				} else if (NumberUtils.isDouble(pType)) {
					obj = value;
				} else if (BigDecimal.class.equals(pType)) {
					obj = BigDecimal.valueOf(value);
				}
			}
		} else if (Boolean.class.isAssignableFrom(d.getPropertyType())) {
			obj = getBooleanValueWithDefault(unit, field);
		} else if (LocalDate.class.isAssignableFrom(d.getPropertyType())) {
			obj = getDateValueWithDefault(unit, field);
		}
		return obj;
	}

	/**
	 * Retrieves a numeric value from an input unit and falls back to a default if
	 * the value is empty or not defined
	 * 
	 * @param unit  the input unit
	 * @param field the field definition
	 * @return the Double value
	 */
	protected abstract Double getNumericValueWithDefault(U unit, ImportField field);

	/**
	 * Retrieves a string from the input and falls back to a default if the value is
	 * empty or not defined
	 * 
	 * @param unit  the input unit
	 * @param field the field definition
	 * @return the String value
	 */
	protected abstract String getStringValueWithDefault(U unit, ImportField field);

	/**
	 * Retrieves a unit (a single cell or field) from a row
	 * 
	 * @param row   the row
	 * @param field the field definition
	 * @return the retrieved unit
	 */
	protected abstract U getUnit(R row, ImportField field);

	/**
	 * Check if the class is a numeric class
	 * 
	 * @param clazz the class to check
	 * @return true if the value is numeric, false otherwise
	 */
	private boolean isNumeric(Class<?> clazz) {
		return Number.class.isAssignableFrom(clazz) || int.class.equals(clazz) || long.class.equals(clazz)
				|| double.class.equals(clazz) || float.class.equals(clazz);
	}

	/**
	 * Indicates whether fraction values are automatically converted to percentages
	 * 
	 * @return true if this is the case, false otherwise
	 */
	public abstract boolean isPercentageCorrectionSupported();

	/**
	 * Checks whether a field index is within the range of available columns
	 * 
	 * @param row   the row to check
	 * @param field the field definition
	 * @return true if this is the case, false otherwise
	 */
	protected abstract boolean isWithinRange(R row, ImportField field);

	/**
	 * Processes a single row from the input and turns it into an object
	 * 
	 * @param rowNum the row number
	 * @param row    the row
	 * @param clazz  the class of the object that must be created
	 * @return
	 */
	public <T extends AbstractDTO> T processRow(int rowNum, R row, Class<T> clazz) {
		T dto = ClassUtils.instantiateClass(clazz);
		dto.setRowNum(rowNum);

		PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);
		for (PropertyDescriptor descriptor : descriptors) {
			ImportField field = ClassUtils.getAnnotation(clazz, descriptor.getName(), ImportField.class);
			if (field != null) {
				if (isWithinRange(row, field)) {
					U unit = getUnit(row, field);

					Object obj = getFieldValue(descriptor, unit, field);
					if (obj != null) {
						ClassUtils.setFieldValue(dto, descriptor.getName(), obj);
					} else if (field.required()) {
						// a required value is missing!
						throw new OCSImportException(
								format("Required value for field '%s' is missing", descriptor.getName()));
					}
				} else {
					throw new OCSImportException(format("Row %d doesn't have enough columns", rowNum));
				}
			}
		}
		return dto;
	}
}
