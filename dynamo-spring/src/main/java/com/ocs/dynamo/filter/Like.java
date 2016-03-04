package com.ocs.dynamo.filter;

import org.apache.commons.lang.ObjectUtils;

/**
 * A filter for checking if a string value contains a certain pattern
 * 
 * @author bas.rutten
 */
public class Like extends AbstractFilter implements PropertyFilter {

	private final String propertyId;

	private final String value;

	private boolean caseSensitive;

	/**
	 * Constructor
	 * 
	 * @param propertyId
	 * @param value
	 */
	public Like(String propertyId, String value) {
		this(propertyId, value, true);
	}

	/**
	 * Construct a new {@link Like} with given case sensitivity
	 * 
	 * @param propertyId
	 *            The property name
	 * @param value
	 *            The value to match, may contain '%' to denote pre- and postfix
	 *            matches.
	 * @param caseSensitive
	 *            When true, do a case sensitive match.
	 */
	public Like(String propertyId, String value, boolean caseSensitive) {
		this.propertyId = propertyId;
		this.value = value;
		setCaseSensitive(caseSensitive);
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	public String getValue() {
		return value;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean evaluate(Object that) {
		if (that == null) {
			return false;
		}
		Object val = getProperty(that, getPropertyId());
		if (val == null) {
			return false;
		}
		if (!val.getClass().isAssignableFrom(String.class)) {
			// We can only handle strings
			return false;
		}
		String pattern = getValue().replace("%", ".*");
		if (isCaseSensitive()) {
			return ((String) val).matches(pattern);
		}
		return ((String) val).toUpperCase().matches(pattern.toUpperCase());
	}

	@Override
	public int hashCode() {
		return getPropertyId().hashCode() + getValue().hashCode() + (caseSensitive ? 0 : 1);
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (!(obj instanceof Like)) {
			return false;
		}
		Like o = (Like) obj;
		return ObjectUtils.equals(propertyId, o.getPropertyId())
		        && ObjectUtils.equals(value, o.getValue())
		        && ObjectUtils.equals(caseSensitive, o.isCaseSensitive());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getValue()
		        + (isCaseSensitive() ? "" : " (ignore case)");
	}

}
