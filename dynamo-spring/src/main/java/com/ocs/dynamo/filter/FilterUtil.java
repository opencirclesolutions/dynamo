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

import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.AbstractJunctionFilter;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.data.util.filter.Compare.Equal;

public final class FilterUtil {

    private FilterUtil() {
    }

    /**
     * Indicated whether a certain filter (that contains somewhere as a child of the provided
     * filter) has the value "true"
     * 
     * @param filter
     *            the root filter
     * @param propertyId
     * @return
     */
    public static boolean isTrue(Filter filter, String propertyId) {
        Filter extracted = extractFilter(filter, propertyId);
        if (extracted != null && extracted instanceof Equal) {
            Equal equal = (Equal) extracted;
            return Boolean.TRUE.equals(equal.getValue());
        }
        return false;
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
        } else if (filter instanceof com.vaadin.data.util.filter.Compare) {
            com.vaadin.data.util.filter.Compare compare = (com.vaadin.data.util.filter.Compare) filter;
            if (compare.getPropertyId().equals(propertyId)) {
                return compare;
            }
        } else if (filter instanceof com.vaadin.data.util.filter.Like) {
            com.vaadin.data.util.filter.Like like = (com.vaadin.data.util.filter.Like) filter;
            if (like.getPropertyId().equals(propertyId)) {
                return like;
            }
        } else if (filter instanceof SimpleStringFilter) {
            SimpleStringFilter ssf = (SimpleStringFilter) filter;
            if (ssf.getPropertyId().equals(propertyId)) {
                return ssf;
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
    public static com.ocs.dynamo.filter.Filter extractFilter(com.ocs.dynamo.filter.Filter filter,
            String propertyId) {
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
        }

        return null;

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
    public static void replaceFilter(com.ocs.dynamo.filter.Filter parent,
            com.ocs.dynamo.filter.Filter original, com.ocs.dynamo.filter.Filter newFilter,
            String propertyId) {
        if (original instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
            // junction filter, iterate over its children
            com.ocs.dynamo.filter.AbstractJunctionFilter junction = (com.ocs.dynamo.filter.AbstractJunctionFilter) original;
            for (com.ocs.dynamo.filter.Filter child : junction.getFilters()) {
                replaceFilter(junction, child, newFilter, propertyId);
            }
        } else if (original instanceof PropertyFilter) {
            // filter has a property ID, see if it matches
            PropertyFilter pf = (PropertyFilter) original;
            if (pf.getPropertyId().equals(propertyId)) {
                if (parent instanceof com.ocs.dynamo.filter.AbstractJunctionFilter) {
                    com.ocs.dynamo.filter.AbstractJunctionFilter pj = (com.ocs.dynamo.filter.AbstractJunctionFilter) parent;
                    pj.replace(original, newFilter);
                } else if (parent instanceof Not) {
                    Not not = (Not) parent;
                    not.setFilter(newFilter);
                }
            }
        } else if (original instanceof Not) {
            // in case of a not-filter, propagate to the child
            Not not = (Not) original;
            replaceFilter(not, not.getFilter(), newFilter, propertyId);
        }
    }
}
