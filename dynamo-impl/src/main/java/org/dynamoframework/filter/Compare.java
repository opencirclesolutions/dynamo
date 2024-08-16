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
package org.dynamoframework.filter;

import java.util.Objects;

/**
 * A filter that compares a property value against a provided value
 * 
 * @author bas.rutten
 */
public abstract class Compare extends AbstractFilter implements PropertyFilter {

    public enum Operation {
        EQUAL, GREATER, LESS, GREATER_OR_EQUAL, LESS_OR_EQUAL
    }

    private final String propertyId;

    private final Operation operation;

    private final Object value;

    /**
     * A filter that evaluates to true if the property value is equal to the provided value
     * 
     * @author bas.rutten
     */
	public static final class Equal extends Compare {

        /**
         * Constructor
         * 
         * @param propertyId
         * @param value
         */
        public Equal(String propertyId, Object value) {
            super(propertyId, value, Operation.EQUAL);
        }
    }

    /**
     * A filter that evaluates to true if the property value is greater than the provided value
     * 
     * @author bas.rutten
     */
    public static final class Greater extends Compare {

        /**
         * Constructor
         * 
         * @param propertyId
         * @param value
         */
        public Greater(String propertyId, Object value) {
            super(propertyId, value, Operation.GREATER);
        }
    }

    /**
     * A filter that evaluates to true if the property value is less than the provided value
     * 
     * @author bas.rutten
     */
    public static final class Less extends Compare {

        /**
         * Constructor
         * 
         * @param propertyId
         * @param value
         */
        public Less(String propertyId, Object value) {
            super(propertyId, value, Operation.LESS);
        }
    }

    /**
     * A filter that evaluates to true if the property value is greater than or equal to the
     * provided value
     * 
     * @author bas.rutten
     */
    public static final class GreaterOrEqual extends Compare {

        /**
         * @param propertyId
         * @param value
         */
        public GreaterOrEqual(String propertyId, Object value) {
            super(propertyId, value, Operation.GREATER_OR_EQUAL);
        }
    }

    /**
     * A filter that evaluates to true if the property value is less than or equal to the provided
     * value
     * 
     * @author bas.rutten
     */
    public static final class LessOrEqual extends Compare {

        /**
         * Constructor
         * 
         * @param propertyId
         * @param value
         */
        public LessOrEqual(String propertyId, Object value) {
            super(propertyId, value, Operation.LESS_OR_EQUAL);
        }
    }

    /**
     * Base constructor
     * 
     * @param propertyId
     * @param value
     * @param operation
     */
    Compare(String propertyId, Object value, Operation operation) {
        this.propertyId = propertyId;
        this.value = value;
        this.operation = operation;
    }

    @Override
    public boolean evaluate(Object that) {
        if (null == that) {
            return false;
        }
        Object val = getProperty(that, getPropertyId());
        if (val == null) {
            return false;
        }
        switch (getOperation()) {
        case EQUAL:
            return compareEquals(val);
        case GREATER:
            return compareValue(val) > 0;
        case LESS:
            return compareValue(val) < 0;
        case GREATER_OR_EQUAL:
            return compareValue(val) >= 0;
        case LESS_OR_EQUAL:
            return compareValue(val) <= 0;
        default:
            return false;
        }
    }

    /**
     * Checks if the this value equals the given value. Favors Comparable over equals to better
     * support e.g. BigDecimal where equals is stricter than compareTo.
     * 
     * @param otherValue
     *            The value to compare to
     * @return true if the values are equal, false otherwise
     */
    @SuppressWarnings("unchecked")
    private boolean compareEquals(Object otherValue) {
        if (value == null || otherValue == null) {
            return otherValue == value;
        } else if (value == otherValue) {
            return true;
        } else if (value instanceof Comparable
                && otherValue.getClass().isAssignableFrom(getValue().getClass())) {
            return ((Comparable<Object>) value).compareTo(otherValue) == 0;
        } else {
            return value.equals(otherValue);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected int compareValue(Object value1) {
        if (null == value) {
            return null == value1 ? 0 : -1;
        } else if (null == value1) {
            return 1;
        } else if (getValue() instanceof Comparable
                && value1.getClass().isAssignableFrom(getValue().getClass())) {
            return -((Comparable) getValue()).compareTo(value1);
        }
        throw new IllegalArgumentException(
                "Could not compare the arguments: " + value1 + ", " + getValue());
    }

    @Override
    public boolean equals(Object obj) {

        // Only objects of the same class can be equal
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        final Compare o = (Compare) obj;

        if (!Objects.equals(propertyId, o.propertyId)) {
            return false;
        }

        return Objects.equals(this.value, o.value);
    }

    @Override
    public int hashCode() {
        return (null != getPropertyId() ? getPropertyId().hashCode() : 0)
                ^ (null != getValue() ? getValue().hashCode() : 0);
    }

    @Override
    public String getPropertyId() {
        return propertyId;
    }

    public Operation getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getPropertyId() + " " + super.toString() + " " + getValue();
    }

}
