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

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.domain.query.DataSetIterator;
import com.ocs.dynamo.domain.query.FixedDataSetIterator;
import com.ocs.dynamo.domain.query.PagingDataSetIterator;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.ui.composite.type.ExportMode;

/**
 * Base class for entity model based exports to Excel or CSV
 * 
 * @author bas.rutten
 *
 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public abstract class BaseExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> {

    /**
     * The width for a fixed with column
     */
    protected static final int FIXED_COLUMN_WIDTH = 20 * 256;

    /**
     * The height of the title row in pixels
     */
    protected static final int TITLE_ROW_HEIGHT = 40;

    /**
     * The maximum number of rows that will be created normally. If the number is
     * larger, then a streaming writer will be used. This is faster but it will mean
     * we cannot auto size the columns
     */
    protected static final int MAX_SIZE_BEFORE_STREAMING = 1000;

    /**
     * The page size
     */
    protected static final int PAGE_SIZE = 200;

    /**
     * Entity model factory
     */
    private final EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

    /**
     * The filter to use to restrict the search results
     */
    private final Filter filter;

    /**
     * Custom joins to use
     */
    private final FetchJoinInformation[] joins;

    /**
     * Service used to retrieve search results
     */
    private final BaseService<ID, T> service;

    /**
     * Sort orders to apply
     */
    private final SortOrder[] sortOrders;

    /**
     * The title of the export
     */
    private final String title;

    /**
     * The entity model
     */
    private final EntityModel<T> entityModel;

    /**
     * The desired export mode
     */
    private final ExportMode exportMode;

    /**
     * Constructor
     *
     * @param service         the service used to retrieve the data
     * @param sortOrders      the sort order
     * @param filter          the filter used to limit the data
     * @param title           the title of the sheet
     * @param customGenerator custom generator used to apply extra styling
     * @param joins
     */
    public BaseExportTemplate(BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode, SortOrder[] sortOrders,
            Filter filter, String title, FetchJoinInformation... joins) {
        this.service = service;
        this.exportMode = exportMode;
        this.entityModel = entityModel;
        // if no sort order specified, then sort by id descending
        this.sortOrders = sortOrders != null ? sortOrders : new SortOrder[] { new SortOrder(DynamoConstants.ID, Direction.DESC) };
        this.filter = filter;
        this.title = title;
        this.joins = joins;
    }

    /**
     * Generates the file
     *
     * @param iterator data set iterator that contains the rows to include
     * @return
     * @throws IOException
     */
    protected abstract byte[] generate(DataSetIterator<ID, T> iterator) throws IOException;

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public EntityModelFactory getEntityModelFactory() {
        return entityModelFactory;
    }

    /**
     * Check whether a certain attribute model must be included in the export
     *
     * @param am the attribute model
     * @return <code>true</code> if the attribute must be included,
     *         <code>false</code> otherwise
     */
    protected boolean show(AttributeModel am) {
        boolean visible = ExportMode.FULL.equals(exportMode) ? am.isVisible() : am.isVisibleInGrid();

        // never show invisible or LOB attributes
        return visible && !AttributeType.LOB.equals(am.getAttributeType());

    }

    public Filter getFilter() {
        return filter;
    }

    public FetchJoinInformation[] getJoins() {
        return joins;
    }

    public BaseService<ID, T> getService() {
        return service;
    }

    public SortOrder[] getSortOrders() {
        return sortOrders;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Processes the input and creates a file
     *
     * @param xls whether to export to Excel (xlsx)
     * @return
     * @throws IOException
     */
    public final byte[] process() {
        try {
            // retrieve all store series based on the IDs
            List<ID> ids = service.findIds(getFilter(), sortOrders);
            PagingDataSetIterator<ID, T> iterator = new PagingDataSetIterator<ID, T>(ids, PAGE_SIZE) {

                @Override
                protected List<T> readPage(List<ID> ids) {
                    return service.fetchByIds(ids, new SortOrders(sortOrders), joins);
                }
            };

            return generate(iterator);
        } catch (IOException ex) {
            throw new OCSRuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Processes a fixed set of data
     * 
     * @param items the set of data to process
     * @return
     */
    public final byte[] processFixed(List<T> items) {
        try {
            // retrieve all store series based on the IDs
            FixedDataSetIterator<ID, T> iterator = new FixedDataSetIterator<>(items);
            return generate(iterator);
        } catch (IOException ex) {
            throw new OCSRuntimeException(ex.getMessage(), ex);
        }
    }

}
