package com.ocs.dynamo.filter;

import java.util.Collection;

/**
 * A composite filter that evaluates to true if all of the subfilters match
 * 
 * @author bas.rutten
 * 
 */
public final class And extends AbstractJunctionFilter {

	/**
	 * 
	 * @param filters
	 *            filters of which the And filter will be composed
	 */
	public And(Filter... filters) {
		super(filters);
	}

	/**
	 * @param filters
	 *            filters of which the And filter will be composed
	 */
	public And(Collection<Filter> filters) {
		super(filters);
	}

	@Override
	public boolean evaluate(Object that) {
		for (Filter filter : getFilters()) {
			if (!filter.evaluate(that)) {
				return false;
			}
		}
		return true;
	}

}
