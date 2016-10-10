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

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import com.ocs.dynamo.domain.AbstractEntity;

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

	private CellStyle bigDecimalPercentageStyle;

	private CellStyle bigDecimalStyle;

	private CellStyle headerStyle;

	private CellStyle integerStyle;

	private CellStyle normal;

	private CellStyle titleStyle;

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
		integerStyle = workbook.createCellStyle();
		integerStyle.setAlignment(CellStyle.ALIGN_RIGHT);
		setBorder(integerStyle, CellStyle.BORDER_THIN);
		integerStyle.setDataFormat(format.getFormat("#,#"));

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
	public CellStyle getCellStyle(int index, T entity, Object value) {
		if (value instanceof Integer) {
			return integerStyle;
		}
		if (value instanceof BigDecimal) {
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
		if (index == 0) {
			return titleStyle;
		}
		if (headerStyle == null) {
			return headerStyle;
		}
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

}
