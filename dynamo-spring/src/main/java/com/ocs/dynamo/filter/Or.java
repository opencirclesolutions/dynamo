package com.ocs.dynamo.filter;

import java.util.Collection;

/**
 * A composite filter that evaluates to true if at least one of its subfilters
 * does
 * 
 * @author bas.rutten
 * 
 */
public final class Or extends AbstractJunctionFilter {

	/**
	 * Constructor
	 * 
	 * @param filters
	 */
	public Or(Filter... filters) {
		super(filters);
	}

	/**
	 * Constructor
	 * 
	 * @param filters
	 */
	public Or(Collection<Filter> filters) {
		super(filters);
	}

	@Override
	public boolean evaluate(Object that) {
		for (Filter filter : getFilters()) {
			if (filter.evaluate(that)) {
				return true;
			}
		}
		return false;
	}
}
