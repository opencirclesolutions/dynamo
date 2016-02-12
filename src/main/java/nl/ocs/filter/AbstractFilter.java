package nl.ocs.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nl.ocs.filter.Compare.Equal;
import nl.ocs.filter.Compare.Greater;
import nl.ocs.filter.Compare.GreaterOrEqual;
import nl.ocs.filter.Compare.Less;
import nl.ocs.filter.Compare.LessOrEqual;

import org.springframework.beans.PropertyAccessorFactory;

/**
 * 
 * @author bas.rutten
 * 
 */
public abstract class AbstractFilter implements Filter {

	/**
	 * Wraps the filter in an And filter (if not already present)
	 * 
	 * @return
	 */
	public And and() {
		if (this instanceof And) {
			// Do nothing
			return (And) this;
		}
		// Else create And add current
		return new And(this);
	}

	/**
	 * Constructs a filter that is a conjunction of the provided filters
	 * 
	 * @param filters
	 * @return
	 */
	public And and(Filter... filters) {
		return and(Arrays.asList(filters));
	}

	/**
	 * Constructs a filter that is a conjunction of the provided filters
	 * 
	 * @param filters
	 * @return
	 */
	public And and(Collection<Filter> filters) {
		And result = null;
		if (this instanceof And) {
			result = (And) this;
			// Add filters to current
			result.getFilters().addAll(filters);
		} else {
			// Create And and add current and given
			result = new And(this).and(filters);
		}
		return result;
	}

	/**
	 * Wraps the filter in an Or filter (if not already present)
	 * 
	 * @return
	 */
	public Or or() {
		if (this instanceof Or) {
			// Do nothing
			return (Or) this;
		}
		// Else create Or add current
		return new Or(this);
	}

	/**
	 * Constructs a filter that is a disjunction of the provided filters
	 * 
	 * @param filters
	 * @return
	 */
	public Or or(Filter... filters) {
		return or(Arrays.asList(filters));
	}

	/**
	 * Constructs a filter that is a disjunction of the provided filters
	 * 
	 * @param filters
	 * @return
	 */
	public Or or(Collection<Filter> filters) {
		Or result = null;
		if (this instanceof Or) {
			result = (Or) this;
			// Add filters to current
			result.getFilters().addAll(filters);
		} else {
			// Create And and add current and given
			result = new Or(this).or(filters);
		}
		return result;
	}

	/**
	 * Constructs a "between" for the provided property
	 * 
	 * @param propertyId
	 * @param startValue
	 * @param endValue
	 * @return
	 */
	public AbstractJunctionFilter between(String propertyId, Comparable<?> startValue,
			Comparable<?> endValue) {
		return newJunction(new Between(propertyId, startValue, endValue));
	}

	/**
	 * Constructs an "equals" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter isEqual(String propertyId, Object value) {
		return newJunction(new Equal(propertyId, value));
	}

	/**
	 * Constructs an "greater than" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter greater(String propertyId, Object value) {
		return newJunction(new Greater(propertyId, value));
	}

	/**
	 * Constructs an "greater or equals" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter greaterOrEqual(String propertyId, Object value) {
		return newJunction(new GreaterOrEqual(propertyId, value));
	}

	/**
	 * Constructs an "less than" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter less(String propertyId, Object value) {
		return newJunction(new Less(propertyId, value));
	}

	/**
	 * Constructs a "less or equal" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter lessOrEqual(String propertyId, Object value) {
		return newJunction(new LessOrEqual(propertyId, value));
	}

	/**
	 * Constructs a "isNull" filter
	 * 
	 * @param propertyId
	 * @return
	 */
	public AbstractJunctionFilter isNull(String propertyId) {
		return newJunction(new IsNull(propertyId));
	}

	/**
	 * Constructs a negation filter
	 * 
	 * @param filter
	 * @return
	 */
	public AbstractJunctionFilter not(Filter filter) {
		return newJunction(new Not(filter));
	}

	/**
	 * Constructs a "like" filter for string comparison
	 * 
	 * @param propertyId
	 * @param value
	 * @param caseSensitive
	 * @return
	 */
	public AbstractJunctionFilter like(String propertyId, String value, boolean caseSensitive) {
		return newJunction(new Like(propertyId, value, caseSensitive));
	}

	/**
	 * Constructs a case-sensitive "like" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter like(String propertyId, String value) {
		return newJunction(new Like(propertyId, value, true));
	}

	public AbstractJunctionFilter likeIgnoreCase(String propertyId, String value) {
		return newJunction(new Like(propertyId, value, false));
	}

	private AbstractJunctionFilter newJunction(Filter... filters) {
		if (this instanceof Or) {
			return or(filters);
		}
		// Default use And junction
		return and(filters);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Applies the filter to a collection of objects and returns those that
	 * match the filter
	 */
	@Override
	public <T> List<T> applyFilter(Collection<T> collection) {
		List<T> result = new ArrayList<T>();
		if (collection != null) {
			for (T that : collection) {
				if (this.evaluate(that)) {
					result.add(that);
				}
			}
		}
		return result;
	}

	/**
	 * Get the value of a property of the given bean by use of optimized
	 * reflection by Spring.
	 * 
	 * @param bean
	 *            The bean
	 * @param propertyName
	 *            The name of the property to get
	 * @return The property value
	 */
	protected Object getProperty(Object bean, String propertyName) {
		if (bean == null || propertyName == null) {
			return null;
		}
		return PropertyAccessorFactory.forBeanPropertyAccess(bean).getPropertyValue(propertyName);
	}
}
