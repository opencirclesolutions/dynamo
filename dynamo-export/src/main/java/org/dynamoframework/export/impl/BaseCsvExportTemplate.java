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

import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.Filter;
import org.dynamoframework.service.BaseService;

import java.io.Serializable;
import java.util.List;

/**
 * Base class for CSV exports
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T>  the type of the entity to export
 * @author Bas Rutten
 */
public abstract class BaseCsvExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>>
        extends BaseExportTemplate<ID, T> {

    /**
     * Constructor
     *
     * @param service     the service used for retrieving data
     * @param entityModel the entity model of the entity being exported
     * @param exportMode  the export mode
     * @param sortOrders  the sort order
     * @param joins       fetch joins to apply when querying the database
     */
    protected BaseCsvExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
                                    List<SortOrder> sortOrders, Filter filter, FetchJoinInformation... joins) {
        super(service, entityModel, exportMode, sortOrders, filter, "", joins);
    }

}
