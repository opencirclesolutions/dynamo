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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.sqm.tree.domain.SqmEntityValuedSimplePath;

import com.ocs.dynamo.constants.DynamoConstants;
import com.ocs.dynamo.dao.FetchJoinInformation;
import com.ocs.dynamo.dao.QueryFunction;
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
import com.ocs.dynamo.util.SystemPropertyUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.Attribute;
import lombok.extern.slf4j.Slf4j;

/**
 * @author patrick.deenen
 * @author bas.rutten Class for constructing JPA queries built on the criteria
 *         API
 */
@Slf4j
public final class JpaQueryBuilder {

	/**
	 * Adds fetch join information to a query root
	 *
	 * @param root       the query root
	 * @param fetchJoins the fetch joins
	 * @return <code>true</code> if the fetches include a collection,
	 *         <code>false</code> otherwise
	 */
	private static <T> boolean addFetchJoins(FetchParent<T, ?> root, FetchJoinInformation... fetchJoins) {
		boolean collection = false;

		Map<String,FetchParent<T, ?>> fetchMap = new HashMap<>();
		
		if (root != null && fetchJoins != null) {
			for (FetchJoinInformation s : fetchJoins) {

				// Support nested properties
				FetchParent<T, ?> fetch = root;
				String[] propertyPath = s.getProperty().split("\\.");
				StringBuilder prefix = new StringBuilder();
				
				for (String property : propertyPath) {
					if (prefix.length() > 0) {
						prefix.append(".");
					}
					prefix.append(property);
					
					if (fetchMap.containsKey(prefix.toString())) {
						fetch = fetchMap.get(prefix.toString());
					} else {
				      fetch = fetch.fetch(property, translateJoinType(s.getJoinType()));
					  fetchMap.put(prefix.toString(), fetch);
					}
				}
			}

			// check if any collection is fetched. If so then the results need
			// to be cleaned up using "distinct"
			collection = isCollectionFetch(root);
		}
		return collection;
	}

	/**
	 * Adds the "order by" clause to a criteria query
	 *
	 * @param builder    the criteria builder
	 * @param cq         the criteria query
	 * @param root       the query root
	 * @param distinct   whether a "distinct" is applied to the query
	 * @param sortOrders the sort orders
	 * @return the query with the sorting clause appended to it
	 */
	private static <T, R> CriteriaQuery<R> addOrderBy(CriteriaBuilder builder, CriteriaQuery<R> cq, Root<T> root,
			boolean distinct, SortOrder... sortOrders) {
		return addOrderBy(builder, cq, root, null, distinct, sortOrders);
	}

