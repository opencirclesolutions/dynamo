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

import com.google.common.collect.Lists;
import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.SortOrder;
import com.ocs.dynamo.dao.SortOrders;
import com.ocs.dynamo.filter.And;
import com.ocs.dynamo.filter.Between;
import com.ocs.dynamo.filter.Compare;
import com.ocs.dynamo.filter.Contains;
import com.ocs.dynamo.filter.Filter;
import com.ocs.dynamo.filter.In;
import com.ocs.dynamo.filter.IsNull;
import com.ocs.dynamo.filter.Like;
import com.ocs.dynamo.filter.Modulo;
import com.ocs.dynamo.filter.Not;
import com.ocs.dynamo.filter.Or;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author patrick.deenen
 * @author bas.rutten Class for constructing JPA queries built on the criteria
 *         API
 */
public final class JpaQueryBuilder {

	private JpaQueryBuilder() {
		// hidden private constructor
	}

	/**
	 * Adds fetch join information to a query root
	 * 
	 * @param root
	 *            the query root
	 * @param fetchJoins
	 *            the fetch joins
	 * @return <code>true</code> if the fetches include a collection,
	 *         <code>false</code> otherwise
	 */
	private static <T> boolean addFetchJoinInformation(FetchParent<T, ?> root, FetchJoinInformation... fetchJoins) {
		boolean collection = false;

		if (root != null && fetchJoins != null) {
			for (FetchJoinInformation s : fetchJoins) {

				// Support nested properties
				FetchParent<T, ?> fetch = root;
				String[] ppath = s.getProperty().split("\\.");
				for (String prop : ppath) {
					fetch = fetch.fetch(prop, translateJoinType(s.getJoinType()));
				}
			}

			// check if any collection is fetched. If so then the results need
			// to be cleaned up using "distinct"
			collection = isCollectionFetch(root);
		}
		return collection;
	}

	/**
	 * Adds the "order by" clause to a JPA 2 criteria query
	 * 
	 * @param builder
	 *            the criteria builder
	 * @param cq
	 *            the criteria query
	 * @param root
	 *            the query root
	 * @param sortOrders
	 *            the sort orders
	 * @return
	 */
	private static <T, R> CriteriaQuery<R> addSortInformation(CriteriaBuilder builder, CriteriaQuery<R> cq,
			Root<T> root, SortOrder... sortOrders) {
		return addSortInformation(builder, cq, root, (List<Selection<?>>) null, sortOrders);
	}

	/**
	 * Adds the "order by" clause to a JPA 2 criteria query
	 *
	 * @param builder
	 *            the criteria builder
	 * @param cq
	 *            the criteria query
	 * @param root
	 *            the query root
	 * @param multiSelect
	 *            optional properties, when supplied applied as multi select
	 * @param sortOrders
	 *            the sort orders
	 * @return
	 */
	private static <T, R> CriteriaQuery<R> addSortInformation(CriteriaBuilder builder, CriteriaQuery<R> cq,
			Root<T> root, List<Selection<?>> multiSelect, SortOrder... sortOrders) {
		List<Selection<?>> ms = new ArrayList<>();
		if (multiSelect != null && !multiSelect.isEmpty()) {
			ms.addAll(multiSelect);
		}
		if (sortOrders != null && sortOrders.length > 0) {
			List<javax.persistence.criteria.Order> orders = new ArrayList<>();
			for (SortOrder sortOrder : sortOrders) {
				Expression<?> property = getPropertyPath(root, sortOrder.getProperty());
				ms.add(property);
				orders.add(sortOrder.isAscending() ? builder.asc(property) : builder.desc(property));
			}
			cq.orderBy(orders);
		}
		if (multiSelect != null && !ms.isEmpty()) {
			cq.multiselect(ms);
		}
		return cq;
	}

	/**
	 * Creates a predicate based on an "And" filter
	 * 
	 * @param builder
	 *            the criteria builder
	 * @param root
	 *            the root object
	 * @param filter
	 *            the "And" filter
	 * @return
	 */
	private static Predicate createAndPredicate(CriteriaBuilder builder, Root<?> root, Filter filter,
			Map<String, Object> parameters) {
		And and = (And) filter;
		List<Filter> filters = new ArrayList<>(and.getFilters());

		Predicate predicate = null;
		if (!filters.isEmpty()) {
			predicate = createPredicate(filters.remove(0), builder, root, parameters);
			while (!filters.isEmpty()) {
				Predicate next = createPredicate(filters.remove(0), builder, root, parameters);
				if (next != null) {
					predicate = builder.and(predicate, next);
				}
			}
		}
		return predicate;
	}

