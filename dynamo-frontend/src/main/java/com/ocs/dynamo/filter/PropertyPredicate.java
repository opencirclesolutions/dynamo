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

import java.util.function.Predicate;

import com.vaadin.flow.function.SerializablePredicate;

import lombok.Getter;
import lombok.ToString;

/**
 * Base class for a predicate that is used for checking a property against a
 * given value
 * 
 * @author Bas Rutten
 *
 * @param <T>
 */
@ToString
@Getter
public abstract class PropertyPredicate<T> implements SerializablePredicate<T> {

	private static final long serialVersionUID = 777842598678435139L;

	private final String property;

	private final Object value;

	protected PropertyPredicate(String property, Object value) {
		this.property = property;
		this.value = value;
	}

	@Override
	@SuppressWarnings("unchecked")
	public AndPredicate<T> and(Predicate<? super T> other) {
		return new AndPredicate<>(this, (SerializablePredicate<T>) other);
	}

	/**
	 * Checks whether the predicate applies to the specified property
	 * 
	 * @param property the property
	 * @return
	 */
	public boolean appliesToProperty(String property) {
		return this.property.equals(property);
	}

}
