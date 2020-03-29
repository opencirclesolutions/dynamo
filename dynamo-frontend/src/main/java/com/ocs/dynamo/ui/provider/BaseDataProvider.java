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
import java.util.List;
import java.util.function.Consumer;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrder.Direction;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.domain.model.EntityModelFactory;
import com.ocs.dynamo.filter.FilterConverter;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.MessageService;
import com.ocs.dynamo.service.ServiceLocatorFactory;
import com.ocs.dynamo.util.SystemPropertyUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.provider.AbstractDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Abstract class for data providers
 * 
 * @author Bas Rutten
 *
 * @param <ID> the type of the primary key
 * @param <T>  the type of the entity
 */
public abstract class BaseDataProvider<ID extends Serializable, T extends AbstractEntity<ID>>
        extends AbstractDataProvider<T, SerializablePredicate<T>> {

    private static final long serialVersionUID = 7409567551591729117L;

    private final EntityModel<T> entityModel;

    private EntityModelFactory entityModelFactory = ServiceLocatorFactory.getServiceLocator().getEntityModelFactory();

    private final FetchJoinInformation[] joins;

    /**
     * The maximum number of search results to include in the results table
     */
    private Integer maxResults;

    /**
     * The message service
     */
    private MessageService messageService = ServiceLocatorFactory.getServiceLocator().getMessageService();

    /**
     * The service used to query the DB
     */
    private final BaseService<ID, T> service;

    /**
     * ID of the currently selected item
     */
    private ID currentlySelectedId;

    /**
     * Sort orders to fall back to when no sort orders are defined directly on the
     * grid
     */
    private List<com.vaadin.flow.data.provider.SortOrder<?>> fallBackSortOrders;

    /**
     * The IDs of the entities to display
     */
    protected List<ID> ids;

    /**
     * Code to carry out after the count query completes
     */
    private Consumer<Integer> afterCountCompleted;

    /**
     * Constructor
     * 
     * @param service     the service used for retrieving data from the database
     * @param entityModel the entity model
     * @param joins       the join data to use
     */
    public BaseDataProvider(BaseService<ID, T> service, EntityModel<T> entityModel, FetchJoinInformation... joins) {
        this.service = service;
        this.entityModel = entityModel;
        this.joins = joins;
    }

    /**
     * Creates the desired sort order
     * 
     * @param query the query predicate
     * @return
     */
    protected SortOrders createSortOrder(Query<T, SerializablePredicate<T>> query) {
        List<QuerySortOrder> orders = query.getSortOrders();
        SortOrders so = new SortOrders();

        if (!orders.isEmpty()) {
            for (QuerySortOrder order : orders) {
                so.addSortOrder(new SortOrder(order.getSorted(),
                        SortDirection.ASCENDING.equals(order.getDirection()) ? Direction.ASC : Direction.DESC));
            }
        } else if (fallBackSortOrders != null && !fallBackSortOrders.isEmpty()) {
            for (com.vaadin.flow.data.provider.SortOrder<?> order : fallBackSortOrders) {
                so.addSortOrder(new SortOrder(order.getSorted().toString(),
                        SortDirection.ASCENDING.equals(order.getDirection()) ? Direction.ASC : Direction.DESC));
            }
        }

        // if no sort order defined, order descending on ID
        if (so.getNrOfSortOrders() == 0) {
            so.addSortOrder(new SortOrder(DynamoConstants.ID, Direction.DESC));
        }

        return so;
    }

    public abstract ID firstItemId();

    public Consumer<Integer> getAfterCountCompleted() {
        return afterCountCompleted;
    }

    protected ID getCurrentlySelectedId() {
        return currentlySelectedId;
    }

    public EntityModel<T> getEntityModel() {
        return entityModel;
    }

    public List<com.vaadin.flow.data.provider.SortOrder<?>> getFallBackSortOrders() {
        return fallBackSortOrders;
    }

    protected FilterConverter<T> getFilterConverter() {
        EntityModel<T> em = getEntityModel();
        if (em == null) {
            em = entityModelFactory.getModel(getService().getEntityClass());
        }
        return new FilterConverter<>(em);
    }

    public FetchJoinInformation[] getJoins() {
        return joins;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public ID getNextItemId() {
        int index = ids.indexOf(currentlySelectedId);
        if (index < ids.size() - 1) {
            currentlySelectedId = ids.get(index + 1);
            return currentlySelectedId;
        }
        return null;
    }

    public ID getPreviousItemId() {
        int index = ids.indexOf(currentlySelectedId);
        if (index > 0) {
            currentlySelectedId = ids.get(index - 1);
            return currentlySelectedId;
        }
        return null;
    }

    public BaseService<ID, T> getService() {
        return service;
    }

    /**
     * Returns the number of items in the provider
     * 
     * @return
     */
    public abstract int getSize();

    /**
     * 
     * @return whether the data set contains a next item
     */
    public boolean hasNextItemId() {
        if (ids == null) {
            return false;
        }
        int index = ids.indexOf(currentlySelectedId);
        return index < ids.size() - 1;
    }

    /**
     * 
     * @return whether the data set contains a previous item
     */
    public boolean hasPreviousItemId() {
        if (ids == null) {
            return false;
        }
        int index = ids.indexOf(currentlySelectedId);
        return index > 0;
    }

    /**
     * Returns the index of the item identified by the specified ID
     * 
     * @param id
     * @return
     */
    public abstract int indexOf(ID id);

    @Override
    public boolean isInMemory() {
        return false;
    }

    public void setAfterCountCompleted(Consumer<Integer> afterCountCompleted) {
        this.afterCountCompleted = afterCountCompleted;
    }

    public void setCurrentlySelectedId(ID id) {
        this.currentlySelectedId = id;
    }

    public void setFallBackSortOrders(List<com.vaadin.flow.data.provider.SortOrder<?>> fallBackSortOrders) {
        this.fallBackSortOrders = fallBackSortOrders;
    }

    public void setMaxResults(final Integer maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Shows a notification message
     * 
     * @param message the message to show
     */
    protected void showNotification(String message) {
        if (UI.getCurrent() != null) {
            Notification.show(message, SystemPropertyUtils.getDefaultMessageDisplayTime(), Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
