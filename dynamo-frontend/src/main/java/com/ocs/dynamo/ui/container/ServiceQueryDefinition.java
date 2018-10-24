///*
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// */
//package com.ocs.dynamo.ui.container;
//
//import java.io.Serializable;
//
//import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
//
//import com.ocs.dynamo.constants.DynamoConstants;
//import com.ocs.dynamo.dao.FetchJoinInformation;
//import com.ocs.dynamo.domain.AbstractEntity;
//import com.ocs.dynamo.domain.model.EntityModel;
//import com.ocs.dynamo.service.BaseService;
//
///**
// * @author bas.rutten Base class for a query definition that uses a service for data retrieval
// * @param <ID>
// *            the class of the primary key
// * @param <T>
// *            the class of the entity
// */
//public class ServiceQueryDefinition<ID extends Serializable, T extends AbstractEntity<ID>> extends LazyQueryDefinition {
//
//    private static final long serialVersionUID = 2780009981072027606L;
//
//    private static final int MAX_NESTING_LEVEL = 3;
//
//    private final BaseService<ID, T> service;
//
//    private QueryType queryType;
//
//    private final FetchJoinInformation[] joins;
//
//    /**
//     * used for setting a predetermined record count - this will then skip the count query leading
//     * to potential performance improvements
//     */
//    private Integer predeterminedCount;
//
//    /**
//     * The entity model to base the query translation on
//     */
//    private EntityModel<T> entityModel;
//
//    private Integer maxResults;
//
//    /**
//     * Constructor
//     * 
//     * @param service
//     *            the service
//     * @param compositeItems
//     *            whether composite items are allowed
//     * @param batchSize
//     *            the default batch size
//     */
//    public ServiceQueryDefinition(BaseService<ID, T> service, boolean compositeItems, int batchSize,
//            EntityModel<T> entityModel, QueryType queryType, FetchJoinInformation... joins) {
//        super(compositeItems, batchSize, DynamoConstants.ID);
//        this.service = service;
//        this.queryType = queryType;
//        this.joins = joins;
//        this.entityModel = entityModel;
//        setMaxNestedPropertyDepth(MAX_NESTING_LEVEL);
//    }
//
//    /**
//     * Constructor
//     * 
//     * @param service
//     *            the service
//     * @param compositeItems
//     *            whether to use composite items
//     * @param batchSize
//     *            the batch size
//     * @param idPropertyId
//     *            the name of the primary key property
//     * @param maxNestedPropertyDepth
//     *            maximum nested property depth
//     * @param queryType
//     *            the query type
//     * @param joins
//     *            the joins to include in the query
//     */
//    public ServiceQueryDefinition(BaseService<ID, T> service, boolean compositeItems, int batchSize,
//            Object idPropertyId, int maxNestedPropertyDepth, QueryType queryType, FetchJoinInformation... joins) {
//        super(compositeItems, batchSize, idPropertyId);
//        this.service = service;
//        this.queryType = queryType;
//        this.joins = joins;
//        setMaxNestedPropertyDepth(maxNestedPropertyDepth);
//    }
//
//    public BaseService<ID, T> getService() {
//        return service;
//    }
//
//    public QueryType getQueryType() {
//        return queryType;
//    }
//
//    public FetchJoinInformation[] getJoins() {
//        return joins;
//    }
//
//    /**
//     * @return the predetermined number of records that will be returned by the query
//     */
//    public Integer getPredeterminedCount() {
//        return predeterminedCount;
//    }
//
//    /**
//     * Sets the predetermined count. this can be used as a shortcut if you know how many records
//     * will be returned by the query
//     * 
//     * @param predeterminedCount
//     */
//    public void setPredeterminedCount(Integer predeterminedCount) {
//        this.predeterminedCount = predeterminedCount;
//    }
//
//    public EntityModel<T> getEntityModel() {
//        return entityModel;
//    }
//
//    public void setEntityModel(EntityModel<T> entityModel) {
//        this.entityModel = entityModel;
//    }
//
//    public void setQueryType(QueryType queryType) {
//        this.queryType = queryType;
//    }
//
//    public Integer getMaxResults() {
//        return maxResults;
//    }
//
//    public void setMaxResults(Integer maxResults) {
//        this.maxResults = maxResults;
//    }
//
//}
