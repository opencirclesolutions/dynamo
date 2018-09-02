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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;

/**
 * 
 * @author Bas Rutten
 *
 */
public final class DynamoFilterUtil {

	private DynamoFilterUtil() {
	}

	/**
	 * Extracts a specific filter from a (possibly) composite filter
	 * 
	 * @param filter
	 *            the filter from which to extract a certain part
	 * @param propertyId
	 *            the propertyId of the filter to extract
	 * @return
	 */
	public static Filter extractFilter(Filter filter, String propertyId) {
		if (filter instanceof AbstractJunctionFilter) {
			AbstractJunctionFilter junction = (AbstractJunctionFilter) filter;
			for (Filter child : junction.getFilters()) {
				Filter found = extractFilter(child, propertyId);
				if (found != null) {
					return found;
				}
			}
		} else if (filter instanceof Compare) {
			Compare compare = (Compare) filter;
			if (compare.getPropertyId().equals(propertyId)) {
				return compare;
			}
		} else if (filter instanceof Like) {
			Like like = (Like) filter;
			if (like.getPropertyId().equals(propertyId)) {
				return like;
			}
		} else if (filter instanceof In) {
			In in = (In) filter;
			if (in.getPropertyId().equals(propertyId)) {
				return in;
			}
		} else if (filter instanceof Contains) {
			Contains c = (Contains) filter;
			if (c.getPropertyId().equals(propertyId)) {
				return c;
			}
		} else if (filter instanceof Between) {
			Between between = (Between) filter;
			if (between.getPropertyId().equals(propertyId)) {
				return between;
			}
		} else if (filter instanceof Not) {
			Not not = (Not) filter;
			return extractFilter(not.getFilter(), propertyId);
		}
		return null;

	}

	/**
	 * Take a (nested) And filter and flatten it to a single level
	 * 
	 * @param and
	 *            the filter to flatten
	 * @return
	 */
	public static List<Filter> flattenAnd(And and) {
		List<Filter> children = new ArrayList<>();

		for (Filter f : and.getFilters()) {
			if (f instanceof And) {
				And childAnd = (And) f;
				List<Filter> temp = flattenAnd(childAnd);
				children.addAll(temp);
			} else {
				children.add(f);
			}
		}
		return children;
	}

