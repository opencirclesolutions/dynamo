package nl.ocs.filter;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;

/**
 * A filter for testing that the value of a certain property is included in a
 * collection of values
 * 
 * @author bas.rutten
 *
 */
public class In extends AbstractFilter implements PropertyFilter {

	private final Collection<?> values;

	private final String propertyId;

	/**
	 * 
	 * @param propertyId
	 *            the property that represents the collection
	 * @param value
	 *            the object that needs to be checked
	 */
	public In(String propertyId, Collection<?> values) {
		this.propertyId = propertyId;
		this.values = values;
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	public Collection<?> getValues() {
		return values;
	}

	@Override
	public boolean evaluate(Object that) {
		if (that == null) {
			return false;
		}
		Object other = getProperty(that, getPropertyId());
		if (other == null) {
			return false;
		}

		return values.contains(other);
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(values) + ObjectUtils.hashCode(propertyId);
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		In c = (In) obj;

		return ObjectUtils.equals(propertyId, c.getPropertyId())
				&& ObjectUtils.equals(values, c.getValues());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getValues();
	}
}
