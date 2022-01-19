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
package com.ocs.dynamo.filter;

import java.util.Collection;

import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.collect.Lists;
import com.ocs.dynamo.filter.Compare.Equal;
import com.ocs.dynamo.filter.Compare.Greater;
import com.ocs.dynamo.filter.Compare.GreaterOrEqual;
import com.ocs.dynamo.filter.Compare.Less;
import com.ocs.dynamo.filter.Compare.LessOrEqual;

/**
 * Abstract base class for filters
 * 
 * @author bas.rutten
 */
public abstract class AbstractFilter implements Filter {

    /**
     * Wraps the filter in an And filter (if not already present)
     * 
     * @return
     */
    public And and() {
        if (this instanceof And) {
            return (And) this;
        }
        return new And(this);
    }

    /**
     * Constructs a filter that is a conjunction of the provided filters
     * 
     * @param filters the filter to combine
     * @return
     */
    public And and(Collection<Filter> filters) {
        And result = null;
        if (this instanceof And) {
            result = (And) this;
            result.getFilters().addAll(filters);
        } else {
            result = new And(this).and(filters);
        }
        return result;
    }

	/**
	 * Constructs a filter that is a conjunction of the provided filters
	 * 
	 * @param filters the filters
	 * @return
	 */
	public And and(Filter... filters) {
		return and(Lists.newArrayList(filters));
	}

	/**
	 * Constructs a "between" for the provided property
	 * 
	 * @param propertyId the property
	 * @param startValue the start value (inclusive)
	 * @param endValue   the end value (inclusive)
	 * @return
	 */
	public AbstractJunctionFilter between(String propertyId, Comparable<?> startValue, Comparable<?> endValue) {
		return newJunction(new Between(propertyId, startValue, endValue));
	}

	/**
	 * Get the value of a property of the given bean by use of optimized reflection
	 * by Spring.
	 * 
	 * @param bean         The bean
	 * @param propertyName The name of the property to get
	 * @return The property value
	 */
	protected Object getProperty(Object bean, String propertyName) {
		if (bean == null || propertyName == null) {
			return null;
		}
		return PropertyAccessorFactory.forBeanPropertyAccess(bean).getPropertyValue(propertyName);
	}

	/**
	 * Constructs an "greater than" filter for a property
	 * 
	 * @param propertyId the property
	 * @param value      the value to compare to
	 * @return
	 */
	public AbstractJunctionFilter greater(String propertyId, Object value) {
		return newJunction(new Greater(propertyId, value));
	}

	/**
	 * Constructs an "greater or equals" filter for a property
	 * 
	 * @param propertyId the property
	 * @param value      the value to compare to
	 * @return
	 */
	public AbstractJunctionFilter greaterOrEqual(String propertyId, Object value) {
		return newJunction(new GreaterOrEqual(propertyId, value));
	}

	/**
	 * Constructs an "equals" filter
	 * 
	 * @param propertyId the property
	 * @param value      the value to compare to
	 * @return
	 */
	public AbstractJunctionFilter isEqual(String propertyId, Object value) {
		return newJunction(new Equal(propertyId, value));
	}

	/**
	 * Constructs a "isNull" filter for a property
	 * 
	 * @param propertyId the property
	 * @return
	 */
	public AbstractJunctionFilter isNull(String propertyId) {
		return newJunction(new IsNull(propertyId));
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
	 * Constructs a case-sensitive "like" filter
	 * 
	 * @param propertyId
	 * @param value
	 * @return
	 */
	public AbstractJunctionFilter like(String propertyId, String value) {
		return newJunction(new Like(propertyId, value, true));
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

	/**
	 * Constructs a filter that is the negation of the provided filter
	 * 
	 * @param filter the filter to negate
	 * @return
	 */
	public AbstractJunctionFilter not(Filter filter) {
		return newJunction(new Not(filter));
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
	 * Constructs a filter that is a disjunction of the provided filters
	 * 
	 * @param filters
	 * @return
	 */
	public Or or(Filter... filters) {
		return or(Lists.newArrayList(filters));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
