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
package com.ocs.dynamo.filter;

import java.util.Objects;

/**
 * A filter that matches if the property to check for falls within the defined
 * range. This range is inclusive (i.e. boundary values count)
 * 
 * @author bas.rutten
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
		if (obj == null || !(obj instanceof Between)) {
			return false;
		}
		final Between o = (Between) obj;

		return Objects.equals(propertyId, o.getPropertyId()) && Objects.equals(startValue, o.getStartValue())
				&& Objects.equals(endValue, o.getEndValue());
	}

	@Override
	public String toString() {
		return getPropertyId() + " " + super.toString() + " " + getStartValue() + " and " + getEndValue();
	}

}
