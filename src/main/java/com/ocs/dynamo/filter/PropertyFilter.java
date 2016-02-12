package com.ocs.dynamo.filter;

/**
 * A filter that is used to filter on a single property value
 * @author bas.rutten
 *
 */
public interface PropertyFilter {

	/**
	 * The name of the property
	 * @return
	 */
	public String getPropertyId();
}
