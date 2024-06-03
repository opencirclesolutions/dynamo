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
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Set;

/**
 * Various utility methods for dealing with filters
 * 
 * @author bas.rutten
 *
 */
@UtilityClass
public final class PredicateUtils {

	/**
	 * Extracts a specific predicate from a larger predicate
	 *
	 * @param predicate   the predicate from which to extract a certain part
	 * @param propertyId  the propertyId of the predicate to extract
	 * @param typesToFind the types/classes of the predicates to look for
	 * @return the predicate that results
	 */
	public static <T> SerializablePredicate<T> extractPredicate(SerializablePredicate<T> predicate, String propertyId,
			Class<?>... typesToFind) {
		List<Class<?>> types = typesToFind == null || typesToFind.length == 0
				|| (typesToFind.length == 1 && typesToFind[0] == null) ? null : List.of(typesToFind);
		if (predicate instanceof CompositePredicate<T> comp) {
			for (SerializablePredicate<T> child : comp.getOperands()) {
				SerializablePredicate<T> found = extractPredicate(child, propertyId, typesToFind);
				if (found != null) {
					return found;
				}
			}
		} else if (predicate instanceof PropertyPredicate<T> prop && (types == null || types.contains(predicate.getClass()))) {
			if (prop.getProperty().equals(propertyId)) {
				return prop;
			}
		} else if (predicate instanceof NotPredicate<T> not) {
			return extractPredicate(not.getOperand(), propertyId);
		}
		return null;
	}

	/**
	 * Extracts a specific predicate value from a (possibly) composite predicate,
	 * for between predicates only the start value is returned
	 *
	 * @param predicate  the predicate from which to extract a certain part
	 * @param propertyId the propertyId of the predicate to extract
	 * @return typesToFind can be used to limit the types of predicates to check
	 */
	public static <T> Object extractPredicateValue(SerializablePredicate<T> predicate, String propertyId,
			Class<?>... typesToFind) {
		SerializablePredicate<T> extracted = extractPredicate(predicate, propertyId, typesToFind);
		if (extracted instanceof PropertyPredicate<T> prop) {
			return prop.getValue();
		}
		return null;
	}

	/**
	 * Indicated whether a certain predicate (that is contained somewhere as a child
	 * of the provided predicate) has the value "true"
	 *
	 * @param predicate  the root predicate
	 * @param propertyId the property ID
	 * @return whether the predicate evaluates to true
	 */
	public static <T> boolean isTrue(SerializablePredicate<T> predicate, String propertyId) {
		SerializablePredicate<T> extracted = extractPredicate(predicate, propertyId);
		if (extracted instanceof EqualsPredicate<T> equal) {
			return Boolean.TRUE.equals(equal.getValue());
		}
		return false;
	}

	/**
	 * Checks if at least one filter value is set
	 *
	 * @param predicate the filter to check
	 * @param ignore the list of properties to ignore when checking (even if a value
	 *               for one or more of these properties is set, this will not cause
	 *               a return value of <code>true</code>)
	 * @return true if the predicate value has been set, false otherwise
	 */
	public static <T> boolean isPredicateValueSet(SerializablePredicate<T> predicate, Set<String> ignore) {
		boolean result = false;
		if (predicate instanceof CompositePredicate<T> composite) {
			for (SerializablePredicate<T> pred : composite.getOperands()) {
				result |= isPredicateValueSet(pred, ignore);
			}
		} else if (predicate instanceof BetweenPredicate<T> between) {
			if (ignore.contains(between.getProperty())) {
				return false;
			}
			return between.getValue() != null || between.getToValue() != null;
		} else if (predicate instanceof PropertyPredicate<T> eq) {
			if (ignore.contains(eq.getProperty())) {
				return false;
			}
			return eq.getValue() != null;
		}
		return result;
	}

}
