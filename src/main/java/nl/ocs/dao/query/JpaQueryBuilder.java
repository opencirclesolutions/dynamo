package nl.ocs.dao.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;

import nl.ocs.constants.OCSConstants;
import nl.ocs.filter.And;
import nl.ocs.filter.Between;
import nl.ocs.filter.Compare;
import nl.ocs.filter.Contains;
import nl.ocs.filter.Filter;
import nl.ocs.filter.In;
import nl.ocs.filter.IsNull;
import nl.ocs.filter.Like;
import nl.ocs.filter.Modulo;
import nl.ocs.filter.Not;
import nl.ocs.filter.Or;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

import com.google.common.collect.Lists;

/**
 * @author patrick.deenen
 * @author bas.rutten
 * 
 *         Class for constructing JPA queries built on the criteria API
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
	private static <T> boolean addFetchJoinInformation(FetchParent<T, ?> root,
			FetchJoinInformation... fetchJoins) {
		boolean collection = false;

		if (root != null && fetchJoins != null) {
			for (FetchJoinInformation s : fetchJoins) {

				// Support nested properties
				FetchParent<T, ?> fetch = root;
				String[] ppath = s.getProperty().split("\\.");
				for (String prop : ppath) {
					fetch = fetch.fetch(prop, s.getJoinType());
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
	 * @param sortOrder
	 *            the sort object
	 * @return
	 */
	private static <T, R> CriteriaQuery<R> addSortInformation(CriteriaBuilder builder,
			CriteriaQuery<R> cq, Root<T> root, Sort sortOrder) {
		if (sortOrder != null) {
			Iterator<Order> it = sortOrder.iterator();
			List<javax.persistence.criteria.Order> orders = new ArrayList<>();
			while (it.hasNext()) {
				Order order = it.next();
				Expression<?> property = (Expression<?>) getPropertyPath(root, order.getProperty());
				orders.add(order.isAscending() ? builder.asc(property) : builder.desc(property));
			}
			cq.orderBy(orders);
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
	private static Predicate createAndPredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		And and = (And) filter;
		List<Filter> filters = new ArrayList<>(and.getFilters());
		Predicate predicate = createPredicate(filters.remove(0), builder, root);

		while (!filters.isEmpty()) {
			predicate = builder.and(predicate, createPredicate(filters.remove(0), builder, root));
		}
		return predicate;
	}

	/**
	 * Creates a predicate based on a "Compare" filter
	 * 
	 * @param builder
	 * @param root
	 * @param filter
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createComparePredicate(CriteriaBuilder builder, Root<?> root,
			Filter filter) {
		Compare compare = (Compare) filter;
		Expression<Comparable> property = (Expression) getPropertyPath(root,
				compare.getPropertyId());
		Object value = compare.getValue();

		// number representation may contain locale specific separators.
		// Here, we remove
		// those and make sure a period is used in all cases
		if (value instanceof String) {

			// strip out any "%" sign from decimal fields
			value = ((String) value).replace('%', ' ').trim();

			String str = (String) value;
			if (str != null
					&& org.apache.commons.lang.StringUtils.isNumeric(str.replaceAll("\\.", "")
							.replaceAll(",", ""))) {
				// first remove all periods (which may be used as
				// thousand
				// separators), then replace comma by period
				str = str.replaceAll("\\.", "").replace(',', '.');
				value = str;
			}

		}

		switch (compare.getOperation()) {
		case EQUAL:
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
	public static <T> CriteriaQuery<Long> createCountQuery(EntityManager entityManager,
			Class<T> entityClass, Filter filter, boolean distinct) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = builder.createQuery(Long.class);
		Root<T> root = cq.from(entityClass);

		cq.select(distinct ? builder.countDistinct(root) : builder.count(root));

		Predicate p = createPredicate(filter, builder, root);
		if (p != null) {
			cq.where(p);
		}
		return cq;
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
	 * @param sortOrder
	 *            the sorting information
	 * @param fetchJoins
	 *            the desired fetch joins
	 * @return
	 */
	public static <ID, T> CriteriaQuery<T> createFetchQuery(EntityManager entityManager,
			Class<T> entityClass, List<ID> ids, Sort sortOrder, FetchJoinInformation[] fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		boolean distinct = addFetchJoinInformation(root, fetchJoins);

		Expression<String> exp = root.get(OCSConstants.ID);
		cq.where(exp.in(ids));
		cq.distinct(distinct);

		return addSortInformation(builder, cq, root, sortOrder);
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
	public static <ID, T> CriteriaQuery<T> createFetchSingleObjectQuery(
			EntityManager entityManager, Class<T> entityClass, ID id,
			FetchJoinInformation[] fetchJoins) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		addFetchJoinInformation(root, fetchJoins);

		cq.where(builder.equal(root.get(OCSConstants.ID), id));
		return cq;
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
	public static <T> CriteriaQuery<Tuple> createIdQuery(EntityManager entityManager,
			Class<T> entityClass, Filter filter, Sort sortOrder) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = builder.createTupleQuery();
		Root<T> root = cq.from(entityClass);

		// select only the ID
		cq.multiselect(root.get(OCSConstants.ID));

		// Set where clause
		Predicate p = createPredicate(filter, builder, root);
		if (p != null) {
			cq.where(p);
		}

		// add order clause - this is also important in case of an ID query
		// since we do need to return the correct IDs!
		return addSortInformation(builder, cq, root, sortOrder);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Predicate createLikePredicate(CriteriaBuilder builder, Root<?> root,
			Filter filter) {
		Like like = (Like) filter;
		if (like.isCaseSensitive()) {
			return builder.like((Expression) getPropertyPath(root, like.getPropertyId()),
					like.getValue());
		} else {
			return builder.like(builder.lower((Expression) getPropertyPath(root,
					like.getPropertyId())), like.getValue().toLowerCase());
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
	private static Predicate createModuloPredicate(CriteriaBuilder builder, Root<?> root,
			Filter filter) {
		Modulo modulo = (Modulo) filter;
		if (modulo.getModExpression() != null) {
			// compare to a literal expression
			return builder.equal(builder.mod(
					(Expression) getPropertyPath(root, modulo.getPropertyId()),
					(Expression) getPropertyPath(root, modulo.getModExpression())), modulo
					.getResult());
		} else {
			// compare to a property
			return builder.equal(builder.mod(
					(Expression) getPropertyPath(root, modulo.getPropertyId()), modulo
							.getModValue().intValue()), modulo.getResult());
		}
	}

	private static Predicate createOrPredicate(CriteriaBuilder builder, Root<?> root, Filter filter) {
		Or or = (Or) filter;
		List<Filter> filters = new ArrayList<>(or.getFilters());

		Predicate predicate = createPredicate(filters.remove(0), builder, root);

		while (!filters.isEmpty()) {
			predicate = builder.or(predicate, createPredicate(filters.remove(0), builder, root));
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
	private static Predicate createPredicate(Filter filter, CriteriaBuilder builder, Root<?> root) {
		if (filter == null) {
			return null;
		}

		if (filter instanceof And) {
			return createAndPredicate(builder, root, filter);
		} else if (filter instanceof Or) {
			return createOrPredicate(builder, root, filter);
		} else if (filter instanceof Not) {
			Not not = (Not) filter;
			return builder.not(createPredicate(not.getFilter(), builder, root));
		} else if (filter instanceof Between) {
			Between between = (Between) filter;
			Expression property = getPropertyPath(root, between.getPropertyId());
			return builder.between(property, (Comparable) between.getStartValue(),
					(Comparable) between.getEndValue());
		} else if (filter instanceof Compare) {
			return createComparePredicate(builder, root, filter);
		} else if (filter instanceof IsNull) {
			IsNull isNull = (IsNull) filter;
			return builder.isNull(getPropertyPath(root, isNull.getPropertyId()));
		} else if (filter instanceof Like) {
			return createLikePredicate(builder, root, filter);
		} else if (filter instanceof Contains) {
			Contains contains = (Contains) filter;
			return builder.isMember(contains.getValue(),
					(Expression) getPropertyPath(root, contains.getPropertyId()));
		} else if (filter instanceof In) {
			In in = (In) filter;
			if (in.getValues() != null && !in.getValues().isEmpty()) {
				Expression exp = getPropertyPath(root, in.getPropertyId());
				return exp.in(in.getValues());
			} else {
				Expression exp = getPropertyPath(root, in.getPropertyId());
				return exp.in(Lists.newArrayList(-1));
			}
		} else if (filter instanceof Modulo) {
			return createModuloPredicate(builder, root, filter);
		}

		throw new UnsupportedOperationException("Filter: " + filter.getClass().getName()
				+ " not recognized");
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
	public static <T> CriteriaQuery<T> createSelectQuery(Filter filter,
			EntityManager entityManager, Class<T> entityClass, FetchJoinInformation[] fetchJoins,
			Sort sortOrder) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> cq = builder.createQuery(entityClass);
		Root<T> root = cq.from(entityClass);

		addFetchJoinInformation(root, fetchJoins);
		cq.select(root);

		Predicate p = createPredicate(filter, builder, root);
		if (p != null) {
			cq.where(p);
		}

		return addSortInformation(builder, cq, root, sortOrder);
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
	public static <T> CriteriaQuery<T> createUniquePropertyFetchQuery(EntityManager entityManager,
			Class<T> entityClass, FetchJoinInformation[] fetchJoins, String propertyName,
			Object value, boolean caseSensitive) {
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
	 * Creates a query used to retrieve a single entity based on a unique
	 * property value
	 * 
	 * @param entityManager
	 * @param entityClass
	 * @param propertyName
	 * @param value
	 * @return
	 */
	public static <T> CriteriaQuery<T> createUniquePropertyQuery(EntityManager entityManager,
			Class<T> entityClass, String propertyName, Object value, boolean caseSensitive) {
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
		String[] propertyIdParts = ((String) propertyId).split("\\.");

		Path<Object> path = null;
		for (String part : propertyIdParts) {
			if (path == null) {
				path = root.get(part);
			} else {
				path = path.get(part);
			}
		}
		return path;
	}

	private static boolean isCollectionFetch(FetchParent<?, ?> parent) {
		boolean result = false;

		for (Fetch<?, ?> fetch : parent.getFetches()) {
			Attribute<?, ?> attribute = fetch.getAttribute();

			boolean nested = isCollectionFetch(fetch);
			result = result || attribute.isCollection() || nested;
		}
		return result;
	}

}
