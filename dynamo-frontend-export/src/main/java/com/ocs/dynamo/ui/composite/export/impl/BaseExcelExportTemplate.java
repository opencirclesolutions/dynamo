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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.XlsStyleGenerator;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.FormatUtils;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.DateUtils;
import com.ocs.dynamo.utils.MathUtils;
import com.ocs.dynamo.utils.NumberUtils;

public abstract class BaseExcelExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
		extends BaseExportTemplate<ID, T> {

	private XlsStyleGenerator<ID, T> generator;

	private CustomXlsStyleGenerator<ID, T> customGenerator;

	/**
	 * The Excel workbook
	 */
	private Workbook workbook;

	public BaseExcelExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
			SortOrder[] sortOrders, Filter filter, String title, CustomXlsStyleGenerator<ID, T> customGenerator,
			FetchJoinInformation... joins) {
		super(service, entityModel, exportMode, sortOrders, filter, title, joins);
		this.customGenerator = customGenerator;
	}

	protected Workbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	/**
	 * Indicates whether it is possible to resize the columns
	 *
	 * @return
	 */
	protected boolean canResize() {
		return !(getWorkbook() instanceof SXSSFWorkbook);
	}

	/**
	 * Creates the style generator
	 *
	 * @param workbook the work book that is being created
	 * @return
	 */
	protected XlsStyleGenerator<ID, T> createGenerator(Workbook workbook) {
		return new BaseXlsStyleGenerator<>(workbook);
	}

	/**
	 * Creates an Excel cell and applies the correct style. The style to use depends
	 * on the attribute model and the value to display in the cell
	 *
	 * @param row            the row to which to add the cell
	 * @param colIndex       the column index of the cell
	 * @param entity         the entity that is represented in the row
	 * @param value          the cell value
	 * @param attributeModel the attribute model used to determine the style
	 * @return
	 */
	protected Cell createCell(Row row, int colIndex, T entity, Object value, AttributeModel attributeModel) {
		Cell cell = row.createCell(colIndex);
		cell.setCellStyle(getGenerator().getCellStyle(colIndex, entity, value, attributeModel));
		if (customGenerator != null) {
			// override default style with custom style
			CellStyle custom = customGenerator.getCustomCellStyle(workbook, entity, value, attributeModel);
			if (custom != null) {
				cell.setCellStyle(custom);
			}
		}
		return cell;
	}

	/*
	 * Creates an appropriate work book - if the size is below the threshold then a
	 * normal workbook is created. Otherwise a streaming workbook is created. This
	 * is much faster and more efficient, but you cannot auto resize the columns
	 *
	 * @param size the number of rows
	 * 
	 * @return
	 */
	protected Workbook createWorkbook(int size) {
		if (size > MAX_SIZE_BEFORE_STREAMING) {
			return new SXSSFWorkbook();
		}
		return new XSSFWorkbook();
	}

	public XlsStyleGenerator<ID, T> getGenerator() {
		return generator;
	}

	public void setGenerator(XlsStyleGenerator<ID, T> generator) {
		this.generator = generator;
	}

	/**
	 * Resizes all columns on a sheet if possible
	 *
	 * @param sheet the sheet
	 */
	protected void resizeColumns(Sheet sheet) {
		if (canResize()) {
			for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
				sheet.autoSizeColumn(i);
			}
		}
	}

	protected void writeCellValue(Cell cell, Object value, EntityModel<T> em, AttributeModel am) {
		if (NumberUtils.isInteger(value) || NumberUtils.isLong(value)) {
			// integer or long numbers
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Date && (am == null || !am.isWeek())) {
			cell.setCellValue((Date) value);
		} else if (value instanceof LocalDate) {
			cell.setCellValue(DateUtils.toLegacyDate((LocalDate) value));
		} else if (value instanceof LocalDateTime) {
			cell.setCellValue(DateUtils.toLegacyDate((LocalDateTime) value));
		} else if (value instanceof BigDecimal || NumberUtils.isDouble(value)) {
			if (value instanceof Double) {
				value = BigDecimal.valueOf((Double) value);
			}
			boolean isPercentage = am != null && am.isPercentage();
			int defaultPrecision = SystemPropertyUtils.getDefaultDecimalPrecision();
			if (isPercentage) {
				// percentages in the application are just numbers,
				// but in Excel they are fractions that
				// are displayed as percentages -> so, divide by 100
				double temp = ((BigDecimal) value)
						.divide(MathUtils.HUNDRED, DynamoConstants.INTERMEDIATE_PRECISION, RoundingMode.HALF_UP)
						.setScale(am.getPrecision() + defaultPrecision, RoundingMode.HALF_UP).doubleValue();
				cell.setCellValue(temp);
			} else {
				cell.setCellValue(((BigDecimal) value)
						.setScale(am == null ? defaultPrecision : am.getPrecision(), RoundingMode.HALF_UP)
						.doubleValue());
			}
		} else if (am != null) {
			// use the attribute model
			String str = FormatUtils.formatPropertyValue(getEntityModelFactory(), am, value, ", ");
			cell.setCellValue(str);
		} else if (value != null) {
			// fall back - just call toSTring
			cell.setCellValue(value.toString());
		}
	}

}
