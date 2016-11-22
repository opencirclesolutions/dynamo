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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.utils.SystemPropertyUtils;

/**
 * Base style generator for Excel exports
 * 
 * @author bas.rutten
 *
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class BaseXlsStyleGenerator<ID extends Serializable, T extends AbstractEntity<ID>> implements
        XlsStyleGenerator<ID, T> {

	/**
	 * Whether to use thousands groupings for integer (and longs)
	 */
	private boolean thousandsGrouping;

	private CellStyle bigDecimalPercentageStyle;

	private CellStyle bigDecimalStyle;

	private CellStyle headerStyle;

	private CellStyle numberStyle;

	private CellStyle numberSimpleStyle;

	private CellStyle normal;

	private CellStyle titleStyle;

	private CellStyle dateStyle;

	private CellStyle dateTimeStyle;

	private CellStyle timeStyle;

	private CellStyle currencyStyle;

	private Workbook workbook;

	/**
	 * Constructor
	 * 
	 * @param workbook
	 */
	public BaseXlsStyleGenerator(Workbook workbook, boolean thousandsGrouping) {
		this.workbook = workbook;
		this.thousandsGrouping = thousandsGrouping;
		DataFormat format = workbook.createDataFormat();

		// create the cell styles only once- this is a huge performance
		// gain!
		numberStyle = workbook.createCellStyle();
		numberStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		setBorder(numberStyle, CellStyle.BORDER_THIN);
		numberStyle.setDataFormat(format.getFormat("#,#"));

		// number style without thousand separators
		numberSimpleStyle = workbook.createCellStyle();
		numberSimpleStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		setBorder(numberSimpleStyle, CellStyle.BORDER_THIN);
		numberSimpleStyle.setDataFormat(format.getFormat("#"));

		bigDecimalStyle = workbook.createCellStyle();
		bigDecimalStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		setBorder(bigDecimalStyle, CellStyle.BORDER_THIN);
		bigDecimalStyle.setDataFormat(format.getFormat("#,##0.00"));

		bigDecimalPercentageStyle = workbook.createCellStyle();
		bigDecimalPercentageStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		setBorder(bigDecimalPercentageStyle, CellStyle.BORDER_THIN);
		bigDecimalPercentageStyle.setDataFormat(format.getFormat("#,##0.00%"));

		normal = workbook.createCellStyle();
		normal.setAlignment(CellStyle.ALIGN_LEFT);
		setBorder(normal, CellStyle.BORDER_THIN);

		// title style (for top left cell)
		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleStyle = workbook.createCellStyle();
		titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
		titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		titleStyle.setFont(titleFont);

		// header style (for the rest of the first row)
		Font monthFont = workbook.createFont();
		monthFont.setFontHeightInPoints((short) 11);
		monthFont.setColor(IndexedColors.WHITE.getIndex());
		headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headerStyle.setFont(monthFont);
		headerStyle.setWrapText(true);

		dateStyle = workbook.createCellStyle();
		setBorder(dateStyle, CellStyle.BORDER_THIN);
		dateStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultDateFormat()));

		dateTimeStyle = workbook.createCellStyle();
		setBorder(dateTimeStyle, CellStyle.BORDER_THIN);
		dateTimeStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultDateTimeFormat()));

		timeStyle = workbook.createCellStyle();
		setBorder(timeStyle, CellStyle.BORDER_THIN);
		timeStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultTimeFormat()));

		currencyStyle = workbook.createCellStyle();
		setBorder(currencyStyle, CellStyle.BORDER_THIN);
		currencyStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultCurrencySymbol() + " #,##0.00"));
	}

	/**
	 * Returns the cell style for a certain cell
	 * 
	 * @param index
	 *            the column index
	 * @param entity
	 *            the entity
	 * @param value
	 *            the value
	 */
	@Override
	public CellStyle getCellStyle(int index, T entity, Object value, AttributeModel attributeModel) {
		if (value instanceof Integer || value instanceof Long) {
			return thousandsGrouping ? numberStyle : numberSimpleStyle;
		} else if (value instanceof Date) {
			if (attributeModel == null || !attributeModel.isWeek()) {
				if (attributeModel == null) {
					return dateStyle;
				} else if (AttributeDateType.TIMESTAMP.equals(attributeModel.getDateType())) {
					return dateTimeStyle;
				} else if (AttributeDateType.DATE.equals(attributeModel.getDateType())) {
					return dateStyle;
				} else if (AttributeDateType.TIME.equals(attributeModel.getDateType())) {
					return timeStyle;
				}
			}
		} else if (value instanceof BigDecimal) {
			if (attributeModel != null && attributeModel.isPercentage()) {
				return bigDecimalPercentageStyle;
			} else if (attributeModel != null && attributeModel.isCurrency()) {
				return currencyStyle;
			}
			return bigDecimalStyle;
		}
		return normal;
	}

	/**
	 * Returns the style used in the header row
	 * 
	 * @param index
	 *            the column index
	 * @return
	 */
	@Override
	public CellStyle getHeaderStyle(int index) {
		return headerStyle;
	}

	public Workbook getWorkbook() {
		return workbook;
	}

	/**
	 * Sets a certain border for a cell style
	 * 
	 * @param style
	 *            the cell style
	 * @param border
	 *            the border type
	 */
	private void setBorder(CellStyle style, short border) {
		style.setBorderBottom(border);
		style.setBorderTop(border);
		style.setBorderLeft(border);
		style.setBorderRight(border);
	}

	public boolean isThousandsGrouping() {
		return thousandsGrouping;
	}

}
