package nl.ocs.filter;

import org.apache.commons.lang.ObjectUtils;

/**
 * A filter that matches if the property to check for falls within the defined
 * range. This range is inclusive (i.e. boundary values count)
 * 
 * @author bas.rutten
 * 
 */
public class Between extends AbstractFilter implements PropertyFilter {

	private final String propertyId;

	private final Comparable<?> startValue;

	private final Comparable<?> endValue;

	/**
	 * Constructor
	 * 
	 * @param propertyId
	 * @param startValue
	 * @param endValue
	 */
	public Between(String propertyId, Comparable<?> startValue, Comparable<?> endValue) {
		this.propertyId = propertyId;
		this.startValue = startValue;
		this.endValue = endValue;
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	public Comparable<?> getStartValue() {
		return startValue;
	}

	public Comparable<?> getEndValue() {
		return endValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean evaluate(Object that) {
		Object value = getProperty(that, getPropertyId());
		if (value instanceof Comparable) {
			Comparable<Object> comp = (Comparable<Object>) value;
			return comp.compareTo(getStartValue()) >= 0 && comp.compareTo(getEndValue()) <= 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getPropertyId().hashCode() + getStartValue().hashCode() + getEndValue().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// Only objects of the same class can be equal
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		final Between o = (Between) obj;

		return ObjectUtils.equals(propertyId, o.getPropertyId())
				&& ObjectUtils.equals(startValue, o.getStartValue())
				&& ObjectUtils.equals(endValue, o.getEndValue());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getStartValue() + " and "
				+ getEndValue();
	}

}
