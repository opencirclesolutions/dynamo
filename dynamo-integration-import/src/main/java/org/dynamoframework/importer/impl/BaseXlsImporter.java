package org.dynamoframework.importer.impl;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.pjfanning.xlsx.StreamingReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dynamoframework.exception.OCSImportException;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.importer.ImportField;
import org.dynamoframework.importer.dto.AbstractDTO;
import org.dynamoframework.utils.ClassUtils;
import org.dynamoframework.configuration.DynamoPropertiesHolder;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;

/**
 * Base class for services that can be used to import Excel files.
 * 
 * @author bas.rutten
 */
public class BaseXlsImporter extends BaseImporter<Row, Cell> {

	private static final int CACHE_SIZE = 500;

	/**
	 * Checks if any cell in a row contains a certain (String) value
	 * 
	 * @param row   the row to check
	 * @param value the String value to check for
	 * @return true if this is the case, false otherwise
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
					// exception when there is not a String in the cell - nothing can be done about
					// this
				}
			}
		}
		return found;
	}

	@Override
	public int countRows(byte[] bytes, int sheetIndex) {
		int count = 0;
		try (Workbook wb = createReader(bytes, CACHE_SIZE)) {
			for (Row r : wb.getSheetAt(sheetIndex)) {
				// if a row in the middle of the sheet is empty, we assume
				// everything else is empty
				if (isRowEmpty(r)) {
					break;
				}
				count++;
			}
			return count;
		} catch (IOException ex) {
			throw new OCSRuntimeException(ex.getMessage());
		}
	}

	/**
	 * Creates a reader for processing an Excel file using streaming
	 * 
	 * @param bytes      the content of the file
	 * @param cacheSize  the cache size
	 * @return the reader
	 */
	public Workbook createReader(byte[] bytes, int cacheSize) {
		return StreamingReader.builder().rowCacheSize(cacheSize).open(new ByteArrayInputStream(bytes));
	}

