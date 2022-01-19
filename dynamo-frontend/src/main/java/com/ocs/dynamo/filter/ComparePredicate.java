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

/**
 * 
 * A predicate for checking that a certain property has an exact value
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity to filter
 */
public abstract class ComparePredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -1140861553052524418L;

	/**
	 * Constructor
	 * 
	 * @param property the property to compare
	 * @param value    the property value to compare to
	 */
	public ComparePredicate(String property, Object value) {
		super(property, value);
	}

	/**
	 * Compares the provided value to the filter value
	 * 
	 * @param value1
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int compareValue(Object value1) {
		if (null == getValue()) {
			return null == value1 ? 0 : -1;
		} else if (null == value1) {
			return 1;
		} else if (getValue() instanceof Comparable && value1.getClass().isAssignableFrom(getValue().getClass())) {
			return -((Comparable) getValue()).compareTo(value1);
		}
		throw new IllegalArgumentException("Could not compare the arguments: " + value1 + ", " + getValue());
	}


}
