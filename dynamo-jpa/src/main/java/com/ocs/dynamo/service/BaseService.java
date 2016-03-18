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
package com.ocs.dynamo.service;

import java.util.List;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

/**
 * TODO Description of BaseService.
 * 
 * @param <ID>
 * @param <T>
 */
public interface BaseService<ID, T extends AbstractEntity<ID>> {

    /**
     * Returns the total number of objects of this type
     * 
     * @return
     */
    long count();

    /**
     * Returns the number of objects that match a certain filter
     * 
     * @param filter
     *            the filter
     * @param distinct
     *            whether to return only distinct results
     * @return
     */
    long count(Filter filter, boolean distinct);

    /**
     * Creates a new entity
     * 
     * @return the newly created entity
     */
    T createNewEntity();

    /**
     * Deletes all of the provided entities
     * 
     * @param list
     *            the list of entities to delete
     */
    void delete(List<T> list);

    /**
     * Deletes the provided entity
     * 
     * @param t
     *            the entity to delete
     */
    void delete(T t);

    /**
     * Fetches entities based on a filter Careful - when any OneToMany or ManyToMany relations are
     * fetched, this might lead to in-memory calculation!
     * 
     * @param filter
     *            the Filter object
     * @param pageNumber
     *            the number of the first page
     * @param pageSize
     *            the page size
     * @param fetchJoins
     *            the optional fetch join definitions
     * @param orders
     *            the sorting information
     * @return
     */
    List<T> fetch(Filter filter, int pageNumber, int pageSize, FetchJoinInformation[] fetchJoins,
            SortOrder... orders);

    /**
     * Fetches an entity based on its ID
     * 
     * @param id
     *            the ID of the entity
     * @param joins
     *            the desired joins
     * @return
     */
    T fetchById(ID id, FetchJoinInformation... joins);

    /**
     * Fetches the entities identified by the provided IDs
     * 
     * @param ids
     *            the IDs
     * @param joins
     *            the desired joins
     * @param orders
     *            sorting order information
     * @return
     */
    List<T> fetchByIds(List<ID> ids, FetchJoinInformation[] joins, SortOrder... orders);

    /**
     * Fetches an object based on an unique property value
     * 
     * @param propertyName
     *            the name of the property
     * @param value
     *            the (unique) value
     * @param caseSensitive
     *            indicates whether string comparison is case sensitive
     * @return
     */
    T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
            FetchJoinInformation... joins);

    /**
     * Finds entities based on a Filter
     * 
     * @param filter
     *            the Filter object
     * @param pageNumber
     *            the number of the first page
     * @param pageSize
     *            the page size
     * @param orders
     *            the sorting information
     * @return
     */
    List<T> find(Filter filter, int pageNumber, int pageSize, SortOrder... orders);

    /**
     * Finds entities based on a filter and a sort order
     * 
     * @param filter
     * @param orders
     * @return
     */
    List<T> find(Filter filter, SortOrder... orders);

    /**
     * Returns a list of all domain objects of a certain type. Use with caution
     * 
     * @return
     */
    List<T> findAll(SortOrder... orders);

    /**
     * Finds an object based on its ID
     * 
     * @param id
     *            the ID
     * @return the object identified by the ID, or <code>null</code> if it cannot be found
     */
    T findById(ID id);

    /**
     * Finds an item based on a unique property value
     * 
     * @param propertyName
     *            the name of the property
     * @param value
     *            the (unique) value
     * @param caseSensitive
     *            indicates if string comparison is case sensitive when comparing
     * @return
     */
    T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive);

    /**
     * Finds entity IDs based on a filter
     * 
     * @param filter
     * @param orders
     * @return
     */
    List<ID> findIds(Filter filter, SortOrder... orders);

    /**
     * Returns the class of the entities managed by this server
     * 
     * @return
     */
    Class<T> getEntityClass();

    /**
     * Saves a list of entities
     * 
     * @param list
     *            the list of entities
     * @return
     */
    List<T> save(List<T> list);

    /**
     * Saves the provided entity
     * 
     * @param t
     *            the entity to save
     * @return
     */
    T save(T t);
}
