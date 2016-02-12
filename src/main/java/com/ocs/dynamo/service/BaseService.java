package com.ocs.dynamo.service;

import java.util.List;

import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

public interface BaseService<ID, T extends AbstractEntity<ID>> {

	/**
	 * Returns the total number of objects of this type
	 * 
	 * @return
	 */
	public long count();

	/**
	 * Returns the number of objects that match a certain filter
	 * 
	 * @param filter
	 *            the filter
	 * @param distinct
	 *            whether to return only distinct results
	 * @return
	 */
	public long count(Filter filter, boolean distinct);

	/**
	 * Creates a new entity
	 * 
	 * @return the newly created entity
	 */
	public T createNewEntity();

	/**
	 * Deletes all of the provided entities
	 * 
	 * @param list
	 *            the list of entities to delete
	 */
	public void delete(List<T> list);

	/**
	 * Deletes the provided entity
	 * 
	 * @param t
	 *            the entity to delete
	 */
	public void delete(T t);

	/**
	 * Fetches entities based on a filter
	 * 
	 * Careful - when any OneToMany or ManyToMany relations are fetched, this
	 * might lead to in-memory calculation!
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
	public List<T> fetch(Filter filter, int pageNumber, int pageSize,
			FetchJoinInformation[] fetchJoins, SortOrder... orders);

	/**
	 * Fetches an entity based on its ID
	 * 
	 * @param id
	 *            the ID of the entity
	 * @param joins
	 *            the desired joins
	 * @return
	 */
	public T fetchById(ID id, FetchJoinInformation... joins);

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
	public List<T> fetchByIds(List<ID> ids, FetchJoinInformation[] joins, SortOrder... orders);

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
	public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
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
	public List<T> find(Filter filter, int pageNumber, int pageSize, SortOrder... orders);

	/**
	 * Finds entities based on a filter and a sort order
	 * 
	 * @param filter
	 * @param orders
	 * @return
	 */
	public List<T> find(Filter filter, SortOrder... orders);

	/**
	 * Returns a list of all domain objects of a certain type. Use with caution
	 * 
	 * @return
	 */
	public List<T> findAll(SortOrder... orders);

	/**
	 * Finds an object based on its ID
	 * 
	 * @param id
	 *            the ID
	 * @return the object identified by the ID, or <code>null</code> if it
	 *         cannot be found
	 */
	public T findById(ID id);

	/**
	 * Finds an item based on a unique property value
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @param value
	 *            the (unique) value
	 * @param caseSensitive
	 *            indicates if string comparison is case sensitive when
	 *            comparing
	 * @return
	 */
	public T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive);

	/**
	 * Finds entity IDs based on a filter
	 * 
	 * @param filter
	 * @param orders
	 * @return
	 */
	public List<ID> findIds(Filter filter, SortOrder... orders);

	/**
	 * Returns the class of the entities managed by this server
	 * 
	 * @return
	 */
	public Class<T> getEntityClass();

	/**
	 * Saves a list of entities
	 * 
	 * @param list
	 *            the list of entities
	 * @return
	 */
	public List<T> save(List<T> list);

	/**
	 * Saves the provided entity
	 * 
	 * @param t
	 *            the entity to save
	 * @return
	 */
	public T save(T t);
}
