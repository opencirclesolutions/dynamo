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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.ocs.dynamo.domain.model.AttributeModel;
import com.ocs.dynamo.domain.model.AttributeType;
import com.ocs.dynamo.domain.model.EntityModel;
import com.ocs.dynamo.exception.OCSRuntimeException;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.AbstractJunctionFilter;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Like;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.SimpleStringFilter;

/**
 * Various utility methods for dealing with filters
 * 
 * @author bas.rutten
 *
 */
public final class FilterUtil {

	private FilterUtil() {
		// hidden constructor
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
	public static com.ocs.dynamo.filter.Filter extractFilter(com.ocs.dynamo.filter.Filter filter, String propertyId) {
		if (filter instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
			com.ocs.dynamo.filter.AbstractJunctionFilter junction = (com.ocs.dynamo.filter.AbstractJunctionFilter) filter;
			for (com.ocs.dynamo.filter.Filter child : junction.getFilters()) {
				com.ocs.dynamo.filter.Filter found = extractFilter(child, propertyId);
				if (found != null) {
					return found;
				}
			}
		} else if (filter instanceof com.ocs.dynamo.filter.Compare) {
			com.ocs.dynamo.filter.Compare compare = (com.ocs.dynamo.filter.Compare) filter;
			if (compare.getPropertyId().equals(propertyId)) {
				return compare;
			}
		} else if (filter instanceof com.ocs.dynamo.filter.Like) {
			com.ocs.dynamo.filter.Like like = (com.ocs.dynamo.filter.Like) filter;
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
		} else if (filter instanceof com.ocs.dynamo.filter.Between) {
			com.ocs.dynamo.filter.Between between = (com.ocs.dynamo.filter.Between) filter;
			if (between.getPropertyId().equals(propertyId)) {
				return between;
			}
		}
		return null;

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

	public static Filter extractFilter(Filter filter, String propertyId, Class<? extends Filter>[] typesToFind) {
		List<Class<? extends Filter>> types = typesToFind == null || typesToFind.length == 0
		        || (typesToFind.length == 1 && typesToFind[0] == null) ? null : Arrays.asList(typesToFind);
		if (filter instanceof AbstractJunctionFilter) {
			AbstractJunctionFilter junction = (AbstractJunctionFilter) filter;
			for (Filter child : junction.getFilters()) {
				Filter found = extractFilter(child, propertyId, typesToFind);
				if (found != null) {
					return found;
				}
			}
		} else if (filter instanceof com.vaadin.data.util.filter.Compare
		        && (types == null || types.contains(filter.getClass()))) {
			com.vaadin.data.util.filter.Compare compare = (com.vaadin.data.util.filter.Compare) filter;
			if (compare.getPropertyId().equals(propertyId)) {
				return compare;
			}
		} else if (filter instanceof com.vaadin.data.util.filter.Like
		        && (types == null || types.contains(filter.getClass()))) {
			com.vaadin.data.util.filter.Like like = (com.vaadin.data.util.filter.Like) filter;
			if (like.getPropertyId().equals(propertyId)) {
				return like;
			}
		} else if (filter instanceof SimpleStringFilter && (types == null || types.contains(filter.getClass()))) {
			SimpleStringFilter ssf = (SimpleStringFilter) filter;
			if (ssf.getPropertyId().equals(propertyId)) {
				return ssf;
			}
		} else if (filter instanceof Between && (types == null || types.contains(filter.getClass()))) {
			Between between = (Between) filter;
			if (between.getPropertyId().equals(propertyId)) {
				return between;
			}
		}
		return null;
	}

	/**
	 * Extracts a specific filter value from a (possibly) composite filter, for between only the
	 * start value is returned
	 *
	 * @param filter
	 *            the filter from which to extract a certain part
	 * @param propertyId
	 *            the propertyId of the filter to extract
	 * @return
	 */
	@SafeVarargs
	public static Object extractFilterValue(Filter filter, String propertyId, Class<? extends Filter>... typesToFind) {
		List<Class<? extends Filter>> types = typesToFind == null
		        || (typesToFind.length == 1 && typesToFind[0] == null) ? null : Arrays.asList(typesToFind);
		if (filter instanceof AbstractJunctionFilter) {
			AbstractJunctionFilter junction = (AbstractJunctionFilter) filter;
			for (Filter child : junction.getFilters()) {
				Filter found = extractFilter(child, propertyId, typesToFind);
				if (found != null) {
					Object value = extractFilterValue(found, propertyId, typesToFind);
					if (value != null) {
						return value;
					}
				}
			}
		} else if (filter instanceof com.vaadin.data.util.filter.Compare
		        && (types == null || types.contains(filter.getClass()))) {
			com.vaadin.data.util.filter.Compare compare = (com.vaadin.data.util.filter.Compare) filter;
			if (compare.getPropertyId().equals(propertyId)) {
				return compare.getValue();
			}
		} else if (filter instanceof com.vaadin.data.util.filter.Like
		        && (types == null || types.contains(filter.getClass()))) {
			com.vaadin.data.util.filter.Like like = (com.vaadin.data.util.filter.Like) filter;
			if (like.getPropertyId().equals(propertyId)) {
				return like.getValue();
			}
		} else if (filter instanceof SimpleStringFilter && (types == null || types.contains(filter.getClass()))) {
			SimpleStringFilter ssf = (SimpleStringFilter) filter;
			if (ssf.getPropertyId().equals(propertyId)) {
				return ssf.getFilterString();
			}
		} else if (filter instanceof Between && (types == null || types.contains(filter.getClass()))) {
			Between ssf = (Between) filter;
			if (ssf.getPropertyId().equals(propertyId)) {
				return ssf.getStartValue();
			}
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
	public static List<com.ocs.dynamo.filter.Filter> flattenAnd(com.ocs.dynamo.filter.And and) {
		List<com.ocs.dynamo.filter.Filter> children = new ArrayList<>();

		for (com.ocs.dynamo.filter.Filter f : and.getFilters()) {
			if (f instanceof com.ocs.dynamo.filter.And) {
				com.ocs.dynamo.filter.And childAnd = (com.ocs.dynamo.filter.And) f;
				List<com.ocs.dynamo.filter.Filter> temp = flattenAnd(childAnd);
				children.addAll(temp);
			} else {
				children.add(f);
			}
		}
		return children;
	}

	/**
	 * Indicated whether a certain filter (that is contained somewhere as a child of the provided
	 * filter) has the value "true"
	 * 
	 * @param filter
	 *            the root filter
	 * @param propertyId
	 *            the property ID
	 * @return
	 */
	public static boolean isTrue(Filter filter, String propertyId) {
		Filter extracted = extractFilter(filter, propertyId, null);
		if (extracted != null && extracted instanceof Equal) {
			Equal equal = (Equal) extracted;
			return Boolean.TRUE.equals(equal.getValue());
		}
		return false;
	}

	/**
	 * Checks if at least one filter value is set
	 * 
	 * @param filter
	 *            the filter to check
	 * @param ignore
	 *            the list of properties to ignore when checking (even if a value for one or more of
	 *            these properties is set, this will not cause a return value of <code>true</code>)
	 * @return
	 */
	public static boolean isFilterValueSet(Filter filter, Set<String> ignore) {
		boolean result = false;
		if (filter instanceof AbstractJunctionFilter) {
			AbstractJunctionFilter jf = (AbstractJunctionFilter) filter;
			for (Filter f : jf.getFilters()) {
				result |= isFilterValueSet(f, ignore);
			}
		} else if (filter instanceof Compare) {
			Compare eq = (Compare) filter;
			if (ignore.contains(eq.getPropertyId())) {
				return false;
			}
			return eq.getValue() != null;
		} else if (filter instanceof SimpleStringFilter) {
			SimpleStringFilter sf = (SimpleStringFilter) filter;
			if (ignore.contains(sf.getPropertyId())) {
				return false;
			}
			return !StringUtils.isEmpty(sf.getFilterString());
		} else if (filter instanceof Like) {
			Like lf = (Like) filter;
			if (ignore.contains(lf.getPropertyId())) {
				return false;
			}
			return !StringUtils.isEmpty(lf.getValue());
		} else if (filter instanceof Between) {
			Between bt = (Between) filter;
			if (ignore.contains(bt.getPropertyId())) {
				return false;
			}
			return bt.getStartValue() != null || bt.getEndValue() != null;
		}

		return result;
	}

	/**
	 * Removes filters with certain property IDs from a certain filter
	 * 
	 * @param filter
	 *            the filter to remove the filters from
	 * @param propertyIds
	 *            the property IDs of the filters to remove
	 */
	public static void removeFilters(com.ocs.dynamo.filter.Filter filter, String... propertyIds) {
		if (filter instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
			// junction filter, iterate over its children
			com.ocs.dynamo.filter.AbstractJunctionFilter junction = (com.ocs.dynamo.filter.AbstractJunctionFilter) filter;
			Iterator<com.ocs.dynamo.filter.Filter> it = junction.getFilters().iterator();

			// remove simple filters
			while (it.hasNext()) {
				com.ocs.dynamo.filter.Filter child = it.next();
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
				com.ocs.dynamo.filter.Filter child = it.next();
				if (!(child instanceof PropertyFilter)) {
					removeFilters(child, propertyIds);
				}
			}

			// clean up empty filters
			it = junction.getFilters().iterator();
			while (it.hasNext()) {
				com.ocs.dynamo.filter.Filter child = it.next();
				if (child instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
					com.ocs.dynamo.filter.AbstractJunctionFilter ajf = (com.ocs.dynamo.filter.AbstractJunctionFilter) child;
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

			com.ocs.dynamo.filter.Filter child = not.getFilter();
			if (child instanceof PropertyFilter) {
				PropertyFilter pf = (PropertyFilter) child;
				for (String s : propertyIds) {
					if (pf.getPropertyId().equals(s)) {
						not.setFilter(null);
					}
				}
			} else if (child instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
				com.ocs.dynamo.filter.AbstractJunctionFilter ajf = (com.ocs.dynamo.filter.AbstractJunctionFilter) child;
				if (ajf.getFilters().isEmpty()) {
					not.setFilter(null);
				}
			}
		}
	}

	/**
	 * Replaces a filter by another filter. This method only works for junction filters
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
	private static void replaceFilter(com.ocs.dynamo.filter.Filter parent, com.ocs.dynamo.filter.Filter original,
	        com.ocs.dynamo.filter.Filter newFilter, String propertyId, boolean firstOnly) {
		if (original instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
			// junction filter, iterate over its children
			com.ocs.dynamo.filter.AbstractJunctionFilter junction = (com.ocs.dynamo.filter.AbstractJunctionFilter) original;
			for (com.ocs.dynamo.filter.Filter child : junction.getFilters()) {
				replaceFilter(junction, child, newFilter, propertyId, firstOnly);
			}
		} else if (original instanceof PropertyFilter) {
			// filter has a property ID, see if it matches
			PropertyFilter pf = (PropertyFilter) original;
			if (pf.getPropertyId().equals(propertyId)) {
				if (parent instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
					com.ocs.dynamo.filter.AbstractJunctionFilter pj = (com.ocs.dynamo.filter.AbstractJunctionFilter) parent;
					pj.replace(original, newFilter, firstOnly);
				} else if (parent instanceof Not) {
					Not not = (Not) parent;
					not.setFilter(newFilter);
				}

				// throw exception to abort processing - this is nasty but better than propagating
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
	public static void replaceFilter(com.ocs.dynamo.filter.Filter original, com.ocs.dynamo.filter.Filter newFilter,
	        String propertyId, boolean firstOnly) {
		try {
			replaceFilter(null, original, newFilter, propertyId, firstOnly);
		} catch (RuntimeException ex) {
			// do nothing - only used to break out of loop
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
	 *            optional property - if supplied, then we the application will check this property
	 *            instead of the property supplied in the original filters
	 */
	public static void replaceMasterAndDetailFilters(com.ocs.dynamo.filter.Filter filter, EntityModel<?> entityModel) {

		// iterate over models and try to find filters that query DETAIL relations
		for (AttributeModel am : entityModel.getAttributeModels()) {
			replaceMasterDetailFilter(filter, am);

			if (am.getNestedEntityModel() != null) {
				replaceMasterAndDetailFilters(filter, am.getNestedEntityModel());
			}
		}
	}

	/**
	 * Replaces a "Compare.Equal" filter that searches on a master or detail field by a "Contains"
	 * or "In" filter
	 * 
	 * @param filter
	 *            the filter
	 * @param am
	 *            the attribute model
	 */
	private static void replaceMasterDetailFilter(com.ocs.dynamo.filter.Filter filter, AttributeModel am) {
		if (AttributeType.DETAIL.equals(am.getAttributeType())
		        || AttributeType.ELEMENT_COLLECTION.equals(am.getAttributeType())
		        || ((AttributeType.MASTER.equals(am.getAttributeType()) || AttributeType.BASIC.equals(am
		                .getAttributeType())) && am.isMultipleSearch())) {
			com.ocs.dynamo.filter.Filter detailFilter = FilterUtil.extractFilter(filter, am.getPath());
			if (detailFilter != null && detailFilter instanceof com.ocs.dynamo.filter.Compare.Equal) {
				// check which property to use in the query
				String prop = am.getReplacementSearchPath() != null ? am.getReplacementSearchPath() : am.getPath();

				com.ocs.dynamo.filter.Compare.Equal bf = (com.ocs.dynamo.filter.Compare.Equal) detailFilter;
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
}
