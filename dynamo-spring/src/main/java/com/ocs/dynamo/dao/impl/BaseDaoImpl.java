package com.ocs.dynamo.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.beans.support.SortDefinition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.PathBuilder;
import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.query.FetchJoinInformation;
import com.ocs.dynamo.dao.query.JpaQueryBuilder;
import com.ocs.dynamo.domain.AbstractEntity;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.ocs.dynamo.filter.Filter;

/**
 * Base class for all DAO implementations
 * 
 * @author bas.rutten
 * @param <ID>
 *            type parameter, the type of the primary key of the domain object
 * @param <T>
 *            type parameter, the domain object class
 */
public abstract class BaseDaoImpl<ID, T extends AbstractEntity<ID>> implements BaseDao<ID, T> {

	@PersistenceContext
	private EntityManager entityManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSorting(JPAQuery query, SortDefinition... sorts) {
		PathBuilder<T> builder = new PathBuilder<T>(getDslRoot().getType(),
		        getDslRoot().getMetadata());

		for (SortDefinition s : sorts) {
			Expression<Object> property = builder.get(s.getProperty());
			query.orderBy(new OrderSpecifier(s.isAscending() ? Order.ASC : Order.DESC, property));
		}
	}

	@Override
	public long count() {
		return createQuery().count();
	}

	@Override
	public long count(Filter filter, boolean distinct) {
		CriteriaQuery<Long> cq = JpaQueryBuilder.createCountQuery(entityManager, getEntityClass(),
		        filter, distinct);
		TypedQuery<Long> query = entityManager.createQuery(cq);
		return query.getSingleResult();
	}

	@Override
	public long count(Predicate predicate) {
		JPAQuery query = createQuery();
		if (predicate != null) {
			query.where(predicate);
		}
		return query.count();
	}

	/**
	 * Creates a default query that simply retrieves instances of the domain
	 * class
	 * 
	 * @return
	 */
	protected JPAQuery createQuery() {
		JPAQuery query = new JPAQuery(entityManager);
		query.from(getDslRoot());
		return query;
	}

	@Override
	public void delete(List<T> list) {
		for (T t : list) {
			delete(t);
		}
	}

	@Override
	public void delete(T t) {
		t = entityManager.merge(t);
		entityManager.remove(t);
	}

	@Override
	public T fetchById(ID id, FetchJoinInformation... joins) {
		CriteriaQuery<T> cq = JpaQueryBuilder.createFetchSingleObjectQuery(entityManager,
		        getEntityClass(), id,
		        (joins != null && joins.length > 0) ? joins : getFetchJoins());
		TypedQuery<T> query = entityManager.createQuery(cq);
		return getFirstValue(query.getResultList());
	}

	@Override
	public List<T> fetchByIds(List<ID> ids, Sort sort, FetchJoinInformation... joins) {
		CriteriaQuery<T> cq = JpaQueryBuilder.createFetchQuery(entityManager, getEntityClass(), ids,
		        sort, (joins != null && joins.length > 0) ? joins : getFetchJoins());
		TypedQuery<T> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	@Override
	public List<T> find(Filter filter) {
		return find(filter, (Pageable) null);
	}

	@Override
	public List<T> find(Filter filter, Sort sort, FetchJoinInformation... fetchJoins) {
		CriteriaQuery<T> cq = JpaQueryBuilder.createSelectQuery(filter, entityManager,
		        getEntityClass(), fetchJoins, sort);

		TypedQuery<T> query = entityManager.createQuery(cq);
		return query.getResultList();
	}

	@Override
	public List<T> find(Filter filter, Pageable pageable, FetchJoinInformation... fetchJoins) {
		// Create select and where clauses
		CriteriaQuery<T> cq = JpaQueryBuilder.createSelectQuery(filter, entityManager,
		        getEntityClass(), fetchJoins, pageable != null ? pageable.getSort() : null);

		TypedQuery<T> query = entityManager.createQuery(cq);

		// Limit results
		if (pageable != null) {
			query.setFirstResult(pageable.getOffset());
			query.setMaxResults(pageable.getPageSize());
		}

		return query.getResultList();
	}

	@Override
	public List<T> find(Predicate predicate) {
		JPAQuery query = createQuery();
		if (predicate != null) {
			query.where(predicate);
		}
		return query.list(getDslRoot());
	}

	@Override
	public List<T> find(Predicate predicate, int firstIndex, int maxResults,
	        SortDefinition... sorts) {
		JPAQuery query = createQuery();
		if (predicate != null) {
			query.where(predicate);
		}
		query.offset(firstIndex);
		query.limit(maxResults);

		addSorting(query, sorts);
		return query.list(getDslRoot());
	}

	@Override
	public List<T> findAll(Sort sort) {
		return find(null, sort);
	}

	@Override
	public T findById(ID id) {
		return entityManager.find(getEntityClass(), id);
	}

	@Override
	public T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive) {
		CriteriaQuery<T> cq = JpaQueryBuilder.createUniquePropertyQuery(entityManager,
		        getEntityClass(), propertyName, value, caseSensitive);
		TypedQuery<T> query = entityManager.createQuery(cq);
		try {
			return query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		} catch (NonUniqueResultException ex) {
			throw new OCSRuntimeException("Query for unique property returned multiple results",
			        ex);
		}
	}

	@Override
	public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
	        FetchJoinInformation... joins) {
		CriteriaQuery<T> cq = JpaQueryBuilder.createUniquePropertyFetchQuery(entityManager,
		        getEntityClass(), (joins == null || joins.length == 0) ? getFetchJoins() : joins,
		        propertyName, value, caseSensitive);
		TypedQuery<T> query = entityManager.createQuery(cq);
		try {
			return query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		} catch (NonUniqueResultException ex) {
			throw new OCSRuntimeException("Query for unique property returned multiple results",
			        ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ID> findIds(Filter filter, Sort sort) {
		CriteriaQuery<Tuple> cq = JpaQueryBuilder.createIdQuery(entityManager, getEntityClass(),
		        filter, sort);

		TypedQuery<Tuple> query = entityManager.createQuery(cq);
		List<Tuple> temp = query.getResultList();
		List<ID> result = new ArrayList<>();

		for (Tuple t : temp) {
			ID id = (ID) t.get(0);
			result.add(id);
		}

		return result;
	}

	@Override
	public void flushAndClear() {
		entityManager.flush();
		entityManager.clear();
	}

	/**
	 * Returns the query DSL root
	 * 
	 * @return
	 */
	protected abstract EntityPathBase<T> getDslRoot();

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Returns the fetch joins that must be included in a query to fetch the
	 * IDs. This method return an empty array by default - override when needed
	 * 
	 * @return
	 */
	protected FetchJoinInformation[] getFetchJoins() {
		return new FetchJoinInformation[] {};
	}

	/**
	 * Returns the first value of a list
	 * 
	 * @param list
	 *            the list
	 * @return the first value of the list, or <code>null</code> if this does
	 *         not exist
	 */
	protected T getFirstValue(List<T> list) {
		return list != null && !list.isEmpty() ? list.get(0) : null;
	}

	@Override
	public List<T> save(List<T> list) {
		for (int i = 0; i < list.size(); i++) {
			T t = list.get(i);
			t = save(t);
			list.set(i, t);
		}
		return list;
	}

	@Override
	public T save(T t) {
		if (t.getId() == null) {
			entityManager.persist(t);
		} else {
			t = entityManager.merge(t);
		}
		return t;
	}
}
