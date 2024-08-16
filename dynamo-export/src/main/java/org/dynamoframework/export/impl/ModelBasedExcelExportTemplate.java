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
package org.dynamoframework.export.impl;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.LocaleUtil;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.query.DataSetIterator;
import org.dynamoframework.export.CustomXlsStyleGenerator;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.Filter;
import org.dynamoframework.service.BaseService;
import org.dynamoframework.utils.ClassUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Template for exporting a data set to Excel based on the Entity model
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 * @author bas.rutten
 */
public class ModelBasedExcelExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseExcelExportTemplate<ID, T> {

    /**
     * Constructor
     *
     * @param service         the service used to retrieve the data
     * @param entityModel     the entity model
     * @param sortOrders      the sort order
     * @param filter          the filter that is used to retrieve the appropriate
     *                        data
     * @param title           the title of the sheet
     * @param customGenerator custom style generator
     * @param locale          the locale to use
     * @param joins           fetch joins to use when retrieving data
     */
    public ModelBasedExcelExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode mode,
                                         List<SortOrder> sortOrders, Filter filter, String title,
                                         Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator,
                                         Locale locale, FetchJoinInformation... joins) {
        super(service, entityModel, mode, sortOrders, filter, title, customGenerator, locale, joins);
    }

    @Override
    protected byte[] generate(DataSetIterator<ID, T> iterator) throws IOException {
        setWorkbook(createWorkbook(iterator.size()));
        Sheet sheet = getWorkbook().createSheet(getTitle());
        setGenerator(createGenerator(getWorkbook()));

        boolean resize = canResize();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(TITLE_ROW_HEIGHT);

        addHeaderRow(sheet, resize, titleRow);
        addContentRows(iterator, sheet);
        resizeColumns(sheet);

        getWorkbook().write(stream);
        return stream.toByteArray();
    }

    private void addHeaderRow(Sheet sheet, boolean resize, Row titleRow) {
        int i = 0;
        for (AttributeModel am : getEntityModel().getAttributeModelsSortedForGrid()) {
            if (mustShow(am)) {
                if (!resize) {
                    sheet.setColumnWidth(i, FIXED_COLUMN_WIDTH);
                }
                Cell cell = titleRow.createCell(i);
                cell.setCellStyle(getGenerator().getHeaderStyle(i));
                cell.setCellValue(am.getDisplayName(LocaleUtil.getUserLocale()));
                i++;
            }
        }
    }

    private void addContentRows(DataSetIterator<ID, T> iterator, Sheet sheet) {
        int rowIndex = 1;
        T entity = iterator.next();
        while (entity != null) {
            Row row = sheet.createRow(rowIndex);
            int colIndex = 0;
            for (AttributeModel am : getEntityModel().getAttributeModelsSortedForGrid()) {
                if (am != null && mustShow(am)) {
                    Object value = ClassUtils.getFieldValue(entity, am.getPath());
                    Cell cell = createCell(row, colIndex, entity, value, am, null);
                    writeCellValue(cell, value, am, false);
                    colIndex++;
                }
            }
            rowIndex++;
            entity = iterator.next();
        }
    }
}
