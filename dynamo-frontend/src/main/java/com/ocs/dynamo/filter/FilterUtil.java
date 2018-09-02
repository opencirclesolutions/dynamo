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

import org.springframework.util.StringUtils;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.AbstractJunctionFilter;
import com.vaadin.data.util.filter.Between;
import com.vaadin.data.util.filter.Compare;
import com.vaadin.data.util.filter.Compare.Equal;
import com.vaadin.data.util.filter.Like;
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
	 * Extracts a specific filter value from a (possibly) composite filter, for
	 * between only the start value is returned
	 *
	 * @param filter
	 *            the filter from which to extract a certain part
	 * @param propertyId
	 *            the propertyId of the filter to extract
	 * @return
	 */
	@SafeVarargs
	public static Object extractFilterValue(Filter filter, String propertyId, Class<? extends Filter>... typesToFind) {
		List<Class<? extends Filter>> types = typesToFind == null || (typesToFind.length == 1 && typesToFind[0] == null)
				? null
				: Arrays.asList(typesToFind);
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
	 * Indicated whether a certain filter (that is contained somewhere as a child of
	 * the provided filter) has the value "true"
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
	 *            the list of properties to ignore when checking (even if a value
	 *            for one or more of these properties is set, this will not cause a
	 *            return value of <code>true</code>)
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

}
