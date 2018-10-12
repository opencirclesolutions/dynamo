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

import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

/**
 * The interface for a service that manages an entity
 * 
 * @param <ID>
 *            the type of the primary key of the entity
 * @param <T>
 *            the type of the entity
 */
public interface BaseService<ID, T extends AbstractEntity<ID>> {

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
	 * Creates a new entity
	 * 
	 * @return
	 */
	T createNewEntity();

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
	 *            the filter ot match
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<T> fetch(Filter filter, FetchJoinInformation... joins);

	/**
	 * Fetches a page of entities that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param pageNumber
	 *            the page number of the page to fetch
	 * @param pageSize
	 *            the page size
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<T> fetch(Filter filter, int pageNumber, int pageSize, FetchJoinInformation... joins);

	/**
	 * Fetches a page of entities that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param pageNumber
	 *            the page number of the page to fetch
	 * @param pageSize
	 *            the page size
	 * @param sortOrders
	 *            any sort orders to apply to the search results
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<T> fetch(Filter filter, int pageNumber, int pageSize, SortOrders sortOrders, FetchJoinInformation... joins);

	/**
	 * Fetches and sorts properties (NOT ENTITIES) that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param selectProperties
	 *            the properties to use in the selection
	 * @param sortOrders
	 *            the sort order
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<Object[]> fetchSelect(Filter filter, String[] selectProperties, SortOrders sortOrders,
			FetchJoinInformation... joins);

	/**
	 * Fetches and sorts properties (NOT ENTITIES) that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param selectProperties
	 *            the properties to use in the selection
	 * @param pageNumber
	 *            the page number of the page to fetch
	 * @param pageSize
	 *            the page size
	 * @param sortOrders
	 *            the sort order
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<Object[]> fetchSelect(Filter filter, String[] selectProperties, int pageNumber, int pageSize,
			SortOrders sortOrders, FetchJoinInformation... joins);

	/**
	 * Fetches a list of entities that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param order
	 *            the sort orders to apply
	 * @param joins
	 *            the desired relations to fetch
	 * @return
	 */
	List<T> fetch(Filter filter, SortOrders orders, FetchJoinInformation... joins);

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
	 *            the IDs
	 * @param joins
	 *            desired relations to fetch
	 * @return
	 */
	List<T> fetchByIds(List<ID> ids, FetchJoinInformation... joins);

	/**
	 * Fetches the entities identified by the provided IDs
	 * 
	 * @param ids
	 *            the IDs of the entities to fetch
	 * @param sortOrders
	 *            the sort orders to apply
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
	 * Returns all entities that match the provided filter, sorted according to the
	 * provided sort orders
	 * 
	 * @param filter
	 *            the filter
	 * @param orders
	 *            the sort order
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
	 * Returns a list of all entities, sorted according to the provided sort orders
	 * 
	 * @param sort
	 *            the desired sort orders
	 * @return
	 */
	List<T> findAll(SortOrder... sortOrders);

	/**
	 * Finds an object based on its ID
	 * 
	 * @param id
	 *            the ID
	 * @return the object identified by the ID, or <code>null</code> if it cannot be
	 *         found
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
	 * Returns all distinct values that appear in a certain field for all entities
	 * that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param distinctField
	 *            the field for which to return the distinct values
	 * @param resultType
	 *            the type of the distinct values
	 * @param orders
	 *            the sort orders to apply
	 * @return
	 */
	<S> List<S> findDistinct(Filter filter, String distinctField, Class<S> resultType, SortOrder... orders);

	/**
	 * Finds distinct elements in a collection table
	 * 
	 * @param tableName
	 *            the name of the table
	 * @param distinctField
	 *            the name of the field
	 * @param elementType
	 *            the type of the values to return
	 * @return
	 */
	<S> List<S> findDistinctInCollectionTable(String tableName, String distinctField, Class<S> elementType);

	/**
	 * Returns the IDs of the entities that match the provided filter
	 * 
	 * @param filter
	 *            the filter
	 * @param sort
	 *            the desired sorting
	 * @return
	 */
	List<ID> findIds(Filter filter, SortOrder... orders);

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
	 * Updates, adds, and deletes
	 * 
	 * @param toUpdate
	 *            to update
	 * @param toAdd
	 *            to add
	 * @param toDelete
	 *            to delete
	 */
	void update(List<T> toUpdate, List<T> toAdd, List<T> toDelete);

	/**
	 * 
	 * @param t
	 */
	void validate(T t);
}
