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

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import com.monitorjbl.xlsx.StreamingReader;
import com.ocs.dynamo.exception.OCSImportException;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.exception.OCSValidationException;
import com.ocs.dynamo.importer.ImportField;
import com.ocs.dynamo.importer.dto.AbstractDTO;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.SystemPropertyUtils;

/**
 * Base class for services that can be used to import Excel files.
 * 
 * @author bas.rutten
 */
public class BaseXlsImporter extends BaseImporter<Row, Cell> {

	/**
	 * Checks if any cell in the row contains a certain (String) value
	 * 
	 * @param row
	 *            the row
	 * @param value
	 *            the String value
	 * @return
	 */
	protected boolean containsStringValue(Row row, String value) {
		if (row == null || !row.iterator().hasNext()) {
			return false;
		}

		boolean found = false;
		for (int i = row.getFirstCellNum(); !found && i < row.getLastCellNum(); i++) {
			if (row.getCell(i) != null) {
				try {
					found = value.equalsIgnoreCase(row.getCell(i).getStringCellValue());
				} catch (Exception ex) {
					// do nothing
				}
			}
		}
		return found;
	}

	@Override
	public int countRows(byte[] bytes, int row, int column) {
		Workbook wb = createWorkbook(bytes);
		if (wb.getNumberOfSheets() == 0) {
			return 0;
		} else {
			Sheet sheet = wb.getSheetAt(0);
			return sheet.getLastRowNum() - sheet.getFirstRowNum() + 1;
		}
	}

	/**
	 * Creates a reader for processing an Excel file using streaming
	 * 
	 * @param bytes
	 *            the content of the file
	 * @param cacheSize
	 *            the size of the cache
	 * @return
	 */
	public StreamingReader createReader(byte[] bytes, int cacheSize) {
		return createReader(bytes, 0, cacheSize);
	}

	/**
	 * Creates a reader for processing an Excel file using streaming
	 * 
	 * @param bytes
	 *            the content of the file
	 * @param cacheSize
	 *            the cache size
	 * @return
	 */
	public StreamingReader createReader(byte[] bytes, int sheetIndex, int cacheSize) {
		return StreamingReader.builder().rowCacheSize(cacheSize).sheetIndex(sheetIndex)
		        .read(new ByteArrayInputStream(bytes));
	}

