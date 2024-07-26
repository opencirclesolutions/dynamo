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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.export.type.ExportMode;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.utils.ClassUtils;
import com.ocs.dynamo.utils.FormatUtils;
import com.ocs.dynamo.utils.SystemPropertyUtils;
import com.opencsv.CSVWriter;
import org.apache.poi.util.LocaleUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A template for exporting data to CSV
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T>  the type of the entity to export
 * @author Bas Rutten
 */
public class ModelBasedCsvExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseCsvExportTemplate<ID, T> {

    private final Locale locale;

    /**
     * Constructor
     *
     * @param service     service used for retrieving data from the database
     * @param entityModel the entity model of the entities to export
     * @param exportMode  the export mode
     * @param sortOrders  the sort orders used to order the data
     * @param filter      filter to apply to limit the results
     * @param joins       fetch joins to use when querying the database
     */
    public ModelBasedCsvExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
                                       List<SortOrder> sortOrders, Filter filter, Locale locale, FetchJoinInformation... joins) {
        super(service, entityModel, exportMode, sortOrders, filter, joins);
        this.locale = locale;
    }

    @Override
    protected byte[] generate(DataSetIterator<ID, T> iterator) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8),
                     SystemPropertyUtils.getCsvSeparator().charAt(0),
                     SystemPropertyUtils.getCsvQuoteChar().charAt(0),
                     SystemPropertyUtils.getCsvEscapeChar().charAt(0), String.format("%n"))) {

            addHeaderRow(writer);

            T entity = iterator.next();
            while (entity != null) {
                List<String> row = new ArrayList<>();
                for (AttributeModel am : getEntityModel().getAttributeModelsSortedForGrid()) {
                    if (mustShow(am)) {
                        Object value = ClassUtils.getFieldValue(entity, am.getPath());
                        String str = FormatUtils.formatPropertyValue(am, value, ", ",
                                locale);
                        row.add(str);
                    }
                }
                if (!row.isEmpty()) {
                    writer.writeNext(row.toArray(new String[0]));
                }
                entity = iterator.next();
            }
            writer.flush();
            return out.toByteArray();
        }
    }

    private void addHeaderRow(CSVWriter writer) {
        List<String> headers = new ArrayList<>();
        for (AttributeModel am : getEntityModel().getAttributeModelsSortedForGrid()) {
            if (mustShow(am)) {
                headers.add(am.getDisplayName(LocaleUtil.getUserLocale()));
            }
        }
        writer.writeNext(headers.toArray(new String[0]));
    }

}
