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
import java.util.List;
import java.util.Map;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

/**
 * A version of the BaseServiceQuery that retrieves data using a simple paging mechanism
 * 
 * @author bas.rutten
 * @param <ID>
 *            the type of the primary key
 * @param <T>
 *            the type of the entity
 */
public class PagingServiceQuery<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseServiceQuery<ID, T> {

    private static final long serialVersionUID = -324739194626626683L;

    /**
     * Constructor
     * 
     * @param queryDefinition
     * @param queryConfiguration
     */
    public PagingServiceQuery(ServiceQueryDefinition<ID, T> queryDefinition,
            Map<String, Object> queryConfiguration) {
        super(queryDefinition, queryConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<T> loadBeans(int startIndex, int count) {
        Filter serviceFilter = constructFilter();
        SortOrder[] orders = constructOrder();
        ServiceQueryDefinition<ID, T> definition = getCustomQueryDefinition();
        return definition.getService().fetch(serviceFilter, startIndex / definition.getBatchSize(),
                definition.getBatchSize(), new SortOrders(orders), definition.getJoins());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        if (getCustomQueryDefinition().getPredeterminedCount() != null) {
            return getCustomQueryDefinition().getPredeterminedCount();
        }
        return (int) getCustomQueryDefinition().getService().count(constructFilter(), false);
    }
}