	/**
	 * Adds the "order by" clause to a criteria query
	 *
	 * @param builder     the criteria builder
	 * @param cq          the criteria query
	 * @param root        the query root
	 * @param multiSelect whether to select multiple properties
	 * @param distinct    whether a 'distinct' is applied to the query. This
	 *                    influences how the sort part is built
	 * @param sortOrders  the sort orders
	 * @return the criteria query with any relevant sorting instructions added to it
	 */
	private static <T, R> CriteriaQuery<R> addOrderBy(CriteriaBuilder builder, CriteriaQuery<R> cq, Root<T> root,
			List<Selection<?>> multiSelect, boolean distinct, SortOrder... sortOrders) {
		List<Selection<?>> ms = new ArrayList<>();
		if (multiSelect != null && !multiSelect.isEmpty()) {
			ms.addAll(multiSelect);
		}
		if (sortOrders != null && sortOrders.length > 0) {
			List<Order> orders = new ArrayList<>();
			for (SortOrder sortOrder : sortOrders) {
				Expression<?> property = distinct ? getPropertyPath(root, sortOrder.getProperty(), true)
						: getPropertyPathForSort(root, sortOrder.getProperty());
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
	 * @param builder    the criteria builder
	 * @param root       the root object
	 * @param filter     the "And" filter
	 * @param parameters the parameters passed to the query
	 * @return the predicate
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
	 * Creates a predicate based on a case-insensitive Like-predicate
	 *
	 * @param builder the criteria builder
	 * @param root    the root object
	 * @param like    the predicate
	 * @return the constructed predicate
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createCaseInsensitiveLikePredicate(CriteriaBuilder builder, Root<?> root, Like like) {
		String unaccentName = SystemPropertyUtils.getUnAccentFunctionName();
		if (!StringUtils.isEmpty(unaccentName)) {
			return builder.like(
					builder.function(unaccentName, String.class,
							builder.lower((Expression) getPropertyPath(root, like.getPropertyId(), true))),
					removeAccents(like.getValue().toLowerCase()));
		}

		return builder.like(builder.lower((Expression) getPropertyPath(root, like.getPropertyId(), true)),
				like.getValue().toLowerCase());
	}

	/**
	 * Creates a predicate based on a "Compare" filter
	 *
	 * @param builder the criteria builder
	 * @param root    the query root
	 * @param filter  the Compare filter
	 * @return the predicate
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createComparePredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Compare compare = (Compare) filter;
		Path path = getPropertyPath(root, compare.getPropertyId(), true);
		Object value = compare.getValue();

		// number representation may contain locale specific separators.
		// Here, we remove
		// those and make sure a period is used in all cases
		if (value instanceof String str) {

			// strip out any "%" sign from decimal fields
			value = str.replace('%', ' ').trim();
			if (StringUtils.isNumeric(str.replace(".", "").replace(",", ""))) {
				// first remove all periods (which may be used as
				// thousands separators), then replace comma by period
				str = str.replace(".", "").replace(',', '.');
				value = str;
			}
		}

		switch (compare.getOperation()) {
		case EQUAL:
			if (value instanceof Class<?>) {
				// When instance of class the use type expression
				return builder.equal(path.type(), builder.literal(value));
			}
			return builder.equal(path, value);
		case GREATER:
			return builder.greaterThan(path, (Comparable) value);
		case GREATER_OR_EQUAL:
			return builder.greaterThanOrEqualTo(path, (Comparable) value);
		case LESS:
			return builder.lessThan(path, (Comparable) value);
		case LESS_OR_EQUAL:
			return builder.lessThanOrEqualTo(path, (Comparable) value);
		default:
			return null;
		}
	}

	/**
	 * Creates a query that performs a count
	 *
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param filter        the filter to apply
	 * @param distinct      whether to return only distinct results
	 * @return the constructed query
	 */
	public static <T> TypedQuery<Long> createCountQuery(EntityManager entityManager, Class<T> entityClass,
			Filter filter, boolean distinct) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = builder.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);

		cq.select(distinct ? builder.countDistinct(root) : builder.count(root));

		Map<String, Object> pars = createParameterMap();
		Predicate predicate = createPredicate(filter, builder, root, pars);
		if (predicate != null) {
			cq.where(predicate);
		}
		TypedQuery<Long> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a query for retrieving all distinct values for a certain field
	 *
	 * @param filter        the search filter
	 * @param entityManager the entity manager
	 * @param entityClass   the class of the entity to query
	 * @param distinctField the name of the field for which to retrieve the distinct
	 *                      values
	 * @param sortOrders    the sort orders
	 * @return the constructed query
	 */
	public static <T> TypedQuery<Tuple> createDistinctQuery(Filter filter, EntityManager entityManager,
			Class<T> entityClass, String distinctField, SortOrder... sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<T> root = cq.from(entityClass);

		// select only the distinctField
		cq.multiselect(getPropertyPath(root, distinctField, true));

		Map<String, Object> pars = createParameterMap();
		Predicate predicate = createPredicate(filter, builder, root, pars);
		if (predicate != null) {
			cq.where(predicate);
		}
		cq.distinct(true);
		cq = addOrderBy(builder, cq, root, true, sortOrders);

		TypedQuery<Tuple> query = entityManager.createQuery(cq);
		setParameters(query, pars);

		return query;
	}

	/**
	 * Creates a query that fetches objects based on their IDs
	 *
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param ids           the IDs of the desired entities
	 * @param sortOrders    the sort orders
	 * @param fetchJoins    the desired fetch joins
	 * @return the constructed query
	 */
	@SuppressWarnings("rawtypes")
	public static <ID, T> TypedQuery<T> createFetchQuery(EntityManager entityManager, Class<T> entityClass,
			List<ID> ids, Filter additionalFilter, SortOrders sortOrders, FetchJoinInformation... fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		boolean distinct = addFetchJoins(root, fetchJoins);
		if (distinct) {
			log.warn("Using distinct select, sorting on complex properties is not supported!");
		}

		Expression<String> exp = root.get(DynamoConstants.ID);
		ParameterExpression<List> idExpression = builder.parameter(List.class, DynamoConstants.IDS);
		cq.distinct(distinct);

		Map<String, Object> pars = createParameterMap();
		if (additionalFilter != null) {
			Predicate predicate = createPredicate(additionalFilter, builder, root, pars);
			if (predicate != null) {
				cq.where(predicate, exp.in(idExpression));
			} else {
				cq.where(exp.in(idExpression));
			}
		} else {
			cq.where(exp.in(idExpression));
		}

		addOrderBy(builder, cq, root, distinct, sortOrders == null ? null : sortOrders.toArray());
		TypedQuery<T> query = entityManager.createQuery(cq);

		query.setParameter(DynamoConstants.IDS, ids);

		if (additionalFilter != null) {
			setParameters(query, pars);
		}

		return query;
	}

	/**
	 * Create a query for fetching a single object
	 *
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param id            ID of the object to return
	 * @param fetchJoins    fetch joins to include
	 * @return the constructed query
	 */
	public static <ID, T> TypedQuery<T> createFetchSingleObjectQuery(EntityManager entityManager, Class<T> entityClass,
			ID id, FetchJoinInformation[] fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		addFetchJoins(root, fetchJoins);
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
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param filter        the filter to apply
	 * @param sortOrders    the sorting to apply
	 * @return the constructed query
	 */
	public static <T> TypedQuery<Tuple> createIdQuery(EntityManager entityManager, Class<T> entityClass, Filter filter,
			SortOrder... sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<T> root = cq.from(entityClass);

		List<Selection<?>> selection = new ArrayList<>();
		selection.add(root.get(DynamoConstants.ID));

		Map<String, Object> pars = createParameterMap();
		Predicate predicate = createPredicate(filter, builder, root, pars);
		if (predicate != null) {
			cq.where(predicate);
		}

		// When joins are added (by getPropertyPath) do distinct query
		if (!root.getJoins().isEmpty()) {
			cq.distinct(true);
		}

		// add order by clause - this is also important in case of an ID query
		// since we do need to return the correct IDs!
		// note: "distinct" must be false here
		cq = addOrderBy(builder, cq, root, selection, false, sortOrders);
		TypedQuery<Tuple> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a predicate based on a "Like"-filter
	 *
	 * @param builder the criteria builder
	 * @param root    the query root
	 * @param filter  the filter
	 * @return the constructed predicate
	 */
	private static Predicate createLikePredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Like like = (Like) filter;
		if (like.isCaseSensitive()) {
			return createLikePredicate(builder, root, like);
		} else {
			return createCaseInsensitiveLikePredicate(builder, root, like);
		}
	}

	/**
	 * Creates a predicate based on a "Like"-filter (case-insensitive)
	 *
	 * @param builder the criteria builder
	 * @param root    the query root
	 * @param like    the Like filter
	 * @return the constructed predicate
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createLikePredicate(CriteriaBuilder builder, Root<?> root, Like like) {
		String unaccentName = SystemPropertyUtils.getUnAccentFunctionName();
		if (!StringUtils.isEmpty(unaccentName)) {
			return builder.like(
					builder.function(unaccentName, String.class, getPropertyPath(root, like.getPropertyId(), true)),
					removeAccents(like.getValue()));
		}

		return builder.like((Expression) getPropertyPath(root, like.getPropertyId(), true), like.getValue());
	}

	/**
	 * Creates a modulo predicate
	 *
	 * @param builder the criteria builder
	 * @param root    the query root
	 * @param filter  the filter to apply
	 * @return the constructed predicate
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createModuloPredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Modulo modulo = (Modulo) filter;
		if (modulo.getModExpression() != null) {
			// compare to a literal expression
			return builder.equal(builder.mod((Expression) getPropertyPath(root, modulo.getPropertyId(), true),
					(Expression) getPropertyPath(root, modulo.getModExpression(), true)), modulo.getResult());
		} else {
			// compare to a property
			return builder.equal(builder.mod((Expression) getPropertyPath(root, modulo.getPropertyId(), true),
					modulo.getModValue().intValue()), modulo.getResult());
		}
	}

	/**
	 * Creates a predicate for a logical OR
	 *
	 * @param builder    the criteria builder
	 * @param root       the query root
	 * @param filter     the filter to apply
	 * @param parameters the query parameter mapping
	 * @return the constructed predicate
	 */
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

	private static Map<String, Object> createParameterMap() {
		return new HashMap<>();
	}

	/**
	 * Creates a predicate based on a Filter
	 *
	 * @param filter  the filter
	 * @param builder the criteria builder
	 * @param root    the entity root
	 * @return the constructed predicate
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
		} else if (filter instanceof Not not) {
			return builder.not(createPredicate(not.getFilter(), builder, root, parameters));
		} else if (filter instanceof Between between) {
			Expression property = getPropertyPath(root, between.getPropertyId(), true);
			return builder.between(property, (Comparable) between.getStartValue(), (Comparable) between.getEndValue());
		} else if (filter instanceof Compare) {
			return createComparePredicate(builder, root, filter);
		} else if (filter instanceof IsNull isNull) {
			Path path = getPropertyPath(root, isNull.getPropertyId(), true);
			if (isCollection(path)) {
				return builder.isEmpty(path);
			}
			return builder.isNull(path);
		} else if (filter instanceof Like) {
			return createLikePredicate(builder, root, filter);
		} else if (filter instanceof Contains contains) {
			return builder.isMember(contains.getValue(),
					(Expression) getPropertyPath(root, contains.getPropertyId(), true));
		} else if (filter instanceof In in) {
			if (in.getValues() != null && !in.getValues().isEmpty()) {
				Expression<?> exp = getPropertyPath(root, in.getPropertyId(), true);
				String parName = in.getPropertyId().replace('.', '_');
				// Support multiple parameters
				if (parameters.containsKey(parName)) {
					parName = parName + System.currentTimeMillis();
				}

				ParameterExpression<Collection> p = builder.parameter(Collection.class, parName);
				parameters.put(parName, in.getValues());
				return exp.in(p);
			} else {
				// match with an empty list
				Expression exp = getPropertyPath(root, in.getPropertyId(), true);
				return exp.in(List.of(-1));
			}
		} else if (filter instanceof Modulo) {
			return createModuloPredicate(builder, root, filter);
		}

		throw new UnsupportedOperationException("Filter: " + filter.getClass().getName() + " not recognized");
	}

	/**
	 * Creates a query that selects objects based on the specified filter
	 *
	 * @param filter        the filter
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param sortOrders    the sorting information
	 * @return the constructed query
	 */
	public static <T> TypedQuery<T> createSelectQuery(Filter filter, EntityManager entityManager, Class<T> entityClass,
			FetchJoinInformation[] fetchJoins, SortOrder... sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		boolean distinct = addFetchJoins(root, fetchJoins);
		cq.select(root);
		cq.distinct(distinct);

		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}
		cq = addOrderBy(builder, cq, root, distinct, sortOrders);
		TypedQuery<T> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a query that fetches properties instead of entities. Supports
	 * aggregated functions; when used will automatically add group by expressions
	 * for all properties in the select list without an aggregated function.
	 *
	 * @param filter           the filter
	 * @param entityManager    the entity manager
	 * @param entityClass      the entity class
	 * @param selectProperties the properties to use in the selection
	 * @param sortOrders       the sorting information
	 * @return the constructed query
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> TypedQuery<Object[]> createSelectQuery(Filter filter, EntityManager entityManager,
			Class<T> entityClass, String[] selectProperties, SortOrders sortOrders) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = builder.createQuery(Object[].class);
		Root<T> root = cq.from(entityClass);
		ArrayList<Expression<?>> grouping = new ArrayList<>();
		boolean aggregated = false;

		// Set select
		if (selectProperties != null && selectProperties.length > 0) {
			Selection<?>[] selections = new Selection<?>[selectProperties.length];
			int i = 0;
			for (String sp : selectProperties) {

				// Support nested properties
				String[] propertyPath = sp.split("\\.");
				// Test for function
				QueryFunction queryFunction = null;
				Path path = null;
				try {
					if (propertyPath.length > 1) {
						queryFunction = QueryFunction.valueOf(propertyPath[propertyPath.length - 1]);
						path = getPropertyPath(root, sp.substring(0, sp.lastIndexOf('.')), true);
					}
				} catch (Exception e) {
					// Do nothing; not a supported function; assume property name
				}
				if (queryFunction != null) {
					selections[i] = switch (queryFunction) {
					case AF_AVG -> builder.avg(path);
					case AF_COUNT -> builder.count(path);
					case AF_COUNT_DISTINCT -> builder.countDistinct(path);
					case AF_SUM -> builder.sum(path);
					};
					aggregated = true;
				} else {
					path = getPropertyPath(root, sp, true);
					selections[i] = path;
					grouping.add(path);
				}
				i++;
			}
			cq.select(builder.array(selections));
		}

		Map<String, Object> pars = createParameterMap();
		Predicate p = createPredicate(filter, builder, root, pars);
		if (p != null) {
			cq.where(p);
		}
		if (aggregated) {
			cq.groupBy(grouping);
		}
		cq = addOrderBy(builder, cq, root, true, sortOrders == null ? null : sortOrders.toArray());
		TypedQuery<Object[]> query = entityManager.createQuery(cq);
		setParameters(query, pars);
		return query;
	}

	/**
	 * Creates a query to fetch an object based on a value of a unique property
	 *
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param fetchJoins    the fetch joins to include
	 * @param propertyName  name of the property to search on
	 * @param value         value of the property to search on
	 * @return the constructed query
	 */
	public static <T> CriteriaQuery<T> createUniquePropertyFetchQuery(EntityManager entityManager, Class<T> entityClass,
			FetchJoinInformation[] fetchJoins, String propertyName, Object value, boolean caseSensitive) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		addFetchJoins(root, fetchJoins);

		Predicate equals;
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
	 * @param entityManager the entity manager
	 * @param entityClass   the entity class
	 * @param propertyName  the property name
	 * @param value         the unique value
	 * @return the constructed query
	 */
	public static <T> CriteriaQuery<T> createUniquePropertyQuery(EntityManager entityManager, Class<T> entityClass,
			String propertyName, Object value, boolean caseSensitive) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		Predicate equals;
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
	 * @param root       the root where path starts form
	 * @param propertyId the property ID
	 * @param join       set to true if you want implicit joins to be created for
	 *                   ALL collections
	 * @return the path to property
	 */
	@SuppressWarnings("unchecked")
	private static Path<Object> getPropertyPath(Root<?> root, Object propertyId, boolean join) {
		String[] propertyIdParts = ((String) propertyId).split("\\.");

		Path<?> path = null;
		Join<?, ?> curJoin = null;
		for (String part : propertyIdParts) {
			if (path == null) {
				path = root.get(part);
			} else {
				path = path.get(part);
			}

			if (join && isCollection(path)) {
				// Reuse existing join
				Join<?, ?> detailJoin = null;
				Collection<Join<?, ?>> joins = (Collection<Join<?, ?>>) (curJoin == null ? root.getJoins()
						: curJoin.getJoins());
				if (joins != null) {
					for (Join<?, ?> j : joins) {
						if (part.equals(j.getAttribute().getName())) {
							path = j;
							detailJoin = j;
							break;
						}
					}
				}
				// when no existing join then add new
				if (detailJoin == null) {
					if (curJoin == null) {
						curJoin = root.join(part);
					} else {
						curJoin = curJoin.join(part);
					}
					path = curJoin;
				}
			}
		}
		return (Path<Object>) path;
	}

	/**
	 * Adds a property path specifically for sorting
	 *
	 * @param root       the query root
	 * @param propertyId the property
	 * @return the constructed expression
	 */
	@SuppressWarnings("unchecked")
	private static Expression<?> getPropertyPathForSort(Root<?> root, Object propertyId) {
		String[] propertyIdParts = ((String) propertyId).split("\\.");

		Path<?> path = null;
		Join<?, ?> curJoin = null;
		for (String part : propertyIdParts) {
			if (path == null) {
				path = root.get(part);
			} else {
				path = path.get(part);
			}

			if (isEntityOrCollection(path)) {
				// Reuse existing join
				Join<?, ?> detailJoin = null;
				Collection<Join<?, ?>> joins = (Collection<Join<?, ?>>) (curJoin == null ? root.getJoins()
						: curJoin.getJoins());
				if (joins != null) {
					for (Join<?, ?> j : joins) {
						if (part.equals(j.getAttribute().getName())) {
							path = j;
							detailJoin = j;
							break;
						}
					}
				}
				// when no existing join then add new
				if (detailJoin == null) {
					if (curJoin == null) {
						curJoin = root.join(part, JoinType.LEFT);
					} else {
						curJoin = curJoin.join(part, JoinType.LEFT);
					}
					path = curJoin;
				}
			}

		}
		return path;
	}

	/**
	 * Indicates whether at least one of the specified fetches is a fetch that
	 * fetches a collection
	 *
	 * @param parent the fetch parent
	 * @return true if this is the case, false otherwise
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

	private static boolean isCollection(Path<?> path) {
		boolean collection = false;
		try {
			collection = path.type() != null && java.util.Collection.class.isAssignableFrom(path.type().getJavaType());
		} catch (Exception ex) {
			// do nothing (new JPA is stricter on this than before)
		}
		return collection;
	}

	private static boolean isEntityOrCollection(Path<?> path) {
		boolean entityOrCollection = false;

		try {
			entityOrCollection = path instanceof SqmEntityValuedSimplePath
					|| Collection.class.isAssignableFrom(path.getJavaType());
		} catch (Exception ex) {
			// do nothing (new JPA is stricter on this than before)
		}
		return entityOrCollection;

	}

	private static String removeAccents(String input) {
		return com.ocs.dynamo.utils.StringUtils.removeAccents(input);
	}

	/**
	 * Sets the values of all parameters used in the query
	 *
	 * @param query the query
	 * @param pars  the parameter values
	 */
	private static void setParameters(TypedQuery<?> query, Map<String, Object> pars) {
		for (Entry<String, Object> entry : pars.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Translates a JoinType
	 *
	 * @param type the type to translate
	 * @return the result of the translation
	 */
	private static JoinType translateJoinType(com.ocs.dynamo.dao.JoinType type) {
		return switch (type) {
		case INNER -> JoinType.INNER;
		case LEFT -> JoinType.LEFT;
		default -> JoinType.RIGHT;
		};
	}

	private JpaQueryBuilder() {
		// hidden private constructor
	}
}
