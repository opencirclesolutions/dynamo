/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.dynamoframework.filter;

/**
 * A filter for determining if an expression matches a certain modulo
 * calculation
 * 
 * @author bas.rutten
 */
public class Modulo extends AbstractFilter implements PropertyFilter {

	private final String propertyId;

	private final String modExpression;

	private final Integer modValue;

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
			throw new IllegalArgumentException("Modulo operator cannot be used with '0' as its second argument");
		}

		return temp % modVal == result.longValue();
	}

	public Number getResult() {
		return result;
	}

	public Number getModValue() {
		return modValue;
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getModExpression() + " = " + getResult();
	}
}
