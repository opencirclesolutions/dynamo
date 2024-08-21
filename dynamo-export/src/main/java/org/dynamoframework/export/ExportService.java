package org.dynamoframework.export;

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

import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.Filter;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Service for exporting grid contents to XLSX or CSV
 *
 * @author Bas Rutten
 */
public interface ExportService {

    /**
     * Exports to CSV
     *
     * @param entityModel the entity model of the entity to export
     * @param mode        the desired export mode
     * @param filter      the filter that determines how to limit the results
     * @param sortOrders  the list of sort orders
     * @param joins       the joins to use when fetching data
     * @return the result of the export
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportCsv(EntityModel<T> entityModel,
                                                                             ExportMode mode, Filter filter, List<SortOrder> sortOrders,
                                                                             Locale locale,
                                                                             FetchJoinInformation... joins);

    /**
     * Exports a fixed set of data to CSV
     *
     * @param entityModel the entity model
     * @param mode        the desired export mode
     * @param items       the set of items to export
     * @return the result of the export
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportCsvFixed(EntityModel<T> entityModel,
                                                                                 ExportMode mode, List<T> items, Locale locale);

    /**
     * Exports to Excel
     *
     * @param entityModel     the entity model of the entity to export
     * @param filter          the predicate
     * @param mode            the desired export mode
     * @param sortOrders      the list of sort orders
     * @param customGenerator the custom style generator *
     * @return the result of the export
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportExcel(EntityModel<T> entityModel,
                                                                               ExportMode mode, Filter filter, List<SortOrder> sortOrders,
                                                                               Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator,
                                                                               Locale locale, FetchJoinInformation... joins);

    /**
     * Exports a fixed set to Excel
     *
     * @param entityModel the entity model
     * @param mode        the export mode
     * @param items       the set of items to export
     * @return the result of the export
     */
    <ID extends Serializable, T extends AbstractEntity<ID>> byte[] exportExcelFixed(EntityModel<T> entityModel,
                                                                                    ExportMode mode, Supplier<CustomXlsStyleGenerator<ID, T>> customGenerator, List<T> items,
                                                                                    Locale locale);
}
