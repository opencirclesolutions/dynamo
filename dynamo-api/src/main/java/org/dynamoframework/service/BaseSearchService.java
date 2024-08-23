package org.dynamoframework.service;

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

import org.dynamoframework.dao.FetchJoinInformation;
import org.dynamoframework.dao.SortOrder;
import org.dynamoframework.dao.SortOrders;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.filter.Filter;

import java.util.List;

/**
 * The interface for a service that can be used to search on or retrieve an entity

 * @param <ID> the type of the primary key of the entity
 * @param <T>  the type of the entity
 */
public interface BaseSearchService<ID, T extends AbstractEntity<ID>> {

    /**
     * @return the total number of entities of this type
     */
    long count();

    /**
     * Returns the number of entities that match the provided filter
     *
     * @param filter   the filter
     * @param distinct whether to return only distinct results
     * @return the number of entities that match the filter
     */
    long count(Filter filter, boolean distinct);

    /**
     * Fetches entities that match the provided filter
     *
     * @param filter the filter to match
     * @param joins  the desired relations to fetch
     * @return a list of entities that match the filter
     */
    List<T> fetch(Filter filter, FetchJoinInformation... joins);

    /**
     * Fetches a page of entities that match the provided filter
     *
     * @param filter     the filter
     * @param pageNumber the page number of the page to fetch
     * @param pageSize   the page size
     * @param joins      the desired relations to fetch
     * @return a list of entities that match the filter
     */
    default List<T> fetch(Filter filter, int pageNumber, int pageSize, FetchJoinInformation... joins) {
        return fetch(filter, pageNumber, pageSize, null, joins);
    }

    /**
     * Fetches a page of entities that match the provided filter
     *
     * @param filter     the filter
     * @param pageNumber the page number of the page to fetch
     * @param pageSize   the page size
     * @param sortOrders the sort orders that must be used
     * @param joins      the desired relations to fetch
     * @return a list of entities that match the filter
     */
    List<T> fetch(Filter filter, int pageNumber, int pageSize, SortOrders sortOrders, FetchJoinInformation... joins);

    /**
     * Fetches a list of entities that match the provided filter
     *
     * @param filter the filter
     * @param orders  the sort orders that must be used
     * @param joins  the desired relations to fetch
     * @return a list of entities that match the filter
     */
    List<T> fetch(Filter filter, SortOrders orders, FetchJoinInformation... joins);

    /**
     * Fetches an entity (and its relations) based on its ID
     *
     * @param id    the ID of the entity
     * @param joins the desired relations to fetch
     * @return the entity identified by the supplied ID, or null if it cannot be found
     */
    T fetchById(ID id, FetchJoinInformation... joins);

    /**
     * Fetches the entities identified by the provided IDs
     *
     * @param ids   the IDs
     * @param joins desired relations to fetch
     * @return a list of the entities identified by the supplied IDs
     */
    default List<T> fetchByIds(List<ID> ids, FetchJoinInformation... joins) {
        return fetchByIds(ids, null, null, joins);
    }

    /**
     * Fetches the entities identified by the provided IDs
     *
     * @param ids        the IDs of the entities to fetch
     * @param sortOrders the sort orders that must be used
     * @param joins      the desired relations to fetch
     * @return a list of the entities identified by the supplied IDs
     */
    default List<T> fetchByIds(List<ID> ids, SortOrders sortOrders, FetchJoinInformation... joins) {
        return fetchByIds(ids, null, sortOrders, joins);
    }

    /**
     * Fetches the entities identified by the provided IDs
     *
     * @param ids        the IDs of the entities to fetch
     * @param sortOrders the sort orders that must be used
     * @param joins      the desired relations to fetch
     * @return a list of the entities identified by the supplied IDs
     */
    List<T> fetchByIds(List<ID> ids, Filter additionalFilter, SortOrders sortOrders, FetchJoinInformation... joins);


    /**
     * Fetches an entity based on a unique property
     *
     * @param propertyName  the name of the property
     * @param value         the value of the property
     * @param caseSensitive indicates whether the value is case-sensitive
     * @param joins         the desired relations to fetch
     * @return the entity identified by the unique property value, or null if no such entity can be found
     */
    T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive, FetchJoinInformation... joins);

    /**
     * Returns all entities that match the provided filter
     *
     * @param filter the filter
     * @return a list containing the entities that match the filter
     */
    default List<T> find(Filter filter) {
        return find(filter, new SortOrder[0]);
    }

    /**
     * Returns all entities that match the provided filter, sorted according to the
     * provided sort orders
     *
     * @param filter the filter
     * @param sortOrders the sort orders that must be used
     * @return a list containing the entities that match the filter
     */
    List<T> find(Filter filter, SortOrder... sortOrders);

    /**
     * Returns a list of all entities. Use with caution
     * @return a list containing all the entities
     */
    default List<T> findAll() {
        return findAll(new SortOrder[0]);
    }

    /**
     * Returns a list of all entities, sorted according to the provided sort orders
     *
     * @param sortOrders the sort orders that must be used
     * @return a list containing all the entities
     */
    List<T> findAll(SortOrder... sortOrders);

    /**
     * Finds an object based on its ID
     *
     * @param id the ID
     * @return the object identified by the ID, or <code>null</code> if it cannot be
     *         found
     */
    T findById(ID id);

    /**
     * Retrieves an object based on a unique property value. Does not apply any fetches
     *
     * @param propertyName  the name of the property
     * @param value         the desired value of the property
     * @param caseSensitive whether the match is case-sensitive
     * @return the entity identified by the unique property value, or null if no such entity can be found
     */
    T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive);

    /**
     * Returns all distinct values that appear in a certain field for all entities
     * that match the provided filter
     *
     * @param filter        the filter
     * @param distinctField the field for which to return the distinct values
     * @param resultType    the type of the distinct values
     * @param sortOrders    the sort orders
     * @return the list of distinct values
     */
    <S> List<S> findDistinctValues(Filter filter, String distinctField, Class<S> resultType, SortOrder... sortOrders);

    /**
     * Returns the IDs of the entities that match the provided filter
     *
     * @param filter     the filter
     * @param maxResults limit the amount of results
     * @param sortOrders the sort orders
     * @return a list containing the IDs
     */
    List<ID> findIds(Filter filter, Integer maxResults, SortOrder... sortOrders);

    /**
     * Returns the IDs of the entities that match the provided filter
     *
     * @param filter the filter
     * @param sortOrders the sort orders
     * @return a list containing the IDs
     */
    List<ID> findIds(Filter filter, SortOrder... sortOrders);

    /**
     * Returns the class of the entity managed by this DAO
     *
     * @return the entity class
     */
    Class<T> getEntityClass();

}