	/**
	 * Creates a workbook from an array of bytes
	 * 
	 * @param bytes
	 * @return
	 */
	public Workbook createWorkbook(byte[] bytes) {
		Workbook workbook = null;
		try {
			// first, try to check if it's an original (old) Excel file
			workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes));
		} catch (OfficeXmlFileException ex) {
			try {
				workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
			} catch (IOException e) {
				throw new OCSRuntimeException(e.getMessage(), e);
			}
		} catch (IOException e) {
			throw new OCSRuntimeException(e.getMessage(), e);
		}
		return workbook;
	}

	/**
	 * Extracts a boolean value from a cell
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected Boolean getBooleanValue(Cell cell) {
		if (cell != null && (Cell.CELL_TYPE_BOOLEAN == cell.getCellType())) {
			return cell.getBooleanCellValue();
		} else if (cell != null && Cell.CELL_TYPE_STRING == cell.getCellType()) {
			return Boolean.valueOf(cell.getStringCellValue());
		}
		return Boolean.FALSE;
	}

	@Override
	protected Boolean getBooleanValueWithDefault(Cell unit, ImportField field) {
		Boolean b = getBooleanValue(unit);
		if (b == null && field.defaultValue() != null) {
			return Boolean.valueOf(field.defaultValue());
		}
		return b;
	}

	/**
	 * Retrieves a date value from a cell
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected Date getDateValue(Cell cell) {
		if (cell != null
		        && (Cell.CELL_TYPE_NUMERIC == cell.getCellType() || Cell.CELL_TYPE_BLANK == cell.getCellType())) {
			try {
				Date date = cell.getDateCellValue();
				return date;
			} catch (NullPointerException nex) {
				// horrible code throws NPE in case of empty cell
				return null;
			}
		}
		return null;
	}

	@Override
	protected Date getDateValueWithDefault(Cell cell, ImportField field) {
		Date value = getDateValue(cell);
		if (value == null && field.defaultValue() != null && !"".equals(field.defaultValue())) {
			try {
				value = new SimpleDateFormat(SystemPropertyUtils.getDefaultDateFormat()).parse(field.defaultValue());
			} catch (ParseException e) {
				throw new OCSImportException(field.defaultValue() + " cannot be converted to a date");
			}
		}
		return value;
	}

	/**
	 * Retrieves the numeric value of a cell
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected Double getNumericValue(Cell cell) {
		if (cell != null
		        && (Cell.CELL_TYPE_NUMERIC == cell.getCellType() || Cell.CELL_TYPE_BLANK == cell.getCellType())) {
			try {
				return cell.getNumericCellValue();
			} catch (NullPointerException nex) {
				// cannot return null from getNumericCellValue - so if the cell
				// is empty we
				// have to handle it in this ugly way
				return null;
			} catch (Exception ex) {
				throw new OCSImportException("Found an invalid numeric value: " + cell.getStringCellValue(), ex);
			}
		} else if (cell != null && Cell.CELL_TYPE_STRING == cell.getCellType()) {
			// in case the value is not numeric, simply output a warning. If the
			// field is required, this will trigger
			// an error at a later stage
			if (!StringUtils.isEmpty(cell.getStringCellValue().trim())) {
				throw new OCSImportException("Found an invalid numeric value: " + cell.getStringCellValue());
			}
		}
		return null;
	}

	/**
	 * Retrieves the numeric value of a cell, or falls back to a suitable default if the cell is
	 * empty and the default is defined
	 * 
	 * @param cell
	 *            the cell
	 * @param field
	 *            the field definition
	 * @return
	 */
	@Override
	public Double getNumericValueWithDefault(Cell cell, ImportField field) {
		Double value = getNumericValue(cell);
		if (value == null && !StringUtils.isEmpty(field.defaultValue())) {
			value = Double.valueOf(field.defaultValue());
		}
		return value;
	}

	/**
	 * Returns a date value from a cell. Throws an exception if the cell is empty
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected Date getRequiredDateValue(Cell cell) {
		Date result = getDateValue(cell);
		if (result == null) {
			throw new OCSValidationException("Required value not set: " + cell.getColumnIndex());
		}
		return result;
	}

	/**
	 * Extracts a String value from a cell and throw an exception if this value is empty
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected Double getRequiredNumericValue(Cell cell) {
		Double result = getNumericValue(cell);
		if (result == null) {
			throw new OCSValidationException("Required value not set: " + cell.getColumnIndex());
		}
		return result;
	}

	/**
	 * Extracts a String value from a cell and throw an exception if this value is empty
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected String getRequiredStringValue(Cell cell) {
		String result = getStringValue(cell);
		if (result == null) {
			throw new OCSValidationException("Required value not set: " + cell.getColumnIndex());
		}
		return result;
	}

	/**
	 * Retrieves the value of a cell as a string. Returns <code>null</code> if the cell does not
	 * contain a string
	 * 
	 * @param cell
	 *            the cell
	 * @return
	 */
	protected String getStringValue(Cell cell) {
		if (cell != null && (Cell.CELL_TYPE_STRING == cell.getCellType() || cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
			String value = cell.getStringCellValue();
			return value == null ? null : value.trim();
		} else if (cell != null && Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
			// if a number is entered in a field that is supposed to contain a
			// string, Excel goes insane. We have to compensate for this
			Double d = cell.getNumericCellValue();
			return d == null ? null : Long.toString(d.longValue());
		}
		return null;
	}

	/**
	 * Retrieves the value of a cell as a string, or falls back to the default if the value is empty
	 * and a suitable default is defined
	 * 
	 * @param cell
	 *            the cell
	 * @param field
	 *            the field definition
	 * @return
	 */
	@Override
	protected String getStringValueWithDefault(Cell cell, ImportField field) {
		String value = getStringValue(cell);
		if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(field.defaultValue())) {
			value = field.defaultValue();
		}
		return value;
	}

	@Override
	protected Cell getUnit(Row row, ImportField field) {
		return row.getCell(row.getFirstCellNum() + field.index());
	}

	@Override
	public boolean isPercentageCorrectionSupported() {
		return true;
	}

	/**
	 * Check if the specified row is completely empty
	 * 
	 * @param row
	 * @return
	 */
	public boolean isRowEmpty(Row row) {
		if (row == null || row.getFirstCellNum() < 0) {
			return true;
		}

		Iterator<Cell> iterator = row.iterator();
		while (iterator.hasNext()) {
			Cell next = iterator.next();
			String value = next.getStringCellValue();
			if (!StringUtils.isEmpty(value)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean isWithinRange(Row row, ImportField field) {
		return row.getFirstCellNum() + field.index() < row.getLastCellNum();
	}

	/**
	 * Processes a number of consecutive rows and translates them into a record
	 * 
	 * @param firstRowNum
	 *            the row number of the first row
	 * @param clazz
	 *            the class of the record
	 * @return
	 */
	public <T extends AbstractDTO> T processRows(Sheet sheet, int firstRowIndex, int colIndex, Class<T> clazz) {
		T t = ClassUtils.instantiateClass(clazz);

		Object firstCellValue = null;
		try {
			firstCellValue = sheet.getRow(firstRowIndex).getCell(colIndex).getStringCellValue();
		} catch (Exception ex) {
			// do nothing
		}

		if (firstCellValue != null && !firstCellValue.toString().equals("")) {
			PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(clazz);
			for (PropertyDescriptor d : descriptors) {
				ImportField field = ClassUtils.getAnnotation(clazz, d.getName(), ImportField.class);
				if (field != null) {
					int rowNum = firstRowIndex + field.index();
					if (rowNum <= sheet.getLastRowNum()) {
						Row row = sheet.getRow(rowNum);

						Cell unit = row.getCell(colIndex);

						Object obj = getFieldValue(d, unit, field);
						if (obj != null) {
							ClassUtils.setFieldValue(t, d.getName(), obj);
						} else if (field.required()) {
							// a required value is missing!
							throw new OCSImportException("Required value for field '" + d.getName() + "' is missing");
						}
					} else {
						throw new OCSImportException("Input doesn't have enoug rows: row " + rowNum + " does not exist");
					}
				}
			}
		}

		return t;
	}
}
