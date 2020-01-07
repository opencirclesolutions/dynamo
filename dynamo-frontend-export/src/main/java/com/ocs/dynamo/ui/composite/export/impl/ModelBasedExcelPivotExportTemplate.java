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
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.type.ExportMode;
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

    private PivotParameters pivotParameters;

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
    public ModelBasedExcelPivotExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, SortOrder[] sortOrders, Filter filter,
            String title, CustomXlsStyleGenerator<ID, T> customGenerator, PivotParameters pivotParameters, FetchJoinInformation... joins) {
        super(service, entityModel, ExportMode.ONLY_VISIBLE_IN_GRID, sortOrders, filter, title, customGenerator, joins);
        this.pivotParameters = pivotParameters;
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

        String prevRowKey = null;
        Row row = null;
        int colIndex = 0;
        int propIndex = 0;

        // iterate over the rows
        T entity = iterator.next();
        while (entity != null) {

            String rowKey = ClassUtils.getFieldValueAsString(entity, pivotParameters.getRowKeyProperty());
            if (!Objects.equals(prevRowKey, rowKey)) {
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
            }

            Object object = pivotParameters.getPossibleColumnKeys().get(colIndex);
            if (!columnValueMatches(entity, object)) {
                // appropriate value is missing, write empty cell
                createCell(row, pivotParameters.getFixedColumnKeys().size() + colIndex, entity, "", null);
            } else {
                // get cell value

                String prop = pivotParameters.getPivotedProperties().get(propIndex);
                Object value = ClassUtils.getFieldValue(entity, prop);
                Cell cell = createCell(row, pivotParameters.getFixedColumnKeys().size() + colIndex, entity, value, null);
                writeCellValue(cell, value, getEntityModel(), null);
            }

            if (propIndex == pivotParameters.getPivotedProperties().size() - 1) {
                propIndex = 0;
                colIndex = colIndex + 1;
            } else {
                propIndex++;
            }

            // rowIndex++;
            entity = iterator.next();
            prevRowKey = rowKey;
        }
        resizeColumns(sheet);

        getWorkbook().write(stream);
        return stream.toByteArray();
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

}
