package org.dynamoframework.filter;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class for composite filters
 * 
 * @author bas.rutten
 */
public abstract class AbstractJunctionFilter extends AbstractFilter {

	@Getter
	private final List<Filter> filters = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param filters the filters to include
	 */
	protected AbstractJunctionFilter(Filter... filters) {
		this.filters.addAll(List.of(filters));
	}

	/**
	 * Constructor
	 * 
	 * @param filters the filters to include
	 */
	protected AbstractJunctionFilter(Collection<Filter> filters) {
		this.filters.addAll(filters);
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
		return ReflectionToStringBuilder.toString(this);
	}

	/**
	 * Removes a filter
	 * 
	 * @param filter the filter to remove
	 */
	public void remove(Filter filter) {
		filters.removeIf(next -> next.equals(filter));
	}

	/**
	 * Replaces a filter by a new filter. Does nothing if the old filter is not
	 * found
	 * 
	 * @param oldFilter the filter that must be replaced
	 * @param newFilter the filter that must replace the old filter
	 * @param firstOnly indicates whether only the first match must be replaced
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
