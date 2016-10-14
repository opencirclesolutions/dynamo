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
package com.ocs.dynamo.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import au.com.bytecode.opencsv.CSVWriter;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.dao.query.DataSetIterator;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.utils.VaadinUtils;

/**
 * Base class for template classes for exporting pivoted data
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the class of the ID of the entity that is displayed in the row
 * @param <T>
 *            the class of the entity that is displayed in the row
 * @param <ID2>
 *            the class of the ID of the entity that is displayed in the column
 * @param <U>
 *            the class of the entity that is displayed in the row
 */
public abstract class ExportPivotTemplate<ID extends Serializable, T extends AbstractEntity<ID>, ID2 extends Serializable, U extends AbstractEntity<ID2>> {

	/**
	 * The width for a fixed with column
	 */
	protected static final int FIXED_COLUMN_WIDTH = 20 * 256;

	/**
	 * The maximum number of rows that will be created normally. If the number is larger, then a
	 * streaming writer will be used. This is faster but it will mean we cannot auto size the
	 * columns
	 */
	protected static final int MAX_SIZE = 20000;

	private Workbook workbook;

	private final List<String> captions;

	private final Filter filter;

	private XlsStyleGenerator<ID, T> generator;

	private final FetchJoinInformation[] joins;

	private final BaseService<ID, T> service;

	private final SortOrder[] sortOrders;

	private final String title;

	private CustomXlsStyleGenerator<ID, T> customGenerator;

	/**
	 * Constructor
	 * 
	 * @param service
	 *            the service
	 * @param sortOrders
	 *            the sort orders used to order the search results
	 * @param filter
	 *            the filter used to retrieve the search results
	 * @param captions
	 *            the captions used for the fixed columns
	 * @param title
	 *            the report title
	 * @param customGenerator
	 *            custom cell style generator
	 * @param joins
	 *            the joins used when fetching search results
	 */
	public ExportPivotTemplate(BaseService<ID, T> service, SortOrder[] sortOrders, Filter filter,
	        List<String> captions, String title, CustomXlsStyleGenerator<ID, T> customGenerator,
	        FetchJoinInformation... joins) {
		this.service = service;
		this.sortOrders = sortOrders;
		this.filter = filter;
		this.captions = captions;
		this.title = title;
		this.joins = joins;
		this.customGenerator = customGenerator;
	}

	/**
	 * Indicates whether to include an average column at the end of a row
	 * 
	 * @return
	 */
	protected abstract boolean createAveragesColumn();

	/**
	 * Creates an Excel cell
	 * 
	 * @param row
	 *            the row to which to add the cell
	 * @param colIndex
	 *            the column index of the cell
	 * @param entity
	 *            the entity that is represented int he row
	 * @param value
	 *            the cell value
	 * @return
	 */
	protected Cell createCell(Row row, int colIndex, T entity, Object value) {
		Cell cell = row.createCell(colIndex);
		cell.setCellStyle(generator.getCellStyle(colIndex, entity, value, false));
		if (customGenerator != null) {
			CellStyle custom = customGenerator.getCustomCellStyle(workbook, entity, value);
			if (custom != null) {
				cell.setCellStyle(custom);
			}
		}
		return cell;
	}

	/**
	 * Creates the style generator
	 * 
	 * @param workbook
	 *            the work book that is being created
	 * @return
	 */
	protected XlsStyleGenerator<ID, T> createGenerator(Workbook workbook) {
		return new BaseXlsStyleGenerator<>(workbook);
	}

	/**
	 * Indicates whether to include a sum column at the end of a row
	 * 
	 * @return
	 */
	protected abstract boolean createSumColumn();

	/**
	 * Creates an appropriate work book - if the size is below the threshold then a normal workbook
	 * is created. Otherwise a streaming workbook is created. This is much faster and more
	 * efficient, but you cannot auto resize the columns
	 * 
	 * @param size
	 *            the number of rows
	 * @return
	 */
	protected Workbook createWorkbook(int size) {
		if (size > MAX_SIZE) {
			return new SXSSFWorkbook();
		}
		return new XSSFWorkbook();
	}

