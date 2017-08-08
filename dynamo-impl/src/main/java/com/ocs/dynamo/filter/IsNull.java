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
		if (obj == null || !(obj instanceof IsNull)) {
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
