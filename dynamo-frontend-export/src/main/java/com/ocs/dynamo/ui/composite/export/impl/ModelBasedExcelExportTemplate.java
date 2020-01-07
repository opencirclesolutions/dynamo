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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.export.CustomXlsStyleGenerator;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.ocs.dynamo.utils.ClassUtils;

/**
 * Template for exporting a data set to Excel based on the Entity model
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public class ModelBasedExcelExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseExcelExportTemplate<ID, T> {

    /**
     * Constructor
     *
     * @param service         the service used to retrieve the data
     * @param entityModel     the entity model
     * @param sortOrders      any sort orders to apply to the data
     * @param filter          the filter that is used to retrieve the appropriate
     *                        data
     * @param title           the title of the sheet
     * @param customGenerator custom style generator
     * @param joins
     */
    public ModelBasedExcelExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode mode, SortOrder[] sortOrders,
            Filter filter, String title, CustomXlsStyleGenerator<ID, T> customGenerator, FetchJoinInformation... joins) {
        super(service, entityModel, mode, sortOrders, filter, title, customGenerator, joins);
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

        int i = 0;
        for (AttributeModel am : getEntityModel().getAttributeModels()) {
            if (show(am)) {
                if (!resize) {
                    sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
                }
                Cell cell = titleRow.createCell(i);
                cell.setCellStyle(getGenerator().getHeaderStyle(i));
                cell.setCellValue(am.getDisplayName(VaadinUtils.getLocale()));
                i++;
            }
        }

        // iterate over the rows
        int rowIndex = 1;
        T entity = iterator.next();
        while (entity != null) {
            Row row = sheet.createRow(rowIndex);
            int colIndex = 0;
            for (AttributeModel am : getEntityModel().getAttributeModels()) {
                if (am != null && show(am)) {
                    Object value = ClassUtils.getFieldValue(entity, am.getPath());
                    Cell cell = createCell(row, colIndex, entity, value, am);
                    writeCellValue(cell, value, getEntityModel(), am);
                    colIndex++;
                }
            }
            rowIndex++;
            entity = iterator.next();
        }
        resizeColumns(sheet);

        getWorkbook().write(stream);
        return stream.toByteArray();
    }


}
