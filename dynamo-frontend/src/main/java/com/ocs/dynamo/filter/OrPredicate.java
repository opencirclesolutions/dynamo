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

/**
 * A predicate for joining multiple predicates based on the logical OR
 * 
 * @author Bas Rutten
 *
 * @param <T> the type parameters
 */
public class OrPredicate<T> extends CompositePredicate<T> {

	private static final long serialVersionUID = 4018552369404222691L;

	@SafeVarargs
	public OrPredicate(SerializablePredicate<T>... predicates) {
		if (predicates != null) {
			for (SerializablePredicate<T> p : predicates) {
				getOperands().add(p);
			}
		}
	}

	@Override
	public boolean test(T t) {
		return getOperands().stream().anyMatch(o -> o.test(t));
	}

	@Override
	@SuppressWarnings("unchecked")
	public SerializablePredicate<T> or(Predicate<? super T> other) {
		getOperands().add((SerializablePredicate<T>) other);
		return this;
	}

}
