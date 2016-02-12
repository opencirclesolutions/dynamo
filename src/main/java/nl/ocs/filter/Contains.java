package nl.ocs.filter;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;

/**
 * A filter that checks if a value is contained in a collection
 * 
 * @author bas.rutten
 * 
 */
public class Contains extends AbstractFilter implements PropertyFilter {

	private final Object value;

	private final String propertyId;

	/**
	 * 
	 * @param propertyId
	 *            the property that represents the collection
	 * @param value
	 *            the object that needs to be checked
	 */
	public Contains(String propertyId, Object value) {
		this.propertyId = propertyId;
		this.value = value;
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public boolean evaluate(Object that) {
		if (that == null) {
			return false;
		}
		Object collection = getProperty(that, getPropertyId());
		if (collection != null && Collection.class.isAssignableFrom(collection.getClass())) {
			Collection<?> col = (Collection<?>) collection;
			return col.contains(value);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.hashCode(value) + ObjectUtils.hashCode(propertyId);
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		Contains c = (Contains) obj;

		return ObjectUtils.equals(propertyId, c.getPropertyId())
				&& ObjectUtils.equals(value, c.getValue());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getValue();
	}

}
