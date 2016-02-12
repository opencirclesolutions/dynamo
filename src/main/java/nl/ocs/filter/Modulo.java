package nl.ocs.filter;

/**
 * A filter for determining if an expression matches a certain modulo
 * calculation
 * 
 * @author bas.rutten
 *
 */
public class Modulo extends AbstractFilter implements PropertyFilter {

	private final String propertyId;

	private final String modExpression;

	private Integer modValue;

	private final Number result;

	public Modulo(String propertyId, String modExpression, Number result) {
		this(propertyId, modExpression, null, result);
	}

	public Modulo(String propertyId, Integer modValue, Number result) {
		this(propertyId, null, modValue, result);
	}

	/**
	 * Constructor
	 * 
	 * @param propertyId
	 * @param modExpression
	 * @param result
	 */
	private Modulo(String propertyId, String modExpression, Integer modValue, Number result) {
		this.propertyId = propertyId;
		this.modExpression = modExpression;
		this.result = result;
		this.modValue = modValue;
	}

	@Override
	public String getPropertyId() {
		return propertyId;
	}

	public String getModExpression() {
		return modExpression;
	}

	@Override
	public boolean evaluate(Object that) {
		if (that == null) {
			return false;
		}
		Object value = getProperty(that, getPropertyId());
		if (value == null) {
			return false;
		}
		if (!Number.class.isAssignableFrom(value.getClass())) {
			return false;
		}

		long temp = ((Number) value).longValue();

		long modVal = 0;
		if (getModExpression() != null) {
			modVal = ((Number) getProperty(that, getModExpression())).longValue();
		} else {
			modVal = modValue.longValue();
		}

		if (modVal == 0L) {
			throw new IllegalArgumentException(
					"Modulo operator cannot be used with '0' as its second argument");
		}

		return temp % modVal == result.longValue();
	}

	public Number getResult() {
		return result;
	}

	public Number getModValue() {
		return modValue;
	}

	public void setModValue(Integer modValue) {
		this.modValue = modValue;
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getModExpression() + " = "
				+ getResult();
	}
}
