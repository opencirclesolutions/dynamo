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
package com.ocs.dynamo.export.impl;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeDateType;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.export.XlsStyleGenerator;
import com.ocs.dynamo.utils.NumberUtils;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base style generator for Excel exports
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 * @author bas.rutten
 */
public class BaseXlsStyleGenerator<ID extends Serializable, T extends AbstractEntity<ID>>
        implements XlsStyleGenerator<ID, T> {

    private final Workbook workbook;

    private final boolean thousandsGrouping;

    private final CellStyle percentageStyle;

    private final CellStyle fractionalStyle;

    private CellStyle headerStyle;

    private final CellStyle numberStyle;

    private final CellStyle numberSimpleStyle;

    private final CellStyle normal;

    private final CellStyle dateStyle;

    private final CellStyle dateTimeStyle;

    private final CellStyle timeStyle;

    private final ConcurrentMap<String, CellStyle> currencyStyles = new ConcurrentHashMap<>();

    /**
     * Constructor
     *
     * @param workbook the Workbook to apply the styles to
     */
    public BaseXlsStyleGenerator(Workbook workbook) {
        this.workbook = workbook;
        DataFormat format = workbook.createDataFormat();

        thousandsGrouping = SystemPropertyUtils.useXlsThousandsGrouping();

        // create the cell styles only once - this is a huge performance
        // gain!
        numberStyle = workbook.createCellStyle();
        numberStyle.setAlignment(HorizontalAlignment.RIGHT);
        setBorder(numberStyle, BorderStyle.THIN);
        numberStyle.setDataFormat(format.getFormat("#,#"));

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

        createTitleStyle(workbook);
        createHeaderStyle(workbook);

        dateStyle = workbook.createCellStyle();
        setBorder(dateStyle, BorderStyle.THIN);
        dateStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultDateFormat()));

        dateTimeStyle = workbook.createCellStyle();
        setBorder(dateTimeStyle, BorderStyle.THIN);
        dateTimeStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultDateTimeFormat()));

        timeStyle = workbook.createCellStyle();
        setBorder(timeStyle, BorderStyle.THIN);
        timeStyle.setDataFormat(format.getFormat(SystemPropertyUtils.getDefaultTimeFormat()));
    }

    /**
     * Returns the style used in the header row
     *
     * @param index the column index
     * @return the selected cell style
     */
    @Override
    public CellStyle getHeaderStyle(int index) {
        return headerStyle;
    }

    private void createTitleStyle(Workbook workbook) {
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 18);
        titleFont.setBold(true);
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFont(titleFont);
    }

    private void createHeaderStyle(Workbook workbook) {
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
    }

    @Override
    public CellStyle getCellStyle(int index, T entity, Object value, AttributeModel attributeModel) {
        if (value instanceof Integer || value instanceof Long) {
            return thousandsGrouping ? numberStyle : numberSimpleStyle;
        } else if (value instanceof Date || value instanceof LocalDate || value instanceof LocalDateTime) {
            return getDateStyle(attributeModel);
        } else if (value instanceof BigDecimal || NumberUtils.isDouble(value)) {
            return getDecimalStyle(attributeModel, attributeModel.getCurrencyCode());
        }
        return normal;
    }

    private CellStyle getDecimalStyle(AttributeModel attributeModel, String currencyCode) {
        if (attributeModel != null && attributeModel.isPercentage()) {
            return percentageStyle;
        } else if (attributeModel != null && !StringUtils.isEmpty(attributeModel.getCurrencyCode())) {
            initCurrencyStyle(currencyCode);
            return currencyStyles.get(currencyCode);
        }
        return fractionalStyle;
    }

    private CellStyle getDateStyle(AttributeModel attributeModel) {
        if (attributeModel == null || AttributeDateType.DATE.equals(attributeModel.getDateType())) {
            return dateStyle;
        } else if (AttributeDateType.LOCAL_DATE_TIME.equals(attributeModel.getDateType()) ||
                AttributeDateType.INSTANT.equals(attributeModel.getDateType())) {
            return dateTimeStyle;
        } else {
            return timeStyle;
        }
    }

    private void initCurrencyStyle(String currencyCode) {
        if (!currencyStyles.containsKey(currencyCode)) {
            DataFormat format = workbook.createDataFormat();
            CellStyle currencyStyle = workbook.createCellStyle();
            setBorder(currencyStyle, BorderStyle.THIN);
            currencyStyle.setDataFormat(format.getFormat(mapCurrencyCode(currencyCode) + " #,##0.00"));
            currencyStyles.put(currencyCode, currencyStyle);
        }
    }

    private String mapCurrencyCode(String currencyCode) {
      return Currency.getInstance(currencyCode).getSymbol();
    }

    @Override
    public CellStyle getTotalsStyle(Class<?> type, AttributeModel attributeModel) {
        if (NumberUtils.isInteger(type) || NumberUtils.isLong(type)) {
            return thousandsGrouping ? numberStyle : numberSimpleStyle;
        } else {
            if (attributeModel != null && attributeModel.isPercentage()) {
                return percentageStyle;
            } else if (attributeModel != null && !StringUtils.isEmpty(attributeModel.getCurrencyCode())) {
                initCurrencyStyle(attributeModel.getCurrencyCode());
                return currencyStyles.get(attributeModel.getCurrencyCode());
            }
            return fractionalStyle;
        }
    }

    /**
     * Sets a certain border for a cell style
     *
     * @param style       the cell style
     * @param borderStyle the border style
     */
    private void setBorder(CellStyle style, BorderStyle borderStyle) {
        style.setBorderBottom(borderStyle);
        style.setBorderTop(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
    }

}
