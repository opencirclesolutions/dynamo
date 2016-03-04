package com.ocs.dynamo.filter;

import org.apache.commons.lang.ObjectUtils;

/**
 * A filter for checking if a property is null
 * 
 * @author bas.rutten
 */
public final class IsNull extends AbstractFilter implements PropertyFilter {

	private final String propertyId;

	/**
	 * Constructor
	 * 
	 * @param propertyId
	 */
	public IsNull(String propertyId) {
		this.propertyId = propertyId;
	}

	@Override
	public boolean evaluate(Object that) {
		if (null == that) {
			return false;
		}
		return null == getProperty(that, getPropertyId());
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		final IsNull o = (IsNull) obj;

		// Checks the properties one by one
		return ObjectUtils.equals(this.propertyId, o.propertyId);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(propertyId);
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString();
	}

}
