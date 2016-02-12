package nl.ocs.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import nl.ocs.exception.OCSImportException;
import nl.ocs.exception.OCSRuntimeException;
import nl.ocs.importer.XlsField;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;

import com.monitorjbl.xlsx.StreamingReader;

/**
 * Base class for services that can be used to import Excel files.
 * 
 * @author bas.rutten
 * 
 */
public class BaseXlsImporter extends BaseImporter<Row, Cell> {

	/**
	 * Checks if any cell in the row contains a certain (String) value
	 * 
	 * @param row
	 * @param value
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
	 *            the cache size
	 * @return
	 */
	public StreamingReader createReader(byte[] bytes, int cacheSize) {
		return StreamingReader.builder().rowCacheSize(cacheSize).sheetIndex(0)
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
	 * Retrieves the numeric value of a cell
	 * 
	 * @param cell
	 * @return
	 */
	protected Double getNumericValue(Cell cell) {
		if (cell != null
				&& (Cell.CELL_TYPE_NUMERIC == cell.getCellType() || Cell.CELL_TYPE_BLANK == cell
						.getCellType())) {
			try {
				return cell.getNumericCellValue();
			} catch (NullPointerException nex) {
				// cannot return null from getNumericCellValue - so if the cell
				// is empty we
				// have to handle it in this ugly way
				return null;
			} catch (Exception ex) {
				throw new OCSImportException("Found an invalid numeric value: "
						+ cell.getStringCellValue(), ex);
			}
		} else if (cell != null && Cell.CELL_TYPE_STRING == cell.getCellType()) {
			// in case the value is not numeric, simply output a warning. If the
			// field is required, this will trigger
			// an error at a later stage
			if (!StringUtils.isEmpty(cell.getStringCellValue().trim())) {
				throw new OCSImportException("Found an invalid numeric value: "
						+ cell.getStringCellValue());
			}
		}
		return null;
	}

	/**
	 * Retrieves the numeric value of a cell, or falls back to a suitable
	 * default if the cell is empty and the default is defined
	 * 
	 * @param cell
	 * @param field
	 * @return
	 */
	@Override
	public Double getNumericValueWithDefault(Cell cell, XlsField field) {
		Double value = getNumericValue(cell);
		if (value == null && !StringUtils.isEmpty(field.defaultValue())) {
			value = Double.valueOf(field.defaultValue());
		}
		return value;
	}

	/**
	 * Retrieves the value of a cell as a string. Returns <code>null</code> if
	 * the cell does not contain a string
	 * 
	 * @param cell
	 * @return
	 */
	protected String getStringValue(Cell cell) {
		if (cell != null
				&& (Cell.CELL_TYPE_STRING == cell.getCellType() || cell.getCellType() == Cell.CELL_TYPE_BLANK)) {
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
	 * Retrieves the value of a cell as a string, or falls back to the default
	 * if the value is empty and a suitable default is defined
	 * 
	 * @param cell
	 * @param field
	 * @return
	 */
	@Override
	protected String getStringValueWithDefault(Cell cell, XlsField field) {
		String value = getStringValue(cell);
		if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(field.defaultValue())) {
			value = field.defaultValue();
		}
		return value;
	}

	@Override
	protected Cell getUnit(Row row, XlsField field) {
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
	protected boolean isWithinRange(Row row, XlsField field) {
		return row.getFirstCellNum() + field.index() < row.getLastCellNum();
	}
}