	/**
	 * Creates a predicate based on a "Compare" filter
	 * 
	 * @param builder
	 *            the criteria builder
	 * @param root
	 *            the query root
	 * @param filter
	 *            the Compare filter
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createComparePredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Compare compare = (Compare) filter;
		Path path = getPropertyPath(root, compare.getPropertyId(), true);
		Expression<Comparable> property = path;
		Object value = compare.getValue();

		// number representation may contain locale specific separators.
		// Here, we remove
		// those and make sure a period is used in all cases
		if (value instanceof String) {

			// strip out any "%" sign from decimal fields
			value = ((String) value).replace('%', ' ').trim();

			String str = (String) value;
			if (str != null
					&& org.apache.commons.lang.StringUtils.isNumeric(str.replaceAll("\\.", "").replaceAll(",", ""))) {
				// first remove all periods (which may be used as
				// thousand
				// separators), then replace comma by period
				str = str.replaceAll("\\.", "").replace(',', '.');
				value = str;
			}

		}

		switch (compare.getOperation()) {
		case EQUAL:
			if (value instanceof Class<?>) {
				// When instance of class the use type expression
				return builder.equal(path.type(), builder.literal(value));
			}
			return builder.equal(property, value);
		case GREATER:
			return builder.greaterThan(property, (Comparable) value);
		case GREATER_OR_EQUAL:
			return builder.greaterThanOrEqualTo(property, (Comparable) value);
		case LESS:
			return builder.lessThan(property, (Comparable) value);
		case LESS_OR_EQUAL:
			return builder.lessThanOrEqualTo(property, (Comparable) value);
		default:
			return null;
		}
	}

	/**
	 * Creates an empty parameter map
	 * 
	 * @return
	 */
	public static Map<String, Object> createParameterMap() {
		return new HashMap<>();
	}

	/**
	 * Sets any parameter values on the query
	 * 
	 * @param query
	 *            the query
	 * @param pars
	 *            the parameter values
	 */
	public static void setParameters(TypedQuery<?> query, Map<String, Object> pars) {
		for (Entry<String, Object> entry : pars.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates a query that performs a count
	 * 
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param filter
	 *            the filter to apply
	 * @param distinct
	 *            whether to return only distinct results
	 * @return
	 */
	public static <T> TypedQuery<Long> createCountQuery(EntityManager entityManager, Class<T> entityClass,
			Filter filter, boolean distinct) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = builder.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);

		cq.select(distinct ? builder.countDistinct(root) : builder.count(root));

		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}
		TypedQuery<Long> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a distinct query
	 * 
	 * @param filter
	 * @param entityManager
	 * @param entityClass
	 * @param distinctField
	 * @param sortOrders
	 * @return
	 */
	public static <T> TypedQuery<Tuple> createDistinctQuery(Filter filter, EntityManager entityManager,
			Class<T> entityClass, String distinctField, SortOrder... sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<T> root = cq.from(entityClass);

		// select only the distinctField
		cq.multiselect(root.get(distinctField));

		// Set where clause
		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}
		cq.distinct(true);
		cq = addSortInformation(builder, cq, root, sortOrders);

		TypedQuery<Tuple> query = entityManager.createQuery(cq);
		setParameters(query, pars);