	/**
	 * Generate the XLS (Excel) output
	 * 
	 * @param iterator
	 *            data set iterator for iterating over the rows
	 * @return
	 * @throws IOException
	 */
	protected byte[] generateXls(DataSetIterator<ID, T> iterator) throws IOException {
		workbook = createWorkbook(iterator.size());
		Sheet sheet = workbook.createSheet(getTitle());
		generator = createGenerator(workbook);

		boolean resize = !(workbook instanceof SXSSFWorkbook);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		// add header row
		Row titleRow = sheet.createRow(0);
		titleRow.setHeightInPoints(40);

		for (int i = 0; i < getCaptions().size(); i++) {
			if (!resize) {
				sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
			}
			Cell cell = titleRow.createCell(i);
			cell.setCellStyle(generator.getHeaderStyle(i));
			cell.setCellValue(getCaptions().get(i));
		}

		List<U> columns = getColumns();

		// add headers for the columns
		int j = 0;
		for (U u : getColumns()) {
			Cell cell = titleRow.createCell(getCaptions().size() + j);
			cell.setCellStyle(generator.getHeaderStyle(getCaptions().size() + j));
			cell.setCellValue(getColumnHeader(u));
			j++;
		}

		// caption for the totals row
		String rowTotalCaption = getRowTotalCaption();

		int lastColumnIndex = getCaptions().size() + columns.size();
		if (rowTotalCaption != null) {
			Cell totalCell = titleRow.createCell(lastColumnIndex);
			totalCell.setCellStyle(generator.getHeaderStyle(lastColumnIndex));
			totalCell.setCellValue(rowTotalCaption);
		}

		Object prevRowId = null;
		int index = 0;
		Row row = null;
		int rowSum = 0;
		int valueCount = 0;
		BigDecimal rowAverage = BigDecimal.ZERO;

		T entity = iterator.next();
		while (entity != null) {

			Object rowId = getRowId(entity);
			if (!ObjectUtils.equals(prevRowId, rowId)) {
				// time to move to a new row

				// add empty cells add the end
				writeEmptyCells(row, index, lastColumnIndex);

				// add row total
				writeXlsRowTotal(row, lastColumnIndex, rowSum, rowAverage, valueCount);

				// add a new row if the combination of store and product
				// changes
				row = sheet.createRow(sheet.getLastRowNum() + 1);

				// add the first values
				setXlsCellValues(row, entity);

				rowSum = 0;
				rowAverage = BigDecimal.ZERO;
				index = 0;
				valueCount = 0;
			}

			Object value = getValue(entity);

			int colIndex = getCaptions().size() + index;

			if (value != null) {
				// write the value - currently supports only BigDecimal an Integer
				if (value instanceof BigDecimal) {
					createCell(row, colIndex, entity, value).setCellValue(((BigDecimal) value).doubleValue());
					rowSum += ((BigDecimal) value).intValue();
					rowAverage = rowAverage.add((BigDecimal) value);
				} else {
					createCell(row, colIndex, entity, value).setCellValue((Integer) value);
					rowSum += (Integer) value;
					rowAverage = rowAverage.add(BigDecimal.valueOf((Integer) value));
				}
				valueCount++;
			} else {
				// create empty cell
				createCell(row, colIndex, entity, null);
			}

			index++;
			prevRowId = rowId;
			entity = iterator.next();
		}

		// add the last row total
		writeEmptyCells(row, index, lastColumnIndex);
		writeXlsRowTotal(row, lastColumnIndex, rowSum, rowAverage, valueCount);

		// auto-resize if possible
		if (resize) {
			for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}
		}

