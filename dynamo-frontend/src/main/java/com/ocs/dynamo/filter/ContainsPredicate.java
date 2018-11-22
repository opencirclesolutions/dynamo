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

import com.ocs.dynamo.utils.ClassUtils;

import java.util.Collection;

/**
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity to filter
 */
public class ContainsPredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -2480590128514763691L;

	public ContainsPredicate(final String property, final Object value) {
		super(property, value);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean test(final T t) {
		if (t == null) {
			return false;
		}
		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (!(v instanceof Collection)) {
			throw new IllegalArgumentException(
					"Property: " + getProperty() + " of class: " + t.getClass().toString() + " is not a Collection");
		}
		return ((Collection<T>) v).contains(getValue());
	}
}