		return query;
	}

	/**
	 * Creates a query that fetches objects based on their IDs
	 * 
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param ids
	 *            the IDs of the desired entities
	 * @param sortOrders
	 *            the sorting information
	 * @param fetchJoins
	 *            the desired fetch joins
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <ID, T> TypedQuery<T> createFetchQuery(EntityManager entityManager, Class<T> entityClass,
			List<ID> ids, SortOrders sortOrders, FetchJoinInformation[] fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		boolean distinct = addFetchJoinInformation(root, fetchJoins);

		// use parameter to prevent Hibernate from creating different query plan
		// every time
		Expression<String> exp = root.get(DynamoConstants.ID);
		ParameterExpression<List> p = builder.parameter(List.class, DynamoConstants.IDS);
		cq.where(exp.in(p));
		cq.distinct(distinct);

		addSortInformation(builder, cq, root, sortOrders == null ? null : sortOrders.toArray());
		TypedQuery<T> query = entityManager.createQuery(cq);

		query.setParameter(DynamoConstants.IDS, ids);
		return query;
	}


	/**
	 * Create a query for fetching a single object
	 * 
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param id
	 *            ID of the object to return
	 * @param fetchJoins
	 *            fetch joins to include
	 * @return
	 */
	public static <ID, T> TypedQuery<T> createFetchSingleObjectQuery(EntityManager entityManager, Class<T> entityClass,
			ID id, FetchJoinInformation[] fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		addFetchJoinInformation(root, fetchJoins);
		Expression<String> exp = root.get(DynamoConstants.ID);

		boolean parameterSet = true;
		if (id instanceof Integer) {
			ParameterExpression<Integer> p = builder.parameter(Integer.class, DynamoConstants.ID);
			cq.where(builder.equal(exp, p));
		} else if (id instanceof Long) {
			ParameterExpression<Long> p = builder.parameter(Long.class, DynamoConstants.ID);
			cq.where(builder.equal(exp, p));
		} else if (id instanceof String) {
			ParameterExpression<String> p = builder.parameter(String.class, DynamoConstants.ID);
			cq.where(builder.equal(exp, p));
		} else {
			// no parameter but query directly
			parameterSet = false;
			cq.where(builder.equal(root.get(DynamoConstants.ID), id));
		}

		TypedQuery<T> query = entityManager.createQuery(cq);
		if (parameterSet) {
			query.setParameter(DynamoConstants.ID, id);
		}

		return query;
	}

	/**
	 * Creates a query for retrieving the IDs of the entities that match the
	 * provided filter
	 * 
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param filter
	 *            the filter to apply
	 * @param sortOrder
	 *            the sorting to apply
	 * @return
	 */
	public static <T> TypedQuery<Tuple> createIdQuery(EntityManager entityManager, Class<T> entityClass, Filter filter,
			SortOrder... sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<T> root = cq.from(entityClass);

		// select only the ID
		List<Selection<?>> selection = new ArrayList<>();
		selection.add(root.get(DynamoConstants.ID));

		// Set where clause
		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}

		// When joins are added (by getPropertyPath) do distinct query
		if (!root.getJoins().isEmpty()) {
			cq.distinct(true);
		}

		// add order clause - this is also important in case of an ID query
		// since we do need to return the correct IDs!
		cq = addSortInformation(builder, cq, root, selection, sortOrders);
		TypedQuery<Tuple> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createLikePredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Like like = (Like) filter;
		if (like.isCaseSensitive()) {
			return builder.like((Expression) getPropertyPath(root, like.getPropertyId()), like.getValue());
		} else {
			return builder.like(builder.lower((Expression) getPropertyPath(root, like.getPropertyId())),
					like.getValue().toLowerCase());
		}
	}

	/**
	 * Create a modulo predicate
	 * 
	 * @param builder
	 * @param filter
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createModuloPredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Modulo modulo = (Modulo) filter;
		if (modulo.getModExpression() != null) {
			// compare to a literal expression
			return builder.equal(builder.mod((Expression) getPropertyPath(root, modulo.getPropertyId()),
					(Expression) getPropertyPath(root, modulo.getModExpression())), modulo.getResult());
		} else {
			// compare to a property
			return builder.equal(builder.mod((Expression) getPropertyPath(root, modulo.getPropertyId()),
					modulo.getModValue().intValue()), modulo.getResult());
		}
	}

	private static Predicate createOrPredicate(CriteriaBuilder builder, Root<?> root, Filter filter,
			Map<String, Object> parameters) {
		Or or = (Or) filter;
		List<Filter> filters = new ArrayList<>(or.getFilters());

		Predicate predicate = null;
		if (!filters.isEmpty()) {
			predicate = createPredicate(filters.remove(0), builder, root, parameters);
			while (!filters.isEmpty()) {
				Predicate next = createPredicate(filters.remove(0), builder, root, parameters);
				if (next != null) {
					predicate = builder.or(predicate, next);
				}
			}
		}

		return predicate;
	}

	/**
	 * Creates a JPA2 predicate based on a Filter
	 * 
	 * @param filter
	 *            the filter
	 * @param builder
	 *            the criteria builder
	 * @param root
	 *            the entity root
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Predicate createPredicate(Filter filter, CriteriaBuilder builder, Root<?> root,
			Map<String, Object> parameters) {
		if (filter == null) {
			return null;
		}

		if (filter instanceof And) {
			return createAndPredicate(builder, root, filter, parameters);
		} else if (filter instanceof Or) {
			return createOrPredicate(builder, root, filter, parameters);
		} else if (filter instanceof Not) {
			Not not = (Not) filter;
			return builder.not(createPredicate(not.getFilter(), builder, root, parameters));
		} else if (filter instanceof Between) {
			Between between = (Between) filter;
			Expression property = getPropertyPath(root, between.getPropertyId());
			return builder.between(property, (Comparable) between.getStartValue(), (Comparable) between.getEndValue());
		} else if (filter instanceof Compare) {
			return createComparePredicate(builder, root, filter);
		} else if (filter instanceof IsNull) {
			IsNull isNull = (IsNull) filter;
			Path p = getPropertyPath(root, isNull.getPropertyId());
			if (p.type() != null && java.util.Collection.class.isAssignableFrom(p.type().getJavaType())) {
				return builder.isEmpty(p);
			}
			return builder.isNull(p);
		} else if (filter instanceof Like) {
			return createLikePredicate(builder, root, filter);
		} else if (filter instanceof Contains) {
			Contains contains = (Contains) filter;
			return builder.isMember(contains.getValue(), (Expression) getPropertyPath(root, contains.getPropertyId()));
		} else if (filter instanceof In) {
			In in = (In) filter;
			if (in.getValues() != null && !in.getValues().isEmpty()) {
				Expression<?> exp = getPropertyPath(root, in.getPropertyId());
				String parName = in.getPropertyId().replace('.', '_');
				ParameterExpression<List> p = builder.parameter(List.class, parName);
				parameters.put(parName, in.getValues());
				return exp.in(p);
			} else {
				Expression exp = getPropertyPath(root, in.getPropertyId());
				return exp.in(Lists.newArrayList(-1));
			}
		} else if (filter instanceof Modulo) {
			return createModuloPredicate(builder, root, filter);
		}

		throw new UnsupportedOperationException("Filter: " + filter.getClass().getName() + " not recognized");
	}

	/**
	 * Creates a query that fetches properties instead of entities
	 *
	 * @param filter
	 *            the filter
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param selectProperties
	 *            the properties to use in the selection
	 * @param sortOrders
	 *            the sorting information
	 * @param fetchJoins
	 *            the desired fetch joins
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <ID, T> TypedQuery<Object[]> createSelectQuery(Filter filter, EntityManager entityManager,
			Class<T> entityClass, String[] selectProperties, SortOrders sortOrders, FetchJoinInformation[] fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = builder.createQuery(Object[].class);
		Root<T> root = cq.from(entityClass);

		// Set select
		if (selectProperties != null && selectProperties.length > 0) {
			Selection<?>[] selections = new Selection<?>[selectProperties.length];
			int i = 0;
			for (String sp : selectProperties) {

				// Support nested properties
				String[] ppath = sp.split("\\.");
				Path path = root;
				for (String prop : ppath) {
					path = path.get(prop);
				}
				selections[i] = path;
				i++;
			}
			cq.select(builder.array(selections));
		}

		boolean distinct = addFetchJoinInformation(root, fetchJoins);
		cq.distinct(distinct);

		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}
		cq = addSortInformation(builder, cq, root, sortOrders == null ? null : sortOrders.toArray());
		TypedQuery<Object[]> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a query that simply selects some objects based on some filter
	 * 
	 * @param filter
	 *            the filter
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param sortOrder
	 *            the sorting information
	 * @return
	 */
	public static <T> TypedQuery<T> createSelectQuery(Filter filter, EntityManager entityManager, Class<T> entityClass,
			FetchJoinInformation[] fetchJoins, SortOrder... sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		boolean distinct = addFetchJoinInformation(root, fetchJoins);
		cq.select(root);
		cq.distinct(distinct);

		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}
		cq = addSortInformation(builder, cq, root, sortOrders);
		TypedQuery<T> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a query to fetch an object based on a value of a unique property
	 * 
	 * @param entityManager
	 *            the entity manager
	 * @param entityClass
	 *            the entity class
	 * @param fetchJoins
	 *            the fetch joins to include
	 * @param propertyName
	 *            name of the property to search on
	 * @param value
	 *            value of the property to search on
	 * @return
	 */
	public static <T> CriteriaQuery<T> createUniquePropertyFetchQuery(EntityManager entityManager, Class<T> entityClass,
			FetchJoinInformation[] fetchJoins, String propertyName, Object value, boolean caseSensitive) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		addFetchJoinInformation(root, fetchJoins);

		Predicate equals = null;
		if (value instanceof String && !caseSensitive) {
			equals = builder.equal(builder.upper(root.get(propertyName).as(String.class)),
					((String) value).toUpperCase());
		} else {
			equals = builder.equal(root.get(propertyName), value);
		}
		cq.where(equals);
		cq.distinct(true);

		return cq;
	}

	/**
	 * Creates a query used to retrieve a single entity based on a unique property
	 * value
	 * 
	 * @param entityManager
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public static <T> CriteriaQuery<T> createUniquePropertyQuery(EntityManager entityManager, Class<T> entityClass,
			String propertyName, Object value, boolean caseSensitive) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		Predicate equals = null;
		if (value instanceof String && !caseSensitive) {
			equals = builder.equal(builder.upper(root.get(propertyName).as(String.class)),
					((String) value).toUpperCase());
		} else {
			equals = builder.equal(root.get(propertyName), value);
		}
		cq.where(equals);

		return cq;
	}

	/**
	 * Gets property path.
	 * 
	 * @param root
	 *            the root where path starts form
	 * @param propertyId
	 *            the property ID
	 * @return the path to property
	 */
	private static Path<Object> getPropertyPath(Root<?> root, Object propertyId) {
		return getPropertyPath(root, propertyId, false);
	}

	/**
	 * Gets property path.
	 *
	 * @param root
	 *            the root where path starts form
	 * @param propertyId
	 *            the property ID
	 * @param join
	 *            set to true if you want implicit joins to be created for ALL collections
	 * @return the path to property
	 */
	private static Path<Object> getPropertyPath(Root<?> root, Object propertyId, boolean join) {
		String[] propertyIdParts = ((String) propertyId).split("\\.");

		Path<Object> path = null;
		for (int i = 0; i < propertyIdParts.length; i++) {
			String part = propertyIdParts[i];
			if (path == null) {
				path = root.get(part);
			} else {
				path = path.get(part);
			}
			// Just one collection in the path supported!
			if (join && java.util.Collection.class.isAssignableFrom(path.type().getJavaType())) {
				path = root.join(propertyIdParts[0]);
				for (int k = 1; k <= i; k++) {
					part = propertyIdParts[k];
					path = ((From<?, ?>) path).join(part);
				}
			}
		}
		return path;
	}

	/**
	 * Indicates whether at least one of the specified fetches is a fetch that
	 * fetches a collection
	 * 
	 * @param parent
	 *            the fetch parent
	 * @return
	 */
	private static boolean isCollectionFetch(FetchParent<?, ?> parent) {
		boolean result = false;

		for (Fetch<?, ?> fetch : parent.getFetches()) {
			Attribute<?, ?> attribute = fetch.getAttribute();

			boolean nested = isCollectionFetch(fetch);
			result = result || attribute.isCollection() || nested;
		}
		return result;
	}

	/**
	 * Translates a JoinType
	 * 
	 * @param type
	 *            the type to translate
	 * @return
	 */
	private static JoinType translateJoinType(com.ocs.dynamo.dao.JoinType type) {
		switch (type) {
		case INNER:
			return JoinType.INNER;
		case LEFT:
			return JoinType.LEFT;
		default:
			return JoinType.RIGHT;
		}
	}
}
