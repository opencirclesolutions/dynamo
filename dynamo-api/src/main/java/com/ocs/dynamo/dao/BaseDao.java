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
package com.ocs.dynamo.dao;

import java.util.List;

import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

/**
 * Interface that all DAO objects must implement
 * 
 * @author bas.rutten
 * @param <ID>
 *            The type of the primary key of the entity managed by this DAO
 * @param <T>
 *            The type of the entity managed by this DAO
 */
public interface BaseDao<ID, T extends AbstractEntity<ID>> {

    /**
     * Returns the total number of entities of this type
     * 
     * @return
     */
    long count();

    /**
     * Returns the number of entities that match the provided filter
     * 
     * @param filter
     *            the filter
     * @param distinct
     *            whether to return only distinct results
     * @return
     */
    long count(Filter filter, boolean distinct);

    /**
     * Deletes all entities in the provided list
     * 
     * @param list
     *            the list of entities to delete
     */
    void delete(List<T> list);

    /**
     * Deletes the provided entity
     * 
     * @param entity
     *            the entity to delete
     */
    void delete(T entity);

    /**
     * Fetches entities that match the provided filter
     * 
     * @param filter
     *            the filter
     * @param joins
     *            the desired relations to fetch
     * @return
     */
    List<T> fetch(Filter filter, FetchJoinInformation... joins);

    /**
     * Fetches entities that match the provided filter
     * 
     * @param filter
     *            the filter
     * @param pageable
     *            the page info
     * @param joins
     *            the desired relations to fetch
     * @return
     */
    List<T> fetch(Filter filter, Pageable pageable, FetchJoinInformation... joins);

    /**
	 * Fetches entities that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param orders
	 *            the sort info
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
    List<T> fetch(Filter filter, SortOrders orders, FetchJoinInformation... joins);

    /**
	 * Fetches and sorts properties (NOT ENTITIES) that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param selectProperties
	 *            the properties to use in the selection
	 * @param orders
	 *            the sort info
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<Object[]> fetchSelect(Filter filter, String[] selectProperties, SortOrders orders,
			FetchJoinInformation... joins);

	/**
	 * Fetches and sorts properties (NOT ENTITIES) that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param selectProperties
	 *            the properties to use in the selection
	 * @param pageable
	 *            object containing the paging data
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<Object[]> fetchSelect(Filter filter, String[] selectProperties, Pageable pageable,
			FetchJoinInformation... joins);

	/**
	 * Fetches an entity (and its relations) based on its ID
	 * 
	 * @param id
	 *            the ID of the entity
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
    T fetchById(ID id, FetchJoinInformation... joins);

    /**
     * Fetches the entities identified by the provided IDs
     * 
     * @param ids
     *            the IDs of the entities to fetch
     * @param sort
     *            the sort order
     * @param joins
     *            the desired relations to fetch
     * @return
     */
    List<T> fetchByIds(List<ID> ids, SortOrders sortOrders, FetchJoinInformation... joins);

    /**
     * Fetches an entity based on a unique property
     * 
     * @param propertyName
     *            the name of the property
     * @param value
     *            the value of the property
     * @param caseSensitive
     *            indicates whether the value is case sensitive
     * @param joins
     *            the desired relations to fetch
     * 
     * @return
     */
    T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive, FetchJoinInformation... joins);

    /**
     * Returns all entities that match the provided filter
     * 
     * @param filter
     *            the filter
     * @return
     */
    List<T> find(Filter filter);

    /**
     * Returns all entities that match the provided filter
     * 
     * @param filter
     *            the filter
     * @param sort
     *            the sort info
     * @return
     */
    List<T> find(Filter filter, SortOrder... orders);

    /**
     * Returns a list of all entities. Use with caution
     * 
     * @return
     */
    List<T> findAll();

    /**
     * Returns a list of all entities. Use with caution
     * 
     * @param sort
     *            the desired sorting information
     * @return
     */
    List<T> findAll(SortOrder... sortOrders);

    /**
     * Finds an object based on its ID
     * 
     * @param id
     *            the ID
     * @return the object identified by the ID, or <code>null</code> if it cannot be found
     */
    T findById(ID id);

    /**
     * Finds an object based on a unique property value
     * 
     * @param propertyName
     *            the name of the property
     * @param value
     *            the desired value of the property
     * @param caseSensitive
     *            whether the match is case sensitive
     * @return
     */
    T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive);

    /**
     * Returns the IDS of the entities that match the provided filter
     * 
     * @param filter
     *            the filter
     * @param sort
     *            the desired sorting
     * @return
     */
    List<ID> findIds(Filter filter, SortOrder... orders);

    /**
     * Flushes and clears the entity manager (useful after an explicit update or delete)
     */
    void flushAndClear();

    /**
     * Returns the class of the entity managed by this DAO
     * 
     * @return
     */
    Class<T> getEntityClass();

    /**
     * Saves the provide list of entities
     * 
     * @param list
     *            the list of entities
     * @return
     */
    List<T> save(List<T> list);

    /**
     * Saves the provided entity
     * 
     * @param entity
     *            the entity to save
     * @return
     */
    T save(T entity);

    /**
     * Returns all entities that match the provided filter and apply a distinct on the given column
     * 
     * @param filter
     *            the filter
     * @param distinctField
     *            the field used to remove duplicate rows
     * @param orders
     *            the sort info
     * @return
     */
    <S> List<S> findDistinct(Filter filter, String distinctField, Class<S> elementType, SortOrder... orders);

    /**
     * Returns all distinct values in a collection table
     * 
     * @param tableName
     *            the table name
     * @param distinctField
     *            the distinct field
     * @param elementType
     *            the element type
     * @return
     */
    <S> List<S> findDistinctInCollectionTable(String tableName, String distinctField, Class<S> elementType);
}
