package com.ocs.dynamo.dao;

import java.util.List;

import org.springframework.beans.support.SortDefinition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.mysema.query.types.Predicate;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.filter.Filter;

/**
 * Interface that all DAO objects must implement
 * 
 * @author bas.rutten
 * 
 * @param <ID>
 *            type parameter, the type of the primary key
 * @param <T>
 *            type parmaeter, the entity class
 */
public interface BaseDao<ID, T extends AbstractEntity<ID>> {

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
	 * Returns the number of objects that match a certain predicate
	 * 
	 * @return
	 */
	public long count(Predicate predicate);

	/**
	 * Deletes all of the provided objects
	 * 
	 * @param list
	 */
	public void delete(List<T> list);

	/**
	 * Deletes the provided object
	 * 
	 * @param t
	 */
	public void delete(T t);

	/**
	 * Fetches and object (and its relevant related objects) by ID. By default
	 * this will simply delegate to the "findById" method
	 * 
	 * @param id
	 * @return
	 */
	public T fetchById(ID id, FetchJoinInformation... joins);

	/**
	 * Fetches the entities identified by the provided IDs
	 * 
	 * @param ids
	 * @param sort
	 * @param joins
	 *            optional fetch join information
	 * @return
	 */
	public List<T> fetchByIds(List<ID> ids, Sort sort, FetchJoinInformation... joins);

	/**
	 * Finds all objects that match a certain filter
	 * 
	 * @param filter
	 * @return
	 */
	public List<T> find(Filter filter);

	/**
	 * Find all objects that match a certain filter
	 * 
	 * @param filter
	 * @param sort
	 * @return
	 */
	public List<T> find(Filter filter, Sort sort, FetchJoinInformation... fetchJoins);

	/**
	 * Finds the objects that match a certain filter, and returns a page of
	 * those objects
	 * 
	 * @param filter
	 * @param pageable
	 * @param joins
	 *            optional fetch join information
	 * @return
	 */
	public List<T> find(Filter filter, Pageable pageable, FetchJoinInformation... fetchJoins);

	/**
	 * Finds all domain objects that match a certain predicate
	 * 
	 * @param predicate
	 * @return
	 */
	public List<T> find(Predicate predicate);

	/**
	 * Finds domain objects that match a certain predicate and return a list
	 * (page)
	 * 
	 * @param predicate
	 * @param firstIndex
	 *            the index of the first result
	 * @param maxResults
	 *            the maximum number of results
	 * @return
	 */
	public List<T> find(Predicate predicate, int firstIndex, int maxResults,
			SortDefinition... sorts);

	/**
	 * Returns a list of all domain objects of a certain type. Use with caution
	 * 
	 * @param sort
	 *            the desired sorting information
	 * 
	 * @return
	 */
	public List<T> findAll(Sort sort);

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
	 * Finds an object based on a unique property
	 * 
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive);

	/**
	 * Fetches an object based on a unique property
	 * 
	 * @param propertyName
	 *            the name of the property
	 * @param value
	 *            the value of the property
	 * @param caseSensitive
	 *            indicates whether the value is case sensitive
	 * @return
	 */
	public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
			FetchJoinInformation... joins);

	/**
	 * Finds entity IDs based on a filter
	 * 
	 * @param filter
	 * @param distinct
	 * @param sort
	 * @return
	 */
	public List<ID> findIds(Filter filter, Sort sort);

	/**
	 * Flushes and clears the entity manager (useful after an explicit update or
	 * delete)
	 */
	public void flushAndClear();

	/**
	 * Returns the class of the entity managed by this DAO
	 * 
	 * @return
	 */
	public Class<T> getEntityClass();

	/**
	 * Saves a list of entities
	 * 
	 * @param t
	 * @return
	 */
	public List<T> save(List<T> list);

	/**
	 * Saves the provided entity
	 * 
	 * @param t
	 * @return
	 */
	public T save(T t);

}
