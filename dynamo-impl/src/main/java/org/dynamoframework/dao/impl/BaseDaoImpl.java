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
package org.dynamoframework.dao.impl;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaQuery;
import org.dynamoframework.dao.*;
import org.dynamoframework.domain.AbstractEntity;
import org.dynamoframework.domain.model.EntityModelFactory;
import org.dynamoframework.exception.OCSRuntimeException;
import org.dynamoframework.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for all DAO implementations
 *
 * @param <ID> type parameter, the type of the primary key of the domain object
 * @param <T>  type parameter, the entity class
 * @author bas.rutten
 */
public abstract class BaseDaoImpl<ID, T extends AbstractEntity<ID>> implements BaseDao<ID, T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityModelFactory entityModelFactory;

    /**
     * Adds a parameter to a query but only if the provided value is not null
     *
     * @param query the query to add the parameter to
     * @param name  the name of the parameter
     * @param value the value of the parameter
     */
    protected void addParameter(Query query, String name, Object value) {
        if (value != null) {
            query.setParameter(name, value);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public long count() {
        return createQuery().fetchCount();
    }

    @Override
    public long count(Filter filter, boolean distinct) {
        TypedQuery<Long> query = JpaQueryBuilder.createCountQuery(entityManager, getEntityClass(), filter, distinct);
        return query.getSingleResult();
    }

    /**
     * Creates a new JPADeleteClause for the entity
     *
     * @return the newly created JPADeleteClause
     */
    protected JPADeleteClause createDeleteClause() {
        return new JPADeleteClause(getEntityManager(), getDslRoot());
    }

    /**
     * Creates a default query that simply retrieves instances of the domain class
     *
     * @return the newly created JPAQuery
     */
    protected JPAQuery<T> createQuery() {
        JPAQuery<T> query = new JPAQuery<>(entityManager);
        query.from(getDslRoot());
        return query;
    }

    /**
     * Creates a new JPAUpdateClause for the entity
     *
     * @return the newly created JPAUpdateClause
     */
    protected JPAUpdateClause createUpdateClause() {
        return new JPAUpdateClause(getEntityManager(), getDslRoot());
    }

    @Override
    public void delete(List<T> list) {
        list.forEach(this::delete);
    }

    @Override
    public void delete(T entity) {
        entity = entityManager.merge(entity);
        entityManager.remove(entity);
    }

    @Override
    public void deleteAll() {
        this.delete(this.findAll());
    }

    @Override
    public List<T> fetch(Filter filter, FetchJoinInformation... joins) {
        return fetch(filter, null, null, joins);
    }

    @Override
    public List<T> fetch(Filter filter, Pageable pageable, FetchJoinInformation... joins) {
        return fetch(filter, pageable, pageable == null ? null : pageable.getSortOrders(), joins);
    }

    /**
     * Executes a fetch query - watch out, paging combined with one-to-many joins
     * leads to everything being calculated in-memory
     *
     * @param filter     the filter to apply
     * @param pageable   object containing the paging data
     * @param sortOrders list of sort orders that must be applied
     * @param joins      the joins to apply - if null then the default joins will be
     *                   used
     * @return a page of entities that match the filter
     */
    private List<T> fetch(Filter filter, Pageable pageable, SortOrders sortOrders, FetchJoinInformation... joins) {
        TypedQuery<T> query = JpaQueryBuilder.createSelectQuery(filter, entityManager, getEntityClass(),
                (joins == null || joins.length == 0) ? getJoins() : joins,
                sortOrders == null ? null : sortOrders.toArray());

        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    protected FetchJoinInformation[] getJoins() {
        return entityModelFactory.getModel(getEntityClass())
                .getFetchJoins().toArray(new FetchJoinInformation[0]);
    }

    protected FetchJoinInformation[] getDetailJoins() {
        return entityModelFactory.getModel(getEntityClass())
                .getDetailJoins().toArray(new FetchJoinInformation[0]);
    }

    @Override
    public List<T> fetch(Filter filter, SortOrders sortOrders, FetchJoinInformation... joins) {
        return fetch(filter, null, sortOrders, joins);
    }

    @Override
    public T fetchById(ID id, FetchJoinInformation... joins) {
        TypedQuery<T> query = JpaQueryBuilder.createFetchSingleObjectQuery(entityManager, getEntityClass(), id,
                (joins != null && joins.length > 0) ? joins : getDetailJoins());
        return getFirstValue(query.getResultList());
    }

    @Override
    public List<T> fetchByIds(List<ID> ids, Filter additionalFilter, SortOrders sortOrders,
                              FetchJoinInformation... joins) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<T> query = JpaQueryBuilder.createFetchQuery(entityManager, getEntityClass(), ids, additionalFilter,
                sortOrders, (joins != null && joins.length > 0) ? joins : getJoins());
        return query.getResultList();
    }

    @Override
    public List<T> fetchByIds(List<ID> ids, SortOrders sortOrders, FetchJoinInformation... joins) {
        return fetchByIds(ids, null, sortOrders, joins);
    }

    @Override
    public T fetchByUniqueProperty(String propertyName, Object value, boolean caseSensitive,
                                   FetchJoinInformation... joins) {
        CriteriaQuery<T> cq = JpaQueryBuilder.createUniquePropertyFetchQuery(entityManager, getEntityClass(),
                (joins == null || joins.length == 0) ? getDetailJoins() : joins, propertyName, value, caseSensitive);
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

    private List<T> find(Filter filter, Pageable pageable, SortOrders sortOrders) {
        TypedQuery<T> query = JpaQueryBuilder.createSelectQuery(filter, entityManager, getEntityClass(), null,
                sortOrders == null ? null : sortOrders.toArray());

        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public List<T> find(Filter filter, SortOrder... orders) {
        return fetch(filter, null, new SortOrders(orders), (FetchJoinInformation[]) null);
    }

    @Override
    public List<T> findAll() {
        return findAll((SortOrder[]) null);
    }

    @Override
    public List<T> findAll(SortOrder... sortOrders) {
        return find(null, null, new SortOrders(sortOrders));
    }

    @Override
    public T findById(ID id) {
        return entityManager.find(getEntityClass(), id);
    }

    @Override
    public T findByUniqueProperty(String propertyName, Object value, boolean caseSensitive) {
        CriteriaQuery<T> cq = JpaQueryBuilder.createUniquePropertyQuery(entityManager, getEntityClass(), propertyName,
                value, caseSensitive);
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
    public <S> List<S> findDistinctValues(Filter filter, String distinctField, Class<S> elementType, SortOrder... orders) {
        TypedQuery<Tuple> query = JpaQueryBuilder.createDistinctQuery(filter, entityManager, getEntityClass(),
                distinctField, orders);
        return query.getResultList().stream().map(t -> t.get(0)).filter(Objects::nonNull).map(o -> (S) o)
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> List<S> findDistinctInCollectionTable(String tableName, String distinctField, Class<S> elementType) {
        String query = "select distinct %s from %s".formatted(distinctField, tableName);
        return getEntityManager().createNativeQuery(query).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ID> findIds(Filter filter, Integer maxResults, SortOrder... sortOrders) {
        TypedQuery<Tuple> query = JpaQueryBuilder.createIdQuery(entityManager, getEntityClass(), filter, sortOrders);
        if (maxResults != null) {
            query = query.setMaxResults(maxResults);
        }
        return query.getResultList().stream().map(tuple -> tuple.get(0)).map(o -> (ID) o).collect(Collectors.toList());
    }

    @Override
    public List<ID> findIds(Filter filter, SortOrder... sortOrders) {
        return findIds(filter, null, sortOrders);
    }

    @Override
    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Returns the query DSL root
     *
     * @return the DSL root object
     */
    protected abstract EntityPathBase<T> getDslRoot();

    protected EntityManager getEntityManager() {
        return entityManager;
    }

//    /**
//     * Returns the fetch joins that must be included in a query to fetch the IDs.
//     * This method return an empty array by default - override when needed
//     *
//     * @return the fetch joins
//     */
//    protected FetchJoinInformation[] getFetchJoins() {
//        return new FetchJoinInformation[]{};
//    }

    /**
     * Returns the first value of a list
     *
     * @param list the list
     * @return the first value of the list, or <code>null</code> if this does not
     * exist
     */
    protected T getFirstValue(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /**
     * Returns an Optional containing the first value of the provided list
     *
     * @param list the list to select the first value from
     * @return the Optional
     */
    protected Optional<T> getFirstValueOptional(List<T> list) {
        return Optional.ofNullable(getFirstValue(list));
    }

    @Override
    public List<T> save(List<T> list) {
        List<T> result = new ArrayList<>();
        for (T entity : list) {
            result.add(this.save(entity));
        }
        return result;
    }

    @Override
    public T save(T entity) {
        if (entity.getId() == null) {
            entityManager.persist(entity);
        } else {
            entity = entityManager.merge(entity);
        }
        return entity;
    }

}
