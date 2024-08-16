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

import lombok.experimental.UtilityClass;
import org.dynamoframework.domain.model.AttributeModel;
import org.dynamoframework.domain.model.AttributeType;
import org.dynamoframework.domain.model.EntityModel;
import org.dynamoframework.exception.OCSRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Bas Rutten
 */
@UtilityClass
public final class DynamoFilterUtil {

	/**
	 * Extracts a specific filter from a (possibly) composite filter
	 *
	 * @param filter     the filter from which to extract a certain part
	 * @param propertyId the propertyId of the filter to extract
	 * @return the extracted filter
	 */
	public static Filter extractFilter(Filter filter, String propertyId) {
		if (filter instanceof AbstractJunctionFilter junction) {
			for (Filter child : junction.getFilters()) {
				Filter found = extractFilter(child, propertyId);
				if (found != null) {
					return found;
				}
			}
		} else if (filter instanceof Compare compare) {
			if (compare.getPropertyId().equals(propertyId)) {
				return compare;
			}
		} else if (filter instanceof Like like) {
			if (like.getPropertyId().equals(propertyId)) {
				return like;
			}
		} else if (filter instanceof In in) {
			if (in.getPropertyId().equals(propertyId)) {
				return in;
			}
		} else if (filter instanceof Contains contains) {
			if (contains.getPropertyId().equals(propertyId)) {
				return contains;
			}
		} else if (filter instanceof Between between) {
			if (between.getPropertyId().equals(propertyId)) {
				return between;
			}
		} else if (filter instanceof Not not) {
			return extractFilter(not.getFilter(), propertyId);
		}
		return null;

	}

	/**
	 * Flattens the provided filter, removing any nested And-filters
	 *
	 * @param and the filter to flatten
	 * @return the result of the flattening
	 */
	public static List<Filter> flattenAnd(And and) {
		List<Filter> children = new ArrayList<>();

		for (Filter filter : and.getFilters()) {
			if (filter instanceof And childAnd) {
				List<Filter> temp = flattenAnd(childAnd);
				children.addAll(temp);
			} else {
				children.add(filter);
			}
		}
		return children;
	}

	/**
	 * Removes the specified filters from the provided junction filter
	 *
	 * @param junction    the junction filter
	 * @param propertyIds the propertyIds of the filters to remove
	 */
	private static void removeFilterFormJunction(AbstractJunctionFilter junction, String... propertyIds) {
		Iterator<Filter> it = junction.getFilters().iterator();
		while (it.hasNext()) {
			Filter child = it.next();
			if (child instanceof PropertyFilter propertyFilter) {
				for (String s : propertyIds) {
					if (propertyFilter.getPropertyId().equals(s)) {
						it.remove();
					}
				}
			}
		}

		// pass through to nested junction filters
		it = junction.getFilters().iterator();
		while (it.hasNext()) {
			Filter child = it.next();
			if (!(child instanceof PropertyFilter)) {
				removeFilters(child, propertyIds);
			}
		}
	}

