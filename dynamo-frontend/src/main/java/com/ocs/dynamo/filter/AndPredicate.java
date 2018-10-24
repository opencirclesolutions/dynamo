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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.vaadin.server.SerializablePredicate;

/**
 * A predicate for joining multiple predicates based on the logical AND
 * 
 * @author Bas Rutten
 *
 * @param <T>
 */
public class AndPredicate<T> implements SerializablePredicate<T> {

	private static final long serialVersionUID = 4018552369404222691L;

	private final List<SerializablePredicate<T>> operands = new ArrayList<>();

	@Override
	public boolean test(T t) {
		return operands.stream().allMatch(o -> o.test(t));
	}

	@SafeVarargs
	public AndPredicate(SerializablePredicate<T>... predicates) {
		if (predicates != null) {
			for (SerializablePredicate<T> p : predicates) {
				operands.add(p);
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public AndPredicate<T> and(Predicate<? super T> other) {
		operands.add((SerializablePredicate<T>) other);
		return this;
	}

	public List<SerializablePredicate<T>> getOperands() {
		return operands;
	}
}
