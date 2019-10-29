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

import com.vaadin.flow.function.SerializablePredicate;

/**
 * A predicate that can be used for negating another predicate
 * 
 * @author Bas Rutten
 *
 * @param <T> the type of the entity to filter on
 */
public class NotPredicate<T> implements SerializablePredicate<T> {

	private static final long serialVersionUID = 4018552369404222694L;

	private final SerializablePredicate<T> operand;

	@Override
	public boolean test(T t) {
		return !operand.test(t);
	}

	public NotPredicate(SerializablePredicate<T> predicate) {
		if (predicate != null) {
			operand = predicate;
		} else {
			throw new IllegalArgumentException("Predicate may not be null");
		}
	}

	public SerializablePredicate<T> getOperand() {
		return operand;
	}
}
