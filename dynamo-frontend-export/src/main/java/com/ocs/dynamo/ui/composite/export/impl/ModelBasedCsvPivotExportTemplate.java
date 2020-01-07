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
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.export.PivotParameters;
import com.ocs.dynamo.ui.composite.type.ExportMode;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.ocs.dynamo.utils.ClassUtils;
import com.opencsv.CSVWriter;

/**
 * Template for exported a pivoted data set to CSV
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T>  the type of the entity to export
 */
public class ModelBasedCsvPivotExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseCsvExportTemplate<ID, T> {

    private PivotParameters pivotParameters;

    /**
     * Constructor
     * 
     * @param service         the service
     * @param entityModel     the entity model
     * @param exportMode      the export mode
     * @param sortOrders      the sort orders to apply to the data
     * @param filter
     * @param title
     * @param pivotParameters
     * @param joins
     */
    public ModelBasedCsvPivotExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, SortOrder[] sortOrders, Filter filter,
            String title, PivotParameters pivotParameters, FetchJoinInformation... joins) {
        super(service, entityModel, ExportMode.ONLY_VISIBLE_IN_GRID, sortOrders, filter, title, joins);
        this.pivotParameters = pivotParameters;
    }

    @Override
    protected byte[] generate(DataSetIterator<ID, T> iterator) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                        SystemPropertyUtils.getCsvSeparator().charAt(0), SystemPropertyUtils.getCsvQuoteChar().charAt(0),
                        SystemPropertyUtils.getCsvEscapeChar().charAt(0), String.format("%n"))) {

            // add header row

            // add fixed columns
            List<String> headers = new ArrayList<>();
            for (String fc : pivotParameters.getFixedColumnKeys()) {
                headers.add(pivotParameters.getFixedHeaderMapper().apply(fc));
            }

            // add variable columns
            for (Object fc : pivotParameters.getPossibleColumnKeys()) {
                for (String property : pivotParameters.getPivotedProperties()) {
                    String value = pivotParameters.getHeaderMapper().apply(fc, property);
                    headers.add(value);
                }
            }
            writer.writeNext(headers.toArray(new String[0]));

            String prevRowKey = null;
            List<String> row = null;
            int colIndex = 0;
            int propIndex = 0;

            // iterate over the rows
            T entity = iterator.next();
            while (entity != null) {

                String rowKey = ClassUtils.getFieldValueAsString(entity, pivotParameters.getRowKeyProperty());
                if (!Objects.equals(prevRowKey, rowKey)) {

                    if (row != null) {
                        addEmtpyColumnValues(row);
                        writer.writeNext(row.toArray(new String[0]));
                    }

                    row = new ArrayList<>();

                    for (String fc : pivotParameters.getFixedColumnKeys()) {
                        Object value = ClassUtils.getFieldValueAsString(entity, fc);
                        row.add(value.toString());
                    }

                    colIndex = 0;
                    propIndex = 0;
                }

                Object object = pivotParameters.getPossibleColumnKeys().get(colIndex);
                if (!columnValueMatches(entity, object)) {
                    // appropriate value is missing, write empty cell
                    row.add("");
                } else {
                    // get cell value

                    String prop = pivotParameters.getPivotedProperties().get(propIndex);
                    Object value = ClassUtils.getFieldValue(entity, prop);
                    row.add(value == null ? "" : value.toString());
                }

                if (propIndex == pivotParameters.getPivotedProperties().size() - 1) {
                    propIndex = 0;
                    colIndex = colIndex + 1;
                } else {
                    propIndex++;
                }

                entity = iterator.next();
                prevRowKey = rowKey;
            }

            // add last row
            if (row != null) {
                addEmtpyColumnValues(row);
                writer.writeNext(row.toArray(new String[0]));
            }

            writer.flush();
            return out.toByteArray();
        }
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

    private void addEmtpyColumnValues(List<String> row) {
        while (row.size() < pivotParameters.getFixedColumnKeys().size()
                + pivotParameters.getPivotedProperties().size() * pivotParameters.getPossibleColumnKeys().size()) {
            row.add("");
        }
    }
}
