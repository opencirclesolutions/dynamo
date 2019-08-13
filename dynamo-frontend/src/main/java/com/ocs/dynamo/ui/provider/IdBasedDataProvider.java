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
package com.ocs.dynamo.ui.provider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.ui.utils.VaadinUtils;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;

/**
 * A data provider that first looks up the IDs of the matching entities and then
 * uses those IDs for pagination
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key
 * @param <T> the type of the entity
 */
public class IdBasedDataProvider<ID extends Serializable, T extends AbstractEntity<ID>> extends BaseDataProvider<ID, T> {

    private static final long serialVersionUID = -5693366456446998962L;

    /**
     * Constructor
     * 
     * @param service     the service
     * @param entityModel the entity model
     * @param joins       the joins to apply
     */
    public IdBasedDataProvider(BaseService<ID, T> service, EntityModel<T> entityModel, FetchJoinInformation... joins) {
        super(service, entityModel, joins);
    }

    @Override
    public Stream<T> fetch(Query<T, SerializablePredicate<T>> query) {

        // when sort order changes, ID have to be fetched again
        SortOrders so = createSortOrder(query);
        size(query);

        List<ID> results = new ArrayList<>();
        int index = query.getOffset();

        // Try to load the IDs when they have not been loaded yet
        if (ids == null) {
            size(query);
        }
        // construct a page worth of IDs
        if (ids != null && !ids.isEmpty()) {
            while (index < ids.size() && results.size() < query.getLimit()) {
                ID id = ids.get(index);
                results.add(id);
                index++;
            }
        }
        List<T> result = getService().fetchByIds(results, so, getJoins());
        return result.stream();
    }

    @Override
    public int getSize() {
        return ids == null ? 0 : ids.size();
    }

    @Override
    public int size(Query<T, SerializablePredicate<T>> query) {
        SortOrders so = createSortOrder(query);

        FilterConverter<T> converter = getFilterConverter();
        final Filter filter = converter.convert(query.getFilter().orElse(null));

        if (getMaxResults() != null) {
            Long count = getService().count(filter, false);
            if (count >= getMaxResults()) {
                showNotification(getMessageService().getMessage("ocs.too.many.results", VaadinUtils.getLocale(), getMaxResults()));
            }
        }
        ids = getService().findIds(filter, getMaxResults(), so.toArray());
        if (getAfterCountCompleted() != null) {
            getAfterCountCompleted().accept(ids.size());
        }
        return ids.size();
    }

    @Override
    public ID firstItemId() {
        return ids.get(0);
    }

    @Override
    public int indexOf(ID id) {
        return ids.indexOf(id);
    }

    @Override
    public T getItem(ID id) {
        return null;
    }

}
