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
package com.ocs.dynamo.ui.container;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryView;

import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.Searchable;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.shared.data.sort.SortDirection;

/**
 * A Vaadin query container that supports lazy loading and gets its data by calling a service
 * 
 * @author bas.rutten
 * @param <T>
 *            type of the entity
 * @param <ID>
 *            type of the primary key of the entity
 */
public class ServiceContainer<ID extends Serializable, T extends AbstractEntity<ID>> extends LazyQueryContainer
        implements Searchable {

    private static final long serialVersionUID = 2605988307857731787L;

    public static final List<String> DEFAULT_EXCLUDE_COLUMNS = Arrays
            .asList(new String[] { LazyQueryView.PROPERTY_ID_ITEM_STATUS, LazyQueryView.DEBUG_PROPERTY_ID_BATCH_INDEX,
                    LazyQueryView.DEBUG_PROPERTY_ID_BATCH_QUERY_TIME, LazyQueryView.DEBUG_PROPERTY_ID_QUERY_INDEX });

    /**
     * Constructs a service container based on a query definition
     * 
     * @param queryDefinition
     *            the query definition
     */
    public ServiceContainer(ServiceQueryDefinition<ID, T> queryDefinition) {
        super(queryDefinition, new ServiceQueryFactory<ID, T>());
    }

    /**
     * Constructs a service container
     * 
     * @param service
     *            the service
     * @param compositeItems
     *            whether to include composite items
     * @param batchSize
     *            the batch size
     * @param properties
     */
    public ServiceContainer(BaseService<ID, T> service, boolean compositeItems, int batchSize, QueryType queryType,
            FetchJoinInformation[] joins) {
        super(new ServiceQueryDefinition<ID, T>(service, compositeItems, batchSize, queryType, joins),
                new ServiceQueryFactory<ID, T>());
    }

    /**
     * Constructor
     * 
     * @param service
     * @param modelFactory
     * @param compositeItems
     * @param batchSize
     */
    public ServiceContainer(BaseService<ID, T> service, EntityModelFactory emf, boolean compositeItems, int batchSize,
            QueryType queryType, FetchJoinInformation[] joins) {
        super(new ServiceQueryDefinition<ID, T>(service, compositeItems, batchSize, queryType, joins),
                new ServiceQueryFactory<ID, T>());
        addContainerProperties(emf.getModel(service.getEntityClass()));
    }

    /**
     * Constructor
     * 
     * @param service
     * @param model
     * @param compositeItems
     * @param batchSize
     */
    @SuppressWarnings("unchecked")
    public ServiceContainer(BaseService<ID, T> service, EntityModel<T> model, boolean compositeItems, int batchSize,
            QueryType queryType, FetchJoinInformation[] joins) {
        super(new ServiceQueryDefinition<ID, T>(service, compositeItems, batchSize, queryType, joins),
                new ServiceQueryFactory<ID, T>());
        ((ServiceQueryDefinition<ID, T>) getQueryView().getQueryDefinition()).setEntityModel(model);
        addContainerProperties(model);
    }

    /**
     * Adds properties based on an EntityModel
     * 
     * @param model
     */
    public void addContainerProperties(EntityModel<?> model) {
        for (AttributeModel attributeModel : model.getAttributeModels()) {
            if (attributeModel.isVisibleInTable()) {
                addContainerProperty(attributeModel.getName(), attributeModel.getType(),
                        attributeModel.getDefaultValue(), attributeModel.isReadOnly(), attributeModel.isSortable());
            }
        }
    }

    /**
     * Adds a single property based on an attribute model
     * 
     * @param attributeModel
     */
    public void addContainerProperty(AttributeModel attributeModel) {
        addContainerProperty(attributeModel.getPath(), attributeModel.getType(), attributeModel.getDefaultValue(),
                attributeModel.isReadOnly(), attributeModel.isSortable());
    }

    @Override
    public void search(Filter filter) {
        // warning: do not use "removeAllContainerFilters" here since this will
        // trigger an unnecessary refresh
        getQueryView().removeFilters();

        // likewise, don't call "addContainerfilter" here since it will also
        // trigger this refresh
        if (filter != null) {
            getQueryView().addFilter(filter);
        }
        refresh();
    }

    @SuppressWarnings("unchecked")
    public BaseService<ID, T> getService() {
        if (getQueryView() != null && getQueryView().getQueryDefinition() instanceof ServiceQueryDefinition<?, ?>) {
            return ((ServiceQueryDefinition<ID, T>) getQueryView().getQueryDefinition()).getService();
        }
        return null;
    }

    public void sort(SortOrder... sortOrder) {
        if (sortOrder != null && sortOrder.length > 0) {
            Object[] pIds = new Object[sortOrder.length];
            boolean[] sos = new boolean[sortOrder.length];
            int i = 0;
            for (SortOrder so : sortOrder) {
                pIds[i] = so.getPropertyId();
                sos[i++] = SortDirection.ASCENDING == so.getDirection();
            }
            sort(pIds, sos);
        }
    }
}