	/**
	 * Removes filters with certain property IDs from a certain filter
	 * 
	 * @param filter
	 *            the filter to remove the filters from
	 * @param propertyIds
	 *            the property IDs of the filters to remove
	 */
	public static void removeFilters(Filter filter, String... propertyIds) {
		if (filter instanceof AbstractJunctionFilter) {
			// junction filter, iterate over its children
			AbstractJunctionFilter junction = (AbstractJunctionFilter) filter;
			Iterator<Filter> it = junction.getFilters().iterator();

			// remove simple filters
			while (it.hasNext()) {
				Filter child = it.next();
				if (child instanceof PropertyFilter) {
					PropertyFilter pf = (PropertyFilter) child;
					for (String s : propertyIds) {
						if (pf.getPropertyId().equals(s)) {
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

			// clean up empty filters
			it = junction.getFilters().iterator();
			while (it.hasNext()) {
				Filter child = it.next();
				if (child instanceof AbstractJunctionFilter) {
					AbstractJunctionFilter ajf = (AbstractJunctionFilter) child;
					if (ajf.getFilters().isEmpty()) {
						it.remove();
					}
				} else if (child instanceof Not) {
					Not not = (Not) child;
					if (not.getFilter() == null) {
						it.remove();
					}
				}
			}
		} else if (filter instanceof Not) {
			// in case of a not-filter, propagate to the child
			Not not = (Not) filter;

			if (not.getFilter() != null) {
				removeFilters(not.getFilter(), propertyIds);
			}

			Filter child = not.getFilter();
			if (child instanceof PropertyFilter) {
				PropertyFilter pf = (PropertyFilter) child;
				for (String s : propertyIds) {
					if (pf.getPropertyId().equals(s)) {
						not.setFilter(null);
					}
				}
			} else if (child instanceof AbstractJunctionFilter) {
				AbstractJunctionFilter ajf = (AbstractJunctionFilter) child;
				if (ajf.getFilters().isEmpty()) {
					not.setFilter(null);
				}
			}
		}
	}

	/**
	 * Replaces all filters that query a detail relation by the appropriate filters
	 * 
	 * @param filter
	 *            the original filter
	 * @param entityModel
	 *            the entity model used to determine which filters must be replaced
	 * @param overrideProperty
	 *            optional property - if supplied, then we the application will
	 *            check this property instead of the property supplied in the
	 *            original filters
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
	 * @param filter
	 *            the filter
	 * @param am
	 *            the attribute model
	 */
	private static void replaceMasterDetailFilter(Filter filter, AttributeModel am) {
		if (AttributeType.DETAIL.equals(am.getAttributeType())
				|| AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())
				|| ((AttributeType.MASTER.equals(am.getAttributeType())
						|| AttributeType.BASIC.equals(am.getAttributeType())) && am.isMultipleSearch())) {
			Filter detailFilter = extractFilter(filter, am.getPath());
			if (detailFilter != null && detailFilter instanceof Compare.Equal) {
				// check which property to use in the query
				String prop = am.getReplacementSearchPath() != null ? am.getReplacementSearchPath() : am.getPath();

				Compare.Equal bf = (Compare.Equal) detailFilter;
				if (AttributeType.DETAIL.equals(am.getAttributeType())
						|| AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())) {
					if (bf.getValue() instanceof Collection) {
						// multiple values supplied - construct an OR filter
						Collection<?> col = (Collection<?>) bf.getValue();
						Or or = new Or();
						for (Object o : col) {
							or.or(new Contains(prop, o));
						}
						replaceFilter(filter, or, am.getPath(), false);
					} else {
						// just a single value - construct a single contains filter
						replaceFilter(filter, new Contains(prop, bf.getValue()), am.getPath(), false);
					}
				} else {
					// master attribute - translate to an "in" filter
					if (bf.getValue() instanceof Collection) {
						// multiple values supplied - construct an OR filter
						Collection<?> col = (Collection<?>) bf.getValue();
						In in = new In(prop, col);
						replaceFilter(null, filter, in, am.getPath(), false);
					}
				}
			}
		}
	}

	/**
	 * Replaces a filter by another filter
	 * 
	 * @param original
	 *            the main filter that contains the filter to be replaced
	 * @param newFilter
	 *            the replacement filter
	 * @param propertyId
	 *            the property ID of the filter to replace
	 * @param firstOnly
	 *            indicates whether to replace only the first instance
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
	 * @param parent
	 *            the parent
	 * @param original
	 *            the original filter
	 * @param newFilter
	 *            the new filter
	 * @param propertyId
	 *            the property id of the filter that must be replaced
	 */
	private static void replaceFilter(Filter parent, Filter original, Filter newFilter, String propertyId,
			boolean firstOnly) {
		if (original instanceof AbstractJunctionFilter) {
			// junction filter, iterate over its children
			AbstractJunctionFilter junction = (AbstractJunctionFilter) original;
			for (Filter child : junction.getFilters()) {
				replaceFilter(junction, child, newFilter, propertyId, firstOnly);
			}
		} else if (original instanceof PropertyFilter) {
			// filter has a property ID, see if it matches
			PropertyFilter pf = (PropertyFilter) original;
			if (pf.getPropertyId().equals(propertyId)) {
				if (parent instanceof AbstractJunctionFilter) {
					AbstractJunctionFilter pj = (AbstractJunctionFilter) parent;
					pj.replace(original, newFilter, firstOnly);
				} else if (parent instanceof Not) {
					Not not = (Not) parent;
					not.setFilter(newFilter);
				}

				// throw exception to abort processing - this is nasty but better than
				// propagating
				// the state via parameters
				if (firstOnly) {
					throw new OCSRuntimeException();
				}
			}
		} else if (original instanceof Not) {
			// in case of a not-filter, propagate to the child
			Not not = (Not) original;
			replaceFilter(not, not.getFilter(), newFilter, propertyId, firstOnly);
		}
	}

}
