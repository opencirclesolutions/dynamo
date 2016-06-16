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
import com.vaadin.data.Container.Filter;
import com.vaadin.data.util.filter.AbstractJunctionFilter;
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
     * Indicated whether a certain filter (that contains somewhere as a child of the provided
     * filter) has the value "true"
     * 
     * @param filter
     *            the root filter
     * @param propertyId
     *            the property ID
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
     * Removes the specified filters
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
                    if (ajf.getFilters().size() == 0) {
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
                if (ajf.getFilters().size() == 0) {
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
    public static void replaceMasterAndDetailFilters(com.ocs.dynamo.filter.Filter filter,
            EntityModel<?> entityModel) {

        // iterate over models and try to find filters that query DETAIL relations
        for (AttributeModel am : entityModel.getAttributeModels()) {
            if (AttributeType.DETAIL.equals(am.getAttributeType())
                    || (AttributeType.MASTER.equals(am.getAttributeType()) && am.isMultipleSearch())) {
                com.ocs.dynamo.filter.Filter detailFilter = FilterUtil.extractFilter(filter,
                        am.getPath());
                if (detailFilter != null) {
                    // check which property to use
                    String prop = am.getReplacementSearchPath() != null ? am
                            .getReplacementSearchPath() : am.getPath();

                    com.ocs.dynamo.filter.Compare.Equal bf = (Compare.Equal) detailFilter;
                    if (AttributeType.DETAIL.equals(am.getAttributeType())) {

                        if (bf.getValue() instanceof Collection) {
                            // multiple values supplied - construct an OR filter
                            Collection<?> col = (Collection<?>) bf.getValue();
                            Or or = new Or();
                            for (Object o : col) {
                                or.or(new Contains(prop, o));
                            }
                            replaceFilter(null, filter, or, am.getPath());
                        } else {
                            // just a single value
                            replaceFilter(null, filter, new Contains(prop, bf.getValue()),
                                    am.getPath());
                        }
                    } else {
                        // master attribute - translate to an "in" filter
                        if (bf.getValue() instanceof Collection) {
                            // multiple values supplied - construct an OR filter
                            Collection<?> col = (Collection<?>) bf.getValue();
                            In in = new In(prop, col);
                            replaceFilter(null, filter, in, am.getPath());
                        }
                    }
                }
            }
        }
    }
}
