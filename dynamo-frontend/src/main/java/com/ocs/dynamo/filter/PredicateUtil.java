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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.vaadin.server.SerializablePredicate;

/**
 * Various utility methods for dealing with filters
 * 
 * @author bas.rutten
 *
 */
public final class PredicateUtil {

	private PredicateUtil() {
		// hidden constructor
	}

	/**
	 * Extract a predicate from a larger predicate
	 * 
	 * @param predicate  the larger predicate
	 * @param propertyId the property of the predicate to search for
	 * @return
	 */
	public static <T> SerializablePredicate<T> extractPredicate(SerializablePredicate<T> predicate, String propertyId) {
		if (predicate instanceof CompositePredicate) {
			CompositePredicate<T> comp = (CompositePredicate<T>) predicate;
			for (SerializablePredicate<T> child : comp.getOperands()) {
				SerializablePredicate<T> found = extractPredicate(child, propertyId);
				if (found != null) {
					return found;
				}
			}
		} else if (predicate instanceof PropertyPredicate) {
			PropertyPredicate<T> eq = (PropertyPredicate<T>) predicate;
			if (eq.getProperty().equals(propertyId)) {
				return eq;
			}
		} else if (predicate instanceof NotPredicate) {
			NotPredicate<T> not = (NotPredicate<T>) predicate;
			return extractPredicate(not.getOperand(), propertyId);
		}
		return null;
	}

	/**
	 * Extracts a specific predicate from a larger predicate
	 *
	 * @param predicate  the predicate from which to extract a certain part
	 * @param propertyId the propertyId of the predicate to extract
	 * @return
	 */
	public static <T> SerializablePredicate<T> extractPredicate(SerializablePredicate<T> predicate, String propertyId,
			Class<? extends SerializablePredicate<T>>[] typesToFind) {
		List<Class<? extends SerializablePredicate<T>>> types = typesToFind == null || typesToFind.length == 0
				|| (typesToFind.length == 1 && typesToFind[0] == null) ? null : Arrays.asList(typesToFind);
		if (predicate instanceof CompositePredicate) {
			CompositePredicate<T> comp = (CompositePredicate<T>) predicate;
			for (SerializablePredicate<T> child : comp.getOperands()) {
				SerializablePredicate<T> found = extractPredicate(child, propertyId, typesToFind);
				if (found != null) {
					return found;
				}
			}
		} else if (predicate instanceof PropertyPredicate && (types == null || types.contains(predicate.getClass()))) {
			PropertyPredicate<T> prop = (PropertyPredicate<T>) predicate;
			if (prop.getProperty().equals(propertyId)) {
				return prop;
			}
		}
		return null;
	}

	/**
	 * Extracts a specific predicate value from a (possibly) composite predicate,
	 * for between predicates only the start value is returned
	 *
	 * @param predicate  the predicate from which to extract a certain part
	 * @param propertyId the propertyId of the predicate to extract
	 * @return
	 */
	@SafeVarargs
	public static <T> Object extractPredicateValue(SerializablePredicate<T> predicate, String propertyId,
			Class<? extends SerializablePredicate<T>>... typesToFind) {
		List<Class<? extends SerializablePredicate<T>>> types = typesToFind == null || typesToFind.length == 0
				|| (typesToFind.length == 1 && typesToFind[0] == null) ? null : Arrays.asList(typesToFind);
		if (predicate instanceof CompositePredicate) {
			CompositePredicate<T> comp = (CompositePredicate<T>) predicate;
			for (SerializablePredicate<T> child : comp.getOperands()) {
				SerializablePredicate<T> found = extractPredicate(child, propertyId, typesToFind);
				if (found != null) {
					return extractPredicateValue(found, propertyId, typesToFind);
				}
			}
		} else if (predicate instanceof PropertyPredicate && (types == null || types.contains(predicate.getClass()))) {
			PropertyPredicate<T> compare = (PropertyPredicate<T>) predicate;
			if (compare.getProperty().equals(propertyId)) {
				return compare.getValue();
			}
		}
		return null;
	}

	/**
	 * Indicated whether a certain predicate (that is contained somewhere as a child
	 * of the provided predicate) has the value "true"
	 *
	 * @param predicate  the root predicate
	 * @param propertyId the property ID
	 * @return
	 */
	public static <T> boolean isTrue(SerializablePredicate<T> predicate, String propertyId) {
		SerializablePredicate<T> extracted = extractPredicate(predicate, propertyId);
		if (extracted != null && extracted instanceof EqualsPredicate) {
			EqualsPredicate<T> equal = (EqualsPredicate<T>) extracted;
			return Boolean.TRUE.equals(equal.getValue());
		}
		return false;
	}

	/**
	 * Checks if at least one filter value is set
	 *
	 * @param filter the filter to check
	 * @param ignore the list of properties to ignore when checking (even if a value
	 *               for one or more of these properties is set, this will not cause
	 *               a return value of <code>true</code>)
	 * @return
	 */
	public static <T> boolean isPredicateValueSet(SerializablePredicate<T> predicate, Set<String> ignore) {
		boolean result = false;
		if (predicate instanceof CompositePredicate) {
			CompositePredicate<T> jf = (CompositePredicate<T>) predicate;
			for (SerializablePredicate<T> f : jf.getOperands()) {
				result |= isPredicateValueSet(f, ignore);
			}
		} else if (predicate instanceof BetweenPredicate) {
			BetweenPredicate<T> between = (BetweenPredicate<T>) predicate;
			if (ignore.contains(between.getProperty())) {
				return false;
			}
			return between.getValue() != null || between.getToValue() != null;
		} else if (predicate instanceof PropertyPredicate) {
			PropertyPredicate<T> eq = (PropertyPredicate<T>) predicate;
			if (ignore.contains(eq.getProperty())) {
				return false;
			}
			return eq.getValue() != null;
		}

		return result;
	}

}
