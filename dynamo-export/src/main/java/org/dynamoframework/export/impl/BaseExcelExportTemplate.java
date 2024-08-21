package org.dynamoframework.export.impl;

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

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.configuration.DynamoPropertiesHolder;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.export.CustomXlsStyleGenerator;
import org.dynamoframework.export.XlsStyleGenerator;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.Filter;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.utils.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Base class for Excel exports based on the entity model
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 * @author BasRutten
 */
public abstract class BaseExcelExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseExportTemplate<ID, T> {

    protected static final int FIXED_COLUMN_WIDTH = 20 * 256;

    protected static final int TITLE_ROW_HEIGHT = 40;

    @Getter
    @Setter
    private XlsStyleGenerator<ID, T> generator;

    @Getter
    @Setter
    private CustomXlsStyleGenerator<ID, T> customGenerator;

    @Getter
    @Setter
    private Workbook workbook;

    /**
     * Constructor
     *
     * @param service         the database service
     * @param entityModel     the entity model of the entity to export
     * @param exportMode      the export mode
     * @param sortOrders      the sorting orders
     * @param filter          the search filter to apply
     * @param title           the title
     * @param customGenerator the custom style generator
     * @param locale          locale
     * @param joins           the joins
     */
    protected BaseExcelExportTemplate(DynamoProperties dynamoProperties, BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
                                      List<SortOrder> sortOrders, Filter filter, String title,
                                      Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator,
                                      Locale locale, FetchJoinInformation... joins) {
        super(dynamoProperties, service, entityModel, exportMode, sortOrders, filter, title, joins);
        LocaleUtil.setUserLocale(locale);
        this.customGenerator = customGenerator == null ? null : customGenerator.get();
    }

    /**
     * Indicates whether it is possible to resize the columns
     *
     * @return whether resizing is possible
     */
    protected boolean canResize() {
        return !(getWorkbook() instanceof SXSSFWorkbook);
    }

    /**
     * Creates the style generator
     *
     * @param workbook the work book that is being created
     * @return the created generator
     */
    protected XlsStyleGenerator<ID, T> createGenerator(Workbook workbook) {
        BaseXlsStyleGenerator baseXlsStyleGenerator = new BaseXlsStyleGenerator<>(getDynamoProperties(), workbook);
        return baseXlsStyleGenerator;
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
     * @param pivotColumnKey the column key (for pivot export)
     * @return the created cell
     */
    protected Cell createCell(Row row, int colIndex, T entity, Object value, AttributeModel attributeModel,
                              Object pivotColumnKey) {
        Cell cell = row.createCell(colIndex);
        cell.setCellStyle(getGenerator().getCellStyle(colIndex, entity, value, attributeModel));
        if (customGenerator != null) {
            // override default style with custom style
            CellStyle custom = customGenerator.getCustomCellStyle(workbook, entity, value, attributeModel,
                    pivotColumnKey);
            if (custom != null) {
                cell.setCellStyle(custom);
            }
        }
        return cell;
    }

    /**
     * Creates a workbook. This will use a different type of work book depending on
     * the size of the export set
     *
     * @param size the size of the export
     * @return the workbook
     */
    protected Workbook createWorkbook(int size) {
        if (size > getDynamoProperties().getCsv().getMaxRowsBeforeStreaming()) {
            return new SXSSFWorkbook();
        }
        return new XSSFWorkbook();
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

    protected void writeCellValue(Cell cell, Object value, AttributeModel am,
                                  boolean forcePercentage) {
        if (NumberUtils.isInteger(value) || NumberUtils.isLong(value)) {
            // integer or long numbers
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date date) {
            cell.setCellValue(date);
        } else if (value instanceof LocalDate date) {
            cell.setCellValue(DateUtils.toLegacyDate(date));
        } else if (value instanceof LocalDateTime ldt) {
            cell.setCellValue(DateUtils.toLegacyDate(ldt));
        } else if (value instanceof BigDecimal || NumberUtils.isDouble(value)) {
            writeDecimalCellValue(cell, value, am, forcePercentage);
        } else if (am != null) {
            // use the attribute model
            String str = FormatUtils.formatPropertyValue(am, value, ", ",
                    LocaleUtil.getUserLocale());
            cell.setCellValue(str);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * Writes a cell value for a decimal value
     *
     * @param cell            the cell to which to write the value
     * @param value           the value
     * @param am              the attribute model for the property
     * @param forcePercentage whether to force the value to a percentage
     */
    private void writeDecimalCellValue(Cell cell, Object value, AttributeModel am, boolean forcePercentage) {
        if (value instanceof Double aDouble) {
            value = BigDecimal.valueOf(aDouble);
        }
        boolean isPercentage = (am != null && am.isPercentage()) || forcePercentage;

        int defaultPrecision = DynamoPropertiesHolder.getDynamoProperties().getDefaults().getDecimalPrecision();
        int precision = (am == null ? defaultPrecision : am.getPrecision()) + 2;
        if (isPercentage) {
            // percentages in the application are just numbers,
            // but in Excel they are fractions that
            // are displayed as percentages -> so, divide by 100
            double temp = ((BigDecimal) value)
                    .divide(MathUtils.HUNDRED, 10, RoundingMode.HALF_UP)
                    .setScale(precision, RoundingMode.HALF_UP).doubleValue();
            cell.setCellValue(temp);
        } else {
            cell.setCellValue(((BigDecimal) value)
                    .setScale(am == null ? defaultPrecision : am.getPrecision(), RoundingMode.HALF_UP).doubleValue());
        }
    }

}
