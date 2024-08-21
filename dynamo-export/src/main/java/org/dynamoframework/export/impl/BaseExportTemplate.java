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

import lombok.AccessLevel;
import lombok.Getter;
import org.dynamoframework.configuration.DynamoProperties;
import org.dynamoframework.constants.DynamoConstants;
import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.AttributeType;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.domain.query.DataSetIterator;
import org.dynamoframework.domain.query.FixedDataSetIterator;
import org.dynamoframework.domain.query.PagingDataSetIterator;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.export.type.ExportMode;
import org.dynamoframework.filter.Filter;
import org.dynamoframework.service.BaseService;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Base class for entity model based exports to Excel or CSV
 *
 * @param <ID> the type of the primary key of the entity to export
 * @param <T>  the type of the entity to export
 * @author bas.rutten
 */
public abstract class BaseExportTemplate<ID extends Serializable, T extends AbstractEntity<ID>> {

    protected static final int PAGE_SIZE = 1000;

    @Getter(AccessLevel.PROTECTED)
    private final Filter filter;

    @Getter(AccessLevel.PROTECTED)
    private final FetchJoinInformation[] joins;

    @Getter(AccessLevel.PROTECTED)
    private final BaseService<ID, T> service;

    @Getter(AccessLevel.PROTECTED)
    private final SortOrder[] sortOrders;

    @Getter(AccessLevel.PROTECTED)
    private final String title;

    @Getter(AccessLevel.PROTECTED)
    private final EntityModel<T> entityModel;

    @Getter(AccessLevel.PROTECTED)
    private final ExportMode exportMode;

    @Getter(AccessLevel.PROTECTED)
    private final DynamoProperties dynamoProperties;

    /**
     * Constructor
     *
     * @param service     the service used for contacting the database
     * @param entityModel the entity model of the entity to export
     * @param exportMode  the desired export mode
     * @param sortOrders  the sort orders
     * @param filter      the filter used to restrict the result set
     * @param title       the title of the sheet
     * @param joins       the joins to use when retrieving data
     */
    protected BaseExportTemplate(DynamoProperties dynamoProperties, BaseService<ID, T> service, EntityModel<T> entityModel, ExportMode exportMode,
                                 List<SortOrder> sortOrders, Filter filter, String title, FetchJoinInformation... joins) {
        this.dynamoProperties = dynamoProperties;
        this.service = service;
        this.exportMode = exportMode;
        this.entityModel = entityModel;
        // if no sort order specified, then sort by id descending
        this.sortOrders = sortOrders != null ? sortOrders.toArray(new SortOrder[0])
                : new SortOrder[]{new SortOrder(DynamoConstants.ID, SortOrder.Direction.DESC)};
        this.filter = filter;
        this.title = title;
        this.joins = joins;
    }

    /**
     * Generates the content to export
     *
     * @param iterator data set iterator that contains the rows to include
     * @return the byte representation of the exported data
     * @throws IOException when the data cannot be written
     */
    protected abstract byte[] generate(DataSetIterator<ID, T> iterator) throws IOException;

    /**
     * Check whether a certain attribute model must be included in the export
     *
     * @param am the attribute model
     * @return <code>true</code> if the attribute must be included,
     * <code>false</code> otherwise
     */
    protected boolean mustShow(AttributeModel am) {
        boolean visible = ExportMode.FULL.equals(exportMode) ? am.isVisibleInForm() : am.isVisibleInGrid();
        return visible && !AttributeType.LOB.equals(am.getAttributeType());
    }

    /**
     * Carries out the export
     *
     * @return the byte representation of the export
     */
    public final byte[] process() {
        try {
            List<ID> ids = service.findIds(getFilter(), this.getEntityModel().getMaxSearchResults(), sortOrders);
            PagingDataSetIterator<ID, T> iterator = new PagingDataSetIterator<>(ids,
                    page -> service.fetchByIds(page, new SortOrders(sortOrders), joins), PAGE_SIZE);
            return generate(iterator);
        } catch (IOException ex) {
            throw new OCSRuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Processes a fixed set of data
     *
     * @param items the set of data to process
     * @return the byte representation
     */
    public final byte[] processFixed(List<T> items) {
        try {
            FixedDataSetIterator<ID, T> iterator = new FixedDataSetIterator<>(items);
            return generate(iterator);
        } catch (IOException ex) {
            throw new OCSRuntimeException(ex.getMessage(), ex);
        }
    }

}
