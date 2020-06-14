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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.ui.composite.export.XlsStyleGenerator;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.NumberUtils;

/**
 * Base style generator for Excel exports
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public class BaseXlsStyleGenerator<ID extends Serializable, T extends AbstractEntity<ID>>
		implements XlsStyleGenerator<ID, T> {

	/**
	 * Whether to use thousands groupings for integer (and longs)
	 */
	private boolean thousandsGrouping;

	private CellStyle percentageStyle;

	/**
	 * Style for fractional numbers
	 */
	private CellStyle fractionalStyle;

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
	public BaseXlsStyleGenerator(Workbook workbook) {
		this.workbook = workbook;
		DataFormat format = workbook.createDataFormat();

		// create the cell styles only once- this is a huge performance
		// gain!
		numberStyle = workbook.createCellStyle();
		numberStyle.setAlignment(HorizontalAlignment.RIGHT);
		setBorder(numberStyle, BorderStyle.THIN);
		numberStyle.setDataFormat(format.getFormat("#,#"));

		// number style without thousand separators
		numberSimpleStyle = workbook.createCellStyle();
		numberSimpleStyle.setAlignment(HorizontalAlignment.RIGHT);
		setBorder(numberSimpleStyle, BorderStyle.THIN);
		numberSimpleStyle.setDataFormat(format.getFormat("#"));

		fractionalStyle = workbook.createCellStyle();
		fractionalStyle.setAlignment(HorizontalAlignment.RIGHT);
		setBorder(fractionalStyle, BorderStyle.THIN);
		fractionalStyle.setDataFormat(format.getFormat("#,##0.00"));

		percentageStyle = workbook.createCellStyle();
		percentageStyle.setAlignment(HorizontalAlignment.RIGHT);
		setBorder(percentageStyle, BorderStyle.THIN);
		percentageStyle.setDataFormat(format.getFormat("#,##0.00%"));

		normal = workbook.createCellStyle();
		normal.setAlignment(HorizontalAlignment.LEFT);
		setBorder(normal, BorderStyle.THIN);

		// title style (for top left cell)
		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 18);
		titleFont.setBold(true);
		titleStyle = workbook.createCellStyle();
		titleStyle.setAlignment(HorizontalAlignment.CENTER);
		titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		titleStyle.setFont(titleFont);

		// header style (for the rest of the first row)
		Font monthFont = workbook.createFont();
		monthFont.setFontHeightInPoints((short) 11);
		monthFont.setColor(IndexedColors.WHITE.getIndex());
		headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setFont(monthFont);
		headerStyle.setWrapText(true);

		dateStyle = workbook.createCellStyle();
		setBorder(dateStyle, BorderStyle.THIN);
		dateStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultDateFormat()));

		dateTimeStyle = workbook.createCellStyle();
		setBorder(dateTimeStyle, BorderStyle.THIN);
		dateTimeStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultDateTimeFormat()));

		timeStyle = workbook.createCellStyle();
		setBorder(timeStyle, BorderStyle.THIN);
		timeStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultTimeFormat()));

		currencyStyle = workbook.createCellStyle();
		setBorder(currencyStyle, BorderStyle.THIN);
		currencyStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultCurrencySymbol() + " #,##0.00"));
	}

	/**
	 * Returns the cell style for a certain cell
	 * 
	 * @param index  the column index
	 * @param entity the entity
	 * @param value  the value
	 */
	@Override
	public CellStyle getCellStyle(int index, T entity, Object value, AttributeModel attributeModel) {
		if (value instanceof Integer || value instanceof Long) {
			return thousandsGrouping ? numberStyle : numberSimpleStyle;
		} else if (value instanceof Date || value instanceof LocalDate || value instanceof LocalDateTime) {
			if (attributeModel == null || !attributeModel.isWeek()) {
				if (attributeModel == null || AttributeDateType.DATE.equals(attributeModel.getDateType())) {
					// date style is the default
					return dateStyle;
				} else if (AttributeDateType.TIMESTAMP.equals(attributeModel.getDateType())) {
					return dateTimeStyle;
				} else if (AttributeDateType.TIME.equals(attributeModel.getDateType())) {
					return timeStyle;
				}
			}
		} else if (value instanceof BigDecimal || NumberUtils.isDouble(value)) {
			if (attributeModel != null && attributeModel.isPercentage()) {
				return percentageStyle;
			} else if (attributeModel != null && attributeModel.isCurrency()) {
				return currencyStyle;
			}
			return fractionalStyle;
		}
		return normal;
	}

	/**
	 * Returns the style used in the header row
	 * 
	 * @param index the column index
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
	 * @param style  the cell style
	 * @param border the border type
	 */
	private void setBorder(CellStyle style, BorderStyle borderStyle) {
		style.setBorderBottom(borderStyle);
		style.setBorderTop(borderStyle);
		style.setBorderLeft(borderStyle);
		style.setBorderRight(borderStyle);
	}

	public boolean isThousandsGrouping() {
		return thousandsGrouping;
	}

}
