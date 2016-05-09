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
package com.ocs.dynamo.ui.composite.table;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.container.QueryType;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.sort.SortOrder;
import com.vaadin.data.util.BeanItemContainer;

/**
 * A wrapper for a table that displays a fixed number of items
 * 
 * @author bas.rutten
 * @param <ID>
 *            type of the primary key
 * @param <T>
 *            type of the entity
 */
public class FixedTableWrapper<ID extends Serializable, T extends AbstractEntity<ID>> extends
        BaseTableWrapper<ID, T> {

    private static final long serialVersionUID = -6711832174203817230L;

    // the collection of items to display
    private Collection<T> items;

    /**
     * Constructor
     * 
     * @param service
     *            the service
     * @param entityModel
     *            the entity model of the items to display in the table
     * @param items
     *            the items to display
     * @param sortOrder
     *            optional sort order
     */
    public FixedTableWrapper(BaseService<ID, T> service, EntityModel<T> entityModel,
            Collection<T> items, List<SortOrder> sortOrders) {
        super(service, entityModel, QueryType.NONE, sortOrders);
        this.items = items;
    }

    @Override
    protected Container constructContainer() {
        BeanItemContainer<T> container = new BeanItemContainer<T>(getService().getEntityClass());
        container.addAll(items);
        return container;
    }

    @Override
    public void reloadContainer() {
        // do nothing
    }

    @Override
    public void search(Filter filter) {
        // do nothing
    }
}
