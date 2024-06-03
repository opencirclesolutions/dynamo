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

import lombok.Getter;

/**
 * A predicate for filtering on a value that is between a lower and an upper
 * bound. The bounds are inclusive
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity to filter
 */
public class BetweenPredicate<T> extends PropertyPredicate<T> {

	private static final long serialVersionUID = -5077087872701525001L;

	@Getter
	private final Comparable<?> toValue;

	public BetweenPredicate(String property, Comparable<?> fromValue, Comparable<?> toValue) {
		super(property, fromValue);
		this.toValue = toValue;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean test(T t) {
		if (t == null) {
			return false;
		}

		Object v = ClassUtils.getFieldValue(t, getProperty());
		if (v == null) {
			return false;
		}
		return ((Comparable) v).compareTo(getFromValue()) >= 0 && ((Comparable) v).compareTo(toValue) <= 0;
	}

	public Comparable<?> getFromValue() {
		return (Comparable<?>) getValue();
	}

}
