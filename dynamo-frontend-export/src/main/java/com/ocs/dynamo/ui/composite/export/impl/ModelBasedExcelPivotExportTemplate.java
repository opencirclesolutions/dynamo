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
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
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
import com.ocs.dynamo.utils.MathUtils;

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

	private PivotParameters pivotParameters;

	private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

	/**
	 * Constructor
	 * 
	 * @param service
	 * @param entityModel
	 * @param exportMode
	 * @param sortOrders
	 * @param filter
	 * @param title
	 * @param customGenerator
	 * @param pivotParameters
	 * @param joins
	 */
	public ModelBasedExcelPivotExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel,
			SortOrder[] sortOrders, Filter filter, String title,
			Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, PivotParameters pivotParameters,
			FetchJoinInformation... joins) {
		super(service, entityModel, ExportMode.ONLY_VISIBLE_IN_GRID, sortOrders, filter, title, customGenerator, joins);
		this.pivotParameters = pivotParameters;
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
		titleRow.setHeightInPoints(TITLE_ROW_HEIGHT);

		int nrOfPivotProps = pivotParameters.getPivotedProperties().size();
		int nrOfFixedCols = pivotParameters.getFixedColumnKeys().size();

		// add fixed columns
		int i = 0;
		for (String fc : pivotParameters.getFixedColumnKeys()) {
			Cell cell = titleRow.createCell(i);
			if (!resize) {
				sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
			}

			cell.setCellStyle(getGenerator().getHeaderStyle(i));
			cell.setCellValue(pivotParameters.getFixedHeaderMapper().apply(fc));
			i++;
		}

		// add variable columns
		for (Object fc : pivotParameters.getPossibleColumnKeys()) {
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
		}

		// add aggregate column headers
		if (nrOfPivotProps == 1) {
			String prop = pivotParameters.getPivotedProperties().get(0);
			PivotAggregationType type = pivotParameters.getAggregationMap().get(prop);

			if (type != null) {
				Cell cell = titleRow.createCell(i);
				if (!resize) {
					sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
				}
				cell.setCellStyle(getGenerator().getHeaderStyle(i));
				String value = getAggregateHeader(type);
				if (value != null) {
					cell.setCellValue(value);
				}
				i++;
			}
		}

		String prevRowKey = null;
		Row row = null;
		int colIndex = 0;
		int propIndex = 0;
		int colsAdded = 0;

		// iterate over the rows
		T entity = iterator.next();
		while (entity != null) {

			String rowKey = ClassUtils.getFieldValueAsString(entity, pivotParameters.getRowKeyProperty());
			if (!Objects.equals(prevRowKey, rowKey)) {

				// add aggregate columns
				if (row != null) {
					int lastColAdded = nrOfFixedCols + colsAdded - 1;
					if (nrOfPivotProps == 1) {
						String prop = pivotParameters.getPivotedProperties().get(0);
						PivotAggregationType type = pivotParameters.getAggregationMap().get(prop);
						if (type != null) {
							writeRowAggregate(row, type, lastColAdded, nrOfFixedCols, colsAdded);
							colsAdded++;
						}
					}
				}

				row = sheet.createRow(sheet.getLastRowNum() + 1);

				int j = 0;
				for (String fc : pivotParameters.getFixedColumnKeys()) {
					Cell cell = row.createCell(j);
					Object value = ClassUtils.getFieldValueAsString(entity, fc);
					cell.setCellStyle(getGenerator().getCellStyle(j, entity, value, null));
					writeCellValue(cell, value, getEntityModel(), null);
					j++;
				}

				colIndex = 0;
				propIndex = 0;
				colsAdded = 0;
			}

			Object pivotColumnKey = pivotParameters.getPossibleColumnKeys().get(colIndex);
			if (!columnValueMatches(entity, pivotColumnKey)) {
				// appropriate value is missing, write empty cell
				createCell(row, nrOfFixedCols + colsAdded, entity, "", null, null);
			} else {
				// get cell value

				String prop = pivotParameters.getPivotedProperties().get(propIndex);
				Object value = ClassUtils.getFieldValue(entity, prop);
				Cell cell = createCell(row, nrOfFixedCols + colsAdded, entity, value, null, pivotColumnKey);

				// correct for percentage values
				if (cell.getCellStyle() != null && cell.getCellStyle().getDataFormatString().contains("%")) {
					if (value instanceof BigDecimal) {
						value = ((BigDecimal) value).divide(MathUtils.HUNDRED, 2, RoundingMode.HALF_UP);
					}
				}

				writeCellValue(cell, value, getEntityModel(), null);
			}

			if (propIndex == pivotParameters.getPivotedProperties().size() - 1) {
				propIndex = 0;
				colIndex = colIndex + 1;
			} else {
				propIndex++;
			}
			colsAdded++;

			entity = iterator.next();
			prevRowKey = rowKey;
		}

		if (nrOfPivotProps == 1) {
			int lastColAdded = nrOfFixedCols + colsAdded - 1;
			String prop = pivotParameters.getPivotedProperties().get(0);
			PivotAggregationType type = pivotParameters.getAggregationMap().get(prop);
			if (type != null) {
				writeRowAggregate(row, type, lastColAdded, nrOfFixedCols, colsAdded);
				colsAdded++;
			}
		}

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

	private void writeColumnsAggregate(int nrOfFixedCols, Sheet sheet) {
		PivotAggregationType type = null;
		// add an aggregation row at the bottom
		Row totalsRow = null;
		for (int s = 0; s < pivotParameters.getPossibleColumnKeys().size(); s++) {
			if (pivotParameters.getPivotedProperties().size() == 1) {
				String prop = pivotParameters.getPivotedProperties().get(0);
				type = pivotParameters.getAggregationMap().get(prop);
				if (type != null) {

					int ci = nrOfFixedCols + s;

					if (totalsRow == null) {
						totalsRow = sheet.createRow(sheet.getLastRowNum() + 1);
					}

					// add a "totals" cell at the front of the row
					if (ci == nrOfFixedCols) {

						for (int t = 0; t < nrOfFixedCols; t++) {
							Cell joinedCell = totalsRow.createCell(t);
							joinedCell.setCellStyle(getGenerator().getCellStyle(ci, null, null, null));
							if (t == 0) {
								joinedCell.setCellValue(getAggregateHeader(type));
							}
						}
						sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0,
								nrOfFixedCols - 1));
					}

					Cell totalsCell = totalsRow.createCell(ci);

					String col = CellReference.convertNumToColString(ci);
					totalsCell.setCellStyle(getGenerator().getCellStyle(ci, null, null, null));
					totalsCell.setCellType(CellType.FORMULA);
					totalsCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);

					int firstRow = 1;
					int lastRow = sheet.getLastRowNum();
					totalsCell.setCellFormula(toExcelFunction(type) + "(" + col + firstRow + ":" + col + lastRow + ")");
				}
			}
		}

		// add a grand total at bottom right
		if (totalsRow != null) {
			int ci = nrOfFixedCols + pivotParameters.getPossibleColumnKeys().size();

			Cell totalsCell = totalsRow.createCell(ci);
			totalsCell.setCellStyle(getGenerator().getCellStyle(ci, null, null, null));
			totalsCell.setCellType(CellType.FORMULA);
			totalsCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);
			String firstCol = CellReference.convertNumToColString(nrOfFixedCols);
			String lastCol = CellReference.convertNumToColString(ci - 1);
			int rowNum = sheet.getLastRowNum() + 1;

			totalsCell.setCellFormula(toExcelFunction(type) + "(" + firstCol + rowNum + ":" + lastCol + rowNum + ")");
		}
	}

	/**
	 * Writes a cell containing an aggregation total at the end of a rwow
	 * 
	 * @param entity        the entity that is being processed
	 * @param row           the current row
	 * @param type          the aggregation type
	 * @param lastColAdded  the index of the last column that was added
	 * @param nrOfFixedCols the number of fixed columns
	 * @param colsAdded     the number of (variable) columns added so far
	 */
	private void writeRowAggregate(Row row, PivotAggregationType type, int lastColAdded, int nrOfFixedCols,
			int colsAdded) {
		int ci = nrOfFixedCols + colsAdded;
		Cell totalsCell = row.createCell(ci);

		String firstCol = CellReference.convertNumToColString(nrOfFixedCols);
		String lastCol = CellReference.convertNumToColString(lastColAdded);

		totalsCell.setCellStyle(getGenerator().getCellStyle(ci, null, null, null));
		totalsCell.setCellType(CellType.FORMULA);
		totalsCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);

		int rn = row.getRowNum() + 1;
		totalsCell.setCellFormula(toExcelFunction(type) + "(" + firstCol + rn + ":" + lastCol + rn + ")");
	}
}
