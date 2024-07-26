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

import com.ocs.dynamo.filter.Compare.*;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.Collection;
import java.util.List;

/**
 * Abstract base class for filters
 *
 * @author bas.rutten
 */
public abstract class AbstractFilter implements Filter {

    /**
     * @return the filter, wrapped in an And filter
     */
    public And and() {
        if (this instanceof And) {
            return (And) this;
        }
        return new And(this);
    }

    /**
     * Wraps the provided filters in an And filter
     *
     * @param filters the filter to combine
     * @return the provided filters, wrapped in an And filter
     */
    public And and(Collection<Filter> filters) {
        And result;
        if (this instanceof And) {
            result = (And) this;
            result.getFilters().addAll(filters);
        } else {
            result = new And(this).and(filters);
        }
        return result;
    }

    /**
     * Wraps the provided filters in an And filter
     *
     * @param filters the filters
     * @return the provided filters, wrapped in an And filter
     */
    public And and(Filter... filters) {
        return and(List.of(filters));
    }

    /**
     * Constructs a "between" filter for the provided property
     *
     * @param propertyId the property
     * @param startValue the start value (inclusive)
     * @param endValue   the end value (inclusive)
     * @return the constructed filter
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
     * Constructs a "greater than" filter for a property
     *
     * @param propertyId the property
     * @param value      the value to compare to
     * @return the constructed filter
     */
    public AbstractJunctionFilter greater(String propertyId, Object value) {
        return newJunction(new Greater(propertyId, value));
    }

    /**
     * Constructs a "greater or equals" filter for a property
     *
     * @param propertyId the property
     * @param value      the value to compare to
     * @return the constructed filter
     */
    public AbstractJunctionFilter greaterOrEqual(String propertyId, Object value) {
        return newJunction(new GreaterOrEqual(propertyId, value));
    }

    /**
     * Constructs an "equals" filter for a property
     *
     * @param propertyId the property
     * @param value      the value to compare to
     * @return the constructed filter
     */
    public AbstractJunctionFilter isEqual(String propertyId, Object value) {
        return newJunction(new Equal(propertyId, value));
    }

    /**
     * Constructs an "isNull" filter for a property
     *
     * @param propertyId the property
     * @return the constructed filter
     */
    public AbstractJunctionFilter isNull(String propertyId) {
        return newJunction(new IsNull(propertyId));
    }

    /**
     * Constructs a "less than" filter for a property
     *
     * @param propertyId the property
     * @param value      the property value
     * @return the constructed filter
     */
    public AbstractJunctionFilter less(String propertyId, Object value) {
        return newJunction(new Less(propertyId, value));
    }

    /**
     * Constructs a "less or equal" filter for a property
     *
     * @param propertyId the property
     * @param value      the property value
     * @return the constructed filter
     */
    public AbstractJunctionFilter lessOrEqual(String propertyId, Object value) {
        return newJunction(new LessOrEqual(propertyId, value));
    }

    /**
     * Constructs a case-sensitive "like" filter
     *
     * @param propertyId the property
     * @param value      the property value
     * @return the constructed filter
     */
    public AbstractJunctionFilter like(String propertyId, String value) {
        return newJunction(new Like(propertyId, value, true));
    }

    /**
     * Constructs a "like" filter for string comparison
     *
     * @param propertyId    the property
     * @param value         the property value
     * @param caseSensitive whether the search is case-insensitive
     * @return the constructed filter
     */
    public AbstractJunctionFilter like(String propertyId, String value, boolean caseSensitive) {
        return newJunction(new Like(propertyId, value, caseSensitive));
    }

    /**
     * Constructs a "like" filter (case insensitive) for string comparison
     *
     * @param propertyId the property
     * @param value      the property value
     * @return the constructed filter
     */
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
     * @return the constructed filter
     */
    public AbstractJunctionFilter not(Filter filter) {
        return newJunction(new Not(filter));
    }

    /**
     * Wraps the current filter in an Or filter (if not already present)
     *
     * @return the current filter
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
     * Wraps the provided filters in a logical OR
     *
     * @param filters the filter to wrap
     * @return the filters wrapped in a logical or
     */
    public Or or(Collection<Filter> filters) {
        Or result;
        if (this instanceof Or) {
            result = (Or) this;
            result.getFilters().addAll(filters);
        } else {
            result = new Or(this).or(filters);
        }
        return result;
    }

    /**
     * Wraps the provided filters in a logical OR
     *
     * @param filters the filter to wrap
     * @return the filters wrapped in a logical or
     */
    public Or or(Filter... filters) {
        return or(List.of(filters));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