	/**
	 * Creates a workbook from an array of bytes
	 * 
	 * @param bytes the byte content of the file
	 * @return the workbook
	 */
	public Workbook createWorkbook(byte[] bytes) {
		Workbook workbook;
		try {
			// first, try to check if it's an old Excel file
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
	 * Extracts a Boolean value from a cell
	 * 
	 * @param cell the cell to extract the value from
	 * @return the value
	 */
	protected Boolean getBooleanValue(Cell cell) {
		if (cell != null && (CellType.BOOLEAN == cell.getCellType())) {
			return cell.getBooleanCellValue();
		} else if (cell != null && CellType.STRING == cell.getCellType()) {
			return Boolean.valueOf(cell.getStringCellValue());
		} else if (cell != null && CellType.FORMULA == cell.getCellType()) {
			return cell.getBooleanCellValue();
		}
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * @param cell the cell to extract the value from
	 * @return the value
	 */
	protected LocalDate getDateValue(Cell cell) {
		if (cell != null && (CellType.NUMERIC == cell.getCellType() || CellType.BLANK == cell.getCellType())) {
			try {
				return LocalDate.from(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()));
			} catch (NullPointerException nex) {
				// horrible code throws NPE when cell is empty
				return null;
			}
		} else if (cell != null && CellType.FORMULA == cell.getCellType()) {
			try {
				return LocalDate.from(cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()));
			} catch (NullPointerException nex) {
				// horrible code throws NPE when cell is empty
				return null;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LocalDate getDateValueWithDefault(Cell cell, ImportField field) {
		LocalDate value = getDateValue(cell);
		if (value == null && !StringUtils.isEmpty(field.defaultValue())) {
			value = LocalDate.parse(field.defaultValue(),
					DateTimeFormatter.ofPattern(DynamoPropertiesHolder.getDynamoProperties().getDefaults().getDateFormat()));
		}
		return value;
	}

	/**
	 * Retrieves a numeric value for a cell
	 * 
	 * @param cell the cell to retrieve the value from
	 * @return the value
	 */
	protected Double getNumericValue(Cell cell) {
		if (cell != null && (CellType.NUMERIC == cell.getCellType() || CellType.BLANK == cell.getCellType())) {
			try {
				return cell.getNumericCellValue();
			} catch (NullPointerException nex) {
				// cannot return null from getNumericCellValue - so if the cell
				// is empty we
				// have to handle it in this ugly way
				return null;
			} catch (Exception ex) {
				throw new OCSImportException(format("Found an invalid numeric value: %s", cell.getStringCellValue()),
						ex);
			}
		} else if (cell != null && CellType.STRING == cell.getCellType()) {
			// in case the value is not numeric, simply output a warning. If the
			// field is required, this will trigger
			// an error at a later stage
			if (!StringUtils.isEmpty(cell.getStringCellValue().trim())) {
				throw new OCSImportException(format("Found an invalid numeric value: %s", cell.getStringCellValue()));
			}
		} else if (cell != null && CellType.FORMULA == cell.getCellType()) {
			try {
				return cell.getNumericCellValue();
			} catch (NullPointerException nex) {
				return null;
			} catch (NumberFormatException ex) {
				throw new OCSImportException(format("Found an invalid numeric value: %s", cell.getStringCellValue()));
			}
		}
		return null;
	}

	/**
	 * Retrieves the numeric value of a cell, or falls back to a suitable default
	 * value if the cell is empty and a default value has been specified
	 * 
	 * @param cell  the cell to extract the value from
	 * @param field the field definition
	 * @return the value
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
	 * Retrieves the value of a cell as a string. Returns <code>null</code> if the
	 * cell does not contain a string
	 * 
	 * @param cell the cell to extract the value from
	 * @return the value
	 */
	protected String getStringValue(Cell cell) {
		if (cell != null && (CellType.STRING == cell.getCellType() || cell.getCellType() == CellType.BLANK)) {
			String value = cell.getStringCellValue();
			return value == null ? null : value.trim();
		} else if (cell != null && CellType.NUMERIC == cell.getCellType()) {
			// if a number is entered in a field that is supposed to contain a
			// string, Excel goes insane. We have to compensate for this
			double d = cell.getNumericCellValue();
			return Double.toString(d);
		} else if (cell != null && CellType.FORMULA == cell.getCellType()) {
			try {
				return cell.getStringCellValue();
			} catch (Exception ex) {
				throw new OCSImportException("Found an invalid string value", ex);
			}
		}
		return null;
	}

	/**
	 * Retrieves the value of a cell as a String, or falls back to a default value
	 * if the value is empty and a suitable default value is defined
	 * 
	 * @param cell  the cell to extract the value from
	 * @param field the field definition
	 * @return the value
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
	 * @param row the row to check
	 * @return true if ths is the case, false otherwise
	 */
	public boolean isRowEmpty(Row row) {
		if (row == null || row.getFirstCellNum() < 0) {
			return true;
		}

		for (Cell next : row) {
			String value = next.getStringCellValue();
			if (!StringUtils.isEmpty(value)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isWithinRange(Row row, ImportField field) {
		return row.getFirstCellNum() + field.index() < row.getLastCellNum();
	}

	/**
	 * Processes a number of consecutive rows and translates them into a DTO
	 * 
	 * @param sheet         the sheet to read the values from
	 * @param firstRowIndex the index of the first row to start reading from
	 * @param colIndex      the index of the column that contains the values
	 * @param clazz         the class
	 * @return the resulting DTO
	 */
	public <T extends AbstractDTO> T processRows(Sheet sheet, int firstRowIndex, int colIndex, Class<T> clazz) {
		T dto = ClassUtils.instantiateClass(clazz);

		Object firstCellValue = null;
		try {
			firstCellValue = sheet.getRow(firstRowIndex).getCell(colIndex).getStringCellValue();
		} catch (Exception ex) {
			// do nothing - not a String value
		}

		if (firstCellValue != null && !"".equals(firstCellValue.toString())) {
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
							ClassUtils.setFieldValue(dto, d.getName(), obj);
						} else if (field.required()) {
							// a required value is missing!
							throw new OCSImportException(
									format("Required value for field '%s' is missing", d.getName()));
						}
					} else {
						throw new OCSImportException(
								format("Input doesn't have enough rows: row %d does not exist", rowNum));
					}
				}
			}
		}
		return dto;
	}
}
