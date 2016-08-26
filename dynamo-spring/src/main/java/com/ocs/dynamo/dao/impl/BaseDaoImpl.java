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

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Order;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.EntityPathBase;
import com.mysema.query.types.path.PathBuilder;
import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.Pageable;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
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
    private void addSorting(JPAQuery query, SortOrder... sorts) {
        PathBuilder<T> builder = new PathBuilder<T>(getDslRoot().getType(), getDslRoot()
                .getMetadata());

        for (SortOrder s : sorts) {
            if (s != null && s.getProperty() != null) {
                Expression<Object> property = builder.get(s.getProperty());
                query.orderBy(new OrderSpecifier(s.isAscending() ? Order.ASC : Order.DESC, property));
            }
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
     * Creates a default query that simply retrieves instances of the domain class
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
    public List<T> fetch(Filter filter, FetchJoinInformation... joins) {
        return fetch(filter, null, null, joins);
    }

    @Override
    public List<T> fetch(Filter filter, Pageable pageable, FetchJoinInformation... joins) {
        return fetch(filter, pageable, pageable.getSortOrders(), joins);
    }

    private List<T> fetch(Filter filter, Pageable pageable, SortOrders sortOrders,
            FetchJoinInformation... joins) {
        // Create select and where clauses
        CriteriaQuery<T> cq = JpaQueryBuilder.createSelectQuery(filter, entityManager,
                getEntityClass(), (joins == null || joins.length == 0) ? getFetchJoins() : joins,
                sortOrders == null ? null : sortOrders.toArray());

        TypedQuery<T> query = entityManager.createQuery(cq);

        // Limit results
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList();
    }

    @Override
    public List<T> fetch(Filter filter, SortOrders sortOrders, FetchJoinInformation... joins) {
        return fetch(filter, null, sortOrders, joins);
    }

    @Override
    public T fetchById(ID id, FetchJoinInformation... joins) {
        CriteriaQuery<T> cq = JpaQueryBuilder
                .createFetchSingleObjectQuery(entityManager, getEntityClass(), id,
                        (joins != null && joins.length > 0) ? joins : getFetchJoins());
        TypedQuery<T> query = entityManager.createQuery(cq);
        return getFirstValue(query.getResultList());
    }

    @Override
    public List<T> fetchByIds(List<ID> ids, SortOrders sortOrders, FetchJoinInformation... joins) {
        CriteriaQuery<T> cq = JpaQueryBuilder.createFetchQuery(entityManager, getEntityClass(),
                ids, sortOrders, (joins != null && joins.length > 0) ? joins : getFetchJoins());
        TypedQuery<T> query = entityManager.createQuery(cq);
        return query.getResultList();
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
            throw new OCSRuntimeException("Query for unique property returned multiple results", ex);
        }
    }

    @Override
    public List<T> find(Filter filter) {
        return fetch(filter, null, null, (FetchJoinInformation[]) null);
    }

    @Override
    public List<T> find(Filter filter, SortOrder... orders) {
        return fetch(filter, null, new SortOrders(orders), (FetchJoinInformation[]) null);
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
            SortOrder... sortOrders) {
        JPAQuery query = createQuery();
        if (predicate != null) {
            query.where(predicate);
        }
        query.offset(firstIndex);
        query.limit(maxResults);

        addSorting(query, sortOrders);
        return query.list(getDslRoot());
    }

    @Override
    public List<T> findAll() {
        return findAll((SortOrder[]) null);
    }

    @Override
    public List<T> findAll(SortOrder... sortOrders) {
        return fetch(null, null, new SortOrders(sortOrders));
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
            throw new OCSRuntimeException("Query for unique property returned multiple results", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ID> findIds(Filter filter, SortOrder... sortOrders) {
        CriteriaQuery<Tuple> cq = JpaQueryBuilder.createIdQuery(entityManager, getEntityClass(),
                filter, sortOrders);

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
     * Returns the fetch joins that must be included in a query to fetch the IDs. This method return
     * an empty array by default - override when needed
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
     * @return the first value of the list, or <code>null</code> if this does not exist
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
