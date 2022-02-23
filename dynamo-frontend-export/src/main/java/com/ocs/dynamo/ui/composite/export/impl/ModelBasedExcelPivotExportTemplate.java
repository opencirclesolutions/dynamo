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
package com.ocs.dynamo.ui.composite.export.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.provider.PivotAggregationType;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * Template for exporting a pivoted data set to Excel
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the base item to export
 * @param <T>  the type of the base level entity to export
 */
public class ModelBasedExcelPivotExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseExcelExportTemplate<ID, T> {

	private static final int FIRST_DATA_ROW = 3;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	private PivotParameters pivotParameters;

	/**
	 * Constructor
	 * 
	 * @param service         the service used to query the database
	 * @param entityModel     the entity model of the entity to export
	 * @param sortOrders      the sort order to apply to the data
	 * @param filter          the filter used when querying the database
	 * @param title           title of the report
	 * @param customGenerator custom generator
	 * @param pivotParameters pivot parameters
	 * @param joins           the database joins
	 */
	public ModelBasedExcelPivotExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel,
			SortOrder[] sortOrders, Filter filter, String title,
			Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, PivotParameters pivotParameters,
			FetchJoinInformation... joins) {
		super(service, entityModel, ExportMode.ONLY_VISIBLE_IN_GRID, sortOrders, filter, title, customGenerator, joins);
		this.pivotParameters = pivotParameters;
	}

	/**
	 * Adds headers for the aggregate rows
	 * 
	 * @param sheet       the sheet to which to add the aggregates
	 * @param resize      whether to resize the columns
	 * @param titleRow    the title row
	 * @param subtitleRow the sub title row
	 * @param index       the column index
	 */
	private void addAggregateHeaders(Sheet sheet, boolean resize, Row titleRow, Row subtitleRow, int index) {
		List<String> allProps = pivotParameters.getShownAndHiddenProperties();

		for (String aggregateProp : allProps) {
			PivotAggregationType type = pivotParameters.getAggregationMap().get(aggregateProp);
			if (type != null) {
				Cell cell = titleRow.createCell(index);
				if (!resize) {
					sheet.setColumnWidth(index, FIXED_COLUMN_WIDTH);
				}

				// add the totals description
				cell.setCellStyle(getGenerator().getHeaderStyle(index));
				String value = getAggregateHeader(type);
				if (value != null) {
					cell.setCellValue(value);
				}

				// add the pivot property description
				Cell subtitleCell = subtitleRow.createCell(index);
				subtitleCell.setCellStyle(getGenerator().getHeaderStyle(index));
				subtitleCell.setCellValue(pivotParameters.getSubHeaderMapper().apply("", aggregateProp));
				index++;
			}
		}
	}

	/**
	 * Adds fixed column headers
	 * 
	 * @param sheet    the sheet to add the headers to
	 * @param resize   whether to resize the columns
	 * @param titleRow the title row to which to add the header
	 * @param i the row index
	 * @return
	 */
	private int addFixedColumnHeaders(Sheet sheet, boolean resize, Row titleRow, int i) {
		for (String fc : pivotParameters.getFixedColumnKeys()) {
			Cell cell = titleRow.createCell(i);
			if (!resize) {
				sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
			}
			cell.setCellStyle(getGenerator().getHeaderStyle(i));
			cell.setCellValue(pivotParameters.getFixedHeaderMapper().apply(fc));
			i++;
		}
		return i;
	}

	/**
	 * Adds the fixed columns at the start of a row
	 * @param row the row 
	 * @param entity the entity currently being processed
	 */
	private void addFixedColumns(Row row, T entity) {
		int j = 0;
		for (String fc : pivotParameters.getFixedColumnKeys()) {
			Cell cell = row.createCell(j);
			Object value = ClassUtils.getFieldValueAsString(entity, fc);
			cell.setCellStyle(getGenerator().getCellStyle(j, entity, value, null));
			writeCellValue(cell, value, getEntityModel(), null, false);
			j++;
		}
	}

	/**
	 * Adds grand totals (totals of totals) at the very bottom right of the sheet
	 * 
	 * @param nrOfFixedCols the number of fixed columns
	 * @param sheet         the sheet to which to add the columns
	 * @param totalsRow
	 */
	private void addGrandTotals(int nrOfFixedCols, Sheet sheet, Row totalsRow) {
		if (totalsRow != null) {
			int colIndex = nrOfFixedCols + pivotParameters.getTotalNumberOfVariableColumns();

			for (String prop : pivotParameters.getShownAndHiddenProperties()) {
				PivotAggregationType pType = pivotParameters.getAggregationMap().get(prop);
				if (pType != null) {
					Cell totalsCell = totalsRow.createCell(colIndex);
					Class<?> clazz = pivotParameters.getAggregationClassMap().get(prop);
					totalsCell.setCellStyle(getGenerator().getTotalsStyle(clazz, null));

					String col = CellReference.convertNumToColString(colIndex);

					int firstRow = FIRST_DATA_ROW;
					int lastRow = sheet.getLastRowNum();
					totalsCell
							.setCellFormula(toExcelFunction(pType) + "(" + col + firstRow + ":" + col + lastRow + ")");
					colIndex++;
				}
			}
		}
	}

	/**
	 * Adds a cell at the bottom of a sheet that serves as the header for the aggregations row
	 * @param nrOfFixedCols the number of fixed columns
	 * @param sheet the sheet to which to add the cells
	 * @param type
	 * @param totalsRow
	 */
	private void addJoinedTotalsCell(int nrOfFixedCols, Sheet sheet, PivotAggregationType type, Row totalsRow) {
		for (int t = 0; t < nrOfFixedCols; t++) {
			Cell joinedCell = totalsRow.createCell(t);
			joinedCell.setCellStyle(getGenerator().getCellStyle(t, null, null, null));
			if (t == 0) {
				joinedCell.setCellValue(getAggregateHeader(type));
			}
		}
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, nrOfFixedCols - 1));
	}

	private int addPivotColumnHeader(Sheet sheet, boolean resize, Row titleRow, int nrOfPivotProps, int i) {
		for (Object fc : pivotParameters.getPossibleColumnKeys()) {
			int mergeStart = i;
			for (String property : pivotParameters.getPivotedProperties()) {
				Cell cell = titleRow.createCell(i);
				if (!resize) {
					sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
				}

				cell.setCellStyle(getGenerator().getHeaderStyle(i));
				String value = pivotParameters.getHeaderMapper().apply(fc, property);
				if (value != null) {
					cell.setCellValue(value);
				}
				i++;
			}

			if (nrOfPivotProps > 1) {
				sheet.addMergedRegion(
						new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), mergeStart, i - 1));
			}
		}
		return i;
	}

	private int addPivotColumnSubHeader(Sheet sheet, boolean resize, Row subtitleRow, int i) {
		for (Object fc : pivotParameters.getPossibleColumnKeys()) {
			for (String property : pivotParameters.getPivotedProperties()) {
				Cell cell = subtitleRow.createCell(i);
				if (!resize) {
					sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
				}

				cell.setCellStyle(getGenerator().getHeaderStyle(i));
				String value = pivotParameters.getSubHeaderMapper().apply(fc, property);
				if (value != null) {
					cell.setCellValue(value);
				}
				i++;
			}
		}
		return i;
	}

	/**
	 * Checks whether the value of the column key matches the expected value
	 * 
	 * @param entity   the entity to check for the actual value
	 * @param expected the expected value
	 * @return
	 */
	private boolean columnValueMatches(T entity, Object expected) {
		Object actual = ClassUtils.getFieldValue(entity, pivotParameters.getColumnKeyProperty());
		return Objects.equals(actual, expected);
	}

	@Override
	protected byte[] generate(DataSetIterator<ID, T> iterator) throws IOException {
		setWorkbook(createWorkbook(iterator.size()));
		Sheet sheet = getWorkbook().createSheet(getTitle());
		setGenerator(createGenerator(getWorkbook()));

		boolean resize = canResize();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		// add header row
		Row titleRow = sheet.createRow(0);
		Row subtitleRow = sheet.createRow(1);

		titleRow.setHeightInPoints(TITLE_ROW_HEIGHT);

		int nrOfPivotProps = pivotParameters.getPivotedProperties().size();
		int nrOfFixedCols = pivotParameters.getFixedColumnKeys().size();

		// add fixed columns
		int i = 0;
		i = addFixedColumnHeaders(sheet, resize, titleRow, i);

		// add variable columns
		int startIndex = i;

		i = addPivotColumnHeader(sheet, resize, titleRow, nrOfPivotProps, i);

		i = startIndex;
		i = addPivotColumnSubHeader(sheet, resize, subtitleRow, i);

		addAggregateHeaders(sheet, resize, titleRow, subtitleRow, i);

		String prevRowKey = null;
		Row row = null;
		int colIndex = 0;
		int propIndex = 0;
		int colsAdded = 0;
		boolean match = true;

		Map<String, BigDecimal> rowTotals = new HashMap<>();

		// iterate over the rows
		T entity = iterator.next();
		while (entity != null) {

			String rowKey = ClassUtils.getFieldValueAsString(entity, pivotParameters.getRowKeyProperty());
			if (!Objects.equals(prevRowKey, rowKey)) {

				if (row != null) {
					writeRowAggregates(nrOfFixedCols, row, rowTotals);
				}

				row = sheet.createRow(sheet.getLastRowNum() + 1);
				addFixedColumns(row, entity);

				// reset totals
				colIndex = 0;
				propIndex = 0;
				colsAdded = 0;
				rowTotals.clear();
			}

			Object pivotColumnKey = pivotParameters.getPossibleColumnKeys().get(colIndex);
			if (!columnValueMatches(entity, pivotColumnKey)) {
				// appropriate value is missing, write empty cell
				match = false;
				createCell(row, nrOfFixedCols + colsAdded, entity, "", null, pivotColumnKey);
			} else {
				match = true;
				writeActualCellValue(nrOfFixedCols, row, propIndex, colsAdded, rowTotals, entity, pivotColumnKey);
			}

			if (propIndex == pivotParameters.getPivotedProperties().size() - 1) {
				updateHiddenPropertyAggregates(rowTotals, entity);
				propIndex = 0;
				colIndex = colIndex + 1;
				if (match) {
					entity = iterator.next();
				}
			} else {
				propIndex++;
			}
			colsAdded++;

			prevRowKey = rowKey;
		}

		writeRowAggregates(nrOfFixedCols, row, rowTotals);

		// add an aggregation row at the bottom
		if (pivotParameters.isIncludeAggregateRow()) {
			writeColumnsAggregate(nrOfFixedCols, sheet);
		}

		resizeColumns(sheet);

		getWorkbook().write(stream);
		return stream.toByteArray();
	}

	private String getAggregateHeader(PivotAggregationType type) {
		switch (type) {
		case SUM:
			return messageService.getMessage("ocs.sum", VaadinUtils.getLocale());
		case AVERAGE:
			return messageService.getMessage("ocs.average", VaadinUtils.getLocale());
		default:
			return messageService.getMessage("ocs.count", VaadinUtils.getLocale());
		}
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		} else if (value instanceof Integer) {
			return BigDecimal.valueOf(((Integer) value).longValue());
		}
		return BigDecimal.ZERO;
	}

	/**
	 * Converts an aggregation type to an Excel function
	 * 
	 * @param type
	 * @return
	 */
	private String toExcelFunction(PivotAggregationType type) {
		switch (type) {
		case AVERAGE:
			return "AVG";
		case SUM:
			return "SUM";
		default:
			return "COUNT";
		}
	}

	private void updateHiddenPropertyAggregates(Map<String, BigDecimal> rowTotals, T entity) {
		// add aggregates for hidden props
		for (String hiddenProp : pivotParameters.getHiddenPivotedProperties()) {
			Object value = ClassUtils.getFieldValue(entity, hiddenProp);

			PivotAggregationType type = pivotParameters.getAggregationMap().get(hiddenProp);
			if (type != null) {
				rowTotals.putIfAbsent(hiddenProp, BigDecimal.ZERO);
				rowTotals.put(hiddenProp, rowTotals.get(hiddenProp).add(toBigDecimal(value)));
			}
		}
	}

	/**
	 * Writes an actual cell value to the sheet
	 * 
	 * @param nrOfFixedCols  the number of fixed columns
	 * @param row            the row to which to add the cell
	 * @param propIndex      the index of the pivot property
	 * @param colsAdded      the number of columns added so far
	 * @param rowTotals      the running totals for the row
	 * @param entity         the current entity
	 * @param pivotColumnKey the pivot column key
	 */
	private void writeActualCellValue(int nrOfFixedCols, Row row, int propIndex, int colsAdded,
			Map<String, BigDecimal> rowTotals, T entity, Object pivotColumnKey) {
		String prop = pivotParameters.getPivotedProperties().get(propIndex);
		Object value = ClassUtils.getFieldValue(entity, prop);
		Cell cell = createCell(row, nrOfFixedCols + colsAdded, entity, value, null, pivotColumnKey);

		boolean forcePercentage = false;
		if (cell.getCellStyle() != null && cell.getCellStyle().getDataFormatString().contains("%")) {
			forcePercentage = true;
		}
		writeCellValue(cell, value, getEntityModel(), null, forcePercentage);

		PivotAggregationType type = pivotParameters.getAggregationMap().get(prop);
		if (type != null) {
			rowTotals.putIfAbsent(prop, BigDecimal.ZERO);
			rowTotals.put(prop, rowTotals.get(prop).add(toBigDecimal(value)));
		}
	}

	/**
	 * Writes the aggregates per column to the sheet
	 * 
	 * @param nrOfFixedCols the number of fixed columns
	 * @param sheet         the sheet to which to write the values
	 */
	private void writeColumnsAggregate(int nrOfFixedCols, Sheet sheet) {
		PivotAggregationType type = null;
		Class<?> clazz = null;

		Row totalsRow = sheet.createRow(sheet.getLastRowNum() + 1);
		addJoinedTotalsCell(nrOfFixedCols, sheet, PivotAggregationType.SUM, totalsRow);

		for (int columnKeyIndex = 0; columnKeyIndex < pivotParameters.getPossibleColumnKeys()
				.size(); columnKeyIndex++) {
			int propIndex = 0;
			for (String prop : pivotParameters.getPivotedProperties()) {
				int colIndex = nrOfFixedCols + (pivotParameters.getPivotedProperties().size() * columnKeyIndex)
						+ propIndex;

				type = pivotParameters.getAggregationMap().get(prop);

				if (type != null) {
					// an actual cell with a formula
					clazz = pivotParameters.getAggregationClassMap().get(prop);
					Cell totalsCell = totalsRow.createCell(colIndex);

					String col = CellReference.convertNumToColString(colIndex);
					totalsCell.setCellStyle(getGenerator().getTotalsStyle(clazz, null));

					int firstRow = FIRST_DATA_ROW;
					int lastRow = sheet.getLastRowNum();
					totalsCell.setCellFormula(toExcelFunction(type) + "(" + col + firstRow + ":" + col + lastRow + ")");
				} else {
					// just a blank cell
					Cell totalsCell = totalsRow.createCell(colIndex);
					totalsCell.setCellStyle(getGenerator().getTotalsStyle(Integer.class, null));
				}
				propIndex++;
			}
		}

		addGrandTotals(nrOfFixedCols, sheet, totalsRow);
	}

	/**
	 * Writes a cell containing an aggregation total at the end of a rwow
	 * 
	 * @param row              the current row
	 * @param type             the aggregation type
	 * @param aggregateClass   the class of the aggregate object
	 * @param nrOfVariableCols the number of variable columns
	 * @param nrOfFixedCols    the number of fixed columns
	 */
	private void writeRowAggregate(Row row, String property, PivotAggregationType type, Class<?> aggregateClass,
			int nrOfVariableCols, Map<String, BigDecimal> rowTotals, int nrOfFixedCols, int aggregateIndex) {

		int ci = nrOfFixedCols + nrOfVariableCols + aggregateIndex;
		Cell totalsCell = row.createCell(ci);

		double rowTotal = rowTotals.get(property).doubleValue();
		double cellValue = 0;
		switch (type) {
		case SUM:
			cellValue = rowTotal;
			break;
		case COUNT:
			cellValue = nrOfFixedCols;
			break;
		case AVERAGE:
			cellValue = nrOfFixedCols == 0 ? 0 : rowTotal / nrOfFixedCols;
			break;
		}

		totalsCell.setCellStyle(getGenerator().getTotalsStyle(aggregateClass, null));
		totalsCell.setCellValue(cellValue);
	}

	/**
	 * Writes the aggregate values for a row at the end of the row
	 * 
	 * @param nrOfFixedCols the number of fixed columns
	 * @param row           the row
	 * @param rowTotals     the calculated totals for the row
	 */
	private void writeRowAggregates(int nrOfFixedCols, Row row, Map<String, BigDecimal> rowTotals) {
		int aggregateIndex = 0;
		int variableCols = pivotParameters.getTotalNumberOfVariableColumns();

		for (String prop : pivotParameters.getShownAndHiddenProperties()) {
			PivotAggregationType type = pivotParameters.getAggregationMap().get(prop);
			Class<?> clazz = pivotParameters.getAggregationClassMap().get(prop);
			if (type != null) {
				writeRowAggregate(row, prop, type, clazz, variableCols, rowTotals, nrOfFixedCols, aggregateIndex);
				aggregateIndex++;
			}
		}
	}
}