		// produce output
		workbook.write(stream);
		return stream.toByteArray();
	}

	/**
	 * Generates a CSV export
	 * 
	 * @param iterator
	 *            the data set iterator that contains the data to be exported
	 * @return
	 * @throws IOException
	 */
	protected byte[] generateCsv(DataSetIterator<ID, T> iterator) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
		        CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, DynamoConstants.UTF_8), ';')) {

			// add header row
			List<String> header = new ArrayList<>();
			for (int i = 0; i < getCaptions().size(); i++) {
				header.add(getCaptions().get(i));
			}

			// add headers for program weeks
			for (U u : getColumns()) {
				header.add(getColumnHeader(u));
			}

			// caption for the totals row
			String rowTotalCaption = getRowTotalCaption();
			if (rowTotalCaption != null) {
				header.add(rowTotalCaption);
			}

			writer.writeNext(header.toArray(new String[0]));

			Object prevRowId = null;
			int rowSum = 0;
			int valueCount = 0;
			BigDecimal rowAverage = BigDecimal.ZERO;

			List<String> row = new ArrayList<>();

			T entity = iterator.next();
			while (entity != null) {

				Object rowId = getRowId(entity);
				if (!ObjectUtils.equals(prevRowId, rowId)) {

					// add row total
					writeCsvRow(writer, row, rowSum, rowAverage, valueCount);

					row = new ArrayList<>();

					setCsvCellValues(row, entity);

					rowSum = 0;
					rowAverage = BigDecimal.ZERO;
					valueCount = 0;
				}

				Object value = getValue(entity);
				if (value != null) {
					if (value instanceof BigDecimal) {
						String s = VaadinUtils.bigDecimalToString(usePercentages(), true, (BigDecimal) value);
						row.add(s);
						rowSum += ((BigDecimal) value).intValue();
						rowAverage = rowAverage.add((BigDecimal) value);
					} else {
						row.add(VaadinUtils.integerToString(true, (Integer) value));
						rowSum += (Integer) value;
						rowAverage = rowAverage.add(BigDecimal.valueOf((Integer) value));
					}
					valueCount++;
				} else {
					row.add("");
				}

				prevRowId = rowId;
				entity = iterator.next();
			}

			writeCsvRow(writer, row, rowSum, rowAverage, valueCount);

			writer.flush();
			return out.toByteArray();
		}
	}

	public List<String> getCaptions() {
		return captions;
	}

	/**
	 * Returns a header for a certain column
	 * 
	 * @param u
	 *            the entity that corresponds to the column
	 * @return
	 */
	protected abstract String getColumnHeader(U u);

	/**
	 * Returns the entities that correspond to the columns
	 * 
	 * @return
	 */
	protected abstract List<U> getColumns();

	public Filter getFilter() {
		return filter;
	}

	public FetchJoinInformation[] getJoins() {
		return joins;
	}

	/**
	 * Obtains the row ID from the entity - this is used to determine whether to start on a new row
	 * 
	 * @param entity
	 *            the entity
	 * @return
	 */
	protected abstract Object getRowId(T entity);

	/**
	 * Returns the caption of the column that contains the row total
	 * 
	 * @return
	 */
	protected abstract String getRowTotalCaption();

	public BaseService<ID, T> getService() {
		return service;
	}

	public SortOrder[] getSortOrders() {
		return sortOrders;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Obtains the relevant value from the entity
	 * 
	 * @param entity
	 *            the entity
	 * @return
	 */
	protected abstract Object getValue(T entity);

	/**
	 * 
	 * @return
	 */
	protected boolean usePercentages() {
		return false;
	}

	/**
	 * Processes the input and creates a file
	 * 
	 * @param xls
	 * @return
	 * @throws IOException
	 */
	public final byte[] process(boolean xls) throws IOException {

		// retrieve all store series based on the IDs
		List<ID> ids = service.findIds(filter, sortOrders);
		DataSetIterator<ID, T> iterator = new DataSetIterator<ID, T>(ids) {

			@Override
			protected List<T> readPage(List<ID> ids) {
				return service.fetchByIds(ids, new SortOrders(sortOrders), joins);
			}
		};

		if (xls) {
			return generateXls(iterator);
		}
		return generateCsv(iterator);
	}

	/**
	 * Sets the values for the first cells
	 * 
	 * @param row
	 *            the row
	 * @param entity
	 * @return
	 */
	protected abstract void setCsvCellValues(List<String> row, T entity);

	/**
	 * Sets the values for the first cells
	 * 
	 * @param row
	 * @param entity
	 * @return
	 */
	protected abstract void setXlsCellValues(Row row, T entity);

	/**
	 * Writes a row to the CSV writer
	 * 
	 * @param writer
	 *            the CVS writer
	 * @param row
	 *            the current row
	 * @param mode
	 *            the export mode
	 * @param rowSum
	 *            the row sum
	 * @param rowAverage
	 *            the row average total
	 * @param valueCount
	 *            the number of written values
	 */
	private void writeCsvRow(CSVWriter writer, List<String> row, Integer rowSum, BigDecimal rowAverage, int valueCount) {
		if (!row.isEmpty()) {
			if (createSumColumn()) {
				row.add(VaadinUtils.integerToString(true, rowSum));
			} else if (createAveragesColumn() && valueCount > 0) {
				BigDecimal avg = rowAverage.divide(new BigDecimal(valueCount));
				String s = VaadinUtils.bigDecimalToString(usePercentages(), true, (BigDecimal) avg);
				row.add(s);
			}
			writer.writeNext(row.toArray(new String[0]));
		}
	}

	/**
	 * Fills the remainder of a row with empty cells
	 * 
	 * @param row
	 *            the row
	 * @param index
	 *            the index of the first column
	 * @param lastColumnIndex
	 *            the index of the last column
	 */
	private void writeEmptyCells(Row row, int index, int lastColumnIndex) {
		if (row != null) {
			int colIndex = getCaptions().size() + index;
			while (colIndex < lastColumnIndex) {
				createCell(row, colIndex, null, null);
				colIndex++;
			}
		}
	}

	/**
	 * Writes a row total cell to the XLS workbook
	 * 
	 * @param workbook
	 *            the workbook
	 * @param row
	 *            the current row
	 * @param mode
	 *            the export mode
	 * @param lastColumnIndex
	 *            the index of the last column
	 * @param rowSum
	 *            the row sum
	 * @param rowAverage
	 *            the row average
	 * @param valueCount
	 *            the number of values
	 */
	private void writeXlsRowTotal(Row row, int lastColumnIndex, Integer rowSum, BigDecimal rowAverage, int valueCount) {
		if (row != null && (createSumColumn() || createAveragesColumn())) {
			if (createSumColumn()) {
				createCell(row, lastColumnIndex, null, rowSum).setCellValue(rowSum);
			} else if (valueCount > 0) {
				// average
				BigDecimal avg = rowAverage.divide(new BigDecimal(valueCount), DynamoConstants.INTERMEDIATE_PRECISION,
				        RoundingMode.HALF_UP);
				String s = VaadinUtils.bigDecimalToString(usePercentages(), true, (BigDecimal) avg);
				createCell(row, lastColumnIndex, null, avg).setCellValue(s);
			}
		}
	}
}