	/**
	 * Remove any empty junction filters that don't contain any filters of their own
	 * anymore
	 *
	 * @param junction the junction filter to remove the empty filters from
	 */
	private static void cleanupEmptyFilters(AbstractJunctionFilter junction) {
		Iterator<Filter> it = junction.getFilters().iterator();
		while (it.hasNext()) {
			Filter child = it.next();
			if (child instanceof AbstractJunctionFilter junctionFilter) {
				if (junctionFilter.getFilters().isEmpty()) {
					it.remove();
				}
			} else if (child instanceof Not not) {
				if (not.getFilter() == null) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Removes filters with the specified property IDs from a certain filter
	 *
	 * @param filter      the filter to remove the filters from
	 * @param propertyIds the property IDs of the filters to remove
	 */
	public static void removeFilters(Filter filter, String... propertyIds) {
		if (filter instanceof AbstractJunctionFilter junction) {
			// junction filter, iterate over its children
			removeFilterFormJunction(junction, propertyIds);
			cleanupEmptyFilters(junction);
		} else if (filter instanceof Not not) {
			// in case of a not-filter, propagate to the child

			if (not.getFilter() != null) {
				removeFilters(not.getFilter(), propertyIds);
			}

			Filter child = not.getFilter();
			if (child instanceof PropertyFilter propertyFilter) {
				for (String s : propertyIds) {
					if (propertyFilter.getPropertyId().equals(s)) {
						not.setFilter(null);
					}
				}
			} else if (child instanceof AbstractJunctionFilter junctionFilter) {
				if (junctionFilter.getFilters().isEmpty()) {
					not.setFilter(null);
				}
			}
		}
	}

	/**
	 * Replaces all filters that query a detail relation by the appropriate filters
	 *
	 * @param filter      the original filter
	 * @param entityModel the entity model used to determine which filters must be
	 *                    replaced
	 */
	public static void replaceMasterAndDetailFilters(Filter filter, EntityModel<?> entityModel) {

		// iterate over models and try to find filters that query DETAIL relations
		for (AttributeModel am : entityModel.getAttributeModels()) {
			replaceMasterDetailFilter(filter, am);
			if (am.getNestedEntityModel() != null) {
				replaceMasterAndDetailFilters(filter, am.getNestedEntityModel());
			}

		}
	}

	/**
	 * Replaces a "Compare.Equal" filter that searches on a master or detail field
	 * by a "Contains" or "In" filter
	 *
	 * @param filter the filter
	 * @param am     the attribute model
	 */
	private static void replaceMasterDetailFilter(Filter filter, AttributeModel am) {
		if (AttributeType.DETAIL.equals(am.getAttributeType())
				|| AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())
				|| AttributeType.MASTER.equals(am.getAttributeType())
				|| (AttributeType.BASIC.equals(am.getAttributeType()) && am.isMultipleSearch())) {
			Filter detailFilter = extractFilter(filter, am.getPath());
			if (detailFilter instanceof Compare.Equal equal) {
				// check which property to use in the query
				String prop = am.getActualSearchPath();

				if (AttributeType.DETAIL.equals(am.getAttributeType())
						|| AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())) {
					replaceDetailOrElementCollectionFilter(filter, am, prop, equal);
				} else {
					// master attribute - translate to an "in" filter
					replaceMasterFilter(filter, am, prop, equal);
				}
			}
		}
	}

	/**
	 * Replaces a filter on a master attribute
	 *
	 * @param filter the overall filter
	 * @param am     the attribute model
	 * @param prop   the name of the property
	 * @param equal  the "equal" filter to replace
	 */
	private static void replaceMasterFilter(Filter filter, AttributeModel am, String prop, Compare.Equal equal) {
		if (equal.getValue() instanceof Collection<?> col) {
			// multiple values supplied - construct an OR filter
			if (!col.isEmpty()) {
				In in = new In(prop, col);
				replaceFilter(null, filter, in, am.getPath(), false);
			} else {
				// filtering on an empty collection is a bad idea
				removeFilters(filter, am.getPath());
			}
		} else if (am.getReplacementSearchPath() != null) {
			// single value property implemented by means of a collection
			Object o = equal.getValue();
			Compare.Equal equals = new Compare.Equal(prop, o);
			replaceFilter(null, filter, equals, am.getPath(), false);
		}
	}

	/**
	 * Replaces a filter on a detail or element collection
	 *
	 * @param filter the overall filter
	 * @param am     the attribute model
	 * @param prop   the property
	 * @param equal  the "equal" filter to replace
	 */
	private static void replaceDetailOrElementCollectionFilter(Filter filter, AttributeModel am, String prop,
			Compare.Equal equal) {
		if (equal.getValue() instanceof Collection<?> col) {
			// multiple values supplied - construct an OR filter

			if (!col.isEmpty()) {
				Or or = new Or();
				for (Object o : col) {
					or.or(new Contains(prop, o));
				}
				replaceFilter(filter, or, am.getPath(), false);
			} else {
				// filtering on an empty collection is a bad idea
				removeFilters(filter, am.getPath());
			}
		} else {
			// just a single value - construct a single contains filter
			replaceFilter(filter, new Contains(prop, equal.getValue()), am.getPath(), false);
		}
	}

	/**
	 * Replaces a filter by another filter
	 *
	 * @param original   the main filter that contains the filter to be replaced
	 * @param newFilter  the replacement filter
	 * @param propertyId the property ID of the filter to replace
	 * @param firstOnly  indicates whether to replace only the first instance
	 */
	public static void replaceFilter(Filter original, Filter newFilter, String propertyId, boolean firstOnly) {
		try {
			replaceFilter(null, original, newFilter, propertyId, firstOnly);
		} catch (RuntimeException ex) {
			// do nothing - only used to break out of loop
		}
	}

	/**
	 * Replaces a filter by another filter. This method only works for junction
	 * filters
	 *
	 * @param parent     the parent
	 * @param original   the original filter
	 * @param newFilter  the new filter
	 * @param propertyId the property id of the filter that must be replaced
	 * @param firstOnly  whether to only replace the first occurrence
	 */
	private static void replaceFilter(Filter parent, Filter original, Filter newFilter, String propertyId,
			boolean firstOnly) {
		if (original instanceof AbstractJunctionFilter junction) {
			// junction filter, iterate over its children
			for (Filter child : junction.getFilters()) {
				replaceFilter(junction, child, newFilter, propertyId, firstOnly);
			}
		} else if (original instanceof PropertyFilter propertyFilter) {
			// filter has a property ID, see if it matches
			if (propertyFilter.getPropertyId().equals(propertyId)) {
				if (parent instanceof AbstractJunctionFilter junctionFilter) {
					junctionFilter.replace(original, newFilter, firstOnly);
				} else if (parent instanceof Not not) {
					not.setFilter(newFilter);
				}

				// throw exception to abort processing - this is nasty but better than
				// propagating
				// the state via parameters
				if (firstOnly) {
					throw new OCSRuntimeException();
				}
			}
		} else if (original instanceof Not not) {
			// in case of a not-filter, propagate to the child
			replaceFilter(not, not.getFilter(), newFilter, propertyId, firstOnly);
		}
	}

}
