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

import java.io.Serializable;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.composite.type.ExportMode;

/**
 * Base class for CSV exports
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T> the type of the entity to export
 */
public abstract class BaseCsvExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseExportTemplate<ID, T> {

    /**
     * Constructor
     * @param service
     * @param entityModel
     * @param exportMode
     * @param sortOrders
     * @param filter
     * @param title
     * @param joins
     */
    public BaseCsvExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode, SortOrder[] sortOrders,
            Filter filter, String title, FetchJoinInformation[] joins) {
        super(service, entityModel, exportMode, sortOrders, filter, title, joins);
    }

}
