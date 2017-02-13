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

/**
 * Abstract class for composite filters
 * 
 * @author bas.rutten
 */
public abstract class AbstractJunctionFilter extends AbstractFilter {

	private final List<Filter> filters = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param filters
	 */
	public AbstractJunctionFilter(Filter... filters) {
		this.filters.addAll(Arrays.asList(filters));
	}

	/**
	 * Constructor
	 * 
	 * @param filters
	 */
	public AbstractJunctionFilter(Collection<Filter> filters) {
		this.filters.addAll(filters);
	}

	/**
	 * Returns an collection of the sub-filters of this composite filter.
	 * 
	 * @return
	 */
	public List<Filter> getFilters() {
		return filters;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		AbstractJunctionFilter other = (AbstractJunctionFilter) obj;
		// contents comparison with equals()
		return Arrays.equals(filters.toArray(), other.filters.toArray());
	}

	@Override
	public int hashCode() {
		int hash = getFilters().size();
		for (Filter filter : filters) {
			hash = (hash << 1) ^ filter.hashCode();
		}
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		int i = 1;
		for (Filter f : filters) {
			r.append("[");
			r.append(f.toString());
			r.append("]");
			if (i < filters.size()) {
				r.append(" ");
				r.append(super.toString());
				r.append(" ");
			}
			i++;
		}
		return r.toString();
	}

	/**
	 * Removes a filter
	 * 
	 * @param filter
	 *            the filter to remove
	 */
	public void remove(Filter filter) {
		Iterator<Filter> it = filters.iterator();
		while (it.hasNext()) {
			Filter next = it.next();
			if (next.equals(filter)) {
				it.remove();
			}
		}
	}

	/**
	 * Replaces a filter by a new filter. Does nothing if the old filter is not found
	 * 
	 * @param oldFilter
	 *            the filter that must be replaced
	 * @param newFilter
	 *            the filter that must replace the old filter
	 * @param firstOnly
	 *            indicates whether only the first match must be replaced
	 */
	public void replace(Filter oldFilter, Filter newFilter, boolean firstOnly) {
		for (int i = 0; i < filters.size(); i++) {
			if (filters.get(i).equals(oldFilter)) {
				filters.set(i, newFilter);
				if (firstOnly) {
					break;
				}
			}
		}
	}
}
